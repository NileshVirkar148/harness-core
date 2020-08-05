package software.wings.app;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;

import io.harness.mongo.index.migrator.ApiKeysNameUniqueInAccountMigration;
import io.harness.mongo.index.migrator.DelegateProfileNameUniqueInAccountMigration;
import io.harness.mongo.index.migrator.DelegateScopeNameUniqueInAccountMigration;
import io.harness.mongo.index.migrator.Migrator;

public class IndexMigratorModule extends AbstractModule {
  @Override
  protected void configure() {
    MapBinder<String, Migrator> indexMigrators = MapBinder.newMapBinder(binder(), String.class, Migrator.class);
    indexMigrators.addBinding("delegateProfiles.uniqueName").to(DelegateProfileNameUniqueInAccountMigration.class);
    indexMigrators.addBinding("apiKeys.uniqueName").to(ApiKeysNameUniqueInAccountMigration.class);
    indexMigrators.addBinding("delegateScopes.uniqueName").to(DelegateScopeNameUniqueInAccountMigration.class);
  }
}
