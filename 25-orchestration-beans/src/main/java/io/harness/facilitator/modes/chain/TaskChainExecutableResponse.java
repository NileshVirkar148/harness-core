package io.harness.facilitator.modes.chain;

import static io.harness.annotations.dev.HarnessTeam.CDC;

import io.harness.annotations.Redesign;
import io.harness.annotations.dev.OwnedBy;
import io.harness.facilitator.modes.TaskExecutableResponse;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@OwnedBy(CDC)
@Redesign
@Value
@Builder
public class TaskChainExecutableResponse implements TaskExecutableResponse {
  @NonNull String taskId;
  @NonNull String taskIdentifier;
  @NonNull String taskType;
  boolean chainEnd;
}
