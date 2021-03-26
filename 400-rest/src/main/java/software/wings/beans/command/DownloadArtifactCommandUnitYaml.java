package software.wings.beans.command;

import io.harness.annotations.dev.HarnessModule;
import io.harness.annotations.dev.TargetModule;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.List;
import lombok.EqualsAndHashCode;

@TargetModule(HarnessModule._870_CG_YAML_BEANS)
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("DOWNLOAD_ARTIFACT")
public class DownloadArtifactCommandUnitYaml extends ExecCommandUnitAbstractYaml {
  private String artifactVariableName;

  public DownloadArtifactCommandUnitYaml() {
    super(CommandUnitType.DOWNLOAD_ARTIFACT.name());
  }

  @lombok.Builder
  public DownloadArtifactCommandUnitYaml(String name, String deploymentType, String workingDirectory, String scriptType,
      String command, List<TailFilePatternEntryYaml> filePatternEntryList, String artifactVariableName) {
    super(name, CommandUnitType.DOWNLOAD_ARTIFACT.name(), deploymentType, workingDirectory, scriptType, command,
        filePatternEntryList);
    this.artifactVariableName = artifactVariableName;
  }
}
