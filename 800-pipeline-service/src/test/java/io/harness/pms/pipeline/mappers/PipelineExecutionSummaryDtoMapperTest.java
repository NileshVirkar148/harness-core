package io.harness.pms.pipeline.mappers;

import static io.harness.annotations.dev.HarnessTeam.PIPELINE;
import static io.harness.rule.OwnerRule.NAMAN;

import static org.assertj.core.api.Assertions.assertThat;

import io.harness.CategoryTest;
import io.harness.annotations.dev.OwnedBy;
import io.harness.category.element.UnitTests;
import io.harness.execution.StagesExecutionMetadata;
import io.harness.gitsync.sdk.EntityGitDetails;
import io.harness.pms.plan.execution.beans.PipelineExecutionSummaryEntity;
import io.harness.pms.plan.execution.beans.dto.PipelineExecutionSummaryDTO;
import io.harness.rule.Owner;

import java.util.Collections;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@OwnedBy(PIPELINE)
public class PipelineExecutionSummaryDtoMapperTest extends CategoryTest {
  String accountId = "acc";
  String orgId = "org";
  String projId = "proj";
  String pipelineId = "pipelineId";
  String planId = "plan-random";

  String branch = "branch";
  String repo = "repo";
  String objectId = "o";
  String rootFolder = "folder/.harness/";
  String file = "file.yaml";

  @Test
  @Owner(developers = NAMAN)
  @Category(UnitTests.class)
  public void testToDtoForGitDetails() {
    PipelineExecutionSummaryEntity executionSummaryEntity = PipelineExecutionSummaryEntity.builder()
                                                                .accountId(accountId)
                                                                .orgIdentifier(orgId)
                                                                .projectIdentifier(projId)
                                                                .pipelineIdentifier(pipelineId)
                                                                .runSequence(1)
                                                                .planExecutionId(planId)
                                                                .build();
    PipelineExecutionSummaryDTO executionSummaryDTO =
        PipelineExecutionSummaryDtoMapper.toDto(executionSummaryEntity, null);
    assertThat(executionSummaryDTO).isNotNull();
    assertThat(executionSummaryDTO.getGitDetails()).isNull();

    EntityGitDetails entityGitDetails =
        EntityGitDetails.builder().branch(branch).repoIdentifier(repo).objectId(objectId).build();
    executionSummaryDTO = PipelineExecutionSummaryDtoMapper.toDto(executionSummaryEntity, entityGitDetails);
    assertThat(executionSummaryDTO).isNotNull();
    assertThat(executionSummaryDTO.getGitDetails()).isNotNull();
    assertThat(executionSummaryDTO.getGitDetails().getBranch()).isEqualTo(branch);
    assertThat(executionSummaryDTO.getGitDetails().getRepoIdentifier()).isEqualTo(repo);
    assertThat(executionSummaryDTO.getGitDetails().getObjectId()).isEqualTo(objectId);
    assertThat(executionSummaryDTO.getGitDetails().getFilePath()).isNull();
    assertThat(executionSummaryDTO.getGitDetails().getRootFolder()).isNull();

    entityGitDetails = EntityGitDetails.builder()
                           .branch(branch)
                           .repoIdentifier(repo)
                           .objectId(objectId)
                           .rootFolder("__default__")
                           .filePath("__default__")
                           .build();
    executionSummaryDTO = PipelineExecutionSummaryDtoMapper.toDto(executionSummaryEntity, entityGitDetails);
    assertThat(executionSummaryDTO).isNotNull();
    assertThat(executionSummaryDTO.getGitDetails()).isNotNull();
    assertThat(executionSummaryDTO.getGitDetails().getBranch()).isEqualTo(branch);
    assertThat(executionSummaryDTO.getGitDetails().getRepoIdentifier()).isEqualTo(repo);
    assertThat(executionSummaryDTO.getGitDetails().getObjectId()).isEqualTo(objectId);
    assertThat(executionSummaryDTO.getGitDetails().getFilePath()).isNull();
    assertThat(executionSummaryDTO.getGitDetails().getRootFolder()).isNull();

    entityGitDetails = EntityGitDetails.builder()
                           .branch(branch)
                           .repoIdentifier(repo)
                           .objectId(objectId)
                           .rootFolder(rootFolder)
                           .filePath(file)
                           .build();
    executionSummaryDTO = PipelineExecutionSummaryDtoMapper.toDto(executionSummaryEntity, entityGitDetails);
    assertThat(executionSummaryDTO).isNotNull();
    assertThat(executionSummaryDTO.getGitDetails()).isNotNull();
    assertThat(executionSummaryDTO.getGitDetails().getBranch()).isEqualTo(branch);
    assertThat(executionSummaryDTO.getGitDetails().getRepoIdentifier()).isEqualTo(repo);
    assertThat(executionSummaryDTO.getGitDetails().getObjectId()).isEqualTo(objectId);
    assertThat(executionSummaryDTO.getGitDetails().getFilePath()).isEqualTo(file);
    assertThat(executionSummaryDTO.getGitDetails().getRootFolder()).isEqualTo(rootFolder);
  }

  @Test
  @Owner(developers = NAMAN)
  @Category(UnitTests.class)
  public void testToDtoForStagesExecutionMetadata() {
    PipelineExecutionSummaryEntity executionSummaryEntity = PipelineExecutionSummaryEntity.builder()
                                                                .accountId(accountId)
                                                                .orgIdentifier(orgId)
                                                                .projectIdentifier(projId)
                                                                .pipelineIdentifier(pipelineId)
                                                                .runSequence(1)
                                                                .planExecutionId(planId)
                                                                .build();
    PipelineExecutionSummaryDTO executionSummaryDTO =
        PipelineExecutionSummaryDtoMapper.toDto(executionSummaryEntity, null);
    assertThat(executionSummaryDTO.isStagesExecution()).isFalse();
    assertThat(executionSummaryDTO.getStagesExecuted()).isNull();

    PipelineExecutionSummaryEntity executionSummaryEntityWithStages =
        PipelineExecutionSummaryEntity.builder()
            .accountId(accountId)
            .orgIdentifier(orgId)
            .projectIdentifier(projId)
            .pipelineIdentifier(pipelineId)
            .runSequence(1)
            .planExecutionId(planId)
            .stagesExecutionMetadata(StagesExecutionMetadata.builder()
                                         .isStagesExecution(true)
                                         .stageIdentifiers(Collections.singletonList("s1"))
                                         .build())
            .build();
    PipelineExecutionSummaryDTO executionSummaryDTOWithStages =
        PipelineExecutionSummaryDtoMapper.toDto(executionSummaryEntityWithStages, null);
    assertThat(executionSummaryDTOWithStages.isStagesExecution()).isTrue();
    assertThat(executionSummaryDTOWithStages.getStagesExecuted()).hasSize(1);
    assertThat(executionSummaryDTOWithStages.getStagesExecuted().contains("s1")).isTrue();
  }
}