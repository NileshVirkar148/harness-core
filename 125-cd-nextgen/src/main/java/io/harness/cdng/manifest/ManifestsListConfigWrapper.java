package io.harness.cdng.manifest;

import io.harness.cdng.manifest.yaml.ManifestConfig;
import io.harness.cdng.manifest.yaml.ManifestConfigWrapper;
import io.harness.walktree.visitor.Visitable;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.List;

public class ManifestsListConfigWrapper implements Visitable {
  List<ManifestConfig> manifests;

  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public ManifestsListConfigWrapper(List<ManifestConfig> manifests) {
    this.manifests = manifests;
  }
}
