package io.harness.delegate.beans.ci.vm.steps;

import io.harness.delegate.beans.ci.pod.ConnectorDetails;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.Value;

@Data
@Builder
@Value
public class RunStep implements StepInfo {
  private String image;
  private ConnectorDetails imageConnector;
  private String pullPolicy; // always, if-not-exists or never
  private String runAsUser;
  private boolean privileged;

  private List<String> entrypoint;
  private String command;
  private List<String> outputVariables;
  private Map<String, String> envVariables;
  private UnitTestReport unitTestReport;

  @Override
  public StepInfo.Type getType() {
    return Type.RUN;
  }
}
