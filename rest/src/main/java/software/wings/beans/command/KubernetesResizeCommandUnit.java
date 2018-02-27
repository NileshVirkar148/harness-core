package software.wings.beans.command;

import static java.util.Collections.emptyList;
import static software.wings.cloudprovider.ContainerInfo.Status.SUCCESS;

import com.google.inject.Inject;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.fabric8.kubernetes.api.model.HorizontalPodAutoscaler;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.mongodb.morphia.annotations.Transient;
import software.wings.api.ContainerServiceData;
import software.wings.api.DeploymentType;
import software.wings.beans.AzureConfig;
import software.wings.beans.KubernetesConfig;
import software.wings.beans.Log.LogLevel;
import software.wings.beans.SettingAttribute;
import software.wings.cloudprovider.ContainerInfo;
import software.wings.cloudprovider.gke.GkeClusterService;
import software.wings.cloudprovider.gke.KubernetesContainerService;
import software.wings.helpers.ext.azure.AzureHelperService;
import software.wings.security.encryption.EncryptedDataDetail;

import java.util.List;

/**
 * Created by brett on 3/3/17
 */
public class KubernetesResizeCommandUnit extends ContainerResizeCommandUnit {
  @Inject @Transient private transient GkeClusterService gkeClusterService;

  @Inject @Transient private transient KubernetesContainerService kubernetesContainerService;
  @Inject @Transient private transient AzureHelperService azureHelperService;

  public KubernetesResizeCommandUnit() {
    super(CommandUnitType.RESIZE_KUBERNETES);
    setDeploymentType(DeploymentType.KUBERNETES.name());
  }

  @Override
  protected List<ContainerInfo> executeInternal(SettingAttribute cloudProviderSetting, List<EncryptedDataDetail> edd,
      ContainerResizeParams params, ContainerServiceData containerServiceData,
      ExecutionLogCallback executionLogCallback) {
    KubernetesResizeParams resizeParams = (KubernetesResizeParams) params;
    KubernetesConfig kubernetesConfig;
    List<EncryptedDataDetail> encryptedDataDetails;
    if (cloudProviderSetting.getValue() instanceof KubernetesConfig) {
      kubernetesConfig = (KubernetesConfig) cloudProviderSetting.getValue();
      encryptedDataDetails = edd;
    } else if (cloudProviderSetting.getValue() instanceof AzureConfig) {
      AzureConfig azureConfig = (AzureConfig) cloudProviderSetting.getValue();
      kubernetesConfig = azureHelperService.getKubernetesClusterConfig(azureConfig, resizeParams.getSubscriptionId(),
          resizeParams.getResourceGroup(), resizeParams.getClusterName(), resizeParams.getNamespace());
      kubernetesConfig.setDecrypted(true);
      encryptedDataDetails = emptyList();
    } else {
      kubernetesConfig = gkeClusterService.getCluster(
          cloudProviderSetting, edd, resizeParams.getClusterName(), resizeParams.getNamespace());
      kubernetesConfig.setDecrypted(true);
      encryptedDataDetails = emptyList();
    }

    if (resizeParams.isRollbackAutoscaler() && resizeParams.isUseAutoscaler()) {
      HorizontalPodAutoscaler autoscaler = kubernetesContainerService.getAutoscaler(kubernetesConfig,
          encryptedDataDetails, containerServiceData.getName(), ((KubernetesResizeParams) params).getApiVersion());
      if (containerServiceData.getName().equals(autoscaler.getSpec().getScaleTargetRef().getName())) {
        executionLogCallback.saveExecutionLog("Disabling autoscaler " + containerServiceData.getName(), LogLevel.INFO);
        /*
         * Ideally we should be sending resizeParams.getApiVersion(), so we use "v2beta1" when we are dealing with
         * customMetricHPA, but there is a bug in fabric8 library in HasMetadataOperation.replace() method. For
         * customMetricHPA, metric config info resides in HPA.Spec.additionalProperties map. but during execution of
         * replace(), due to build() method in HorizontalPodAutoscalerSpecBuilder, this map goes away, and replace()
         * call actually removes all metricConfig from autoScalar. So currently use v1 version only, till this issue
         * gets fixed. (customMetricConfig is preserved as annotations in version_v1 HPA object, and that path is
         * working fine)
         * */
        kubernetesContainerService.disableAutoscaler(kubernetesConfig, encryptedDataDetails,
            containerServiceData.getName(), ContainerApiVersions.KUBERNETES_V1.getVersionName());
      }
    }

    List<ContainerInfo> containerInfos =
        kubernetesContainerService.setControllerPodCount(kubernetesConfig, encryptedDataDetails,
            resizeParams.getClusterName(), containerServiceData.getName(), containerServiceData.getPreviousCount(),
            containerServiceData.getDesiredCount(), resizeParams.getServiceSteadyStateTimeout(), executionLogCallback);

    boolean allContainersSuccess = containerInfos.stream().allMatch(info -> info.getStatus() == SUCCESS);
    int totalDesiredCount = params.getDesiredCounts().stream().mapToInt(ContainerServiceData::getDesiredCount).sum();
    if (totalDesiredCount > 0 && containerInfos.size() == totalDesiredCount && allContainersSuccess
        && resizeParams.isDeployingToHundredPercent()) {
      if (resizeParams.isUseAutoscaler()) {
        executionLogCallback.saveExecutionLog("Enabling autoscaler " + containerServiceData.getName(), LogLevel.INFO);
        /*
         * Ideally we should be sending resizeParams.getApiVersion(), so we use "v2beta1" when we are dealing with
         * customMetricHPA, but there is a bug in fabric8 library in HasMetadataOperation.replace() method. For
         * customMetricHPA, metric config info resides in HPA.Spec.additionalProperties map. but during execution of
         * replace(), due to build() method in HorizontalPodAutoscalerSpecBuilder, this map goes away, and replace()
         * call actually removes all metricConfig from autoScalar. So currently use v1 version only, till this issue
         * gets fixed. (customMetricConfig is preserved as annotations in version_v1 HPA object, and that path is
         * working fine)
         * */
        kubernetesContainerService.enableAutoscaler(kubernetesConfig, encryptedDataDetails,
            containerServiceData.getName(), ContainerApiVersions.KUBERNETES_V1.getVersionName());
      }
    }

    return containerInfos;
  }

  @Data
  @EqualsAndHashCode(callSuper = true)
  @JsonTypeName("RESIZE_KUBERNETES")
  public static class Yaml extends ContainerResizeCommandUnit.Yaml {
    public Yaml() {
      super(CommandUnitType.RESIZE_KUBERNETES.name());
    }

    @Builder
    public Yaml(String name, String deploymentType) {
      super(name, CommandUnitType.RESIZE_KUBERNETES.name(), deploymentType);
    }
  }
}
