package io.harness.dtos;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.entities.ArtifactDetails;
import io.harness.entities.InstanceType;
import io.harness.entities.instanceinfo.InstanceInfo;
import io.harness.ng.core.environment.beans.EnvironmentType;

import lombok.Builder;
import lombok.Value;

@OwnedBy(HarnessTeam.DX)
@Value
@Builder
public class InstanceDTO {
  String accountIdentifier;
  String orgIdentifier;
  String projectIdentifier;
  InstanceType instanceType;
  String envId;
  String envName;
  EnvironmentType envType;
  String serviceId;
  String serviceName;
  String infrastructureMappingId;
  String infraMappingType;
  String connectorId;
  ArtifactDetails primaryArtifact;
  String lastDeployedById;
  String lastDeployedByName;
  long lastDeployedAt;
  String lastPipelineExecutionId;
  String lastPipelineExecutionName;
  InstanceInfo instanceInfo;
  boolean isDeleted;
  long deletedAt;
  long createdAt;
  long lastModifiedAt;
  boolean needRetry;
}
