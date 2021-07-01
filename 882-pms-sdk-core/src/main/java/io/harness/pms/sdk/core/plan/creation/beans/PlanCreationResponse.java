package io.harness.pms.sdk.core.plan.creation.beans;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.data.structure.EmptyPredicate;
import io.harness.exception.InvalidRequestException;
import io.harness.pms.contracts.plan.Dependencies;
import io.harness.pms.contracts.plan.PlanCreationContextValue;
import io.harness.pms.sdk.core.plan.PlanNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

@OwnedBy(HarnessTeam.PIPELINE)
@Data
@Builder
public class PlanCreationResponse {
  @Singular Map<String, PlanNode> nodes;
  Dependencies dependencies;
  @Singular("contextMap") Map<String, PlanCreationContextValue> contextMap;
  GraphLayoutResponse graphLayoutResponse;

  String startingNodeId;
  @Singular List<String> errorMessages;

  public void merge(PlanCreationResponse other) {
    addNodes(other.getNodes());
    addDependencies(other.getDependencies());
    mergeStartingNodeId(other.getStartingNodeId());
    mergeContext(other.getContextMap());
    mergeLayoutNodeInfo(other.getGraphLayoutResponse());
  }

  public void mergeContext(Map<String, PlanCreationContextValue> contextMap) {
    if (EmptyPredicate.isEmpty(contextMap)) {
      return;
    }
    for (Map.Entry<String, PlanCreationContextValue> entry : contextMap.entrySet()) {
      putContextValue(entry.getKey(), entry.getValue());
    }
  }

  public void mergeLayoutNodeInfo(GraphLayoutResponse layoutNodeInfo) {
    if (layoutNodeInfo == null) {
      return;
    }
    if (graphLayoutResponse == null) {
      graphLayoutResponse = layoutNodeInfo;
      return;
    }
    graphLayoutResponse.mergeStartingNodeId(layoutNodeInfo.getStartingNodeId());
    graphLayoutResponse.addLayoutNodes(layoutNodeInfo.getLayoutNodes());
  }

  public void addNodes(Map<String, PlanNode> newNodes) {
    if (EmptyPredicate.isEmpty(newNodes)) {
      return;
    }
    newNodes.values().forEach(this::addNode);
  }

  public void addNode(PlanNode newNode) {
    if (nodes == null) {
      nodes = new HashMap<>();
    } else if (!(nodes instanceof HashMap)) {
      nodes = new HashMap<>(nodes);
    }

    nodes.put(newNode.getUuid(), newNode);
    if (dependencies != null) {
      dependencies = dependencies.toBuilder().removeDependencies(newNode.getUuid()).build();
    }
  }

  public void addDependencies(Dependencies deps) {
    if (deps == null || EmptyPredicate.isEmpty(deps.getDependenciesMap())) {
      return;
    }
    deps.getDependenciesMap().forEach((key, value) -> addDependency(deps.getYaml(), key, value));
  }

  public void putContextValue(String key, PlanCreationContextValue value) {
    if (contextMap != null && contextMap.containsKey(key)) {
      return;
    }

    if (contextMap == null) {
      contextMap = new HashMap<>();
    } else if (!(contextMap instanceof HashMap)) {
      contextMap = new HashMap<>(contextMap);
    }
    contextMap.put(key, value);
  }

  public void addDependency(String yaml, String nodeId, String yamlPath) {
    if ((dependencies != null && dependencies.getDependenciesMap().containsKey(nodeId))
        || (nodes != null && nodes.containsKey(nodeId))) {
      return;
    }

    if (dependencies == null) {
      dependencies = Dependencies.newBuilder().setYaml(yaml).putDependencies(nodeId, yamlPath).build();
      return;
    }
    dependencies = dependencies.toBuilder().putDependencies(nodeId, yamlPath).build();
  }

  public void mergeStartingNodeId(String otherStartingNodeId) {
    if (EmptyPredicate.isEmpty(otherStartingNodeId)) {
      return;
    }
    if (EmptyPredicate.isEmpty(startingNodeId)) {
      startingNodeId = otherStartingNodeId;
      return;
    }
    if (!startingNodeId.equals(otherStartingNodeId)) {
      throw new InvalidRequestException(
          String.format("Received different set of starting nodes: %s and %s", startingNodeId, otherStartingNodeId));
    }
  }
}
