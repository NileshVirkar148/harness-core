package io.harness.migrations.timescaledb;

public class CreatePipelineTable extends AbstractTimeScaleDBMigration {
  @Override
  public String getFileName() {
    return "timescaledb/create_pipeline_table.sql";
  }
}
