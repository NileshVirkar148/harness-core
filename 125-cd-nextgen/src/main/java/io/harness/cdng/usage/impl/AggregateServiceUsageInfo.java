package io.harness.cdng.usage.impl;

import io.harness.timescaledb.tables.pojos.ServiceInfraInfo;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;

@Getter
@EqualsAndHashCode(callSuper = true)
@FieldNameConstants(innerTypeName = "AggregateServiceUsageInfoKeys")
public class AggregateServiceUsageInfo extends ServiceInfraInfo {
  private long activeInstanceCount;

  public AggregateServiceUsageInfo(String orgIdentifier, String projectId, String serviceId, long activeInstanceCount) {
    super(null, null, serviceId, null, null, null, null, null, null, null, null, null, null, orgIdentifier, projectId,
        null);
    this.activeInstanceCount = activeInstanceCount;
  }
}