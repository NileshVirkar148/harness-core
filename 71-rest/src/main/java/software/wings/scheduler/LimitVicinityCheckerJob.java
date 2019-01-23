package software.wings.scheduler;

import static software.wings.common.Constants.ACCOUNT_ID_KEY;

import com.google.inject.Inject;

import io.harness.scheduler.BackgroundExecutorService;
import io.harness.scheduler.BackgroundSchedulerLocker;
import io.harness.scheduler.PersistentScheduler;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.wings.service.intfc.limits.LimitVicinityHandler;

import java.util.Date;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@DisallowConcurrentExecution
public class LimitVicinityCheckerJob implements Job {
  private static final Logger logger = LoggerFactory.getLogger(LimitVicinityCheckerJob.class);

  public static final String GROUP = "LIMIT_VICINITY_CHECKER_CRON_GROUP";

  private static final int SYNC_INTERVAL_IN_MINUTES = 30;

  @Inject private BackgroundExecutorService executorService;
  @Inject private BackgroundSchedulerLocker persistentLocker;
  @Inject private LimitVicinityHandler limitVicinityHandler;

  public static void addWithDelay(PersistentScheduler jobScheduler, String accountId) {
    // Add some randomness in the trigger start time to avoid overloading quartz by firing jobs at the same time.
    long startTime =
        System.currentTimeMillis() + new Random().nextInt((int) TimeUnit.MINUTES.toMillis(SYNC_INTERVAL_IN_MINUTES));
    addInternal(jobScheduler, accountId, new Date(startTime));
  }

  public static void add(PersistentScheduler jobScheduler, String accountId) {
    addInternal(jobScheduler, accountId, null);
  }

  private static void addInternal(PersistentScheduler jobScheduler, String accountId, Date triggerStartTime) {
    JobDetail job = JobBuilder.newJob(LimitVicinityCheckerJob.class)
                        .withIdentity(accountId, GROUP)
                        .usingJobData(ACCOUNT_ID_KEY, accountId)
                        .build();

    TriggerBuilder triggerBuilder =
        TriggerBuilder.newTrigger()
            .withIdentity(accountId, GROUP)
            .withSchedule(
                SimpleScheduleBuilder.simpleSchedule().withIntervalInMinutes(SYNC_INTERVAL_IN_MINUTES).repeatForever());
    if (triggerStartTime != null) {
      triggerBuilder.startAt(triggerStartTime);
    }

    jobScheduler.ensureJob__UnderConstruction(job, triggerBuilder.build());
  }

  public static void delete(PersistentScheduler jobScheduler, String accountId) {
    jobScheduler.deleteJob(accountId, GROUP);
  }

  @Override
  public void execute(JobExecutionContext jobExecutionContext) {
    executorService.submit(() -> {
      String accountId = (String) jobExecutionContext.getJobDetail().getJobDataMap().get(ACCOUNT_ID_KEY);
      Objects.requireNonNull(accountId, "[LimitVicinityCheckerJob] Account Id must be passed in job context");
      limitVicinityHandler.checkAndAct(accountId);
    });
  }
}
