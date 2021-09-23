package io.harness.template.events;

import static io.harness.annotations.dev.HarnessTeam.CDC;
import static io.harness.template.beans.TemplateEntityConstants.OTHERS;
import static io.harness.template.beans.TemplateEntityConstants.TEMPLATE_CHANGE_SCOPE;
import static io.harness.template.beans.TemplateEntityConstants.TEMPLATE_LAST_UPDATED_FALSE;
import static io.harness.template.beans.TemplateEntityConstants.TEMPLATE_LAST_UPDATED_TRUE;
import static io.harness.template.beans.TemplateEntityConstants.TEMPLATE_STABLE_FALSE;
import static io.harness.template.beans.TemplateEntityConstants.TEMPLATE_STABLE_TRUE;
import static io.harness.template.beans.TemplateEntityConstants.TEMPLATE_STABLE_TRUE_WITH_YAML_CHANGE;

import io.harness.annotations.dev.OwnedBy;
import io.harness.template.beans.TemplateListType;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

@OwnedBy(CDC)
public enum TemplateUpdateEventType {
  @JsonProperty(TEMPLATE_STABLE_TRUE) TEMPLATE_STABLE_TRUE_EVENT(TEMPLATE_STABLE_TRUE),
  @JsonProperty(TEMPLATE_STABLE_TRUE_WITH_YAML_CHANGE)
  TEMPLATE_STABLE_TRUE_WITH_YAML_CHANGE_EVENT(TEMPLATE_STABLE_TRUE_WITH_YAML_CHANGE),
  @JsonProperty(TEMPLATE_STABLE_FALSE) TEMPLATE_STABLE_FALSE_EVENT(TEMPLATE_STABLE_FALSE),
  @JsonProperty(TEMPLATE_LAST_UPDATED_TRUE) TEMPLATE_LAST_UPDATED_TRUE_EVENT(TEMPLATE_LAST_UPDATED_TRUE),
  @JsonProperty(TEMPLATE_LAST_UPDATED_FALSE) TEMPLATE_LAST_UPDATED_FALSE_EVENT(TEMPLATE_LAST_UPDATED_FALSE),
  @JsonProperty(TEMPLATE_CHANGE_SCOPE) TEMPLATE_CHANGE_SCOPE_EVENT(TEMPLATE_CHANGE_SCOPE),
  @JsonProperty(OTHERS) OTHERS_EVENT(OTHERS);

  private final String yamlType;

  TemplateUpdateEventType(String yamlType) {
    this.yamlType = yamlType;
  }

  @JsonCreator
  public static TemplateUpdateEventType getTemplateType(@JsonProperty("type") String yamlType) {
    for (TemplateUpdateEventType value : TemplateUpdateEventType.values()) {
      if (value.yamlType.equalsIgnoreCase(yamlType)) {
        return value;
      }
    }
    throw new IllegalArgumentException(String.format(
        "Invalid value:%s, the expected values are: %s", yamlType, Arrays.toString(TemplateListType.values())));
  }

  @Override
  @JsonValue
  public String toString() {
    return this.yamlType;
  }
}