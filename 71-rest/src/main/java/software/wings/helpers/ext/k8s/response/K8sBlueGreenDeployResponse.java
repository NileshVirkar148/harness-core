package software.wings.helpers.ext.k8s.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class K8sBlueGreenDeployResponse implements K8sTaskResponse {
  Integer releaseNumber;
}
