package io.harness.delegate.beans;

import io.harness.annotation.HarnessEntity;
import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.beans.EmbeddedUser;
import io.harness.mongo.index.CompoundMongoIndex;
import io.harness.mongo.index.FdIndex;
import io.harness.mongo.index.MongoIndex;
import io.harness.persistence.AccountAccess;
import io.harness.persistence.CreatedAtAware;
import io.harness.persistence.CreatedByAware;
import io.harness.persistence.PersistentEntity;
import io.harness.persistence.UpdatedAtAware;
import io.harness.persistence.UpdatedByAware;
import io.harness.persistence.UuidAware;
import io.harness.validation.Update;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.reinert.jjschema.SchemaIgnore;
import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.hibernate.validator.constraints.NotEmpty;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@Entity(value = "delegateProfiles", noClassnameStored = true)
@HarnessEntity(exportable = true)
@FieldNameConstants(innerTypeName = "DelegateProfileKeys")
@OwnedBy(HarnessTeam.DEL)
public final class DelegateProfile implements PersistentEntity, UuidAware, CreatedAtAware, CreatedByAware,
                                              UpdatedAtAware, UpdatedByAware, AccountAccess {
  public static List<MongoIndex> mongoIndexes() {
    return ImmutableList.<MongoIndex>builder()
        .add(CompoundMongoIndex.builder()
                 .field(DelegateProfileKeys.accountId)
                 .field(DelegateProfileKeys.name)
                 .field(DelegateProfileKeys.owner)
                 .unique(true)
                 .name("uniqueDelegateProfileName")
                 .build())
        .add(CompoundMongoIndex.builder()
                 .field(DelegateProfileKeys.accountId)
                 .field(DelegateProfileKeys.ng)
                 .field(DelegateProfileKeys.owner)
                 .name("byAcctNgOwner")
                 .build())
        .build();
  }

  @Id @NotNull(groups = {Update.class}) @SchemaIgnore private String uuid;

  @NotEmpty private String accountId;

  @NotEmpty private String name;
  private String description;

  private boolean primary;

  private boolean approvalRequired;

  private String startupScript;

  private List<DelegateProfileScopingRule> scopingRules;

  private List<String> selectors;

  @SchemaIgnore private EmbeddedUser createdBy;
  @SchemaIgnore @FdIndex private long createdAt;

  @SchemaIgnore private EmbeddedUser lastUpdatedBy;
  @SchemaIgnore @NotNull private long lastUpdatedAt;

  private String identifier;

  // Will be used for NG to hold information about who owns the record, Org or Project or account, if the field is
  // empty
  private DelegateEntityOwner owner;

  // Will be used for segregation of CG vs. NG records.
  private boolean ng;
}
