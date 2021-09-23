package software.wings.graphql.schema.type.usergroup;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;

import software.wings.graphql.schema.type.QLObject;
import software.wings.security.PermissionAttribute;
import software.wings.security.annotations.Scope;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

@Value
@Builder
@FieldNameConstants(innerTypeName = "QLSSOProviderKeys")
@Scope(PermissionAttribute.ResourceType.USER)
@OwnedBy(HarnessTeam.PL)
public class QLSSOSetting implements QLObject {
  QLLinkedSSOSetting linkedSSOSetting;
}