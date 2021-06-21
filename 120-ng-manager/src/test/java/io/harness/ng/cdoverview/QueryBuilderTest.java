package io.harness.ng.cdoverview;

import static io.harness.rule.OwnerRule.MEENAKSHI;
import static io.harness.rule.OwnerRule.PRASHANTSHARMA;

import static org.assertj.core.api.Assertions.assertThat;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.category.element.UnitTests;
import io.harness.ng.cdOverview.service.CDOverviewDashboardServiceImpl;
import io.harness.pms.execution.ExecutionStatus;
import io.harness.rule.Owner;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@OwnedBy(HarnessTeam.CDC)
public class QueryBuilderTest {
  private static final long HOUR_IN_MS = 60 * 60 * 1000;
  private static final long DAY_IN_MS = 24 * HOUR_IN_MS;

  @Test
  @Owner(developers = PRASHANTSHARMA)
  @Category(UnitTests.class)
  public void testSelectStatusTime() {
    String expectedQueryResult =
        "select status,startts from pipeline_execution_summary_cd where accountid='accountId' and orgidentifier='orgId' and projectidentifier='projectId' and startts is not null and startts>=10 and startts<13;";
    String queryResult =
        new CDOverviewDashboardServiceImpl().queryBuilderSelectStatusTime("accountId", "orgId", "projectId", 10L, 13L);
    assertThat(queryResult).isEqualTo(expectedQueryResult);
  }

  @Test
  @Owner(developers = PRASHANTSHARMA)
  @Category(UnitTests.class)
  public void testQueryBuilderEnvironmentType() {
    String expectedQueryResult =
        "select env_type from service_infra_info where pipeline_execution_summary_cd_id in (select id from pipeline_execution_summary_cd where accountid='accountId' and orgidentifier='orgId' and projectidentifier='projectId' and startts is not null and startts>=10 and startts<13 ) and env_type is not null;";
    String queryResult =
        new CDOverviewDashboardServiceImpl().queryBuilderEnvironmentType("accountId", "orgId", "projectId", 10L, 13L);
    assertThat(queryResult).isEqualTo(expectedQueryResult);
  }

  @Test
  @Owner(developers = PRASHANTSHARMA)
  @Category(UnitTests.class)
  public void testGetRate() {
    long current = 0;
    long previous = 0;
    assertThat(new CDOverviewDashboardServiceImpl().getRate(current, previous)).isEqualTo(0.0);

    current = 5;
    assertThat(new CDOverviewDashboardServiceImpl().getRate(current, previous)).isEqualTo(0.0);

    current = 10;
    previous = 5;
    double rateExpected = (current - previous) / (double) previous;
    rateExpected = rateExpected * 100.0;
    assertThat(new CDOverviewDashboardServiceImpl().getRate(current, previous)).isEqualTo(rateExpected);

    current = 10;
    previous = 15;
    rateExpected = (current - previous) / (double) previous;
    rateExpected = rateExpected * 100.0;
    assertThat(new CDOverviewDashboardServiceImpl().getRate(current, previous)).isEqualTo(rateExpected);

    current = 15;
    previous = 15;
    assertThat(new CDOverviewDashboardServiceImpl().getRate(current, previous)).isEqualTo(0.0);
  }

