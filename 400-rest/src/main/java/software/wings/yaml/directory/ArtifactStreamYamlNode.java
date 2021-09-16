package software.wings.yaml.directory;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;

import software.wings.yaml.YamlVersion;

@OwnedBy(HarnessTeam.DX)
public class ArtifactStreamYamlNode extends YamlNode {
  private String artifactStreamId;
  private String appId;

  public ArtifactStreamYamlNode() {}

  public ArtifactStreamYamlNode(String accountId, String appId, String artifactStreamId, String name, Class theClass,
      DirectoryPath directoryPath, YamlVersion.Type yamlVersionType) {
    super(accountId, artifactStreamId, name, theClass, directoryPath, yamlVersionType);
    this.artifactStreamId = artifactStreamId;
    this.appId = appId;
  }

  public String getArtifactStreamId() {
    return artifactStreamId;
  }

  public void setArtifactStreamId(String artifactStreamId) {
    this.artifactStreamId = artifactStreamId;
  }

  public String getAppId() {
    return appId;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }
}
