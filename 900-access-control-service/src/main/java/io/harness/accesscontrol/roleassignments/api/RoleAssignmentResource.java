package io.harness.accesscontrol.roleassignments.api;

import static io.harness.NGCommonEntityConstants.IDENTIFIER_KEY;
import static io.harness.accesscontrol.common.filter.ManagedFilter.NO_FILTER;
import static io.harness.accesscontrol.principals.PrincipalType.USER_GROUP;
import static io.harness.accesscontrol.roleassignments.api.RoleAssignmentDTOMapper.fromDTO;
import static io.harness.accesscontrol.roleassignments.api.RoleAssignmentDTOMapper.toDTO;
import static io.harness.accesscontrol.roleassignments.api.RoleAssignmentDTOMapper.toResponseDTO;
import static io.harness.annotations.dev.HarnessTeam.PL;

import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PRIVATE;

import io.harness.accesscontrol.principals.usergroups.HarnessUserGroupService;
import io.harness.accesscontrol.resourcegroups.api.ResourceGroupDTO;
import io.harness.accesscontrol.resources.resourcegroups.HarnessResourceGroupService;
import io.harness.accesscontrol.resources.resourcegroups.ResourceGroupService;
import io.harness.accesscontrol.resources.resourcegroups.api.ResourceGroupDTOMapper;
import io.harness.accesscontrol.roleassignments.RoleAssignment;
import io.harness.accesscontrol.roleassignments.RoleAssignmentFilter;
import io.harness.accesscontrol.roleassignments.RoleAssignmentService;
import io.harness.accesscontrol.roles.RoleService;
import io.harness.accesscontrol.roles.api.RoleDTOMapper;
import io.harness.accesscontrol.roles.api.RoleResponseDTO;
import io.harness.accesscontrol.roles.filter.RoleFilter;
import io.harness.accesscontrol.scopes.core.Scope;
import io.harness.accesscontrol.scopes.core.ScopeService;
import io.harness.accesscontrol.scopes.harness.HarnessScopeParams;
import io.harness.annotations.dev.OwnedBy;
import io.harness.exception.InvalidRequestException;
import io.harness.ng.beans.PageRequest;
import io.harness.ng.beans.PageResponse;
import io.harness.ng.core.dto.ErrorDTO;
import io.harness.ng.core.dto.FailureDTO;
import io.harness.ng.core.dto.ResponseDTO;

import com.google.inject.Inject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.NotEmpty;
import retrofit2.http.Body;

@OwnedBy(PL)
@Api("roleassignments")
@Path("roleassignments")
@Produces({"application/json", "application/yaml"})
@Consumes({"application/json", "application/yaml"})
@ApiResponses(value =
    {
      @ApiResponse(code = 400, response = FailureDTO.class, message = "Bad Request")
      , @ApiResponse(code = 500, response = ErrorDTO.class, message = "Internal server error")
    })
@FieldDefaults(level = PRIVATE, makeFinal = true)
@AllArgsConstructor(access = PACKAGE, onConstructor = @__({ @Inject }))
public class RoleAssignmentResource {
  RoleAssignmentService roleAssignmentService;
  HarnessResourceGroupService harnessResourceGroupService;
  HarnessUserGroupService harnessUserGroupService;
  ScopeService scopeService;
  RoleService roleService;
  ResourceGroupService resourceGroupService;

  @GET
  @ApiOperation(value = "Get Role Assignments", nickname = "getRoleAssignmentList")
  public ResponseDTO<PageResponse<RoleAssignmentResponseDTO>> get(
      @BeanParam PageRequest pageRequest, @BeanParam HarnessScopeParams harnessScopeParams) {
    String scopeIdentifier = scopeService.buildScopeFromParams(harnessScopeParams).toString();
    PageResponse<RoleAssignment> pageResponse =
        roleAssignmentService.list(pageRequest, RoleAssignmentFilter.builder().scopeFilter(scopeIdentifier).build());
    return ResponseDTO.newResponse(pageResponse.map(RoleAssignmentDTOMapper::toResponseDTO));
  }

