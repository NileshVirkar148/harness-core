/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.cvng.governance.services;

import static io.harness.cvng.governance.beans.ExpansionKeysConstants.ENVIRONMENT_REF;
import static io.harness.cvng.governance.beans.ExpansionKeysConstants.INFRASTRUCTURE;
import static io.harness.cvng.governance.beans.ExpansionKeysConstants.MONITORED_SERVICE_DELIMITER;
import static io.harness.cvng.governance.beans.ExpansionKeysConstants.SERVICE_CONFIG;
import static io.harness.cvng.governance.beans.ExpansionKeysConstants.SERVICE_REF;
import static io.harness.data.structure.EmptyPredicate.isEmpty;

import io.harness.cvng.core.beans.params.ProjectParams;
import io.harness.cvng.core.utils.DateTimeUtils;
import io.harness.cvng.governance.beans.SLOPolicyDTO;
import io.harness.cvng.governance.beans.SLOPolicyExpandedValue;
import io.harness.cvng.servicelevelobjective.beans.SLODashboardWidget;
import io.harness.cvng.servicelevelobjective.entities.ServiceLevelIndicator;
import io.harness.cvng.servicelevelobjective.entities.ServiceLevelObjective;
import io.harness.cvng.servicelevelobjective.services.api.SLIRecordService;
import io.harness.cvng.servicelevelobjective.services.api.ServiceLevelIndicatorService;
import io.harness.cvng.servicelevelobjective.services.api.ServiceLevelObjectiveService;
import io.harness.pms.contracts.governance.ExpansionPlacementStrategy;
import io.harness.pms.contracts.governance.ExpansionRequestMetadata;
import io.harness.pms.sdk.core.governance.ExpandedValue;
import io.harness.pms.sdk.core.governance.ExpansionResponse;
import io.harness.pms.sdk.core.governance.JsonExpansionHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public class SLOPolicyExpansionHandler implements JsonExpansionHandler {
  @Inject ServiceLevelObjectiveService serviceLevelObjectiveService;
  @Inject ServiceLevelIndicatorService serviceLevelIndicatorService;
  @Inject SLIRecordService sliRecordService;
  @Inject private Clock clock;

  @Override
  public ExpansionResponse expand(JsonNode fieldValue, ExpansionRequestMetadata metadata) {
    String accountId = metadata.getAccountId();
    String orgId = metadata.getOrgId();
    String projectId = metadata.getProjectId();
    String serviceRef = fieldValue.get(SERVICE_CONFIG).get(SERVICE_REF).asText();
    String environmentRef = fieldValue.get(INFRASTRUCTURE).get(ENVIRONMENT_REF).asText();
    String monitoredServiceRef = serviceRef + MONITORED_SERVICE_DELIMITER + environmentRef;
    SLOPolicyDTO sloPolicyDTO;
    ProjectParams projectParams =
        ProjectParams.builder().accountIdentifier(accountId).projectIdentifier(projectId).orgIdentifier(orgId).build();

    List<ServiceLevelObjective> serviceLevelObjectiveList =
        serviceLevelObjectiveService.getByMonitoredServiceIdentifier(projectParams, monitoredServiceRef);
    if (isEmpty(serviceLevelObjectiveList)) {
      sloPolicyDTO =
          SLOPolicyDTO.builder().statusOfMonitoredService(SLOPolicyDTO.MonitoredServiceStatus.NOT_CONFIGURED).build();
    } else {
      double sloErrorBudgetRemaining = Double.MAX_VALUE;
      for (ServiceLevelObjective serviceLevelObjective : serviceLevelObjectiveList) {
        Preconditions.checkState(serviceLevelObjective.getServiceLevelIndicators().size() == 1,
            "Only one service level indicator is supported");

        ServiceLevelIndicator serviceLevelIndicator = serviceLevelIndicatorService.getServiceLevelIndicator(
            projectParams, serviceLevelObjective.getServiceLevelIndicators().get(0));

        LocalDateTime currentLocalDate =
            LocalDateTime.ofInstant(clock.instant(), serviceLevelObjective.getZoneOffset());
        int totalErrorBudgetMinutes = serviceLevelObjective.getTotalErrorBudgetMinutes(currentLocalDate);
        ServiceLevelObjective.TimePeriod timePeriod = serviceLevelObjective.getCurrentTimeRange(currentLocalDate);
        Instant currentTimeMinute = DateTimeUtils.roundDownTo1MinBoundary(clock.instant());

        SLODashboardWidget.SLOGraphData sloGraphData = sliRecordService.getGraphData(serviceLevelIndicator.getUuid(),
            timePeriod.getStartTime(serviceLevelObjective.getZoneOffset()), currentTimeMinute, totalErrorBudgetMinutes,
            serviceLevelIndicator.getSliMissingDataType(), serviceLevelIndicator.getVersion());
        if (sloErrorBudgetRemaining > sloGraphData.getErrorBudgetRemainingPercentage()) {
          sloErrorBudgetRemaining = sloGraphData.getErrorBudgetRemainingPercentage();
        }
      }
      sloPolicyDTO = SLOPolicyDTO.builder()
                         .sloErrorBudgetRemaining(sloErrorBudgetRemaining)
                         .statusOfMonitoredService(SLOPolicyDTO.MonitoredServiceStatus.CONFIGURED)
                         .build();
    }
    ExpandedValue value = SLOPolicyExpandedValue.builder().sloPolicyDTO(sloPolicyDTO).build();

    return ExpansionResponse.builder()
        .success(true)
        .key(value.getKey())
        .value(value)
        .placement(ExpansionPlacementStrategy.APPEND)
        .build();
  }
}
