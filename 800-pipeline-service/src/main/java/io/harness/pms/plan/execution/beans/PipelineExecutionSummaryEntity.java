package io.harness.pms.plan.execution.beans;

import io.harness.annotation.HarnessEntity;
import io.harness.data.validator.Trimmed;
import io.harness.mongo.index.CompoundMongoIndex;
import io.harness.mongo.index.FdIndex;
import io.harness.mongo.index.FdUniqueIndex;
import io.harness.mongo.index.MongoIndex;
import io.harness.ng.core.common.beans.NGTag;
import io.harness.persistence.CreatedAtAware;
import io.harness.persistence.PersistentEntity;
import io.harness.persistence.UpdatedAtAware;
import io.harness.persistence.UuidAware;
import io.harness.pms.contracts.execution.ExecutionErrorInfo;
import io.harness.pms.contracts.execution.Status;
import io.harness.pms.contracts.plan.ExecutionTriggerInfo;
import io.harness.pms.execution.ExecutionStatus;
import io.harness.pms.plan.execution.beans.dto.GraphLayoutNodeDTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.reinert.jjschema.SchemaIgnore;
import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Builder;
import lombok.Setter;
import lombok.Singular;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.NonFinal;
import org.hibernate.validator.constraints.NotEmpty;
import org.mongodb.morphia.annotations.Entity;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

@Value
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldNameConstants(innerTypeName = "PlanExecutionSummaryKeys")
@Entity(value = "planExecutionsSummary", noClassnameStored = true)
@Document("planExecutionsSummary")
@TypeAlias("planExecutionsSummary")
@HarnessEntity(exportable = true)
public class PipelineExecutionSummaryEntity implements PersistentEntity, UuidAware, CreatedAtAware, UpdatedAtAware {
  @Setter @NonFinal @Id @org.mongodb.morphia.annotations.Id String uuid;

  @NotEmpty int runSequence;
  @NotEmpty String accountId;
  @NotEmpty String orgIdentifier;
  @Trimmed @NotEmpty String projectIdentifier;

  @NotEmpty String pipelineIdentifier;
  @NotEmpty @FdUniqueIndex String planExecutionId;
  @NotEmpty String name;

  Status internalStatus;
  ExecutionStatus status;

  String inputSetYaml;
  @Singular @Size(max = 128) List<NGTag> tags;

  @Builder.Default Map<String, org.bson.Document> moduleInfo = new HashMap<>();
  @Builder.Default Map<String, GraphLayoutNodeDTO> layoutNodeMap = new HashMap<>();
  List<String> modules;
  String startingNodeId;

  ExecutionTriggerInfo executionTriggerInfo;
  ExecutionErrorInfo executionErrorInfo;

  Long startTs;
  Long endTs;

  @Setter @NonFinal @SchemaIgnore @FdIndex @CreatedDate long createdAt;
  @Setter @NonFinal @SchemaIgnore @NotNull @LastModifiedDate long lastUpdatedAt;
  @Version Long version;

  public ExecutionStatus getStatus() {
    if (internalStatus == null) {
      // For backwards compatibility when internalStatus was not there
      return status;
    }
    return internalStatus == Status.NO_OP ? ExecutionStatus.NOT_STARTED
                                          : ExecutionStatus.getExecutionStatus(internalStatus);
  }

  public static List<MongoIndex> mongoIndexes() {
    return ImmutableList.<MongoIndex>builder()
        .add(CompoundMongoIndex.builder()
                 .name("unique_accountId_organizationId_projectId_planExecutionId")
                 .unique(true)
                 .field(PlanExecutionSummaryKeys.accountId)
                 .field(PlanExecutionSummaryKeys.orgIdentifier)
                 .field(PlanExecutionSummaryKeys.projectIdentifier)
                 .field(PlanExecutionSummaryKeys.planExecutionId)
                 .build())
        .build();
  }
}
