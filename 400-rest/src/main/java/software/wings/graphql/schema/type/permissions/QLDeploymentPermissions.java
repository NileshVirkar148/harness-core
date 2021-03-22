package software.wings.graphql.schema.type.permissions;

import io.harness.annotations.dev.HarnessModule;
import io.harness.annotations.dev.TargetModule;

import java.util.Set;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

@Value
@Builder
@FieldNameConstants(innerTypeName = "QLDeploymentPermissionsKeys")
@TargetModule(HarnessModule._380_CG_GRAPHQL)
public class QLDeploymentPermissions {
  private Set<QLDeploymentFilterType> filterTypes;
  private Set<String> envIds;
}
