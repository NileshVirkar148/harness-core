package io.harness.aggregator.services;

import io.harness.accesscontrol.acl.daos.ACLDAO;
import io.harness.accesscontrol.acl.models.ACL;
import io.harness.accesscontrol.acl.models.SourceMetadata;
import io.harness.accesscontrol.common.filter.ManagedFilter;
import io.harness.accesscontrol.resources.resourcegroups.ResourceGroup;
import io.harness.accesscontrol.resources.resourcegroups.ResourceGroupService;
import io.harness.accesscontrol.resources.resourcegroups.persistence.ResourceGroupDBO;
import io.harness.accesscontrol.roleassignments.persistence.RoleAssignmentDBO;
import io.harness.accesscontrol.roles.Role;
import io.harness.accesscontrol.roles.RoleService;
import io.harness.accesscontrol.roles.persistence.RoleDBO;
import io.harness.aggregator.services.apis.AggregatorService;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.executable.ValidateOnExecution;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;

@AllArgsConstructor(onConstructor = @__({ @Inject }))
@Slf4j
@ValidateOnExecution
public class AggregatorServiceImpl implements AggregatorService {
  private final ACLDAO acldao;
  private final RoleService roleService;
  private final ResourceGroupService resourceGroupService;

  @Override
  public List<ACL> processRoleAssignmentCreation(RoleAssignmentDBO roleAssignmentDBO) {
    Optional<Role> roleOptional = roleService.get(
        roleAssignmentDBO.getRoleIdentifier(), roleAssignmentDBO.getScopeIdentifier(), ManagedFilter.NO_FILTER);
    Optional<ResourceGroup> resourceGroupOptional = resourceGroupService.get(
        roleAssignmentDBO.getResourceGroupIdentifier(), roleAssignmentDBO.getScopeIdentifier());

    if (!roleOptional.isPresent() || !resourceGroupOptional.isPresent()) {
      log.error("Role/Resource group not found, Unable to process creation of role assignment: {}", roleAssignmentDBO);
      return new ArrayList<>();
    }

    Role role = roleOptional.get();
    ResourceGroup resourceGroup = resourceGroupOptional.get();

    List<ACL> acls = new ArrayList<>();
    role.getPermissions().forEach(permission -> resourceGroup.getResourceSelectors().forEach(resourceSelector -> {
      String principalType =
          roleAssignmentDBO.getPrincipalType() == null ? null : roleAssignmentDBO.getPrincipalType().name();
      acls.add(ACL.builder()
                   .roleAssignmentId(roleAssignmentDBO.getId())
                   .scopeIdentifier(roleAssignmentDBO.getScopeIdentifier())
                   .permissionIdentifier(permission)
                   .sourceMetadata(SourceMetadata.builder()
                                       .roleIdentifier(roleAssignmentDBO.getRoleIdentifier())
                                       .roleAssignmentIdentifier(roleAssignmentDBO.getIdentifier())
                                       .resourceGroupIdentifier(roleAssignmentDBO.getResourceGroupIdentifier())
                                       .userGroupIdentifier(null)
                                       .build())
                   .resource(resourceSelector)
                   .principalType(principalType)
                   .principalIdentifier(roleAssignmentDBO.getPrincipalIdentifier())
                   .aclQueryString(ACL.getAclQueryString(roleAssignmentDBO.getScopeIdentifier(), resourceSelector,
                       principalType, roleAssignmentDBO.getPrincipalIdentifier(), permission))
                   .build());
    }));
    if (!acls.isEmpty()) {
      return acldao.saveAll(acls);
    }
    return new ArrayList<>();
  }

  @Override
  public void processRoleAssignmentDeletion(String roleAssignmentId) {
    acldao.deleteByRoleAssignmentId(roleAssignmentId);
  }

  @Override
  public List<ACL> processRoleAssignmentUpdation(RoleAssignmentDBO roleAssignmentDBO) {
    processRoleAssignmentDeletion(roleAssignmentDBO.getId());
    return processRoleAssignmentCreation(roleAssignmentDBO);
  }