  @Test
  @Owner(developers = PRASHANTSHARMA)
  @Category(UnitTests.class)
  public void testQueryBuilderStatus() {
    List<String> failedStatusList =
        Arrays.asList(ExecutionStatus.FAILED.name(), ExecutionStatus.ABORTED.name(), ExecutionStatus.EXPIRED.name());
    List<String> activeStatusList = Arrays.asList(ExecutionStatus.RUNNING.name());
    List<String> pendingStatusList =
        Arrays.asList(ExecutionStatus.INTERVENTIONWAITING.name(), ExecutionStatus.APPROVALWAITING.name());

    // failedStatusList
    String expectedQueryResult =
        "select id,name,startts,endTs,status from pipeline_execution_summary_cd where accountid='accountId' and orgidentifier='orgId' and projectidentifier='projectId' and status in ('FAILED','ABORTED','EXPIRED') and startts is not null ORDER BY startts DESC LIMIT 20;";
    String queryResult = new CDOverviewDashboardServiceImpl().queryBuilderStatus(
        "accountId", "orgId", "projectId", 20, failedStatusList);
    assertThat(queryResult).isEqualTo(expectedQueryResult);

    // activeStatusList
    expectedQueryResult =
        "select id,name,startts,endTs,status from pipeline_execution_summary_cd where accountid='accountId' and orgidentifier='orgId' and projectidentifier='projectId' and status in ('RUNNING') and startts is not null ORDER BY startts DESC LIMIT 20;";
    queryResult = new CDOverviewDashboardServiceImpl().queryBuilderStatus(
        "accountId", "orgId", "projectId", 20, activeStatusList);
    assertThat(queryResult).isEqualTo(expectedQueryResult);

    // pending
    expectedQueryResult =
        "select id,name,startts,endTs,status from pipeline_execution_summary_cd where accountid='accountId' and orgidentifier='orgId' and projectidentifier='projectId' and status in ('INTERVENTIONWAITING','APPROVALWAITING') and startts is not null ORDER BY startts DESC LIMIT 20;";
    queryResult = new CDOverviewDashboardServiceImpl().queryBuilderStatus(
        "accountId", "orgId", "projectId", 20, pendingStatusList);
    assertThat(queryResult).isEqualTo(expectedQueryResult);
  }

  @Test
  @Owner(developers = PRASHANTSHARMA)
  @Category(UnitTests.class)
  public void testQueryBuilderServiceTag() {
    List<String> failedStatusList =
        Arrays.asList(ExecutionStatus.FAILED.name(), ExecutionStatus.ABORTED.name(), ExecutionStatus.EXPIRED.name());
    List<String> activeStatusList = Arrays.asList(ExecutionStatus.RUNNING.name());
    List<String> pendingStatusList =
        Arrays.asList(ExecutionStatus.INTERVENTIONWAITING.name(), ExecutionStatus.APPROVALWAITING.name());

    String expectedQueryResult =
        "select service_name,tag,pipeline_execution_summary_cd_id from service_infra_info where pipeline_execution_summary_cd_id in (abc) and service_name is not null;";
    String queryResult = new CDOverviewDashboardServiceImpl().queryBuilderServiceTag("abc");
    assertThat(queryResult).isEqualTo(expectedQueryResult);
  }

  @Test
  @Owner(developers = PRASHANTSHARMA)
  @Category(UnitTests.class)
  public void testQueryBuilderSelectIdLimitTimeCdTable() {
    List<String> failedStatusList =
        Arrays.asList(ExecutionStatus.FAILED.name(), ExecutionStatus.ABORTED.name(), ExecutionStatus.EXPIRED.name());
    List<String> activeStatusList = Arrays.asList(ExecutionStatus.RUNNING.name());
    List<String> pendingStatusList =
        Arrays.asList(ExecutionStatus.INTERVENTIONWAITING.name(), ExecutionStatus.APPROVALWAITING.name());

    // failed
    String expectedQueryResult =
        "select id from pipeline_execution_summary_cd where accountid='acc' and orgidentifier='org' and projectidentifier='pro' and status in ('FAILED','ABORTED','EXPIRED') and startts is not null ORDER BY startts DESC LIMIT 4";
    String queryResult = new CDOverviewDashboardServiceImpl().queryBuilderSelectIdLimitTimeCdTable(
        "acc", "org", "pro", 4, failedStatusList);
    assertThat(queryResult).isEqualTo(expectedQueryResult);

    // active
    expectedQueryResult =
        "select id from pipeline_execution_summary_cd where accountid='acc' and orgidentifier='org' and projectidentifier='pro' and status in ('RUNNING') and startts is not null ORDER BY startts DESC LIMIT 4";
    queryResult = new CDOverviewDashboardServiceImpl().queryBuilderSelectIdLimitTimeCdTable(
        "acc", "org", "pro", 4, activeStatusList);
    assertThat(queryResult).isEqualTo(expectedQueryResult);

    // pending
    expectedQueryResult =
        "select id from pipeline_execution_summary_cd where accountid='acc' and orgidentifier='org' and projectidentifier='pro' and status in ('INTERVENTIONWAITING','APPROVALWAITING') and startts is not null ORDER BY startts DESC LIMIT 4";
    queryResult = new CDOverviewDashboardServiceImpl().queryBuilderSelectIdLimitTimeCdTable(
        "acc", "org", "pro", 4, pendingStatusList);
    assertThat(queryResult).isEqualTo(expectedQueryResult);
  }

