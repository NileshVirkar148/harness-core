package software.wings.graphql.schema.mutation.connector.input.nexus;

import io.harness.annotations.dev.HarnessModule;
import io.harness.annotations.dev.TargetModule;
@TargetModule(HarnessModule._380_CG_GRAPHQL)
public enum QLNexusVersion {
  V2("2.x"),
  V3("3.x");

  public final String value;

  QLNexusVersion(String value) {
    this.value = value;
  }
}
