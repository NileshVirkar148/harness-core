package migrations.all;

import static io.harness.mongo.MongoUtils.setUnset;
import static io.harness.persistence.HQuery.excludeAuthority;
import static org.mongodb.morphia.mapping.Mapper.ID_KEY;

import com.google.inject.Inject;

import io.harness.persistence.HIterator;
import lombok.extern.slf4j.Slf4j;
import migrations.Migration;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import software.wings.beans.security.UserGroup;
import software.wings.dl.WingsPersistence;
import software.wings.security.PermissionAttribute.PermissionType;
import software.wings.utils.Validator;

import java.util.Set;

@Slf4j
public class UserPermissionReadMigration implements Migration {
  @Inject WingsPersistence wingsPersistence;

  @Override
  public void migrate() {
    logger.info("Running the UserPermissionReadMigration script.");

    UpdateOperations<UserGroup> operations = wingsPersistence.createUpdateOperations(UserGroup.class);
    try (HIterator<UserGroup> userGroupIterator =
             new HIterator<>(wingsPersistence.createQuery(UserGroup.class, excludeAuthority).fetch())) {
      while (userGroupIterator.hasNext()) {
        UserGroup userGroup = userGroupIterator.next();
        if (null != userGroup.getAccountPermissions()) {
          Set<PermissionType> permissions = userGroup.getAccountPermissions().getPermissions();
          if (null != permissions) {
            if (permissions.contains(PermissionType.USER_PERMISSION_MANAGEMENT)) {
              permissions.add(PermissionType.USER_PERMISSION_READ);
              setUnset(operations, "accountPermissions", userGroup.getAccountPermissions());
              update(userGroup, operations);
            }
          }
        }
      }
    } catch (Exception ex) {
      logger.error("UserPermissionReadMigration failed.", ex);
    }
  }

  private void update(UserGroup userGroup, UpdateOperations<UserGroup> operations) {
    Validator.notNullCheck("uuid", userGroup.getUuid());
    Validator.notNullCheck(UserGroup.ACCOUNT_ID_KEY, userGroup.getAccountId());
    Query<UserGroup> query = wingsPersistence.createQuery(UserGroup.class)
                                 .filter(ID_KEY, userGroup.getUuid())
                                 .filter(UserGroup.ACCOUNT_ID_KEY, userGroup.getAccountId());
    wingsPersistence.update(query, operations);
  }
}
