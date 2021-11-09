package io.harness.cvng.core.beans.monitoredService.healthSouceSpec;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Sets;
import io.harness.cvng.beans.CVMonitoringCategory;
import io.harness.cvng.beans.DataSourceType;
import io.harness.cvng.core.beans.DatadogMetricHealthDefinition;
import io.harness.cvng.core.beans.monitoredService.HealthSource;
import io.harness.cvng.core.entities.CVConfig;
import io.harness.cvng.core.entities.DatadogMetricCVConfig;
import io.harness.cvng.core.services.api.MetricPackService;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

@Data
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DatadogMetricHealthSourceSpec extends HealthSourceSpec {

    @NotNull
    String feature;
    private List<DatadogMetricHealthDefinition> metricDefinitions;

    @Override
    public HealthSource.CVConfigUpdateResult getCVConfigUpdateResult(String accountId, String orgIdentifier,
                                                                     String projectIdentifier, String environmentRef,
                                                                     String serviceRef, String identifier, String name,
                                                                     List<CVConfig> existingCVConfigs,
                                                                     MetricPackService metricPackService) {
        List<DatadogMetricCVConfig> cvConfigsFromThisObj =
                toCVConfigs(accountId, orgIdentifier, projectIdentifier, environmentRef, serviceRef, identifier, name);
        Map<Key, DatadogMetricCVConfig> existingConfigMap = new HashMap<>();

        List<DatadogMetricCVConfig> existingSDCVConfigs = (List<DatadogMetricCVConfig>) (List<?>) existingCVConfigs;

        for (DatadogMetricCVConfig datadogMetricCVConfig : existingSDCVConfigs) {
            existingConfigMap.put(getKeyFromConfig(datadogMetricCVConfig), datadogMetricCVConfig);
        }

        Map<Key, DatadogMetricCVConfig> currentCVConfigsMap = new HashMap<>();
        for (DatadogMetricCVConfig datadogMetricCVConfig : cvConfigsFromThisObj) {
            currentCVConfigsMap.put(getKeyFromConfig(datadogMetricCVConfig), datadogMetricCVConfig);
        }

        Set<Key> deleted = Sets.difference(existingConfigMap.keySet(), currentCVConfigsMap.keySet());
        Set<Key> added = Sets.difference(currentCVConfigsMap.keySet(), existingConfigMap.keySet());
        Set<Key> updated = Sets.intersection(existingConfigMap.keySet(), currentCVConfigsMap.keySet());

        List<CVConfig> updatedConfigs =
                updated.stream().map(currentCVConfigsMap::get).collect(Collectors.toList());
        List<CVConfig> updatedConfigWithUuid =
                updated.stream().map(existingConfigMap::get).collect(Collectors.toList());


        for (int i = 0; i < updatedConfigs.size(); i++) {
            updatedConfigs.get(i).setUuid(updatedConfigWithUuid.get(i).getUuid());
        }

        return HealthSource.CVConfigUpdateResult.builder()
                .deleted(deleted.stream().map(existingConfigMap::get).collect(Collectors.toList()))
                .updated(updatedConfigs)
                .added(added.stream().map(currentCVConfigsMap::get).collect(Collectors.toList()))
                .build();
    }

    @Override
    public DataSourceType getType() {
        return DataSourceType.DATADOG_METRICS;
    }


    private List<DatadogMetricCVConfig> toCVConfigs(String accountId, String orgIdentifier, String projectIdentifier,
                                                    String environmentRef, String serviceRef, String identifier, String name) {
        // group things under same service_env_category_dashboard into one config
        Map<Key, List<DatadogMetricHealthDefinition>> keyToDefinitionMap = new HashMap<>();

        metricDefinitions.forEach(definition -> {
            Key key = Key.builder()
                    .category(definition.getRiskProfile().getCategory())
                    .dashboardName(definition.getDashboardName())
                    .dashboardId(definition.getDashboardId())
                    .build();
            if (!keyToDefinitionMap.containsKey(key)) {
                keyToDefinitionMap.put(key, new ArrayList<>());
            }
            keyToDefinitionMap.get(key).add(definition);
        });

        List<DatadogMetricCVConfig> cvConfigs = new ArrayList<>();

        keyToDefinitionMap.forEach((key, datadogDefinitions) -> {
            DatadogMetricCVConfig cvConfig = DatadogMetricCVConfig.builder()
                    .accountId(accountId)
                    .orgIdentifier(orgIdentifier)
                    .projectIdentifier(projectIdentifier)
                    .identifier(identifier)
                    .connectorIdentifier(getConnectorRef())
                    .monitoringSourceName(name)
                    .envIdentifier(environmentRef)
                    .serviceIdentifier(serviceRef)
                    .productName(feature)
                    .category(key.getCategory())
                    .dashboardName(key.getDashboardName())
                    .dashboardId(key.getDashboardId())
                    .build();
            cvConfig.fromMetricDefinitions(datadogDefinitions, key.getCategory());
            cvConfigs.add(cvConfig);
        });

        return cvConfigs;
    }

    private Key getKeyFromConfig(DatadogMetricCVConfig cvConfig) {
        return Key.builder()
                .category(cvConfig.getMetricPack().getCategory())
                .dashboardName(cvConfig.getDashboardName())
                .dashboardId(cvConfig.getDashboardId())
                .build();
    }

    @Value
    @Builder
    private static class Key {
        CVMonitoringCategory category;
        String dashboardName;
        String dashboardId;
    }
}
