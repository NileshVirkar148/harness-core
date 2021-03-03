package software.wings.yaml.templatelibrary;

import static software.wings.common.TemplateConstants.ARTIFACT_SOURCE;
import static software.wings.common.TemplateConstants.CUSTOM;

import io.harness.annotations.dev.Module;
import io.harness.annotations.dev.TargetModule;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName(ARTIFACT_SOURCE)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "sourceType", include = JsonTypeInfo.As.EXISTING_PROPERTY)
@JsonSubTypes({ @JsonSubTypes.Type(value = CustomArtifactSourceTemplateYaml.class, name = CUSTOM) })
@TargetModule(Module._870_CG_YAML_BEANS)
public abstract class ArtifactSourceTemplateYaml extends TemplateLibraryYaml {
  private String sourceType;

  public ArtifactSourceTemplateYaml(String sourceType) {
    this.sourceType = sourceType;
  }

  public ArtifactSourceTemplateYaml(String type, String harnessApiVersion, String description,
      List<TemplateVariableYaml> templateVariableYamlList, String sourceType) {
    super(type, harnessApiVersion, description, templateVariableYamlList);
    this.sourceType = sourceType;
  }
}
