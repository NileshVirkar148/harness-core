package io.harness.service;

import io.harness.dto.DeploymentSummary;
import io.harness.dto.deploymentinfo.DeploymentInfo;
import io.harness.dto.infrastructureMapping.InfrastructureMapping;
import io.harness.entity.InstanceSyncFlowType;
import io.harness.entity.RollbackInfo;
import io.harness.pms.contracts.ambiance.Ambiance;

public interface IInstanceHandler {
  void handleNewDeployment(DeploymentSummary deploymentSummary, RollbackInfo rollbackInfo);

  void syncInstances(String accountId, String orgId, String projectId, String infrastructureMappingId,
      InstanceSyncFlowType instanceSyncFlowType);

  InfrastructureMapping getInfrastructureMapping(Ambiance ambiance);

  DeploymentInfo getDeploymentInfo(Ambiance ambiance);
}
