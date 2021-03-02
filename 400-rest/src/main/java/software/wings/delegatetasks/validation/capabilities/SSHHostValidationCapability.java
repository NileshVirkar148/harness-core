package software.wings.delegatetasks.validation.capabilities;

import io.harness.annotations.dev.Module;
import io.harness.annotations.dev.TargetModule;
import io.harness.delegate.beans.executioncapability.CapabilityType;
import io.harness.delegate.beans.executioncapability.ExecutionCapability;
import io.harness.security.encryption.EncryptedDataDetail;

import software.wings.beans.SSHExecutionCredential;
import software.wings.beans.SettingAttribute;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@TargetModule(Module._930_DELEGATE_TASKS)
public class SSHHostValidationCapability implements ExecutionCapability {
  BasicValidationInfo validationInfo;
  private SettingAttribute hostConnectionAttributes;
  private SettingAttribute bastionConnectionAttributes;
  private List<EncryptedDataDetail> hostConnectionCredentials;
  private List<EncryptedDataDetail> bastionConnectionCredentials;
  private SSHExecutionCredential sshExecutionCredential;
  private Map<String, String> envVariables = new HashMap<>();
  // old impl above, new impl below -- send both sets to get A/B testing and one set to use that result
  private String host;
  private int port;
  @Builder.Default private final CapabilityType capabilityType = CapabilityType.SSH_HOST_CONNECTION;

  @Override
  public EvaluationMode evaluationMode() {
    return EvaluationMode.AGENT;
  }

  @Override
  public String fetchCapabilityBasis() {
    if (validationInfo.isExecuteOnDelegate()) {
      return "localhost";
    }
    return validationInfo.getPublicDns();
  }

  @Override
  public Duration getMaxValidityPeriod() {
    return Duration.ofHours(6);
  }

  @Override
  public Duration getPeriodUntilNextValidation() {
    return Duration.ofHours(4);
  }
}