  @Test
  @Owner(developers = PRASHANTSHARMA)
  @Category(UnitTests.class)
  public void testQueryBuilderSelectWorkload() {
    String queryExpected =
        "select service_name,service_id,service_status as status,service_startts as startts,service_endts as endts,deployment_type, pipeline_execution_summary_cd_id from service_infra_info where pipeline_execution_summary_cd_id in (select id from pipeline_execution_summary_cd where accountid='acc' and orgidentifier='org' and projectidentifier='pro' and startts is not null and startts>=10 and startts<13 ) and service_name is not null and service_id is not null;";

    assertThat(queryExpected)
        .isEqualTo(
            new CDOverviewDashboardServiceImpl().queryBuilderSelectWorkload("acc", "org", "pro", 10L, 13L, null));

    // all parameters as null
    queryExpected =
        "select service_name,service_id,service_status as status,service_startts as startts,service_endts as endts,deployment_type, pipeline_execution_summary_cd_id from service_infra_info where ";
    assertThat(queryExpected)
        .isEqualTo(new CDOverviewDashboardServiceImpl().queryBuilderSelectWorkload(null, null, null, 0L, 0L, null));

    // accountIdentifier as null
    queryExpected =
        "select service_name,service_id,service_status as status,service_startts as startts,service_endts as endts,deployment_type, pipeline_execution_summary_cd_id from service_infra_info where pipeline_execution_summary_cd_id in (select id from pipeline_execution_summary_cd where orgidentifier='org' and projectidentifier='pro' and startts is not null and startts>=10 and startts<13 ) and service_name is not null and service_id is not null;";
    assertThat(queryExpected)
        .isEqualTo(new CDOverviewDashboardServiceImpl().queryBuilderSelectWorkload(null, "org", "pro", 10L, 13L, null));

    // org as null
    queryExpected =
        "select service_name,service_id,service_status as status,service_startts as startts,service_endts as endts,deployment_type, pipeline_execution_summary_cd_id from service_infra_info where pipeline_execution_summary_cd_id in (select id from pipeline_execution_summary_cd where accountid='acc' and projectidentifier='pro' and startts is not null and startts>=10 and startts<13 ) and service_name is not null and service_id is not null;";

    assertThat(queryExpected)
        .isEqualTo(new CDOverviewDashboardServiceImpl().queryBuilderSelectWorkload("acc", null, "pro", 10L, 13L, null));

    // proId as null
    queryExpected =
        "select service_name,service_id,service_status as status,service_startts as startts,service_endts as endts,deployment_type, pipeline_execution_summary_cd_id from service_infra_info where pipeline_execution_summary_cd_id in (select id from pipeline_execution_summary_cd where accountid='acc' and orgidentifier='org' and startts is not null and startts>=10 and startts<13 ) and service_name is not null and service_id is not null;";

    assertThat(queryExpected)
        .isEqualTo(new CDOverviewDashboardServiceImpl().queryBuilderSelectWorkload("acc", "org", null, 10L, 13L, null));

    // startInterval as null
    queryExpected =
        "select service_name,service_id,service_status as status,service_startts as startts,service_endts as endts,deployment_type, pipeline_execution_summary_cd_id from service_infra_info where ";

    assertThat(queryExpected)
        .isEqualTo(new CDOverviewDashboardServiceImpl().queryBuilderSelectWorkload("acc", "org", "pro", 0L, 13L, null));

    // endInterval as null
    queryExpected =
        "select service_name,service_id,service_status as status,service_startts as startts,service_endts as endts,deployment_type, pipeline_execution_summary_cd_id from service_infra_info where ";

    assertThat(queryExpected)
        .isEqualTo(new CDOverviewDashboardServiceImpl().queryBuilderSelectWorkload("acc", "org", "pro", 10L, 0L, null));

    // both interval as null
    queryExpected =
        "select service_name,service_id,service_status as status,service_startts as startts,service_endts as endts,deployment_type, pipeline_execution_summary_cd_id from service_infra_info where ";

    assertThat(queryExpected)
        .isEqualTo(new CDOverviewDashboardServiceImpl().queryBuilderSelectWorkload("acc", "org", "pro", 0L, 0L, null));
  }