  @POST
  @Path("filter")
  @ApiOperation(value = "Get Filtered Role Assignments", nickname = "getFilteredRoleAssignmentList")
  public ResponseDTO<PageResponse<RoleAssignmentResponseDTO>> get(@BeanParam PageRequest pageRequest,
      @BeanParam HarnessScopeParams harnessScopeParams, @Body RoleAssignmentFilterDTO roleAssignmentFilter) {
    String scopeIdentifier = scopeService.buildScopeFromParams(harnessScopeParams).toString();
    PageResponse<RoleAssignment> pageResponse =
        roleAssignmentService.list(pageRequest, fromDTO(scopeIdentifier, roleAssignmentFilter));
    return ResponseDTO.newResponse(pageResponse.map(RoleAssignmentDTOMapper::toResponseDTO));
  }

  @POST
  @Path("aggregate")
  @ApiOperation(value = "Get Role Assignments Aggregate", nickname = "getRoleAssignmentsAggregate")
  public ResponseDTO<RoleAssignmentAggregateResponseDTO> getAggregated(
      @BeanParam HarnessScopeParams harnessScopeParams, @Body RoleAssignmentFilterDTO roleAssignmentFilter) {
    String scopeIdentifier = scopeService.buildScopeFromParams(harnessScopeParams).toString();
    PageRequest pageRequest = PageRequest.builder().pageSize(1000).build();
    List<RoleAssignment> roleAssignments =
        roleAssignmentService.list(pageRequest, fromDTO(scopeIdentifier, roleAssignmentFilter)).getContent();
    List<String> roleIdentifiers =
        roleAssignments.stream().map(RoleAssignment::getRoleIdentifier).distinct().collect(toList());
    RoleFilter roleFilter = RoleFilter.builder()
                                .identifierFilter(new HashSet<>(roleIdentifiers))
                                .scopeIdentifier(scopeIdentifier)
                                .managedFilter(NO_FILTER)
                                .build();
    List<RoleResponseDTO> roleResponseDTOs = roleService.list(pageRequest, roleFilter)
                                                 .getContent()
                                                 .stream()
                                                 .map(RoleDTOMapper::toResponseDTO)
                                                 .collect(toList());
    List<String> resourceGroupIdentifiers =
        roleAssignments.stream().map(RoleAssignment::getResourceGroupIdentifier).distinct().collect(toList());
    List<ResourceGroupDTO> resourceGroupDTOs = resourceGroupService.list(resourceGroupIdentifiers, scopeIdentifier)
                                                   .stream()
                                                   .map(ResourceGroupDTOMapper::toDTO)
                                                   .collect(toList());
    List<RoleAssignmentDTO> roleAssignmentDTOs =
        roleAssignments.stream().map(RoleAssignmentDTOMapper::toDTO).collect(toList());
    return ResponseDTO.newResponse(RoleAssignmentAggregateResponseDTOMapper.toDTO(
        roleAssignmentDTOs, scopeIdentifier, roleResponseDTOs, resourceGroupDTOs));
  }

  @POST
  @ApiOperation(value = "Create Role Assignment", nickname = "createRoleAssignment")
  public ResponseDTO<RoleAssignmentResponseDTO> create(
      @BeanParam HarnessScopeParams harnessScopeParams, @Body RoleAssignmentDTO roleAssignmentDTO) {
    Scope scope = scopeService.buildScopeFromParams(harnessScopeParams);
    harnessResourceGroupService.sync(roleAssignmentDTO.getResourceGroupIdentifier(), scope);
    if (roleAssignmentDTO.getPrincipal().getType().equals(USER_GROUP)) {
      harnessUserGroupService.sync(roleAssignmentDTO.getPrincipal().getIdentifier(), scope);
    }
    RoleAssignment createdRoleAssignment = roleAssignmentService.create(fromDTO(scope.toString(), roleAssignmentDTO));
    return ResponseDTO.newResponse(toResponseDTO(createdRoleAssignment));
  }

