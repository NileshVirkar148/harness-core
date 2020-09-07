package software.wings.delegatetasks.citasks.cik8handler;

import static java.lang.String.format;
import static software.wings.delegatetasks.citasks.cik8handler.params.CIConstants.POD_MAX_WAIT_UNTIL_READY_SECS;
import static software.wings.delegatetasks.citasks.cik8handler.params.CIConstants.POD_PENDING_PHASE;
import static software.wings.delegatetasks.citasks.cik8handler.params.CIConstants.POD_RUNNING_PHASE;
import static software.wings.delegatetasks.citasks.cik8handler.params.CIConstants.POD_WAIT_UNTIL_READY_SLEEP_SECS;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ResourceRequirementsBuilder;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import io.harness.exception.PodNotFoundException;
import io.harness.security.encryption.EncryptableSettingWithEncryptionDetails;
import io.harness.security.encryption.EncryptedDataDetail;
import io.harness.threading.Sleeper;
import lombok.extern.slf4j.Slf4j;
import software.wings.beans.GitConfig;
import software.wings.beans.ci.pod.EncryptedVariableWithType;
import software.wings.beans.ci.pod.ImageDetailsWithConnector;
import software.wings.beans.ci.pod.SecretParams;
import software.wings.delegatetasks.citasks.cik8handler.params.CIConstants;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Helper class to interact with K8 cluster for CRUD operation on K8 entities.
 */

@Singleton
@Slf4j
public class CIK8CtlHandler {
  @Inject private SecretSpecBuilder secretSpecBuilder;
  @Inject Provider<ExecCommandListener> execListenerProvider;
  @Inject private Sleeper sleeper;

  public void createRegistrySecret(
      KubernetesClient kubernetesClient, String namespace, ImageDetailsWithConnector imageDetails) {
    Secret secret = secretSpecBuilder.getRegistrySecretSpec(imageDetails, namespace);
    if (secret != null) {
      kubernetesClient.secrets().inNamespace(namespace).createOrReplace(secret);
    }
  }

  public void createPVC(
      KubernetesClient kubernetesClient, String namespace, String claimName, String storageClass, Integer storageMib) {
    List<String> accessModes = new ArrayList<>();
    accessModes.add(CIConstants.PVC_READ_WRITE_ONCE);

    PersistentVolumeClaim pvc =
        new PersistentVolumeClaimBuilder()
            .withNewMetadata()
            .withName(claimName)
            .endMetadata()
            .withNewSpec()
            .withStorageClassName(storageClass)
            .withAccessModes(accessModes)
            .withResources(new ResourceRequirementsBuilder()
                               .addToRequests(CIConstants.STORAGE,
                                   new Quantity(format("%d%s", storageMib, CIConstants.STORAGE_FORMAT)))
                               .build())
            .endSpec()
            .build();
    kubernetesClient.persistentVolumeClaims().inNamespace(namespace).create(pvc);
  }

  // Creates a kubernetes clusterIP service
  public void createService(KubernetesClient kubernetesClient, String namespace, String serviceName,
      Map<String, String> selectorMap, List<Integer> ports) {
    List<ServicePort> svcPorts = new ArrayList<>();
    for (int idx = 0; idx < ports.size(); idx++) {
      String name = String.format("%d-%d", ports.get(idx), idx);
      ServicePort servicePort = new ServicePortBuilder().withName(name).withPort(ports.get(idx)).build();
      svcPorts.add(servicePort);
    }

    Service svc = new ServiceBuilder()
                      .withNewMetadata()
                      .withName(serviceName)
                      .endMetadata()
                      .withNewSpec()
                      .withSelector(selectorMap)
                      .withPorts(svcPorts)
                      .endSpec()
                      .build();
    kubernetesClient.services().inNamespace(namespace).create(svc);
  }

  public Map<String, SecretParams> fetchCustomVariableSecretKeyMap(
      Map<String, EncryptedVariableWithType> encryptedSecrets) {
    return secretSpecBuilder.decryptCustomSecretVariables(encryptedSecrets);
  }

  public Map<String, SecretParams> fetchPublishArtifactSecretKeyMap(
      Map<String, EncryptableSettingWithEncryptionDetails> publishArtifactEncryptedValues) {
    return secretSpecBuilder.decryptPublishArtifactSecretVariables(publishArtifactEncryptedValues);
  }

