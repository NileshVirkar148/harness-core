package io.harness.beans.sweepingoutputs;

import static io.harness.annotations.dev.HarnessTeam.CI;

import io.harness.annotation.HarnessEntity;
import io.harness.annotation.RecasterAlias;
import io.harness.annotations.dev.OwnedBy;
import io.harness.mongo.index.FdIndex;
import io.harness.ngpipeline.status.BuildStatusUpdateParameter;
import io.harness.persistence.AccountAccess;
import io.harness.persistence.PersistentEntity;
import io.harness.persistence.UuidAware;
import io.harness.validation.Update;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.github.reinert.jjschema.SchemaIgnore;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.springframework.data.annotation.TypeAlias;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity(value = "stageDetails")
@HarnessEntity(exportable = true)
@TypeAlias("StageDetails")
@JsonTypeName("StageDetails")
@OwnedBy(CI)
@RecasterAlias("io.harness.beans.sweepingoutputs.StageDetails")
public class StageDetails implements PersistentEntity, UuidAware, ContextElement, AccountAccess {
  private String stageID;
  private BuildStatusUpdateParameter buildStatusUpdateParameter;
  private long lastUpdatedAt;
  @Id @NotNull(groups = {Update.class}) @SchemaIgnore private String uuid;
  @FdIndex private String accountId;
}