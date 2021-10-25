package software.wings.helpers.ext.helm;

import io.harness.helm.HelmCommandData;

import software.wings.helpers.ext.helm.request.HelmCommandRequest;
import software.wings.helpers.ext.helm.request.HelmInstallCommandRequest;
import software.wings.helpers.ext.helm.request.HelmRollbackCommandRequest;

import com.google.inject.Singleton;
import java.util.Optional;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

@Singleton
@UtilityClass
public class HelmCommandDataMapper {
  public HelmCommandData getHelmCommandData(@NonNull HelmCommandRequest helmCommandRequest) {
    HelmCommandData helmCommandData =
        HelmCommandData.builder()
            .kubeConfigLocation(Optional.ofNullable(helmCommandRequest.getKubeConfigLocation()).orElse(""))
            .helmVersion(helmCommandRequest.getHelmVersion())
            .releaseName(helmCommandRequest.getReleaseName())
            .isRepoConfigNull(helmCommandRequest.getRepoConfig() == null)
            .yamlFiles(helmCommandRequest.getVariableOverridesYamlFiles())
            .logCallback(helmCommandRequest.getExecutionLogCallback())
            .workingDir(helmCommandRequest.getWorkingDir())
            .commandFlags(helmCommandRequest.getCommandFlags())
            .repoName(helmCommandRequest.getRepoName())
            .build();

    if (helmCommandRequest.getChartSpecification() != null) {
      helmCommandData.setChartName(helmCommandRequest.getChartSpecification().getChartName());
      helmCommandData.setChartVersion(helmCommandRequest.getChartSpecification().getChartVersion());
      helmCommandData.setChartUrl(helmCommandRequest.getChartSpecification().getChartUrl());
    }

    if (helmCommandRequest.getHelmCommandFlag() != null) {
      helmCommandData.setHelmCmdFlagsNull(false);
      helmCommandData.setValueMap(helmCommandRequest.getHelmCommandFlag().getValueMap());
    }

    if (helmCommandRequest instanceof HelmInstallCommandRequest) {
      HelmInstallCommandRequest helmInstallComandRequest = (HelmInstallCommandRequest) helmCommandRequest;
      helmCommandData.setNamespace(helmInstallComandRequest.getNamespace());
      helmCommandData.setPrevReleaseVersion(helmInstallComandRequest.getPrevReleaseVersion());
      helmCommandData.setNewReleaseVersion(helmInstallComandRequest.getNewReleaseVersion());
      helmCommandData.setTimeOutInMillis(helmInstallComandRequest.getTimeoutInMillis());
    }

    else if (helmCommandRequest instanceof HelmRollbackCommandRequest) {
      HelmRollbackCommandRequest helmRollbackCommandRequest = (HelmRollbackCommandRequest) helmCommandRequest;
      helmCommandData.setPrevReleaseVersion(helmRollbackCommandRequest.getPrevReleaseVersion());
      helmCommandData.setNewReleaseVersion(helmRollbackCommandRequest.getNewReleaseVersion());
      helmCommandData.setRollBackVersion(helmRollbackCommandRequest.getRollbackVersion());
      helmCommandData.setTimeOutInMillis(helmRollbackCommandRequest.getTimeoutInMillis());
    }
    return helmCommandData;
  }
}