  @Override
  public List<ACL> processRoleUpdation(RoleDBO roleDBO) {
    if (Optional.ofNullable(roleDBO.getPermissions()).filter(x -> !x.isEmpty()).isPresent()) {
      Set<String> currentPermissionsInRole = roleDBO.getPermissions();
      List<ACL> acls = acldao.getByRole(roleDBO.getScopeIdentifier(), roleDBO.getIdentifier(), roleDBO.isManaged());

      Set<String> oldPermissionsFromThisRole =
          acls.stream().map(ACL::getPermissionIdentifier).collect(Collectors.toSet());

      Set<String> permissionsToDelete = Sets.difference(oldPermissionsFromThisRole, currentPermissionsInRole);
      Set<String> permissionsToAdd = Sets.difference(currentPermissionsInRole, oldPermissionsFromThisRole);

      // delete ACLs which contain old permissions
      List<ACL> aclsToDelete = acls.stream()
                                   .filter(x -> permissionsToDelete.contains(x.getPermissionIdentifier()))
                                   .collect(Collectors.toList());
      if (!aclsToDelete.isEmpty()) {
        acldao.deleteAll(aclsToDelete);
      }

      // add new ACLs for new permissions
      List<ACL> aclsToCreate = new ArrayList<>();
      acls.forEach(acl -> permissionsToAdd.forEach(permission -> {
        ACL aclToCreate = ACL.copyOf(acl);
        aclToCreate.setPermissionIdentifier(permission);
        aclToCreate.setAclQueryString(ACL.getAclQueryString(aclToCreate));
        aclsToCreate.add(aclToCreate);
      }));
      for (ACL acl : aclsToCreate) {
        try {
          acldao.save(acl);
        } catch (DuplicateKeyException duplicateKeyException) {
          log.debug("Entry already exists, ignoring");
        }
      }
      return aclsToCreate;
    }
    return new ArrayList<>();
  }

  @Override
  public List<ACL> processResourceGroupUpdation(ResourceGroupDBO resourceGroupDBO) {
    if (Optional.ofNullable(resourceGroupDBO.getResourceSelectors()).filter(x -> !x.isEmpty()).isPresent()) {
      Set<String> currentResourceSelectorsInResourceGroup = resourceGroupDBO.getResourceSelectors();
      List<ACL> acls = acldao.getByResourceGroup(resourceGroupDBO.getScopeIdentifier(),
          resourceGroupDBO.getIdentifier(), Boolean.TRUE.equals(resourceGroupDBO.getManaged()));

      Set<String> oldResourcesFromThisResourceGroup = acls.stream().map(ACL::getResource).collect(Collectors.toSet());

      Set<String> resourcesToDelete =
          Sets.difference(oldResourcesFromThisResourceGroup, currentResourceSelectorsInResourceGroup);
      Set<String> resourcesToAdd =
          Sets.difference(currentResourceSelectorsInResourceGroup, oldResourcesFromThisResourceGroup);

      // delete ACLs which contain old resources
      List<ACL> aclsToDelete =
          acls.stream().filter(x -> resourcesToDelete.contains(x.getResource())).collect(Collectors.toList());
      acldao.deleteAll(aclsToDelete);

      List<ACL> aclsToCreate = new ArrayList<>();
      acls.forEach(acl -> resourcesToAdd.forEach(resource -> {
        ACL aclToCreate = ACL.copyOf(acl);
        aclToCreate.setResource(resource);
        aclToCreate.setAclQueryString(ACL.getAclQueryString(aclToCreate));
        aclsToCreate.add(aclToCreate);
      }));
      for (ACL acl : aclsToCreate) {
        try {
          acldao.save(acl);
        } catch (DuplicateKeyException duplicateKeyException) {
          log.debug("Entry already exists, ignoring");
        }
      }
      return aclsToCreate;
    }
    return new ArrayList<>();
  }
}