  @PUT
  @Path("{identifier}")
  @ApiOperation(value = "Update Role Assignment", nickname = "updateRoleAssignment")
  public ResponseDTO<RoleAssignmentResponseDTO> update(@NotNull @PathParam(IDENTIFIER_KEY) String identifier,
      @BeanParam HarnessScopeParams harnessScopeParams, @Body RoleAssignmentDTO roleAssignmentDTO) {
    Scope scope = scopeService.buildScopeFromParams(harnessScopeParams);
    if (!identifier.equals(roleAssignmentDTO.getIdentifier())) {
      throw new InvalidRequestException("Role Assignment identifier in the request body and the url do not match.");
    }
    RoleAssignment updatedRoleAssignment = roleAssignmentService.update(fromDTO(scope.toString(), roleAssignmentDTO));
    return ResponseDTO.newResponse(toResponseDTO(updatedRoleAssignment));
  }

  @POST
  @Path("/multi")
  @ApiOperation(value = "Create Multiple Role Assignments", nickname = "createRoleAssignments")
  public ResponseDTO<List<RoleAssignmentResponseDTO>> create(@BeanParam HarnessScopeParams harnessScopeParams,
      @Body RoleAssignmentCreateRequestDTO roleAssignmentCreateRequestDTO) {
    Scope scope = scopeService.buildScopeFromParams(harnessScopeParams);
    List<RoleAssignment> roleAssignmentsPayload =
        roleAssignmentCreateRequestDTO.getRoleAssignments()
            .stream()
            .map(roleAssignmentDTO -> fromDTO(scope.toString(), roleAssignmentDTO))
            .collect(Collectors.toList());
    for (RoleAssignment roleAssignment : roleAssignmentsPayload) {
      harnessResourceGroupService.sync(roleAssignment.getResourceGroupIdentifier(), scope);
      if (roleAssignment.getPrincipalType().equals(USER_GROUP)) {
        harnessUserGroupService.sync(roleAssignment.getPrincipalIdentifier(), scope);
      }
    }
    return ResponseDTO.newResponse(roleAssignmentService.createMulti(roleAssignmentsPayload)
                                       .stream()
                                       .map(RoleAssignmentDTOMapper::toResponseDTO)
                                       .collect(toList()));
  }

  @POST
  @Path("/validate")
  @ApiOperation(value = "Validate Role Assignment", nickname = "validateRoleAssignment")
  public ResponseDTO<RoleAssignmentValidationResponseDTO> validate(
      @BeanParam HarnessScopeParams harnessScopeParams, @Body RoleAssignmentValidationRequestDTO validationRequest) {
    Scope scope = scopeService.buildScopeFromParams(harnessScopeParams);
    harnessResourceGroupService.sync(validationRequest.getRoleAssignment().getResourceGroupIdentifier(), scope);
    return ResponseDTO.newResponse(toDTO(roleAssignmentService.validate(fromDTO(scope.toString(), validationRequest))));
  }

  @DELETE
  @Path("{identifier}")
  @ApiOperation(value = "Delete Role Assignment", nickname = "deleteRoleAssignment")
  public ResponseDTO<RoleAssignmentResponseDTO> delete(
      @BeanParam HarnessScopeParams harnessScopeParams, @NotEmpty @PathParam(IDENTIFIER_KEY) String identifier) {
    String scopeIdentifier = scopeService.buildScopeFromParams(harnessScopeParams).toString();
    RoleAssignment deletedRoleAssignment =
        roleAssignmentService.delete(identifier, scopeIdentifier).<NotFoundException>orElseThrow(() -> {
          throw new NotFoundException("Role Assignment not found with the given scope and identifier");
        });
    return ResponseDTO.newResponse(toResponseDTO(deletedRoleAssignment));
  }
}
