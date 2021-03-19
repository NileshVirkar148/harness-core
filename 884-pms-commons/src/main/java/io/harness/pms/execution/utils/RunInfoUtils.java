package io.harness.pms.execution.utils;

import io.harness.data.structure.EmptyPredicate;
import io.harness.pms.yaml.ParameterField;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RunInfoUtils {
  public String getRunCondition(ParameterField<String> whenCondition) {
    if (whenCondition == null) {
      return null;
    }
    if (EmptyPredicate.isNotEmpty(whenCondition.getValue())) {
      return whenCondition.getValue();
    }
    return whenCondition.getExpressionValue();
  }
}
