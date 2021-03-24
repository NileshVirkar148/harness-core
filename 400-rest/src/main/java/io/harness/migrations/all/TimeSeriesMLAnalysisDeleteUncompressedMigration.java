package io.harness.migrations.all;

import io.harness.annotations.dev.HarnessModule;
import io.harness.annotations.dev.TargetModule;
import io.harness.migrations.Migration;
import io.harness.persistence.HIterator;

import software.wings.dl.WingsPersistence;
import software.wings.service.impl.analysis.TimeSeriesMLAnalysisRecord;

import com.google.inject.Inject;
import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import com.mongodb.DBCollection;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@TargetModule(HarnessModule._390_DB_MIGRATION)
public class TimeSeriesMLAnalysisDeleteUncompressedMigration implements Migration {
  private static final int BATCH_SIZE = 50;
  @Inject private WingsPersistence wingsPersistence;

  @Override
  public void migrate() {
    final DBCollection collection = wingsPersistence.getCollection(TimeSeriesMLAnalysisRecord.class);
    BulkWriteOperation bulkWriteOperation = collection.initializeUnorderedBulkOperation();
    int batched = 0;
    int processed = 0;
    try (HIterator<TimeSeriesMLAnalysisRecord> iterator =
             new HIterator<>(wingsPersistence.createQuery(TimeSeriesMLAnalysisRecord.class).fetch())) {
      for (TimeSeriesMLAnalysisRecord mlAnalysisRecord : iterator) {
        processed++;
        log.info("saving " + mlAnalysisRecord.getUuid());
        bulkWriteOperation
            .find(wingsPersistence.createQuery(TimeSeriesMLAnalysisRecord.class)
                      .filter(TimeSeriesMLAnalysisRecord.ID_KEY2, mlAnalysisRecord.getUuid())
                      .getQueryObject())
            .updateOne(new BasicDBObject("$unset", new BasicDBObject("transactions", "")));
        batched++;

        if (processed != 0 && batched % BATCH_SIZE == 0) {
          bulkWriteOperation.execute();
          bulkWriteOperation = collection.initializeUnorderedBulkOperation();
          batched = 0;
          log.info("updated: " + processed);
        }
      }
      if (batched != 0) {
        bulkWriteOperation.execute();
        log.info("updated: " + processed);
      }
      log.info("processed " + processed);
    }
  }
}
