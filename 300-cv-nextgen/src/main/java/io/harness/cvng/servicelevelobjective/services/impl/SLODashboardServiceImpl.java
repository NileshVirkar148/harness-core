package io.harness.cvng.servicelevelobjective.services.impl;

import io.harness.cvng.core.beans.monitoredService.MonitoredServiceDTO;
import io.harness.cvng.core.beans.params.PageParams;
import io.harness.cvng.core.beans.params.ProjectParams;
import io.harness.cvng.core.services.api.monitoredService.MonitoredServiceService;
import io.harness.cvng.core.utils.DateTimeUtils;
import io.harness.cvng.servicelevelobjective.beans.SLODashboardApiFilter;
import io.harness.cvng.servicelevelobjective.beans.SLODashboardWidget;
import io.harness.cvng.servicelevelobjective.beans.ServiceLevelObjectiveDTO;
import io.harness.cvng.servicelevelobjective.beans.ServiceLevelObjectiveResponse;
import io.harness.cvng.servicelevelobjective.entities.ServiceLevelObjective;
import io.harness.cvng.servicelevelobjective.services.api.SLIRecordService;
import io.harness.cvng.servicelevelobjective.services.api.SLODashboardService;
import io.harness.cvng.servicelevelobjective.services.api.ServiceLevelObjectiveService;
import io.harness.ng.beans.PageResponse;
import io.harness.ng.core.common.beans.NGTag;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class SLODashboardServiceImpl implements SLODashboardService {
  @Inject private ServiceLevelObjectiveService serviceLevelObjectiveService;
  @Inject private MonitoredServiceService monitoredServiceService;
  @Inject private SLIRecordService sliRecordService;
  @Inject private Clock clock;
  @Override
  public PageResponse<SLODashboardWidget> getSloDashboardWidgets(
      ProjectParams projectParams, SLODashboardApiFilter filter, PageParams pageParams) {
    PageResponse<ServiceLevelObjectiveResponse> sloPageResponse =
        serviceLevelObjectiveService.getSLOForDashboard(projectParams, filter, pageParams);
    Set<String> monitoredServiceIdentifiers =
        sloPageResponse.getContent()
            .stream()
            .map(slo -> slo.getServiceLevelObjectiveDTO().getMonitoredServiceRef())
            .collect(Collectors.toSet());
    Map<String, MonitoredServiceDTO> identifierToMonitoredServiceMap =
        getIdentifierToMonitoredServiceDTOMap(projectParams, monitoredServiceIdentifiers);
    List<SLODashboardWidget> sloDashboardWidgets =
        sloPageResponse.getContent()
            .stream()
            .map(sloResponse -> {
              Preconditions.checkState(
                  sloResponse.getServiceLevelObjectiveDTO().getServiceLevelIndicators().size() == 1,
                  "Only one service level indicator is supported");
              ServiceLevelObjectiveDTO slo = sloResponse.getServiceLevelObjectiveDTO();
              ServiceLevelObjective serviceLevelObjective =
                  serviceLevelObjectiveService.getEntity(projectParams, slo.getIdentifier());
              MonitoredServiceDTO monitoredService = identifierToMonitoredServiceMap.get(slo.getMonitoredServiceRef());
              LocalDate currentLocalDate = LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC).toLocalDate();
              ServiceLevelObjective.TimePeriod timePeriod = serviceLevelObjective.getCurrentTimeRange(currentLocalDate);
              List<SLODashboardWidget.Point> points = new ArrayList<>();
              Instant currentTimeMinute = DateTimeUtils.roundDownTo1MinBoundary(clock.instant());
              for (int i = 100; i >= 0; i--) {
                points.add(SLODashboardWidget.Point.builder()
                               .timestamp(currentTimeMinute.minus(Duration.ofMinutes(i * 15)).toEpochMilli())
                               .value(i)
                               .build());
              }
              return SLODashboardWidget.builder()
                  .title(slo.getName())
                  .monitoredServiceIdentifier(slo.getMonitoredServiceRef())
                  .monitoredServiceName(monitoredService.getName())
                  .healthSourceIdentifier(slo.getHealthSourceRef())
                  .healthSourceName(getHealthSourceName(monitoredService, slo.getHealthSourceRef()))
                  .tags(getNGTags(slo.getTags()))
                  .type(slo.getServiceLevelIndicators().get(0).getType())
                  .burnRate(SLODashboardWidget.BurnRate.builder().currentRatePercentage(10).build())
                  .sloPerformanceTrend(points)
                  .errorBudgetBurndown(points)
                  .timeRemainingDays(timePeriod.getRemainingDays(currentLocalDate).getDays())
                  .build();
            })
            .collect(Collectors.toList());
    return PageResponse.<SLODashboardWidget>builder()
        .pageSize(sloPageResponse.getPageSize())
        .pageIndex(sloPageResponse.getPageIndex())
        .totalPages(sloPageResponse.getTotalPages())
        .totalItems(sloPageResponse.getTotalItems())
        .pageItemCount(sloPageResponse.getPageItemCount())
        .content(sloDashboardWidgets)
        .build();
  }

  @NotNull
  private Map<String, MonitoredServiceDTO> getIdentifierToMonitoredServiceDTOMap(
      ProjectParams projectParams, Set<String> monitoredServiceIdentifiers) {
    List<MonitoredServiceDTO> monitoredServiceDTOS =
        monitoredServiceService.get(projectParams, monitoredServiceIdentifiers)
            .stream()
            .map(monitoredServiceResponse -> monitoredServiceResponse.getMonitoredServiceDTO())
            .collect(Collectors.toList());
    return monitoredServiceDTOS.stream().collect(
        Collectors.toMap(MonitoredServiceDTO::getIdentifier, Function.identity()));
  }

  private String getHealthSourceName(MonitoredServiceDTO monitoredServiceDTO, String healthSourceRef) {
    return monitoredServiceDTO.getSources()
        .getHealthSources()
        .stream()
        .filter(healthSource -> healthSource.getIdentifier().equals(healthSourceRef))
        .findFirst()
        .orElseThrow(()
                         -> new IllegalStateException(
                             "Health source identifier" + healthSourceRef + " not found in monitored service"))
        .getName();
  }
  private List<NGTag> getNGTags(Map<String, String> tags) {
    return tags.entrySet()
        .stream()
        .map(entry -> NGTag.builder().key(entry.getKey()).value(entry.getValue()).build())
        .collect(Collectors.toList());
  }
}
