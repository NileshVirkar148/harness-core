package software.wings.api;

import static io.harness.annotations.dev.HarnessTeam.CDC;

import io.harness.annotations.dev.HarnessModule;
import io.harness.annotations.dev.OwnedBy;
import io.harness.annotations.dev.TargetModule;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
@OwnedBy(CDC)
@TargetModule(HarnessModule._957_CG_BEANS)
public class ServiceNowImportSetResponse {
  @JsonProperty("import_set") private String importSet;

  @JsonProperty("staging_table") private String stagingTable;

  @JsonProperty("result") private List<ServiceNowImportSetResult> result;
}
