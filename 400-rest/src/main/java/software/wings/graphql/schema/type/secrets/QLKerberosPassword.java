package software.wings.graphql.schema.type.secrets;

import io.harness.annotations.dev.HarnessModule;
import io.harness.annotations.dev.TargetModule;

import software.wings.graphql.schema.type.QLObject;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

@Value
@Builder
@FieldNameConstants(innerTypeName = "QLKerberosPasswordKeys")
@TargetModule(HarnessModule._380_CG_GRAPHQL)
public class QLKerberosPassword implements QLObject {
  String passwordSecretId;
}
