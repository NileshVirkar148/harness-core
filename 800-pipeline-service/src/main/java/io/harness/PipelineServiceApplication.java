package io.harness;

import static io.harness.AuthorizationServiceHeader.PIPELINE_SERVICE;
import static io.harness.PipelineServiceConfiguration.getResourceClasses;
import static io.harness.annotations.dev.HarnessTeam.PIPELINE;
import static io.harness.logging.LoggingInitializer.initializeLogging;
import static io.harness.waiter.PmsNotifyEventListener.PMS_ORCHESTRATION;

import static com.google.common.collect.ImmutableMap.of;
import static java.util.Collections.singletonList;

import io.harness.annotations.dev.OwnedBy;
import io.harness.beans.OrchestrationVisualizationEventLogHandlerAsync;
import io.harness.controller.PrimaryVersionChangeScheduler;
import io.harness.delay.DelayEventListener;
import io.harness.engine.events.OrchestrationEventEmitter;
import io.harness.engine.executions.node.NodeExecutionService;
import io.harness.engine.executions.node.NodeExecutionServiceImpl;
import io.harness.engine.executions.plan.PlanExecutionService;
import io.harness.exception.GeneralException;
import io.harness.execution.SdkResponseEventListener;
import io.harness.gitsync.AbstractGitSyncSdkModule;
import io.harness.gitsync.GitSdkConfiguration;
import io.harness.gitsync.GitSyncEntitiesConfiguration;
import io.harness.gitsync.GitSyncSdkConfiguration;
import io.harness.gitsync.GitSyncSdkInitHelper;
import io.harness.gitsync.persistance.GitAwarePersistence;
import io.harness.gitsync.persistance.testing.NoOpGitAwarePersistenceImpl;
import io.harness.govern.ProviderModule;
import io.harness.health.HealthMonitor;
import io.harness.health.HealthService;
import io.harness.maintenance.MaintenanceController;
import io.harness.metrics.HarnessMetricRegistry;
import io.harness.metrics.MetricRegistryModule;
import io.harness.monitoring.MonitoringQueueObserver;
import io.harness.ng.core.CorrelationFilter;
import io.harness.ngpipeline.common.NGPipelineObjectMapperHelper;
import io.harness.notification.module.NotificationClientModule;
import io.harness.plancreator.pipeline.PipelineConfig;
import io.harness.pms.annotations.PipelineServiceAuth;
import io.harness.pms.approval.ApprovalInstanceExpirationJob;
import io.harness.pms.approval.ApprovalInstanceHandler;
import io.harness.pms.event.PMSEventConsumerService;
import io.harness.pms.exception.WingsExceptionMapper;
import io.harness.pms.inputset.gitsync.InputSetEntityGitSyncHelper;
import io.harness.pms.inputset.gitsync.InputSetYamlDTO;
import io.harness.pms.ngpipeline.inputset.beans.entity.InputSetEntity;
import io.harness.pms.ngpipeline.inputset.observers.InputSetsDeleteObserver;
import io.harness.pms.pipeline.PipelineEntity;
import io.harness.pms.pipeline.PipelineEntityCrudObserver;
import io.harness.pms.pipeline.PipelineSetupUsageHelper;
import io.harness.pms.pipeline.gitsync.PipelineEntityGitSyncHelper;
import io.harness.pms.pipeline.service.PMSPipelineService;
import io.harness.pms.pipeline.service.PMSPipelineServiceImpl;
import io.harness.pms.plan.creation.PipelineServiceFilterCreationResponseMerger;
import io.harness.pms.plan.creation.PipelineServiceInternalInfoProvider;
import io.harness.pms.plan.execution.PmsExecutionServiceInfoProvider;
import io.harness.pms.plan.execution.observers.PipelineExecutionSummaryDeleteObserver;
import io.harness.pms.plan.execution.registrar.PmsOrchestrationEventRegistrar;
import io.harness.pms.sdk.PmsSdkConfiguration;
import io.harness.pms.sdk.PmsSdkConfiguration.DeployMode;
import io.harness.pms.sdk.PmsSdkInitHelper;
import io.harness.pms.sdk.PmsSdkModule;
import io.harness.pms.serializer.jackson.PmsBeansJacksonModule;
import io.harness.pms.triggers.scheduled.ScheduledTriggerHandler;
import io.harness.pms.triggers.webhook.service.TriggerWebhookExecutionService;
import io.harness.pms.utils.PmsConstants;
import io.harness.queue.QueueListenerController;
import io.harness.queue.QueuePublisher;
import io.harness.registrars.PipelineServiceFacilitatorRegistrar;
import io.harness.registrars.PipelineServiceStepRegistrar;
import io.harness.resource.VersionInfoResource;
import io.harness.security.NextGenAuthenticationFilter;
import io.harness.serializer.PipelineServiceUtilAdviserRegistrar;
import io.harness.serializer.jackson.PipelineServiceJacksonModule;
import io.harness.service.impl.DelegateAsyncServiceImpl;
import io.harness.service.impl.DelegateProgressServiceImpl;
import io.harness.service.impl.DelegateSyncServiceImpl;
import io.harness.steps.barriers.service.BarrierServiceImpl;
import io.harness.steps.resourcerestraint.service.ResourceRestraintPersistenceMonitor;
import io.harness.threading.ExecutorModule;
import io.harness.threading.ThreadPool;
import io.harness.timeout.TimeoutEngine;
import io.harness.waiter.NotifierScheduledExecutorService;
import io.harness.waiter.NotifyEvent;
import io.harness.waiter.NotifyQueuePublisherRegister;
import io.harness.waiter.NotifyResponseCleaner;
import io.harness.waiter.PmsNotifyEventListener;
import io.harness.waiter.ProgressUpdateService;
import io.harness.yaml.YamlSdkConfiguration;
import io.harness.yaml.YamlSdkInitHelper;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ServiceManager;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.jersey.errors.EarlyEofExceptionMapper;
import io.dropwizard.jersey.jackson.JsonProcessingExceptionMapper;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.model.Resource;
import org.springframework.data.mongodb.core.MongoTemplate;

