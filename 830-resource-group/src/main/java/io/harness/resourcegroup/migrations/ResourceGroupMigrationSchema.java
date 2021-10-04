package io.harness.resourcegroup.migrations;

import static io.harness.annotations.dev.HarnessTeam.PL;
import static io.harness.ng.DbAliases.RESOURCEGROUP;

import io.harness.annotation.StoreIn;
import io.harness.annotations.dev.OwnedBy;
import io.harness.migration.beans.MigrationType;
import io.harness.migration.entities.NGSchema;

import java.util.Map;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@StoreIn(RESOURCEGROUP)
@Document("schema_resourcegroup")
@TypeAlias("schema_resourcegroup")
@OwnedBy(PL)
public class ResourceGroupMigrationSchema extends NGSchema {
  public ResourceGroupMigrationSchema(
      String id, Long createdAt, Long lastUpdatedAt, String name, Map<MigrationType, Integer> migrationDetails) {
    super(id, createdAt, lastUpdatedAt, name, migrationDetails);
  }
}