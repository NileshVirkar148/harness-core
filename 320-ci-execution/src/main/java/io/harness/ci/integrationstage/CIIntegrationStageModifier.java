package io.harness.ci.integrationstage;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.beans.execution.ExecutionSource;
import io.harness.beans.executionargs.CIExecutionArgs;
import io.harness.beans.yaml.extended.infrastrucutre.Infrastructure;
import io.harness.ci.beans.entities.BuildNumberDetails;
import io.harness.plancreator.execution.ExecutionElementConfig;
import io.harness.plancreator.stages.stage.StageElementConfig;
import io.harness.pms.contracts.plan.ExecutionMetadata;
import io.harness.pms.contracts.plan.PlanCreationContextValue;
import io.harness.pms.sdk.core.plan.creation.beans.PlanCreationContext;
import io.harness.yaml.extended.ci.codebase.CodeBase;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

/**
 * Modifies saved integration stage execution plan by prepending init step for setting up build stage infra e.g. pod or
 * VM.
 */

@Slf4j
@Singleton
@OwnedBy(HarnessTeam.CI)
public class CIIntegrationStageModifier implements StageExecutionModifier {
  @Inject private CIStepGroupUtils ciStepGroupUtils;

  @Override
  public ExecutionElementConfig modifyExecutionPlan(ExecutionElementConfig execution,
      StageElementConfig stageElementConfig, PlanCreationContext context, CodeBase ciCodeBase,
      Infrastructure infrastructure, ExecutionSource executionSource) {
    log.info("Modifying execution plan to prepend initialize step for integration stage {}",
        stageElementConfig.getIdentifier());

    PlanCreationContextValue planCreationContextValue = context.getGlobalContext().get("metadata");
    ExecutionMetadata executionMetadata = planCreationContextValue.getMetadata();

    CIExecutionArgs ciExecutionArgs =
        CIExecutionArgs.builder()
            .executionSource(executionSource)
            .buildNumberDetails(
                BuildNumberDetails.builder().buildNumber((long) executionMetadata.getRunSequence()).build())
            .build();

    log.info("Build execution args for integration stage  {}", stageElementConfig.getIdentifier());
    return ExecutionElementConfig.builder()
        .uuid(execution.getUuid())
        .steps(ciStepGroupUtils.createExecutionWrapperWithInitializeStep(
            stageElementConfig, ciExecutionArgs, ciCodeBase, infrastructure))
        .build();
  }
}