@Slf4j
@OwnedBy(PIPELINE)
public class PipelineServiceApplication extends Application<PipelineServiceConfiguration> {
  private static final SecureRandom random = new SecureRandom();
  private static final String APPLICATION_NAME = "Pipeline Service Application";

  private final MetricRegistry metricRegistry = new MetricRegistry();
  private HarnessMetricRegistry harnessMetricRegistry;

  public static void main(String[] args) throws Exception {
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      log.info("Shutdown hook, entering maintenance...");
      MaintenanceController.forceMaintenance(true);
    }));

    new PipelineServiceApplication().run(args);
  }

  @Override
  public String getName() {
    return APPLICATION_NAME;
  }

  @Override
  public void initialize(Bootstrap<PipelineServiceConfiguration> bootstrap) {
    initializeLogging();
    bootstrap.addCommand(new InspectCommand<>(this));

    // Enable variable substitution with environment variables
    bootstrap.setConfigurationSourceProvider(new SubstitutingSourceProvider(
        bootstrap.getConfigurationSourceProvider(), new EnvironmentVariableSubstitutor(false)));
    configureObjectMapper(bootstrap.getObjectMapper());
    bootstrap.addBundle(new SwaggerBundle<PipelineServiceConfiguration>() {
      @Override
      protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(PipelineServiceConfiguration appConfig) {
        return appConfig.getSwaggerBundleConfiguration();
      }
    });
  }

  public static void configureObjectMapper(final ObjectMapper mapper) {
    NGPipelineObjectMapperHelper.configureNGObjectMapper(mapper);
    mapper.registerModule(new PmsBeansJacksonModule());
    mapper.registerModule(new PipelineServiceJacksonModule());
  }

  @Override
  public void run(PipelineServiceConfiguration appConfig, Environment environment) {
    log.info("Starting Pipeline Service Application ...");
    MaintenanceController.forceMaintenance(true);

    ExecutorModule.getInstance().setExecutorService(ThreadPool.create(
        10, 100, 500L, TimeUnit.MILLISECONDS, new ThreadFactoryBuilder().setNameFormat("main-app-pool-%d").build()));
    List<Module> modules = new ArrayList<>();
    modules.add(new ProviderModule() {
      @Provides
      @Singleton
      PipelineServiceConfiguration configuration() {
        return appConfig;
      }
    });
    modules.add(new NotificationClientModule(appConfig.getNotificationClientConfiguration()));
    modules.add(PipelineServiceModule.getInstance(appConfig));
    modules.add(new MetricRegistryModule(metricRegistry));
    modules.add(PmsSdkModule.getInstance(getPmsSdkConfiguration(appConfig)));
    if (appConfig.isShouldDeployWithGitSync()) {
      GitSyncSdkConfiguration gitSyncSdkConfiguration = getGitSyncConfiguration(appConfig);
      modules.add(new AbstractGitSyncSdkModule() {
        @Override
        public GitSyncSdkConfiguration getGitSyncSdkConfiguration() {
          return gitSyncSdkConfiguration;
        }
      });
    } else {
      modules.add(new SCMGrpcClientModule(appConfig.getGitSdkConfiguration().getScmConnectionConfig()));
      modules.add(new AbstractGitSyncSdkModule() {
        @Override
        protected void configure() {
          bind(GitAwarePersistence.class).to(NoOpGitAwarePersistenceImpl.class);
        }

        @Override
        public GitSyncSdkConfiguration getGitSyncSdkConfiguration() {
          return null;
        }
      });
    }

    Injector injector = Guice.createInjector(modules);
    registerEventListeners(injector);
    registerWaitEnginePublishers(injector);
    registerScheduledJobs(injector);
    registerCorsFilter(appConfig, environment);
    registerResources(environment, injector);
    registerJerseyProviders(environment, injector);
    registerManagedBeans(environment, injector);
    registerAuthFilters(appConfig, environment, injector);
    registerHealthCheck(environment, injector);
    registerObservers(injector);

    harnessMetricRegistry = injector.getInstance(HarnessMetricRegistry.class);
    injector.getInstance(TriggerWebhookExecutionService.class).registerIterators();
    injector.getInstance(ScheduledTriggerHandler.class).registerIterators();
    injector.getInstance(TimeoutEngine.class).registerIterators();
    injector.getInstance(BarrierServiceImpl.class).registerIterators();
    injector.getInstance(ApprovalInstanceHandler.class).registerIterators();
    injector.getInstance(ResourceRestraintPersistenceMonitor.class).registerIterators();
    injector.getInstance(PrimaryVersionChangeScheduler.class).registerExecutors();

    log.info("Initializing gRPC servers...");
    ServiceManager serviceManager = injector.getInstance(ServiceManager.class).startAsync();
    serviceManager.awaitHealthy();
    Runtime.getRuntime().addShutdownHook(new Thread(() -> serviceManager.stopAsync().awaitStopped()));

    registerPmsSdk(appConfig, injector);
    registerYamlSdk(injector);
    if (appConfig.isShouldDeployWithGitSync()) {
      registerGitSyncSdk(appConfig, injector, environment);
    }

    registerCorrelationFilter(environment, injector);
    registerNotificationTemplates(injector);
    createConsumerThreadsToListenToEvents(environment, injector);
    MaintenanceController.forceMaintenance(false);
  }

  private void createConsumerThreadsToListenToEvents(Environment environment, Injector injector) {
    environment.lifecycle().manage(injector.getInstance(PMSEventConsumerService.class));
  }

  private static void registerObservers(Injector injector) {
    // Register Pipeline Observers
    PMSPipelineServiceImpl pmsPipelineService =
        (PMSPipelineServiceImpl) injector.getInstance(Key.get(PMSPipelineService.class));
    pmsPipelineService.getPipelineSubject().register(injector.getInstance(Key.get(PipelineSetupUsageHelper.class)));
    pmsPipelineService.getPipelineSubject().register(injector.getInstance(Key.get(PipelineEntityCrudObserver.class)));
    pmsPipelineService.getPipelineSubject().register(
        injector.getInstance(Key.get(PipelineExecutionSummaryDeleteObserver.class)));
    pmsPipelineService.getPipelineSubject().register(injector.getInstance(Key.get(InputSetsDeleteObserver.class)));

    OrchestrationEventEmitter orchestrationEventEmitter =
        injector.getInstance(Key.get(OrchestrationEventEmitter.class));
    orchestrationEventEmitter.getOrchestrationEventLogSubjectSubject().register(
        injector.getInstance(Key.get(OrchestrationVisualizationEventLogHandlerAsync.class)));

    NodeExecutionServiceImpl nodeExecutionService =
        (NodeExecutionServiceImpl) injector.getInstance(Key.get(NodeExecutionService.class));
    nodeExecutionService.getStepStatusUpdateSubject().register(
        injector.getInstance(Key.get(PlanExecutionService.class)));

    SdkResponseEventListener sdkResponseEventListener = injector.getInstance(SdkResponseEventListener.class);
    sdkResponseEventListener.getQueueListenerObserverSubject().register(
        injector.getInstance(Key.get(MonitoringQueueObserver.class)));
  }

  private void registerCorrelationFilter(Environment environment, Injector injector) {
    environment.jersey().register(injector.getInstance(CorrelationFilter.class));
  }

  private void registerAuthFilters(PipelineServiceConfiguration config, Environment environment, Injector injector) {
    if (config.isEnableAuth()) {
      Predicate<Pair<ResourceInfo, ContainerRequestContext>> predicate = resourceInfoAndRequest
          -> resourceInfoAndRequest.getKey().getResourceMethod().getAnnotation(PipelineServiceAuth.class) != null
          || resourceInfoAndRequest.getKey().getResourceClass().getAnnotation(PipelineServiceAuth.class) != null;
      Map<String, String> serviceToSecretMapping = new HashMap<>();
      serviceToSecretMapping.put(AuthorizationServiceHeader.BEARER.getServiceId(), config.getJwtAuthSecret());
      serviceToSecretMapping.put(
          AuthorizationServiceHeader.IDENTITY_SERVICE.getServiceId(), config.getJwtIdentityServiceSecret());
      serviceToSecretMapping.put(AuthorizationServiceHeader.DEFAULT.getServiceId(), config.getNgManagerServiceSecret());
      environment.jersey().register(new NextGenAuthenticationFilter(predicate, null, serviceToSecretMapping));
    }
  }

  private void registerHealthCheck(Environment environment, Injector injector) {
    final HealthService healthService = injector.getInstance(HealthService.class);
    environment.healthChecks().register("PMS", healthService);
    healthService.registerMonitor((HealthMonitor) injector.getInstance(MongoTemplate.class));
  }

  private void registerPmsSdk(PipelineServiceConfiguration config, Injector injector) {
    PmsSdkConfiguration sdkConfig = getPmsSdkConfiguration(config);
    try {
      PmsSdkInitHelper.initializeSDKInstance(injector, sdkConfig);
    } catch (Exception ex) {
      throw new GeneralException("Failed to start pipeline service because pms sdk registration failed", ex);
    }
  }

  private PmsSdkConfiguration getPmsSdkConfiguration(PipelineServiceConfiguration config) {
    return PmsSdkConfiguration.builder()
        .deploymentMode(DeployMode.REMOTE_IN_PROCESS)
        .serviceName(PmsConstants.INTERNAL_SERVICE_NAME)
        .mongoConfig(config.getMongoConfig())
        .pipelineServiceInfoProviderClass(PipelineServiceInternalInfoProvider.class)
        .filterCreationResponseMerger(new PipelineServiceFilterCreationResponseMerger())
        .engineSteps(PipelineServiceStepRegistrar.getEngineSteps())
        .engineFacilitators(PipelineServiceFacilitatorRegistrar.getEngineFacilitators())
        .engineAdvisers(PipelineServiceUtilAdviserRegistrar.getEngineAdvisers())
        .engineEventHandlersMap(PmsOrchestrationEventRegistrar.getEngineEventHandlers())
        .executionSummaryModuleInfoProviderClass(PmsExecutionServiceInfoProvider.class)
        .build();
  }

  private void registerGitSyncSdk(PipelineServiceConfiguration config, Injector injector, Environment environment) {
    GitSyncSdkConfiguration sdkConfig = getGitSyncConfiguration(config);
    try {
      GitSyncSdkInitHelper.initGitSyncSdk(injector, environment, sdkConfig);
    } catch (Exception ex) {
      throw new GeneralException("Failed to start pipeline service because git sync registration failed", ex);
    }
  }

  private GitSyncSdkConfiguration getGitSyncConfiguration(PipelineServiceConfiguration config) {
    final Supplier<List<EntityType>> sortOrder = () -> Lists.newArrayList(EntityType.PIPELINES, EntityType.INPUT_SETS);
    ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
    configureObjectMapper(objectMapper);
    Set<GitSyncEntitiesConfiguration> gitSyncEntitiesConfigurations = new HashSet<>();
    gitSyncEntitiesConfigurations.add(GitSyncEntitiesConfiguration.builder()
                                          .yamlClass(PipelineConfig.class)
                                          .entityClass(PipelineEntity.class)
                                          .entityType(EntityType.PIPELINES)
                                          .entityHelperClass(PipelineEntityGitSyncHelper.class)
                                          .build());
    gitSyncEntitiesConfigurations.add(GitSyncEntitiesConfiguration.builder()
                                          .yamlClass(InputSetYamlDTO.class)
                                          .entityClass(InputSetEntity.class)
                                          .entityType(EntityType.INPUT_SETS)
                                          .entityHelperClass(InputSetEntityGitSyncHelper.class)
                                          .build());
    final GitSdkConfiguration gitSdkConfiguration = config.getGitSdkConfiguration();
    return GitSyncSdkConfiguration.builder()
        .gitSyncSortOrder(sortOrder)
        .grpcClientConfig(gitSdkConfiguration.getGitManagerGrpcClientConfig())
        .grpcServerConfig(gitSdkConfiguration.getGitSdkGrpcServerConfig())
        .deployMode(GitSyncSdkConfiguration.DeployMode.REMOTE)
        .microservice(Microservice.PMS)
        .scmConnectionConfig(gitSdkConfiguration.getScmConnectionConfig())
        .eventsRedisConfig(config.getEventsFrameworkConfiguration().getRedisConfig())
        .serviceHeader(PIPELINE_SERVICE)
        .gitSyncEntitiesConfiguration(gitSyncEntitiesConfigurations)
        .objectMapper(objectMapper)
        .build();
  }

  private void registerEventListeners(Injector injector) {
    QueueListenerController queueListenerController = injector.getInstance(QueueListenerController.class);
    queueListenerController.register(injector.getInstance(DelayEventListener.class), 1);
    queueListenerController.register(injector.getInstance(SdkResponseEventListener.class), 1);
    queueListenerController.register(injector.getInstance(PmsNotifyEventListener.class), 5);
  }

  private void registerWaitEnginePublishers(Injector injector) {
    final QueuePublisher<NotifyEvent> publisher =
        injector.getInstance(Key.get(new TypeLiteral<QueuePublisher<NotifyEvent>>() {}));
    final NotifyQueuePublisherRegister notifyQueuePublisherRegister =
        injector.getInstance(NotifyQueuePublisherRegister.class);
    notifyQueuePublisherRegister.register(
        PMS_ORCHESTRATION, payload -> publisher.send(singletonList(PMS_ORCHESTRATION), payload));
  }

  private void registerScheduledJobs(Injector injector) {
    injector.getInstance(Key.get(ScheduledExecutorService.class, Names.named("taskPollExecutor")))
        .scheduleWithFixedDelay(injector.getInstance(DelegateSyncServiceImpl.class), 0L, 2L, TimeUnit.SECONDS);
    injector.getInstance(Key.get(ScheduledExecutorService.class, Names.named("taskPollExecutor")))
        .scheduleWithFixedDelay(injector.getInstance(DelegateAsyncServiceImpl.class), 0L, 5L, TimeUnit.SECONDS);
    injector.getInstance(Key.get(ScheduledExecutorService.class, Names.named("taskPollExecutor")))
        .scheduleWithFixedDelay(injector.getInstance(DelegateProgressServiceImpl.class), 0L, 5L, TimeUnit.SECONDS);
    injector.getInstance(Key.get(ScheduledExecutorService.class, Names.named("progressUpdateServiceExecutor")))
        .scheduleWithFixedDelay(injector.getInstance(ProgressUpdateService.class), 0L, 5L, TimeUnit.SECONDS);

    injector.getInstance(NotifierScheduledExecutorService.class)
        .scheduleWithFixedDelay(
            injector.getInstance(NotifyResponseCleaner.class), random.nextInt(200), 200L, TimeUnit.SECONDS);
  }

  private void registerManagedBeans(Environment environment, Injector injector) {
    environment.lifecycle().manage(injector.getInstance(QueueListenerController.class));
    environment.lifecycle().manage(injector.getInstance(ApprovalInstanceExpirationJob.class));
  }

  private void registerCorsFilter(PipelineServiceConfiguration appConfig, Environment environment) {
    FilterRegistration.Dynamic cors = environment.servlets().addFilter("CORS", CrossOriginFilter.class);
    String allowedOrigins = String.join(",", appConfig.getAllowedOrigins());
    cors.setInitParameters(of("allowedOrigins", allowedOrigins, "allowedHeaders",
        "X-Requested-With,Content-Type,Accept,Origin,Authorization,X-api-key", "allowedMethods",
        "OPTIONS,GET,PUT,POST,DELETE,HEAD", "preflightMaxAge", "86400"));
    cors.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/*");
  }

  private void registerResources(Environment environment, Injector injector) {
    for (Class<?> resource : getResourceClasses()) {
      if (Resource.isAcceptable(resource)) {
        environment.jersey().register(injector.getInstance(resource));
      }
    }
    environment.jersey().register(injector.getInstance(VersionInfoResource.class));
  }

  private void registerJerseyProviders(Environment environment, Injector injector) {
    environment.jersey().register(JsonProcessingExceptionMapper.class);
    environment.jersey().register(EarlyEofExceptionMapper.class);
    environment.jersey().register(WingsExceptionMapper.class);

    environment.jersey().register(MultiPartFeature.class);
    //    environment.jersey().register(injector.getInstance(CharsetResponseFilter.class));
    //    environment.jersey().register(injector.getInstance(CorrelationFilter.class));
    //    environment.jersey().register(injector.getInstance(EtagFilter.class));
  }

  private void registerYamlSdk(Injector injector) {
    YamlSdkConfiguration yamlSdkConfiguration = YamlSdkConfiguration.builder()
                                                    .requireSchemaInit(true)
                                                    .requireSnippetInit(true)
                                                    .requireValidatorInit(false)
                                                    .build();
    YamlSdkInitHelper.initialize(injector, yamlSdkConfiguration);
  }

  private void registerNotificationTemplates(Injector injector) {
    ExecutorService executorService =
        injector.getInstance(Key.get(ExecutorService.class, Names.named("templateRegistrationExecutorService")));
    executorService.submit(injector.getInstance(NotificationTemplateRegistrar.class));
  }
}
