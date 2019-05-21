package software.wings.delegatetasks.validation;

import static software.wings.beans.command.CommandExecutionContext.Builder.aCommandExecutionContext;
import static software.wings.common.Constants.WINDOWS_HOME_DIR;
import static software.wings.core.ssh.executors.SshSessionFactory.getSSHSession;
import static software.wings.utils.SshHelperUtils.createSshSessionConfig;

import com.google.common.util.concurrent.TimeLimiter;
import com.google.inject.Inject;

import io.harness.beans.DelegateTask;
import lombok.extern.slf4j.Slf4j;
import software.wings.annotation.EncryptableSetting;
import software.wings.beans.ExecutionCredential;
import software.wings.beans.SettingAttribute;
import software.wings.beans.WinRmConnectionAttributes;
import software.wings.beans.infrastructure.Host;
import software.wings.core.winrm.executors.WinRmSession;
import software.wings.core.winrm.executors.WinRmSessionConfig;
import software.wings.delegatetasks.validation.DelegateConnectionResult.DelegateConnectionResultBuilder;
import software.wings.security.encryption.EncryptedDataDetail;
import software.wings.service.intfc.security.EncryptionService;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Created by brett on 11/2/17
 */
@Slf4j
public class HostValidationValidation extends AbstractDelegateValidateTask {
  @Inject private transient EncryptionService encryptionService;
  @Inject private transient TimeLimiter timeLimiter;
  @Inject private transient Clock clock;

  public HostValidationValidation(
      String delegateId, DelegateTask delegateTask, Consumer<List<DelegateConnectionResult>> postExecute) {
    super(delegateId, delegateTask, postExecute);
  }

  @Override
  public List<DelegateConnectionResult> validate() {
    Object[] parameters = getParameters();
    return validateHosts((List<String>) parameters[2], (SettingAttribute) parameters[3],
        (List<EncryptedDataDetail>) parameters[4], (ExecutionCredential) parameters[5]);
  }

  private List<DelegateConnectionResult> validateHosts(List<String> hostNames, SettingAttribute connectionSetting,
      List<EncryptedDataDetail> encryptionDetails, ExecutionCredential executionCredential) {
    List<DelegateConnectionResult> results = new ArrayList<>();
    encryptionService.decrypt((EncryptableSetting) connectionSetting.getValue(), encryptionDetails);
    try {
      timeLimiter.callWithTimeout(() -> {
        for (String hostName : hostNames) {
          DelegateConnectionResultBuilder resultBuilder = DelegateConnectionResult.builder().criteria(hostName);
          long startTime = clock.millis();
          if (connectionSetting.getValue() instanceof WinRmConnectionAttributes) {
            WinRmConnectionAttributes connectionAttributes = (WinRmConnectionAttributes) connectionSetting.getValue();
            WinRmSessionConfig config = WinRmSessionConfig.builder()
                                            .hostname(hostName)
                                            .commandUnitName("HOST_CONNECTION_TEST")
                                            .domain(connectionAttributes.getDomain())
                                            .username(connectionAttributes.getUsername())
                                            .password(String.valueOf(connectionAttributes.getPassword()))
                                            .authenticationScheme(connectionAttributes.getAuthenticationScheme())
                                            .port(connectionAttributes.getPort())
                                            .skipCertChecks(connectionAttributes.isSkipCertChecks())
                                            .useSSL(connectionAttributes.isUseSSL())
                                            .workingDirectory(WINDOWS_HOME_DIR)
                                            .environment(Collections.emptyMap())
                                            .build();
            logger.info("Validating WinrmSession to Host: {}, Port: {}, useSsl: {}", config.getHostname(),
                config.getPort(), config.isUseSSL());

            try (WinRmSession ignore = new WinRmSession(config)) {
              resultBuilder.validated(true);
            } catch (Exception e) {
              logger.info("Exception in WinrmSession Validation: {}", e);
              resultBuilder.validated(false);
            }
          } else {
            try {
              getSSHSession(
                  createSshSessionConfig("HOST_CONNECTION_TEST",
                      aCommandExecutionContext()
                          .withHostConnectionAttributes(connectionSetting)
                          .withExecutionCredential(executionCredential)
                          .withHost(Host.Builder.aHost().withHostName(hostName).withPublicDns(hostName).build())
                          .build()))
                  .disconnect();
              resultBuilder.validated(true);
            } catch (Exception e) {
              resultBuilder.validated(false);
            }
          }
          results.add(resultBuilder.duration(clock.millis() - startTime).build());
        }

        return true;
      }, 30L, TimeUnit.SECONDS, true);
    } catch (Exception e) {
      // Do nothing
    }
    return results;
  }

  @Override
  public List<String> getCriteria() {
    return (List<String>) getParameters()[2];
  }
}
