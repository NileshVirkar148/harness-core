package io.harness.pms.plan.execution;

import static io.harness.annotations.dev.HarnessTeam.PIPELINE;
import static io.harness.rule.OwnerRule.NAMAN;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import io.harness.CategoryTest;
import io.harness.annotations.dev.OwnedBy;
import io.harness.category.element.UnitTests;
import io.harness.ng.core.dto.ResponseDTO;
import io.harness.pms.pipeline.PipelineEntity;
import io.harness.pms.pipeline.service.PMSPipelineService;
import io.harness.pms.stages.StageExecutionResponse;
import io.harness.rule.Owner;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@OwnedBy(PIPELINE)
public class PlanExecutionResourceTest extends CategoryTest {
  @InjectMocks PlanExecutionResource planExecutionResource;
  @Mock PMSPipelineService pmsPipelineService;

  private final String ACCOUNT_ID = "account_id";
  private final String ORG_IDENTIFIER = "orgId";
  private final String PROJ_IDENTIFIER = "projId";
  private final String PIPELINE_IDENTIFIER = "p1";

  PipelineEntity entity;

  @Before
  public void setUp() throws IOException {
    MockitoAnnotations.initMocks(this);
    String yaml = "pipeline:\n"
        + "  identifier: p1\n"
        + "  name: p1\n"
        + "  stages:\n"
        + "  - stage:\n"
        + "      identifier: qaStage\n"
        + "      type: Approval\n"
        + "      name: qa stage\n"
        + "  - stage:\n"
        + "      identifier: qaStage2\n"
        + "      type: Deployment\n"
        + "      name: qa stage 2";
    entity = PipelineEntity.builder()
                 .accountId(ACCOUNT_ID)
                 .orgIdentifier(ORG_IDENTIFIER)
                 .projectIdentifier(PROJ_IDENTIFIER)
                 .identifier(PIPELINE_IDENTIFIER)
                 .name(PIPELINE_IDENTIFIER)
                 .yaml(yaml)
                 .build();
  }

  @Test
  @Owner(developers = NAMAN)
  @Category(UnitTests.class)
  public void testGetStagesExecutionList() {
    doReturn(Optional.of(entity))
        .when(pmsPipelineService)
        .get(ACCOUNT_ID, ORG_IDENTIFIER, PROJ_IDENTIFIER, PIPELINE_IDENTIFIER, false);
    ResponseDTO<List<StageExecutionResponse>> stagesExecutionList = planExecutionResource.getStagesExecutionList(
        ACCOUNT_ID, ORG_IDENTIFIER, PROJ_IDENTIFIER, PIPELINE_IDENTIFIER, null);
    assertThat(stagesExecutionList.getData()).hasSize(2);
    StageExecutionResponse stage0Data = stagesExecutionList.getData().get(0);
    assertThat(stage0Data.getStageIdentifier()).isEqualTo("qaStage");
    assertThat(stage0Data.getStageName()).isEqualTo("qa stage");
    assertThat(stage0Data.getMessage()).isEqualTo("Running an approval stage individually can be redundant");
    assertThat(stage0Data.getStagesRequired()).hasSize(0);
    StageExecutionResponse stage1Data = stagesExecutionList.getData().get(1);
    assertThat(stage1Data.getStageIdentifier()).isEqualTo("qaStage2");
    assertThat(stage1Data.getStageName()).isEqualTo("qa stage 2");
    assertThat(stage1Data.getMessage()).isNull();
    assertThat(stage1Data.getStagesRequired()).hasSize(1);
    assertThat(stage1Data.getStagesRequired().get(0)).isEqualTo("qaStage");
  }
}