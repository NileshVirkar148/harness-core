package software.wings.graphql.datafetcher.cloudefficiencyevents;

import io.harness.annotations.dev.HarnessModule;
import io.harness.annotations.dev.TargetModule;

import software.wings.graphql.datafetcher.cloudefficiencyevents.CEEventsQueryMetaData.CEEventsMetaDataFields;

@TargetModule(HarnessModule._380_CG_GRAPHQL)
public enum QLEventsSortType {
  Time(CEEventsMetaDataFields.STARTTIME),
  Cost(CEEventsMetaDataFields.BILLINGAMOUNT);
  private CEEventsMetaDataFields eventsMetaData;

  QLEventsSortType(CEEventsMetaDataFields eventsMetaData) {
    this.eventsMetaData = eventsMetaData;
  }

  public CEEventsMetaDataFields getEventsMetaData() {
    return eventsMetaData;
  }
}
