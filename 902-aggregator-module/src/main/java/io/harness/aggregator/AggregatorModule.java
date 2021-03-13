package io.harness.aggregator;

import io.harness.accesscontrol.resources.resourcegroups.persistence.ResourceGroupRepository;
import io.harness.accesscontrol.roleassignments.persistence.repositories.RoleAssignmentRepository;
import io.harness.accesscontrol.roles.persistence.repositories.RoleRepository;
import io.harness.aggregator.consumers.AccessControlDebeziumChangeConsumer;
import io.harness.aggregator.consumers.ChangeConsumer;
import io.harness.aggregator.consumers.ResourceGroupChangeConsumer;
import io.harness.aggregator.consumers.RoleAssignmentChangeConsumer;
import io.harness.aggregator.consumers.RoleChangeConsumer;
import io.harness.aggregator.services.AggregatorServiceImpl;
import io.harness.aggregator.services.apis.AggregatorService;
import io.harness.threading.ExecutorModule;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.format.Json;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AggregatorModule extends AbstractModule {
  private static final String MONGO_DB_CONNECTOR = "io.debezium.connector.mongodb.MongoDbConnector";
  private static final String CONNECTOR_NAME = "name";
  private static final String OFFSET_STORAGE = "offset.storage";
  private static final String OFFSET_STORAGE_FILE_FILENAME = "offset.storage.file.filename";
  private static final String KEY_CONVERTER_SCHEMAS_ENABLE = "key.converter.schemas.enable";
  private static final String VALUE_CONVERTER_SCHEMAS_ENABLE = "value.converter.schemas.enable";
  private static final String OFFSET_FLUSH_INTERVAL_MS = "offset.flush.interval.ms";
  private static final String CONNECTOR_CLASS = "connector.class";
  private static final String MONGODB_HOSTS = "mongodb.hosts";
  private static final String MONGODB_NAME = "mongodb.name";
  private static final String MONGODB_USER = "mongodb.user";
  private static final String MONGODB_PASSWORD = "mongodb.password";
  private static final String MONGODB_SSL_ENABLED = "mongodb.ssl.enabled";
  private static final String DATABASE_INCLUDE_LIST = "database.include.list";
  private static final String COLLECTION_INCLUDE_LIST = "collection.include.list";
  private static final String TRANSFORMS = "transforms";
  private static final String TRANSFORMS_UNWRAP_TYPE = "transforms.unwrap.type";
  private static final String TRANSFORMS_UNWRAP_DROP_TOMBSTONES = "transforms.unwrap.drop.tombstones";
  private static final String TRANSFORMS_UNWRAP_OPERATION_HEADER = "transforms.unwrap.operation.header";
  private static final String DEBEZIUM_CONNECTOR_MONGODB_TRANSFORMS_EXTRACT_NEW_DOCUMENT_STATE =
      "io.debezium.connector.mongodb.transforms.ExtractNewDocumentState";
  private static AggregatorModule instance;
  private final AggregatorConfiguration configuration;
  private final ExecutorService executorService;

  public AggregatorModule(AggregatorConfiguration configuration) {
    this.configuration = configuration;
    this.executorService = Executors.newFixedThreadPool(5);
  }

  public static synchronized AggregatorModule getInstance(AggregatorConfiguration aggregatorConfiguration) {
    if (instance == null) {
      instance = new AggregatorModule(aggregatorConfiguration);
    }
    return instance;
  }

  @Override
  protected void configure() {
    if (configuration.isEnabled()) {
      registerRequiredBindings();

      bind(AggregatorService.class).to(AggregatorServiceImpl.class).in(Scopes.SINGLETON);
      bind(ChangeConsumer.class).annotatedWith(Names.named("roleAssignment")).to(RoleAssignmentChangeConsumer.class);
      bind(ChangeConsumer.class).annotatedWith(Names.named("role")).to(RoleChangeConsumer.class);
      bind(ChangeConsumer.class).annotatedWith(Names.named("resourceGroup")).to(ResourceGroupChangeConsumer.class);

      // configuring debezium
      ExecutorModule.getInstance().setExecutorService(this.executorService);
      install(ExecutorModule.getInstance());
      AccessControlDebeziumChangeConsumer changeConsumer = new AccessControlDebeziumChangeConsumer();
      requestInjection(changeConsumer);
      DebeziumEngine<ChangeEvent<String, String>> debeziumEngine =
          getEngine(configuration.getDebeziumConfig(), changeConsumer);
      executorService.submit(debeziumEngine);
    }
  }

  private static DebeziumEngine<ChangeEvent<String, String>> getEngine(
      DebeziumConfig debeziumConfig, AccessControlDebeziumChangeConsumer changeConsumer) {
    Properties props = new Properties();
    props.setProperty(CONNECTOR_NAME, debeziumConfig.getConnectorName());
    props.setProperty(OFFSET_STORAGE, MongoOffsetBackingStore.class.getName());
    props.setProperty(OFFSET_STORAGE_FILE_FILENAME, debeziumConfig.getOffsetStorageFileName());
    props.setProperty(KEY_CONVERTER_SCHEMAS_ENABLE, debeziumConfig.getKeyConverterSchemasEnable());
    props.setProperty(VALUE_CONVERTER_SCHEMAS_ENABLE, debeziumConfig.getValueConverterSchemasEnable());
    props.setProperty(OFFSET_FLUSH_INTERVAL_MS, debeziumConfig.getOffsetFlushIntervalMillis());

    /* begin connector properties */
    props.setProperty(CONNECTOR_CLASS, MONGO_DB_CONNECTOR);
    props.setProperty(MONGODB_HOSTS, debeziumConfig.getMongodbHosts());
    props.setProperty(MONGODB_NAME, debeziumConfig.getMongodbName());
    Optional.ofNullable(debeziumConfig.getMongodbUser())
        .filter(x -> !x.isEmpty())
        .ifPresent(x -> props.setProperty(MONGODB_USER, x));
    Optional.ofNullable(debeziumConfig.getMongodbPassword())
        .filter(x -> !x.isEmpty())
        .ifPresent(x -> props.setProperty(MONGODB_PASSWORD, x));
    props.setProperty(MONGODB_SSL_ENABLED, debeziumConfig.getSslEnabled());
    props.setProperty(DATABASE_INCLUDE_LIST, debeziumConfig.getDatabaseIncludeList());
    props.setProperty(COLLECTION_INCLUDE_LIST, debeziumConfig.getCollectionIncludeList());
    props.setProperty(TRANSFORMS, "unwrap");
    props.setProperty(TRANSFORMS_UNWRAP_TYPE, DEBEZIUM_CONNECTOR_MONGODB_TRANSFORMS_EXTRACT_NEW_DOCUMENT_STATE);
    props.setProperty(TRANSFORMS_UNWRAP_DROP_TOMBSTONES, "false");
    props.setProperty(TRANSFORMS_UNWRAP_OPERATION_HEADER, "true");

    return DebeziumEngine.create(Json.class).using(props).notifying(changeConsumer).build();
  }

  private void registerRequiredBindings() {
    requireBinding(RoleRepository.class);
    requireBinding(RoleAssignmentRepository.class);
    requireBinding(ResourceGroupRepository.class);
  }
}
