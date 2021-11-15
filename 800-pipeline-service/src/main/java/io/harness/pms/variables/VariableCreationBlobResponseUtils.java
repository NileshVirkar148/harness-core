package io.harness.pms.variables;

import static io.harness.data.structure.EmptyPredicate.isNotEmpty;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.pms.contracts.plan.Dependencies;
import io.harness.pms.contracts.plan.VariablesCreationBlobResponse;
import io.harness.pms.contracts.plan.YamlOutputProperties;
import io.harness.pms.variables.VariableMergeServiceResponse.ServiceExpressionProperties;
import io.harness.pms.variables.VariableMergeServiceResponse.VariableResponseMapValue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.experimental.UtilityClass;

@OwnedBy(HarnessTeam.PIPELINE)
@UtilityClass
public class VariableCreationBlobResponseUtils {
  public VariableMergeServiceResponse getMergeServiceResponse(
      String yaml, VariablesCreationBlobResponse response, Map<String, List<String>> serviceExpressionMap) {
    Map<String, VariableResponseMapValue> metadataMap = new LinkedHashMap<>();
    // Add Yaml Properties
    response.getYamlPropertiesMap().forEach(
        (k, v) -> metadataMap.put(k, VariableResponseMapValue.builder().yamlProperties(v).build()));

    // Add Yaml Output Properties
    response.getYamlOutputPropertiesMap().keySet().forEach(uuid -> {
      YamlOutputProperties yamlOutputProperties = response.getYamlOutputPropertiesMap().get(uuid);
      if (metadataMap.containsKey(uuid)) {
        metadataMap.get(uuid).setYamlOutputProperties(yamlOutputProperties);
      } else {
        metadataMap.put(uuid, VariableResponseMapValue.builder().yamlOutputProperties(yamlOutputProperties).build());
      }
    });
    List<String> errorMessages = new ArrayList<>();
    response.getErrorResponseList().forEach(error -> {
      int messagesCount = error.getMessagesCount();
      for (int i = 0; i < messagesCount; i++) {
        errorMessages.add(error.getMessages(i));
      }
    });
    return VariableMergeServiceResponse.builder()
        .yaml(yaml)
        .metadataMap(metadataMap)
        .errorResponses(isNotEmpty(errorMessages) ? errorMessages : null)
        .serviceExpressionPropertiesList(getExpressionsFromMap(serviceExpressionMap))
        .build();
  }

  public void mergeResponses(
      VariablesCreationBlobResponse.Builder builder, VariablesCreationBlobResponse otherResponse) {
    if (otherResponse == null) {
      return;
    }

    mergeYamlProperties(builder, otherResponse);
    mergeYamlOutputProperties(builder, otherResponse);
    mergeResolvedDependencies(builder, otherResponse);
    mergeDependencies(builder, otherResponse);
    mergeErrorResponses(builder, otherResponse);
  }

  public static void mergeErrorResponses(
      VariablesCreationBlobResponse.Builder builder, VariablesCreationBlobResponse otherResponse) {
    if (isNotEmpty(otherResponse.getErrorResponseList())) {
      otherResponse.getErrorResponseList().forEach(builder::addErrorResponse);
    }
  }

  public void mergeYamlProperties(
      VariablesCreationBlobResponse.Builder builder, VariablesCreationBlobResponse otherResponse) {
    if (isNotEmpty(otherResponse.getYamlPropertiesMap())) {
      otherResponse.getYamlPropertiesMap().forEach(builder::putYamlProperties);
    }
  }

  public void mergeYamlOutputProperties(
      VariablesCreationBlobResponse.Builder builder, VariablesCreationBlobResponse otherResponse) {
    if (isNotEmpty(otherResponse.getYamlOutputPropertiesMap())) {
      otherResponse.getYamlOutputPropertiesMap().forEach(builder::putYamlOutputProperties);
    }
  }

  public List<ServiceExpressionProperties> getExpressionsFromMap(Map<String, List<String>> serviceExpressionMap) {
    List<ServiceExpressionProperties> serviceExpressionProperties = new ArrayList<>();
    for (Map.Entry<String, List<String>> entry : serviceExpressionMap.entrySet()) {
      entry.getValue()
          .stream()
          .map(e -> ServiceExpressionProperties.builder().serviceName(entry.getKey()).expression(e).build())
          .forEachOrdered(serviceExpressionProperties::add);
    }
    return serviceExpressionProperties;
  }

  public void mergeResolvedDependencies(
      VariablesCreationBlobResponse.Builder builder, VariablesCreationBlobResponse otherResponse) {
    if (isNotEmpty(otherResponse.getResolvedDeps().getDependenciesMap())) {
      otherResponse.getResolvedDeps().getDependenciesMap().forEach((key, value) -> {
        builder.setResolvedDeps(
            Dependencies.newBuilder().setYaml(otherResponse.getDeps().getYaml()).putDependencies(key, value).build());
        //        builder.getDeps().getDependenciesMap().remove(key);
      });
    }
  }

  public void mergeDependencies(
      VariablesCreationBlobResponse.Builder builder, VariablesCreationBlobResponse otherResponse) {
    if (isNotEmpty(otherResponse.getDeps().getDependenciesMap())) {
      otherResponse.getDeps().getDependenciesMap().forEach((key, value) -> {
        if (!builder.getResolvedDeps().containsDependencies(key)) {
          builder.setDeps(
              Dependencies.newBuilder().setYaml(otherResponse.getDeps().getYaml()).putDependencies(key, value).build());
        }
      });
    }
  }
}
