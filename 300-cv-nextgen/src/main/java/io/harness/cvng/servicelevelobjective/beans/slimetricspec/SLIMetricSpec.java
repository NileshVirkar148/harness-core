package io.harness.cvng.servicelevelobjective.beans.slimetricspec;

import io.harness.cvng.servicelevelobjective.beans.SLIMetricType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSubTypes({
  @JsonSubTypes.Type(value = ThresholdSLIMetricSpec.class, name = "Threshold")
  , @JsonSubTypes.Type(value = RatioSLIMetricSpec.class, name = "Ratio"),
})
public abstract class SLIMetricSpec {
  @JsonIgnore public abstract SLIMetricType getType();
  public abstract String getMetricName();
}