  @Test
  @Owner(developers = PRASHANTSHARMA)
  @Category(UnitTests.class)
  public void testGetStartingDateEpochValue() {
    // time is in UTC
    long startIntervalEpoch = 1620000000000L; // 3 MAY 2021
    long currentIntervalEpoch = startIntervalEpoch + 1000L; // ADDING 1 sec

    assertThat(startIntervalEpoch)
        .isEqualTo(
            new CDOverviewDashboardServiceImpl().getStartingDateEpochValue(currentIntervalEpoch, startIntervalEpoch));

    currentIntervalEpoch =
        startIntervalEpoch + 23 * HOUR_IN_MS + 59 * 60 * 1000L + 59 * 100L; // 23 hours 59 minutes 59 sec
    assertThat(startIntervalEpoch)
        .isEqualTo(
            new CDOverviewDashboardServiceImpl().getStartingDateEpochValue(currentIntervalEpoch, startIntervalEpoch));

    currentIntervalEpoch = 1620887190000L; // 13 may 2021 11 hr 56 minutes
    assertThat(startIntervalEpoch + 10 * DAY_IN_MS)
        .isEqualTo(
            new CDOverviewDashboardServiceImpl().getStartingDateEpochValue(currentIntervalEpoch, startIntervalEpoch));

    currentIntervalEpoch = 1621016999000L; // 14 MAY 2021 23 HOURS 59 MINUTES 59 SEC
    assertThat(startIntervalEpoch + 11 * DAY_IN_MS)
        .isEqualTo(
            new CDOverviewDashboardServiceImpl().getStartingDateEpochValue(currentIntervalEpoch, startIntervalEpoch));

    currentIntervalEpoch = 1620950401000L; // 14 MAY 2021 + 1 sec
    assertThat(startIntervalEpoch + 11 * DAY_IN_MS)
        .isEqualTo(
            new CDOverviewDashboardServiceImpl().getStartingDateEpochValue(currentIntervalEpoch, startIntervalEpoch));
  }

  @Test
  @Owner(developers = MEENAKSHI)
  @Category(UnitTests.class)
  public void testQueryBuilderServiceDeployments() {
    String expectedQueryResult =
        "select status, time_entity, COUNT(*) as numberOfRecords from (select service_status as status, service_startts as execution_time, time_bucket_gapfill(86400000, service_startts, 1620000000000, 1620950400000) as time_entity, pipeline_execution_summary_cd_id  from service_infra_info where pipeline_execution_summary_cd_id in (select id from pipeline_execution_summary_cd where accountid='account' and orgidentifier='org' and projectidentifier='project') and service_startts>=1620000000000 and service_startts<1620950400000) as innertable group by status, time_entity;";
    String queryResult = new CDOverviewDashboardServiceImpl().queryBuilderServiceDeployments(
        "account", "org", "project", 1620000000000L, 1620950400000L, 1, null);
    assertThat(queryResult).isEqualTo(expectedQueryResult);
  }
}
