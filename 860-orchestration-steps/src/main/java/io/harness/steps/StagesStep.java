package io.harness.steps;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.pms.contracts.steps.StepCategory;
import io.harness.pms.contracts.steps.StepType;
import io.harness.steps.common.NGSectionStepWithRollbackInfo;

@OwnedBy(HarnessTeam.PIPELINE)
public class StagesStep extends NGSectionStepWithRollbackInfo {
  public static final StepType STEP_TYPE =
      StepType.newBuilder().setType(OrchestrationStepTypes.NG_STAGES_STEP).setStepCategory(StepCategory.STEP).build();
}