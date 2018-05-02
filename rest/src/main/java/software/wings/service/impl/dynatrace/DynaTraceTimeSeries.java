package software.wings.service.impl.dynatrace;

import software.wings.metrics.MetricType;
import software.wings.metrics.TimeSeriesMetricDefinition;
import software.wings.service.impl.newrelic.NewRelicMetricValueDefinition;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by rsingh on 1/29/18.
 */
public enum DynaTraceTimeSeries {
  CLIENT_SIDE_FAILURE_RATE("com.dynatrace.builtin:servicemethod.clientsidefailurerate", DynaTraceAggregationType.AVG,
      null, NewRelicMetricValueDefinition.CLIENT_SIDE_FAILURE_RATE, MetricType.ERROR),
  ERROR_COUNT_HTTP_4XX("com.dynatrace.builtin:servicemethod.errorcounthttp4xx", null, null,
      NewRelicMetricValueDefinition.ERROR_COUNT_HTTP_4XX, MetricType.ERROR),
  ERROR_COUNT_HTTP_5XX("com.dynatrace.builtin:servicemethod.errorcounthttp5xx", null, null,
      NewRelicMetricValueDefinition.ERROR_COUNT_HTTP_5XX, MetricType.ERROR),
  REQUEST_PER_MINUTE("com.dynatrace.builtin:servicemethod.requestspermin", DynaTraceAggregationType.COUNT, null,
      NewRelicMetricValueDefinition.REQUEST_PER_MINUTE, MetricType.THROUGHPUT),
  RESPONSE_TIME("com.dynatrace.builtin:servicemethod.responsetime", DynaTraceAggregationType.PERCENTILE, 95,
      NewRelicMetricValueDefinition.RESPONSE_TIME, MetricType.RESP_TIME),
  SERVER_SIDE_FAILURE_RATE("com.dynatrace.builtin:servicemethod.serversidefailurerate", DynaTraceAggregationType.AVG,
      null, NewRelicMetricValueDefinition.SERVER_SIDE_FAILURE_RATE, MetricType.ERROR);

  private final String timeseriesId;
  private final DynaTraceAggregationType aggregationType;
  private final Integer percentile;
  private final String savedFieldName;
  private final MetricType metricType;

  DynaTraceTimeSeries(String timeseriesId, DynaTraceAggregationType aggregationType, Integer percentile,
      String savedFieldName, MetricType metricType) {
    this.timeseriesId = timeseriesId;
    this.aggregationType = aggregationType;
    this.percentile = percentile;
    this.savedFieldName = savedFieldName;
    this.metricType = metricType;
  }

  public String getTimeseriesId() {
    return timeseriesId;
  }

  public DynaTraceAggregationType getAggregationType() {
    return aggregationType;
  }

  public Integer getPercentile() {
    return percentile;
  }

  public String getSavedFieldName() {
    return savedFieldName;
  }

  public MetricType getMetricType() {
    return metricType;
  }

  public static DynaTraceTimeSeries getTimeSeries(String timeseriesId) {
    for (DynaTraceTimeSeries timeSeries : DynaTraceTimeSeries.values()) {
      if (timeSeries.getTimeseriesId().equals(timeseriesId)) {
        return timeSeries;
      }
    }

    return null;
  }

  public static Map<String, TimeSeriesMetricDefinition> getDefinitionsToAnalyze() {
    Map<String, TimeSeriesMetricDefinition> rv = new HashMap<>();

    rv.put(CLIENT_SIDE_FAILURE_RATE.getSavedFieldName(),
        TimeSeriesMetricDefinition.builder()
            .metricName(CLIENT_SIDE_FAILURE_RATE.getSavedFieldName())
            .metricType(MetricType.ERROR)
            .build());

    // errorcounthttp4xx
    rv.put(ERROR_COUNT_HTTP_4XX.getSavedFieldName(),
        TimeSeriesMetricDefinition.builder()
            .metricName(ERROR_COUNT_HTTP_4XX.getSavedFieldName())
            .metricType(MetricType.ERROR)
            .build());

    // errorcounthttp5xx
    rv.put(ERROR_COUNT_HTTP_5XX.getSavedFieldName(),
        TimeSeriesMetricDefinition.builder()
            .metricName(ERROR_COUNT_HTTP_5XX.getSavedFieldName())
            .metricType(MetricType.ERROR)
            .build());

    // serversidefailurerate
    rv.put(SERVER_SIDE_FAILURE_RATE.getSavedFieldName(),
        TimeSeriesMetricDefinition.builder()
            .metricName(SERVER_SIDE_FAILURE_RATE.getSavedFieldName())
            .metricType(MetricType.ERROR)
            .build());

    rv.put(REQUEST_PER_MINUTE.getSavedFieldName(),
        TimeSeriesMetricDefinition.builder()
            .metricName(REQUEST_PER_MINUTE.getSavedFieldName())
            .metricType(MetricType.THROUGHPUT)
            .build());

    rv.put(RESPONSE_TIME.getSavedFieldName(),
        TimeSeriesMetricDefinition.builder()
            .metricName(RESPONSE_TIME.getSavedFieldName())
            .metricType(MetricType.RESP_TIME)
            .build());

    return rv;
  }

  public enum DynaTraceAggregationType { MIN, MAX, AVG, SUM, MEDIAN, COUNT, PERCENTILE }
}