  public Secret createSecret(
      KubernetesClient kubernetesClient, String secretName, String namespace, Map<String, String> data) {
    Secret secret = secretSpecBuilder.createSecret(secretName, namespace, data);

    if (secret != null) {
      kubernetesClient.secrets().inNamespace(namespace).createOrReplace(secret);
    }
    return secret;
  }

  public Pod createPod(KubernetesClient kubernetesClient, Pod pod, String namespace) {
    return kubernetesClient.pods().inNamespace(namespace).create(pod);
  }

  // Waits for the pod to exit PENDING state and returns true if pod is in RUNNING state, else false.
  public boolean waitUntilPodIsReady(KubernetesClient kubernetesClient, String podName, String namespace)
      throws InterruptedException, TimeoutException {
    int waitTimeSec = 0;
    while (waitTimeSec < POD_MAX_WAIT_UNTIL_READY_SECS) {
      Pod pod = kubernetesClient.pods().inNamespace(namespace).withName(podName).get();
      if (pod == null) {
        throw new PodNotFoundException(format("Pod %s is not present in namespace %s", podName, namespace));
      }

      if (!isPodInPendingState(pod)) {
        return isPodInRunningState(pod);
      }

      sleeper.sleep((long) (POD_WAIT_UNTIL_READY_SLEEP_SECS * 1000));
      waitTimeSec += POD_WAIT_UNTIL_READY_SLEEP_SECS;
    }

    throw new TimeoutException(format("Pod %s in namespace %s is in pending state even after %s seconds", podName,
        namespace, POD_MAX_WAIT_UNTIL_READY_SECS));
  }

  private boolean isPodInPendingState(Pod pod) {
    String podPhase = pod.getStatus().getPhase();
    return podPhase.equals(POD_PENDING_PHASE);
  }

  private boolean isPodInRunningState(Pod pod) {
    String podPhase = pod.getStatus().getPhase();
    return podPhase.equals(POD_RUNNING_PHASE);
  }

  public Boolean deletePod(KubernetesClient kubernetesClient, String podName, String namespace) {
    return kubernetesClient.pods().inNamespace(namespace).withName(podName).delete();
  }

  public Boolean deleteService(KubernetesClient kubernetesClient, String namespace, String serviceName) {
    return kubernetesClient.services().inNamespace(namespace).withName(serviceName).delete();
  }

  public void createGitSecret(KubernetesClient kubernetesClient, String namespace, GitConfig gitConfig,
      List<EncryptedDataDetail> gitEncryptedDataDetails) throws UnsupportedEncodingException {
    Secret secret = secretSpecBuilder.getGitSecretSpec(gitConfig, gitEncryptedDataDetails, namespace);
    if (secret != null) {
      kubernetesClient.secrets().inNamespace(namespace).createOrReplace(secret);
    }
  }

  /**
   * Executes a command or a list of commands on a container in a pod.
   */
  public K8ExecCommandResponse executeCommand(KubernetesClient kubernetesClient, String podName, String containerName,
      String namespace, String[] commands, Integer timeoutSecs) throws InterruptedException {
    ExecCommandListener execListener = execListenerProvider.get();
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    try (ExecWatch watch = kubernetesClient.pods()
                               .inNamespace(namespace)
                               .withName(podName)
                               .inContainer(containerName)
                               .writingOutput(outputStream)
                               .usingListener(execListener)
                               .exec(commands)) {
      ExecCommandStatus execCommandStatus = getCmdExecutionStatus(execListener, timeoutSecs);
      return K8ExecCommandResponse.builder().outputStream(outputStream).execCommandStatus(execCommandStatus).build();
    }
  }

  private ExecCommandStatus getCmdExecutionStatus(ExecCommandListener execCommandListener, Integer timeoutSecs)
      throws InterruptedException {
    ExecCommandStatus execCommandStatus;
    try {
      boolean isCommandCompleted = execCommandListener.isCommandExecutionComplete(timeoutSecs);
      if (isCommandCompleted) {
        execCommandStatus = ExecCommandStatus.SUCCESS;
      } else {
        execCommandStatus = ExecCommandStatus.ERROR;
      }
    } catch (TimeoutException e) {
      logger.warn("Failed to execute command with error: ", e);
      execCommandStatus = ExecCommandStatus.TIMEOUT;
    }
    return execCommandStatus;
  }
}
