package software.wings.beans.template;

import io.harness.annotation.HarnessEntity;
import io.harness.mongo.index.CompoundMongoIndex;
import io.harness.mongo.index.MongoIndex;
import io.harness.mongo.index.SortCompoundMongoIndex;
import io.harness.persistence.AccountAccess;

import software.wings.beans.Base;

import com.google.common.collect.ImmutableList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.hibernate.validator.constraints.NotEmpty;
import org.mongodb.morphia.annotations.Entity;

@FieldNameConstants(innerTypeName = "TemplateVersionKeys")
// TODO(abhinav): May have to look at ordering for importedTemplateVersion later.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity(value = "templateVersions", noClassnameStored = true)
@HarnessEntity(exportable = true)
public final class TemplateVersion extends Base implements AccountAccess {
  public static List<MongoIndex> mongoIndexes() {
    return ImmutableList.<MongoIndex>builder()
        .add(CompoundMongoIndex.builder()
                 .name("unique_yaml")
                 .unique(true)
                 .field(TemplateVersionKeys.templateUuid)
                 .field(TemplateVersionKeys.version)
                 .build())
        .add(SortCompoundMongoIndex.builder()
                 .name("account_template_version")
                 .field(TemplateVersionKeys.accountId)
                 .field(TemplateVersionKeys.templateUuid)
                 .descSortField(TemplateVersionKeys.version)
                 .build())
        .add(CompoundMongoIndex.builder()
                 .name("account_imported_template_version")
                 .field(TemplateVersionKeys.accountId)
                 .field(TemplateVersionKeys.templateUuid)
                 .field(TemplateVersionKeys.importedTemplateVersion)
                 .build())
        .build();
  }

  public static final long INITIAL_VERSION = 1;
  public static final String TEMPLATE_UUID_KEY = "templateUuid";
  private String changeType;
  private String templateUuid;
  private String templateName;
  private String templateType;

  private Long version;
  private String versionDetails;
  private String importedTemplateVersion;
  @NotEmpty private String accountId;
  private String galleryId;

  public enum ChangeType { CREATED, UPDATED, IMPORTED }
}
