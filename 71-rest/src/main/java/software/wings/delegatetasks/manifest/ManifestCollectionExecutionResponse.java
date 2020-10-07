package software.wings.delegatetasks.manifest;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.delegate.beans.DelegateMetaInfo;
import io.harness.delegate.beans.DelegateTaskNotifyResponseData;
import io.harness.logging.CommandExecutionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@OwnedBy(HarnessTeam.CDC)
public class ManifestCollectionExecutionResponse implements DelegateTaskNotifyResponseData {
  private DelegateMetaInfo delegateMetaInfo;
  private ManifestCollectionResponse manifestCollectionResponse;
  private String errorMessage;
  private CommandExecutionStatus commandExecutionStatus;
  private String appManifestId;
  private String appId;
}
