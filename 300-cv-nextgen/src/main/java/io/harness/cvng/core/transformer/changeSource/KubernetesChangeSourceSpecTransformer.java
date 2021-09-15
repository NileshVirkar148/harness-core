package io.harness.cvng.core.transformer.changeSource;

import io.harness.cvng.beans.change.ChangeSourceType;
import io.harness.cvng.core.beans.monitoredService.ChangeSourceDTO;
import io.harness.cvng.core.beans.monitoredService.changeSourceSpec.KubernetesChangeSourceSpec;
import io.harness.cvng.core.beans.params.ServiceEnvironmentParams;
import io.harness.cvng.core.entities.changeSource.KubernetesChangeSource;

public class KubernetesChangeSourceSpecTransformer
    extends ChangeSourceSpecTransformer<KubernetesChangeSource, KubernetesChangeSourceSpec> {
  @Override
  public KubernetesChangeSource getEntity(ServiceEnvironmentParams environmentParams, ChangeSourceDTO changeSourceDTO) {
    KubernetesChangeSourceSpec k8ChangeSourceSpec = (KubernetesChangeSourceSpec) changeSourceDTO.getSpec();
    return KubernetesChangeSource.builder()
        .accountId(environmentParams.getAccountIdentifier())
        .orgIdentifier(environmentParams.getOrgIdentifier())
        .projectIdentifier(environmentParams.getProjectIdentifier())
        .serviceIdentifier(environmentParams.getServiceIdentifier())
        .envIdentifier(environmentParams.getEnvironmentIdentifier())
        .identifier(changeSourceDTO.getIdentifier())
        .name(changeSourceDTO.getName())
        .enabled(changeSourceDTO.isEnabled())
        .type(ChangeSourceType.KUBERNETES)
        .connectorIdentifier(k8ChangeSourceSpec.getConnectorRef())
        .build();
  }

  @Override
  protected KubernetesChangeSourceSpec getSpec(KubernetesChangeSource changeSource) {
    return KubernetesChangeSourceSpec.builder().connectorRef(changeSource.getConnectorIdentifier()).build();
  }
}
