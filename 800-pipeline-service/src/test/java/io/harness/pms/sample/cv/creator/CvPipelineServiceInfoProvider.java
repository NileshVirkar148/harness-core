package io.harness.pms.sample.cv.creator;

import io.harness.plancreator.pipeline.NGPipelinePlanCreator;
import io.harness.plancreator.stages.StagesPlanCreator;
import io.harness.plancreator.stages.parallel.ParallelPlanCreator;
import io.harness.pms.sdk.core.pipeline.filters.FilterJsonCreator;
import io.harness.pms.sdk.core.pipeline.filters.PipelineFilterJsonCreator;
import io.harness.pms.sdk.core.plan.creation.creators.PartialPlanCreator;
import io.harness.pms.sdk.core.plan.creation.creators.PipelineServiceInfoProvider;
import io.harness.pms.steps.StepInfo;
import io.harness.pms.steps.StepMetaData;

import java.util.ArrayList;
import java.util.List;

public class CvPipelineServiceInfoProvider implements PipelineServiceInfoProvider {
  @Override
  public List<PartialPlanCreator<?>> getPlanCreators() {
    List<PartialPlanCreator<?>> planCreators = new ArrayList<>();
    planCreators.add(new NGPipelinePlanCreator());
    planCreators.add(new StagesPlanCreator());
    planCreators.add(new CvStepPlanCreator());
    planCreators.add(new ParallelPlanCreator());
    return planCreators;
  }

  @Override
  public List<FilterJsonCreator> getFilterJsonCreators() {
    List<FilterJsonCreator> filterJsonCreators = new ArrayList<>();
    filterJsonCreators.add(new PipelineFilterJsonCreator());
    return filterJsonCreators;
  }

  @Override
  public List<StepInfo> getStepInfo() {
    List<StepInfo> stepInfos = new ArrayList<>();
    stepInfos.add(StepInfo.newBuilder()
                      .setName("appdVerify")
                      .setStepMetaData(StepMetaData.newBuilder().setFolderPath("Verification").build())
                      .build());
    return stepInfos;
  }
}
