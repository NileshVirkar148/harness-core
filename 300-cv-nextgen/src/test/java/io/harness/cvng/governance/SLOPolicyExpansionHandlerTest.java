/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.cvng.governance;

import static io.harness.data.structure.UUIDGenerator.generateUuid;
import static io.harness.rule.OwnerRule.DEEPAK_CHHIKARA;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import io.harness.CvNextGenTestBase;
import io.harness.category.element.UnitTests;
import io.harness.cvng.BuilderFactory;
import io.harness.cvng.core.beans.params.ProjectParams;
import io.harness.cvng.core.utils.DateTimeUtils;
import io.harness.cvng.governance.beans.SLOPolicyDTO;
import io.harness.cvng.governance.beans.SLOPolicyDTO.MonitoredServiceStatus;
import io.harness.cvng.governance.services.SLOPolicyExpansionHandler;
import io.harness.cvng.servicelevelobjective.beans.SLODashboardWidget;
import io.harness.cvng.servicelevelobjective.beans.SLOTargetType;
import io.harness.cvng.servicelevelobjective.beans.ServiceLevelObjectiveDTO;
import io.harness.cvng.servicelevelobjective.entities.ServiceLevelIndicator;
import io.harness.cvng.servicelevelobjective.entities.ServiceLevelObjective;
import io.harness.cvng.servicelevelobjective.services.api.SLIRecordService;
import io.harness.cvng.servicelevelobjective.services.api.ServiceLevelIndicatorService;
import io.harness.cvng.servicelevelobjective.services.api.ServiceLevelObjectiveService;
import io.harness.cvng.servicelevelobjective.transformer.servicelevelindicator.SLOTargetTransformer;
import io.harness.cvng.servicelevelobjective.transformer.servicelevelindicator.ServiceLevelIndicatorEntityAndDTOTransformer;
import io.harness.ng.core.mapper.TagMapper;
import io.harness.pms.contracts.governance.ExpansionPlacementStrategy;
import io.harness.pms.contracts.governance.ExpansionRequestMetadata;
import io.harness.pms.sdk.core.governance.ExpansionResponse;
import io.harness.rule.Owner;
import io.harness.serializer.JsonUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Charsets;
import com.google.inject.Inject;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class SLOPolicyExpansionHandlerTest extends CvNextGenTestBase {
  @InjectMocks SLOPolicyExpansionHandler sloPolicyExpansionHandler;
  @Mock ServiceLevelObjectiveService serviceLevelObjectiveService;
  @Mock ServiceLevelIndicatorService serviceLevelIndicatorService;
  @Mock SLIRecordService sliRecordService;
  @Inject Clock clock;
  @Inject private ServiceLevelIndicatorEntityAndDTOTransformer serviceLevelIndicatorEntityAndDTOTransformer;
  @Inject private Map<SLOTargetType, SLOTargetTransformer> sloTargetTypeSLOTargetTransformerMap;
  BuilderFactory builderFactory;
  String monitoredServiceIndicator = "service_env";
  String healthSourceIndicator = "healthSourceIndicator";
  ProjectParams projectParams;
  ServiceLevelObjective serviceLevelObjective;

  @Before
  public void setUp() throws IllegalAccessException {
    MockitoAnnotations.initMocks(this);
    builderFactory = BuilderFactory.getDefault();
    projectParams = builderFactory.getProjectParams();
    serviceLevelObjective = getServiceLevelObjective();
    ServiceLevelIndicator serviceLevelIndicator = serviceLevelIndicatorEntityAndDTOTransformer.getEntity(projectParams,
        builderFactory.getRatioServiceLevelIndicatorDTOBuilder().build(), monitoredServiceIndicator,
        healthSourceIndicator);
    serviceLevelIndicator.setUuid(generateUuid());
    serviceLevelObjective.setServiceLevelIndicators(Collections.singletonList(serviceLevelIndicator.getIdentifier()));
    FieldUtils.writeField(
        sloPolicyExpansionHandler, "serviceLevelObjectiveService", serviceLevelObjectiveService, true);
    FieldUtils.writeField(
        sloPolicyExpansionHandler, "serviceLevelIndicatorService", serviceLevelIndicatorService, true);
    FieldUtils.writeField(sloPolicyExpansionHandler, "sliRecordService", sliRecordService, true);
    FieldUtils.writeField(sloPolicyExpansionHandler, "clock", clock, true);

    when(serviceLevelObjectiveService.getByMonitoredServiceIdentifier(
             builderFactory.getProjectParams(), monitoredServiceIndicator))
        .thenReturn(Collections.singletonList(serviceLevelObjective));
    when(serviceLevelIndicatorService.getServiceLevelIndicator(
             builderFactory.getProjectParams(), serviceLevelIndicator.getIdentifier()))
        .thenReturn(serviceLevelIndicator);
    LocalDateTime currentLocalDate = LocalDateTime.ofInstant(clock.instant(), serviceLevelObjective.getZoneOffset());
    int totalErrorBudgetMinutes = serviceLevelObjective.getTotalErrorBudgetMinutes(currentLocalDate);
    ServiceLevelObjective.TimePeriod timePeriod = serviceLevelObjective.getCurrentTimeRange(currentLocalDate);
    Instant currentTimeMinute = DateTimeUtils.roundDownTo1MinBoundary(clock.instant());
    when(sliRecordService.getGraphData(serviceLevelIndicator.getUuid(),
             timePeriod.getStartTime(serviceLevelObjective.getZoneOffset()), currentTimeMinute, totalErrorBudgetMinutes,
             serviceLevelIndicator.getSliMissingDataType(), serviceLevelIndicator.getVersion()))
        .thenReturn(SLODashboardWidget.SLOGraphData.builder().errorBudgetRemainingPercentage(50).build());
  }

  @Test
  @Owner(developers = DEEPAK_CHHIKARA)
  @Category(UnitTests.class)
  public void testExpand() throws IOException {
    SLOPolicyDTO sloPolicyDTO = SLOPolicyDTO.builder()
                                    .sloErrorBudgetRemainingPercentage(50D)
                                    .statusOfMonitoredService(MonitoredServiceStatus.CONFIGURED)
                                    .build();
    final String yaml = IOUtils.resourceToString(
        "governance/SLOPolicyExpansionHandlerInput.json", Charsets.UTF_8, this.getClass().getClassLoader());
    JsonNode jsonNode = JsonUtils.asObject(yaml, JsonNode.class);
    ExpansionRequestMetadata metadataProject =
        ExpansionRequestMetadata.newBuilder()
            .setAccountId(builderFactory.getProjectParams().getAccountIdentifier())
            .setOrgId(builderFactory.getProjectParams().getOrgIdentifier())
            .setProjectId(builderFactory.getProjectParams().getProjectIdentifier())
            .build();
    ExpansionResponse expansionResponse = sloPolicyExpansionHandler.expand(jsonNode, metadataProject);
    assertThat(expansionResponse.isSuccess()).isTrue();
    assertThat(expansionResponse.getKey()).isEqualTo("sloPolicy");
    assertThat(expansionResponse.getValue().toJson()).isEqualTo(JsonUtils.asJson(sloPolicyDTO));
    assertThat(expansionResponse.getPlacement()).isEqualTo(ExpansionPlacementStrategy.APPEND);
  }

  @Test
  @Owner(developers = DEEPAK_CHHIKARA)
  @Category(UnitTests.class)
  public void testExpand_notConfigured() throws IOException {
    SLOPolicyDTO sloPolicyDTO =
        SLOPolicyDTO.builder().statusOfMonitoredService(MonitoredServiceStatus.NOT_CONFIGURED).build();
    when(serviceLevelObjectiveService.getByMonitoredServiceIdentifier(
             builderFactory.getProjectParams(), monitoredServiceIndicator))
        .thenReturn(null);
    final String yaml = IOUtils.resourceToString(
        "governance/SLOPolicyExpansionHandlerInput.json", Charsets.UTF_8, this.getClass().getClassLoader());
    JsonNode jsonNode = JsonUtils.asObject(yaml, JsonNode.class);
    ExpansionRequestMetadata metadataProject =
        ExpansionRequestMetadata.newBuilder()
            .setAccountId(builderFactory.getProjectParams().getAccountIdentifier())
            .setOrgId(builderFactory.getProjectParams().getOrgIdentifier())
            .setProjectId(builderFactory.getProjectParams().getProjectIdentifier())
            .build();
    ExpansionResponse expansionResponse = sloPolicyExpansionHandler.expand(jsonNode, metadataProject);
    assertThat(expansionResponse.isSuccess()).isTrue();
    assertThat(expansionResponse.getKey()).isEqualTo("sloPolicy");
    assertThat(expansionResponse.getValue().toJson()).isEqualTo(JsonUtils.asJson(sloPolicyDTO));
    assertThat(expansionResponse.getPlacement()).isEqualTo(ExpansionPlacementStrategy.APPEND);
  }

  ServiceLevelObjective getServiceLevelObjective() {
    ServiceLevelObjectiveDTO serviceLevelObjectiveDTO = builderFactory.getServiceLevelObjectiveDTOBuilder().build();
    return ServiceLevelObjective.builder()
        .accountId(projectParams.getAccountIdentifier())
        .orgIdentifier(projectParams.getOrgIdentifier())
        .projectIdentifier(projectParams.getProjectIdentifier())
        .identifier(serviceLevelObjectiveDTO.getIdentifier())
        .name(serviceLevelObjectiveDTO.getName())
        .desc(serviceLevelObjectiveDTO.getDescription())
        .type(serviceLevelObjectiveDTO.getType())
        .serviceLevelIndicators(serviceLevelIndicatorService.create(projectParams,
            serviceLevelObjectiveDTO.getServiceLevelIndicators(), serviceLevelObjectiveDTO.getIdentifier(),
            serviceLevelObjectiveDTO.getMonitoredServiceRef(), serviceLevelObjectiveDTO.getHealthSourceRef()))
        .monitoredServiceIdentifier(serviceLevelObjectiveDTO.getMonitoredServiceRef())
        .healthSourceIdentifier(serviceLevelObjectiveDTO.getHealthSourceRef())
        .tags(TagMapper.convertToList(serviceLevelObjectiveDTO.getTags()))
        .sloTarget(sloTargetTypeSLOTargetTransformerMap.get(serviceLevelObjectiveDTO.getTarget().getType())
                       .getSLOTarget(serviceLevelObjectiveDTO.getTarget().getSpec()))
        .sloTargetPercentage(serviceLevelObjectiveDTO.getTarget().getSloTargetPercentage())
        .userJourneyIdentifier(serviceLevelObjectiveDTO.getUserJourneyRef())
        .build();
  }
}
