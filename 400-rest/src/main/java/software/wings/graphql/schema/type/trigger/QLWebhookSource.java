package software.wings.graphql.schema.type.trigger;

import io.harness.annotations.dev.HarnessModule;
import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.annotations.dev.TargetModule;
@TargetModule(HarnessModule._380_CG_GRAPHQL) @OwnedBy(HarnessTeam.CDC)
public enum QLWebhookSource { GITHUB, GITLAB, BITBUCKET, CUSTOM }
