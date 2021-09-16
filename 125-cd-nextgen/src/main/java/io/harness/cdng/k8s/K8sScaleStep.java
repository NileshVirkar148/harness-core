package io.harness.cdng.k8s;

import static io.harness.annotations.dev.HarnessTeam.CDP;

import io.harness.annotations.dev.OwnedBy;
import io.harness.cdng.infra.beans.InfrastructureOutcome;
import io.harness.cdng.instance.info.InstanceInfoService;
import io.harness.cdng.k8s.K8sScaleBaseStepInfo.K8sScaleBaseStepInfoKeys;
import io.harness.cdng.k8s.beans.K8sExecutionPassThroughData;
import io.harness.cdng.stepsdependency.constants.OutcomeExpressionConstants;
import io.harness.common.NGTimeConversionHelper;
import io.harness.delegate.beans.instancesync.mapper.K8sPodToServiceInstanceInfoMapper;
import io.harness.delegate.task.k8s.K8sDeployResponse;
import io.harness.delegate.task.k8s.K8sScaleRequest;
import io.harness.delegate.task.k8s.K8sScaleResponse;
import io.harness.delegate.task.k8s.K8sTaskType;
import io.harness.executions.steps.ExecutionNodeType;
import io.harness.logging.CommandExecutionStatus;
import io.harness.plancreator.steps.common.StepElementParameters;
import io.harness.plancreator.steps.common.rollback.TaskExecutableWithRollbackAndRbac;
import io.harness.pms.contracts.ambiance.Ambiance;
import io.harness.pms.contracts.execution.Status;
import io.harness.pms.contracts.execution.tasks.TaskRequest;
import io.harness.pms.contracts.steps.StepCategory;
import io.harness.pms.contracts.steps.StepType;
import io.harness.pms.sdk.core.resolver.RefObjectUtils;
import io.harness.pms.sdk.core.resolver.outcome.OutcomeService;
import io.harness.pms.sdk.core.steps.io.StepInputPackage;
import io.harness.pms.sdk.core.steps.io.StepResponse;
import io.harness.pms.sdk.core.steps.io.StepResponse.StepOutcome;
import io.harness.pms.sdk.core.steps.io.StepResponse.StepResponseBuilder;
import io.harness.supplier.ThrowingSupplier;

import com.google.inject.Inject;
import java.util.Optional;

@OwnedBy(CDP)
public class K8sScaleStep extends TaskExecutableWithRollbackAndRbac<K8sDeployResponse> {
  public static final StepType STEP_TYPE = StepType.newBuilder()
                                               .setType(ExecutionNodeType.K8S_SCALE.getYamlType())
                                               .setStepCategory(StepCategory.STEP)
                                               .build();

  public static final String K8S_SCALE_COMMAND_NAME = "Scale";
  @Inject private OutcomeService outcomeService;
  @Inject private K8sStepHelper k8sStepHelper;
  @Inject private InstanceInfoService instanceInfoService;

  @Override
  public void validateResources(Ambiance ambiance, StepElementParameters stepParameters) {
    // Noop
  }

  @Override
  public TaskRequest obtainTaskAfterRbac(
      Ambiance ambiance, StepElementParameters stepElementParameters, StepInputPackage inputPackage) {
    InfrastructureOutcome infrastructure = (InfrastructureOutcome) outcomeService.resolve(
        ambiance, RefObjectUtils.getOutcomeRefObject(OutcomeExpressionConstants.INFRASTRUCTURE_OUTCOME));

    K8sScaleStepParameter scaleStepParameter = (K8sScaleStepParameter) stepElementParameters.getSpec();
    Integer instances = scaleStepParameter.getInstanceSelection().getSpec().getInstances();

    boolean skipSteadyCheck = K8sStepHelper.getParameterFieldBooleanValue(scaleStepParameter.getSkipSteadyStateCheck(),
        K8sScaleBaseStepInfoKeys.skipSteadyStateCheck, stepElementParameters);
    String releaseName = k8sStepHelper.getReleaseName(ambiance, infrastructure);

    K8sScaleRequest request =
        K8sScaleRequest.builder()
            .commandName(K8S_SCALE_COMMAND_NAME)
            .releaseName(releaseName)
            .instances(instances)
            .instanceUnitType(scaleStepParameter.getInstanceSelection().getType().getInstanceUnitType())
            .workload(scaleStepParameter.getWorkload().getValue())
            .maxInstances(Optional.empty()) // do we need those for scale?
            .skipSteadyStateCheck(skipSteadyCheck)
            .taskType(K8sTaskType.SCALE)
            .timeoutIntervalInMin(
                NGTimeConversionHelper.convertTimeStringToMinutes(stepElementParameters.getTimeout().getValue()))
            .k8sInfraDelegateConfig(k8sStepHelper.getK8sInfraDelegateConfig(infrastructure, ambiance))
            .build();

    k8sStepHelper.publishReleaseNameStepDetails(ambiance, releaseName);
    return k8sStepHelper
        .queueK8sTask(stepElementParameters, request, ambiance,
            K8sExecutionPassThroughData.builder().infrastructure(infrastructure).build())
        .getTaskRequest();
  }

  @Override
  public StepResponse handleTaskResultWithSecurityContext(Ambiance ambiance,
      StepElementParameters stepElementParameters, ThrowingSupplier<K8sDeployResponse> responseSupplier)
      throws Exception {
    K8sDeployResponse k8sTaskExecutionResponse = responseSupplier.get();
    // do we need to include the newPods with instance details + summaries
    StepResponseBuilder stepResponseBuilder =
        StepResponse.builder().unitProgressList(k8sTaskExecutionResponse.getCommandUnitsProgress().getUnitProgresses());

    if (k8sTaskExecutionResponse.getCommandExecutionStatus() != CommandExecutionStatus.SUCCESS) {
      return K8sStepHelper.getFailureResponseBuilder(k8sTaskExecutionResponse, stepResponseBuilder).build();
    }

    K8sScaleResponse k8sNGTaskResponse = (K8sScaleResponse) k8sTaskExecutionResponse.getK8sNGTaskResponse();
    StepOutcome stepOutcome = instanceInfoService.saveServerInstancesIntoSweepingOutput(
        ambiance, K8sPodToServiceInstanceInfoMapper.toServerInstanceInfoList(k8sNGTaskResponse.getK8sPodList()));

    return stepResponseBuilder.status(Status.SUCCEEDED).stepOutcome(stepOutcome).build();
  }

  @Override
  public Class<StepElementParameters> getStepParametersClass() {
    return StepElementParameters.class;
  }
}
