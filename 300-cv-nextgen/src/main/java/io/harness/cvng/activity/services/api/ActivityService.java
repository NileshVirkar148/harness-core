package io.harness.cvng.activity.services.api;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.cvng.activity.beans.ActivityDashboardDTO;
import io.harness.cvng.activity.beans.ActivityVerificationResultDTO;
import io.harness.cvng.activity.beans.DeploymentActivityPopoverResultDTO;
import io.harness.cvng.activity.beans.DeploymentActivityResultDTO;
import io.harness.cvng.activity.beans.DeploymentActivitySummaryDTO;
import io.harness.cvng.activity.beans.DeploymentActivityVerificationResultDTO;
import io.harness.cvng.activity.entities.Activity;
import io.harness.cvng.analysis.beans.LogAnalysisClusterChartDTO;
import io.harness.cvng.analysis.beans.LogAnalysisClusterDTO;
import io.harness.cvng.analysis.beans.TransactionMetricInfoSummaryPageDTO;
import io.harness.cvng.beans.activity.ActivityDTO;
import io.harness.cvng.beans.activity.ActivityStatusDTO;
import io.harness.cvng.beans.activity.ActivityType;
import io.harness.cvng.beans.activity.ActivityVerificationStatus;
import io.harness.cvng.core.beans.DatasourceTypeDTO;
import io.harness.cvng.core.beans.monitoredService.healthSouceSpec.HealthSourceDTO;
import io.harness.cvng.core.beans.params.PageParams;
import io.harness.cvng.core.beans.params.ProjectParams;
import io.harness.cvng.core.beans.params.ServiceEnvironmentParams;
import io.harness.cvng.core.beans.params.filterParams.DeploymentLogAnalysisFilter;
import io.harness.cvng.core.beans.params.filterParams.DeploymentTimeSeriesAnalysisFilter;
import io.harness.ng.beans.PageResponse;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import lombok.NonNull;

@OwnedBy(HarnessTeam.CV)
public interface ActivityService {
  Activity get(String activityId);
  Activity getByVerificationJobInstanceId(String verificationJobInstanceId);

  String register(String accountId, ActivityDTO activityDTO);
  String register(Activity activity);

  void updateActivityStatus(Activity activity);

  List<DeploymentActivityVerificationResultDTO> getRecentDeploymentActivityVerifications(
      String accountId, String orgIdentifier, String projectIdentifier);

  DeploymentActivityResultDTO getDeploymentActivityVerificationsByTag(
      String accountId, String orgIdentifier, String projectIdentifier, String serviceIdentifier, String deploymentTag);
  DeploymentActivityPopoverResultDTO getDeploymentActivityVerificationsPopoverSummary(
      String accountId, String orgIdentifier, String projectIdentifier, String serviceIdentifier, String deploymentTag);
  Activity getActivityFromDTO(ActivityDTO activityDTO);

  String getDeploymentTagFromActivity(String accountId, String verificationJobInstanceId);

  String createActivity(Activity activity);

  List<ActivityDashboardDTO> listActivitiesInTimeRange(@NotNull @NonNull ProjectParams projectParams,
      @Nullable String serviceIdentifier, @Nullable String environmentIdentifier, @NotNull Instant startTime,
      @NotNull Instant endTime);

  List<ActivityVerificationResultDTO> getRecentActivityVerificationResults(
      String accountId, String orgIdentifier, String projectIdentifier, int size);
  ActivityVerificationResultDTO getActivityVerificationResult(String accountId, String activityId);

  DeploymentActivitySummaryDTO getDeploymentSummary(String activityId);

  ActivityStatusDTO getActivityStatus(String accountId, String activityId);
  List<String> createVerificationJobInstancesForActivity(Activity activity);
  TransactionMetricInfoSummaryPageDTO getDeploymentActivityTimeSeriesData(String accountId, String activityId,
      DeploymentTimeSeriesAnalysisFilter deploymentTimeSeriesAnalysisFilter, PageParams pageParams);
  Set<DatasourceTypeDTO> getDataSourcetypes(String accountId, String activityId);

  List<LogAnalysisClusterChartDTO> getDeploymentActivityLogAnalysisClusters(
      String accountId, String activityId, DeploymentLogAnalysisFilter deploymentLogAnalysisFilter);

  PageResponse<LogAnalysisClusterDTO> getDeploymentActivityLogAnalysisResult(String accountId, String activityId,
      Integer label, DeploymentLogAnalysisFilter deploymentLogAnalysisFilter, PageParams pageParams);

  void abort(String activityId);

  Set<HealthSourceDTO> healthSources(String accountId, String activityId);

  void upsert(Activity activity);

  List<Activity> get(ServiceEnvironmentParams serviceEnvironmentParams, List<String> changeSourceIdentifiers,
      Instant startTime, Instant endTime, List<ActivityType> activityTypes);

  Long getCount(ServiceEnvironmentParams serviceEnvironmentParams, List<String> changeSourceIdentifiers,
      Instant startTime, Instant endTime, List<ActivityType> activityTypes);
  String createActivityForDemo(Activity activity, ActivityVerificationStatus verificationStatus);
}
