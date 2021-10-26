package io.harness.ci.integrationstage;

import static io.harness.beans.serializer.RunTimeInputHandler.UNRESOLVED_PARAMETER;
import static io.harness.beans.serializer.RunTimeInputHandler.resolveBooleanParameter;
import static io.harness.beans.serializer.RunTimeInputHandler.resolveIntegerParameter;
import static io.harness.beans.serializer.RunTimeInputHandler.resolveMapParameter;
import static io.harness.beans.serializer.RunTimeInputHandler.resolveStringParameter;
import static io.harness.beans.serializer.RunTimeInputHandler.resolveStringParameterWithDefaultValue;
import static io.harness.common.CICommonPodConstants.POD_NAME_PREFIX;
import static io.harness.common.CIExecutionConstants.ADDON_VOLUME;
import static io.harness.common.CIExecutionConstants.ADDON_VOL_MOUNT_PATH;
import static io.harness.common.CIExecutionConstants.PORT_STARTING_RANGE;
import static io.harness.common.CIExecutionConstants.PVC_DEFAULT_STORAGE_CLASS;
import static io.harness.common.CIExecutionConstants.SHARED_VOLUME_PREFIX;
import static io.harness.common.CIExecutionConstants.STEP_MOUNT_PATH;
import static io.harness.common.CIExecutionConstants.STEP_PREFIX;
import static io.harness.common.CIExecutionConstants.STEP_REQUEST_MEMORY_MIB;
import static io.harness.common.CIExecutionConstants.STEP_REQUEST_MILLI_CPU;
import static io.harness.common.CIExecutionConstants.STEP_VOLUME;
import static io.harness.common.CIExecutionConstants.STEP_WORK_DIR;
import static io.harness.data.structure.EmptyPredicate.isEmpty;
import static io.harness.data.structure.EmptyPredicate.isNotEmpty;

import static java.lang.Character.toLowerCase;
import static java.lang.String.format;
import static org.apache.commons.lang3.CharUtils.isAsciiAlphanumeric;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.beans.environment.BuildJobEnvInfo;
import io.harness.beans.environment.K8BuildJobEnvInfo;
import io.harness.beans.environment.pod.PodSetupInfo;
import io.harness.beans.environment.pod.container.ContainerDefinitionInfo;
import io.harness.beans.environment.pod.container.ContainerImageDetails;
import io.harness.beans.executionargs.CIExecutionArgs;
import io.harness.beans.plugin.compatible.PluginCompatibleStep;
import io.harness.beans.quantity.unit.DecimalQuantityUnit;
import io.harness.beans.quantity.unit.MemoryQuantityUnit;
import io.harness.beans.serializer.RunTimeInputHandler;
import io.harness.beans.stages.IntegrationStageConfig;
import io.harness.beans.steps.CIStepInfo;
import io.harness.beans.steps.CIStepInfoUtils;
import io.harness.beans.steps.stepinfo.PluginStepInfo;
import io.harness.beans.steps.stepinfo.RunStepInfo;
import io.harness.beans.steps.stepinfo.RunTestsStepInfo;
import io.harness.ci.config.CIExecutionServiceConfig;
import io.harness.ci.utils.QuantityUtils;
import io.harness.delegate.beans.ci.pod.CIContainerType;
import io.harness.delegate.beans.ci.pod.ContainerResourceParams;
import io.harness.delegate.beans.ci.pod.EnvVariableEnum;
import io.harness.delegate.beans.ci.pod.PVCParams;
import io.harness.exception.InvalidRequestException;
import io.harness.exception.ngexception.CIStageExecutionException;
import io.harness.plancreator.execution.ExecutionWrapperConfig;
import io.harness.plancreator.stages.stage.StageElementConfig;
import io.harness.plancreator.steps.ParallelStepElementConfig;
import io.harness.plancreator.steps.StepElementConfig;
import io.harness.pms.yaml.YamlUtils;
import io.harness.stateutils.buildstate.PluginSettingUtils;
import io.harness.stateutils.buildstate.providers.StepContainerUtils;
import io.harness.util.ExceptionUtility;
import io.harness.util.PortFinder;
import io.harness.utils.TimeoutUtils;
import io.harness.yaml.core.variables.NGVariableType;
import io.harness.yaml.core.variables.SecretNGVariable;
import io.harness.yaml.core.variables.StringNGVariable;
import io.harness.yaml.extended.ci.container.ContainerResource;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
@OwnedBy(HarnessTeam.CI)
public class K8InitializeStepInfoBuilder implements InitializeStepInfoBuilder {
  private static final String PLUGIN_USERNAME = "PLUGIN_USERNAME";
  private static final String PLUGIN_PASSW = "PLUGIN_PASSWORD";
  private static final String PLUGIN_REGISTRY = "PLUGIN_REGISTRY";
  private static final String PLUGIN_ACCESS_KEY = "PLUGIN_ACCESS_KEY";
  private static final String PLUGIN_SECRET_KEY = "PLUGIN_SECRET_KEY";
  private static final String PLUGIN_JSON_KEY = "PLUGIN_JSON_KEY";
  private static final String PLUGIN_URL = "PLUGIN_URL";
  static final String SOURCE = "123456789bcdfghjklmnpqrstvwxyz";
  static final Integer RANDOM_LENGTH = 8;
  private static final SecureRandom random = new SecureRandom();
  @Inject private CIExecutionServiceConfig ciExecutionServiceConfig;

  @Override
  public BuildJobEnvInfo getInitializeStepInfoBuilder(
      StageElementConfig stageElementConfig, CIExecutionArgs ciExecutionArgs, List<ExecutionWrapperConfig> steps) {
    String uniqueStageExecutionIdentifier = generatePodName(stageElementConfig.getIdentifier());
    return K8BuildJobEnvInfo.builder()
        .podsSetupInfo(
            getCIPodsSetupInfo(stageElementConfig, ciExecutionArgs, steps, true, uniqueStageExecutionIdentifier))
        .workDir(STEP_WORK_DIR)
        .stepConnectorRefs(getStepConnectorRefs(stageElementConfig))
        .build();
  }

  private String generatePodName(String identifier) {
    return POD_NAME_PREFIX + "-" + getK8PodIdentifier(identifier) + "-"
        + generateRandomAlphaNumericString(RANDOM_LENGTH);
  }

  public String getK8PodIdentifier(String identifier) {
    StringBuilder sb = new StringBuilder(15);
    for (char c : identifier.toCharArray()) {
      if (isAsciiAlphanumeric(c)) {
        sb.append(toLowerCase(c));
      }
      if (sb.length() == 15) {
        return sb.toString();
      }
    }
    return sb.toString();
  }

  public static String generateRandomAlphaNumericString(int length) {
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      sb.append(SOURCE.charAt(random.nextInt(SOURCE.length())));
    }
    return sb.toString();
  }

  private K8BuildJobEnvInfo.PodsSetupInfo getCIPodsSetupInfo(StageElementConfig stageElementConfig,
      CIExecutionArgs ciExecutionArgs, List<ExecutionWrapperConfig> steps, boolean isFirstPod, String podName) {
    List<PodSetupInfo> pods = new ArrayList<>();

    Set<Integer> usedPorts = new HashSet<>();
    PortFinder portFinder = PortFinder.builder().startingPort(PORT_STARTING_RANGE).usedPorts(usedPorts).build();
    String workDirPath = STEP_WORK_DIR;
    List<ContainerDefinitionInfo> serviceContainerDefinitionInfos =
        CIServiceBuilder.createServicesContainerDefinition(stageElementConfig, portFinder, ciExecutionServiceConfig);
    List<ContainerDefinitionInfo> stepContainerDefinitionInfos =
        createStepsContainerDefinition(steps, stageElementConfig, ciExecutionArgs, portFinder);

    List<ContainerDefinitionInfo> containerDefinitionInfos = new ArrayList<>();
    containerDefinitionInfos.addAll(serviceContainerDefinitionInfos);
    containerDefinitionInfos.addAll(stepContainerDefinitionInfos);

    Integer stageMemoryRequest = getStageMemoryRequest(steps);
    Integer stageCpuRequest = getStageCpuRequest(steps);
    List<String> serviceIdList = CIServiceBuilder.getServiceIdList(stageElementConfig);
    List<Integer> serviceGrpcPortList = CIServiceBuilder.getServiceGrpcPortList(stageElementConfig);

    IntegrationStageConfig integrationStageConfig = IntegrationStageUtils.getIntegrationStageConfig(stageElementConfig);

    if (integrationStageConfig.getSharedPaths().isExpression()) {
      ExceptionUtility.throwUnresolvedExpressionException(integrationStageConfig.getSharedPaths().getExpressionValue(),
          "sharedPath", "stage with identifier: " + stageElementConfig.getIdentifier());
    }
    Map<String, String> volumeToMountPath =
        getVolumeToMountPath((List<String>) integrationStageConfig.getSharedPaths().fetchFinalValue());
    pods.add(PodSetupInfo.builder()
                 .podSetupParams(
                     PodSetupInfo.PodSetupParams.builder().containerDefinitionInfos(containerDefinitionInfos).build())
                 .name(podName)
                 .pvcParamsList(createPVCParams(isFirstPod, podName, volumeToMountPath))
                 .volumeToMountPath(volumeToMountPath)
                 .stageCpuRequest(stageCpuRequest)
                 .stageMemoryRequest(stageMemoryRequest)
                 .serviceIdList(serviceIdList)
                 .serviceGrpcPortList(serviceGrpcPortList)
                 .workDirPath(workDirPath)
                 .build());
    return K8BuildJobEnvInfo.PodsSetupInfo.builder().podSetupInfoList(pods).build();
  }

  private List<PVCParams> createPVCParams(boolean isFirstPod, String podName, Map<String, String> volumeToMountPath) {
    List<PVCParams> pvcParamsList = new ArrayList<>();

    for (String volumeName : volumeToMountPath.keySet()) {
      String claimName = format("%s-%s", podName, volumeName);
      pvcParamsList.add(PVCParams.builder()
                            .claimName(claimName)
                            .volumeName(volumeName)
                            .isPresent(!isFirstPod)
                            .sizeMib(ciExecutionServiceConfig.getPvcDefaultStorageSize())
                            .storageClass(PVC_DEFAULT_STORAGE_CLASS)
                            .build());
    }
    return pvcParamsList;
  }

  private Map<String, String> getVolumeToMountPath(List<String> sharedPaths) {
    Map<String, String> volumeToMountPath = new HashMap<>();
    volumeToMountPath.put(STEP_VOLUME, STEP_MOUNT_PATH);
    volumeToMountPath.put(ADDON_VOLUME, ADDON_VOL_MOUNT_PATH);

    if (sharedPaths != null) {
      int index = 0;
      for (String path : sharedPaths) {
        if (isEmpty(path)) {
          continue;
        }

        String volumeName = format("%s%d", SHARED_VOLUME_PREFIX, index);
        if (path.equals(STEP_MOUNT_PATH) || path.equals(ADDON_VOL_MOUNT_PATH)) {
          throw new InvalidRequestException(format("Shared path: %s is a reserved keyword ", path));
        }
        volumeToMountPath.put(volumeName, path);
        index++;
      }
    }
    return volumeToMountPath;
  }

  private List<ContainerDefinitionInfo> createStepsContainerDefinition(List<ExecutionWrapperConfig> steps,
      StageElementConfig integrationStage, CIExecutionArgs ciExecutionArgs, PortFinder portFinder) {
    List<ContainerDefinitionInfo> containerDefinitionInfos = new ArrayList<>();
    if (steps == null) {
      return containerDefinitionInfos;
    }

    int stepIndex = 0;
    for (ExecutionWrapperConfig executionWrapper : steps) {
      if (executionWrapper.getStep() != null && !executionWrapper.getStep().isNull()) {
        StepElementConfig stepElementConfig = IntegrationStageUtils.getStepElementConfig(executionWrapper);
        stepIndex++;
        if (stepElementConfig.getTimeout() != null && stepElementConfig.getTimeout().isExpression()) {
          throw new InvalidRequestException(
              "Timeout field must be resolved in step: " + stepElementConfig.getIdentifier());
        }

        ContainerDefinitionInfo containerDefinitionInfo =
            createStepContainerDefinition(stepElementConfig, integrationStage, ciExecutionArgs, portFinder, stepIndex);
        if (containerDefinitionInfo != null) {
          containerDefinitionInfos.add(containerDefinitionInfo);
        }
      } else if (executionWrapper.getParallel() != null && !executionWrapper.getParallel().isNull()) {
        ParallelStepElementConfig parallelStepElementConfig =
            IntegrationStageUtils.getParallelStepElementConfig(executionWrapper);
        if (isNotEmpty(parallelStepElementConfig.getSections())) {
          for (ExecutionWrapperConfig executionWrapperInParallel : parallelStepElementConfig.getSections()) {
            if (executionWrapperInParallel.getStep() == null || executionWrapperInParallel.getStep().isNull()) {
              continue;
            }

            stepIndex++;
            StepElementConfig stepElementConfig =
                IntegrationStageUtils.getStepElementConfig(executionWrapperInParallel);
            ContainerDefinitionInfo containerDefinitionInfo = createStepContainerDefinition(
                stepElementConfig, integrationStage, ciExecutionArgs, portFinder, stepIndex);
            if (containerDefinitionInfo != null) {
              containerDefinitionInfos.add(containerDefinitionInfo);
            }
          }
        }
      }
    }
    return containerDefinitionInfos;
  }

  private ContainerDefinitionInfo createStepContainerDefinition(StepElementConfig stepElement,
      StageElementConfig integrationStage, CIExecutionArgs ciExecutionArgs, PortFinder portFinder, int stepIndex) {
    if (!(stepElement.getStepSpecType() instanceof CIStepInfo)) {
      return null;
    }

    CIStepInfo ciStepInfo = (CIStepInfo) stepElement.getStepSpecType();
    long timeout = TimeoutUtils.getTimeoutInSeconds(stepElement.getTimeout(), ciStepInfo.getDefaultTimeout());
    switch (ciStepInfo.getNonYamlInfo().getStepInfoType()) {
      case RUN:
        return createRunStepContainerDefinition((RunStepInfo) ciStepInfo, integrationStage, ciExecutionArgs, portFinder,
            stepIndex, stepElement.getIdentifier(), stepElement.getName());
      case DOCKER:
      case ECR:
      case GCR:
      case SAVE_CACHE_S3:
      case RESTORE_CACHE_S3:
      case RESTORE_CACHE_GCS:
      case SAVE_CACHE_GCS:
      case UPLOAD_ARTIFACTORY:
      case UPLOAD_S3:
      case UPLOAD_GCS:
        return createPluginCompatibleStepContainerDefinition((PluginCompatibleStep) ciStepInfo, integrationStage,
            ciExecutionArgs, portFinder, stepIndex, stepElement.getIdentifier(), stepElement.getName(),
            stepElement.getType(), timeout);
      case PLUGIN:
        return createPluginStepContainerDefinition((PluginStepInfo) ciStepInfo, integrationStage, ciExecutionArgs,
            portFinder, stepIndex, stepElement.getIdentifier(), stepElement.getName());
      case RUN_TESTS:
        return createRunTestsStepContainerDefinition((RunTestsStepInfo) ciStepInfo, integrationStage, ciExecutionArgs,
            portFinder, stepIndex, stepElement.getIdentifier());
      default:
        return null;
    }
  }

  private ContainerDefinitionInfo createPluginCompatibleStepContainerDefinition(PluginCompatibleStep stepInfo,
      StageElementConfig integrationStage, CIExecutionArgs ciExecutionArgs, PortFinder portFinder, int stepIndex,
      String identifier, String stepName, String stepType, long timeout) {
    Integer port = portFinder.getNextPort();

    String containerName = format("%s%d", STEP_PREFIX, stepIndex);
    Map<String, String> envVarMap = new HashMap<>();
    envVarMap.putAll(getEnvVariables(integrationStage));
    envVarMap.putAll(BuildEnvironmentUtils.getBuildEnvironmentVariables(ciExecutionArgs));
    envVarMap.putAll(PluginSettingUtils.getPluginCompatibleEnvVariables(stepInfo, identifier, timeout));
    Integer runAsUser = resolveIntegerParameter(stepInfo.getRunAsUser(), null);

    return ContainerDefinitionInfo.builder()
        .name(containerName)
        .commands(StepContainerUtils.getCommand())
        .args(StepContainerUtils.getArguments(port))
        .envVars(envVarMap)
        .secretVariables(getSecretVariables(integrationStage))
        .containerImageDetails(ContainerImageDetails.builder()
                                   .imageDetails(IntegrationStageUtils.getImageInfo(
                                       CIStepInfoUtils.getPluginCustomStepImage(stepInfo, ciExecutionServiceConfig)))
                                   .build())
        .isHarnessManagedImage(true)
        .containerResourceParams(getStepContainerResource(stepInfo.getResources(), stepType, identifier))
        .ports(Collections.singletonList(port))
        .containerType(CIContainerType.PLUGIN)
        .stepIdentifier(identifier)
        .stepName(stepName)
        .runAsUser(runAsUser)
        .build();
  }

  private ContainerDefinitionInfo createRunStepContainerDefinition(RunStepInfo runStepInfo,
      StageElementConfig integrationStage, CIExecutionArgs ciExecutionArgs, PortFinder portFinder, int stepIndex,
      String identifier, String name) {
    Integer port = portFinder.getNextPort();

    String containerName = format("%s%d", STEP_PREFIX, stepIndex);
    Map<String, String> stepEnvVars = new HashMap<>();
    stepEnvVars.putAll(getEnvVariables(integrationStage));
    stepEnvVars.putAll(BuildEnvironmentUtils.getBuildEnvironmentVariables(ciExecutionArgs));
    Map<String, String> envvars =
        resolveMapParameter("envVariables", "Run", identifier, runStepInfo.getEnvVariables(), false);
    if (!isEmpty(envvars)) {
      stepEnvVars.putAll(envvars);
    }
    boolean privileged = resolveBooleanParameter(runStepInfo.getPrivileged(), false);
    Integer runAsUser = resolveIntegerParameter(runStepInfo.getRunAsUser(), null);

    return ContainerDefinitionInfo.builder()
        .name(containerName)
        .commands(StepContainerUtils.getCommand())
        .args(StepContainerUtils.getArguments(port))
        .envVars(stepEnvVars)
        .stepIdentifier(identifier)
        .secretVariables(getSecretVariables(integrationStage))
        .containerImageDetails(ContainerImageDetails.builder()
                                   .imageDetails(IntegrationStageUtils.getImageInfo(resolveStringParameter(
                                       "Image", "Run", identifier, runStepInfo.getImage(), true)))
                                   .connectorIdentifier(resolveStringParameter(
                                       "connectorRef", "Run", identifier, runStepInfo.getConnectorRef(), true))
                                   .build())
        .containerResourceParams(getStepContainerResource(runStepInfo.getResources(), "Run", identifier))
        .ports(Collections.singletonList(port))
        .containerType(CIContainerType.RUN)
        .stepName(name)
        .privileged(privileged)
        .runAsUser(runAsUser)
        .imagePullPolicy(RunTimeInputHandler.resolveImagePullPolicy(runStepInfo.getImagePullPolicy()))
        .build();
  }

  private ContainerDefinitionInfo createRunTestsStepContainerDefinition(RunTestsStepInfo runTestsStepInfo,
      StageElementConfig integrationStage, CIExecutionArgs ciExecutionArgs, PortFinder portFinder, int stepIndex,
      String identifier) {
    Integer port = portFinder.getNextPort();

    String containerName = format("%s%d", STEP_PREFIX, stepIndex);
    Map<String, String> stepEnvVars = new HashMap<>();
    stepEnvVars.putAll(getEnvVariables(integrationStage));
    stepEnvVars.putAll(BuildEnvironmentUtils.getBuildEnvironmentVariables(ciExecutionArgs));
    Map<String, String> envvars =
        resolveMapParameter("envVariables", "RunTests", identifier, runTestsStepInfo.getEnvVariables(), false);
    if (!isEmpty(envvars)) {
      stepEnvVars.putAll(envvars);
    }
    boolean privileged = resolveBooleanParameter(runTestsStepInfo.getPrivileged(), false);
    Integer runAsUser = resolveIntegerParameter(runTestsStepInfo.getRunAsUser(), null);

    return ContainerDefinitionInfo.builder()
        .name(containerName)
        .commands(StepContainerUtils.getCommand())
        .args(StepContainerUtils.getArguments(port))
        .envVars(stepEnvVars)
        .stepIdentifier(identifier)
        .secretVariables(getSecretVariables(integrationStage))
        .containerImageDetails(ContainerImageDetails.builder()
                                   .imageDetails(IntegrationStageUtils.getImageInfo(resolveStringParameter(
                                       "Image", "RunTest", identifier, runTestsStepInfo.getImage(), true)))
                                   .connectorIdentifier(resolveStringParameter(
                                       "connectorRef", "RunTest", identifier, runTestsStepInfo.getConnectorRef(), true))
                                   .build())
        .containerResourceParams(getStepContainerResource(runTestsStepInfo.getResources(), "RunTests", identifier))
        .ports(Collections.singletonList(port))
        .containerType(CIContainerType.TEST_INTELLIGENCE)
        .privileged(privileged)
        .runAsUser(runAsUser)
        .imagePullPolicy(RunTimeInputHandler.resolveImagePullPolicy(runTestsStepInfo.getImagePullPolicy()))
        .build();
  }

  private ContainerDefinitionInfo createPluginStepContainerDefinition(PluginStepInfo pluginStepInfo,
      StageElementConfig integrationStage, CIExecutionArgs ciExecutionArgs, PortFinder portFinder, int stepIndex,
      String identifier, String name) {
    Integer port = portFinder.getNextPort();

    String containerName = format("%s%d", STEP_PREFIX, stepIndex);
    Map<String, String> envVarMap = new HashMap<>();
    envVarMap.putAll(getEnvVariables(integrationStage));
    envVarMap.putAll(BuildEnvironmentUtils.getBuildEnvironmentVariables(ciExecutionArgs));
    if (!isEmpty(pluginStepInfo.getEnvVariables())) {
      envVarMap.putAll(pluginStepInfo.getEnvVariables());
    }

    boolean privileged = resolveBooleanParameter(pluginStepInfo.getPrivileged(), false);
    Integer runAsUser = resolveIntegerParameter(pluginStepInfo.getRunAsUser(), null);

    return ContainerDefinitionInfo.builder()
        .name(containerName)
        .commands(StepContainerUtils.getCommand())
        .args(StepContainerUtils.getArguments(port))
        .envVars(envVarMap)
        .stepIdentifier(identifier)
        .secretVariables(getSecretVariables(integrationStage))
        .containerImageDetails(ContainerImageDetails.builder()
                                   .imageDetails(IntegrationStageUtils.getImageInfo(resolveStringParameter(
                                       "Image", "Plugin", identifier, pluginStepInfo.getImage(), true)))
                                   .connectorIdentifier(resolveStringParameter(
                                       "connectorRef", "Plugin", identifier, pluginStepInfo.getConnectorRef(), true))
                                   .build())
        .containerResourceParams(getStepContainerResource(pluginStepInfo.getResources(), "Plugin", identifier))
        .isHarnessManagedImage(pluginStepInfo.isHarnessManagedImage())
        .ports(Collections.singletonList(port))
        .containerType(CIContainerType.PLUGIN)
        .stepName(name)
        .privileged(privileged)
        .runAsUser(runAsUser)
        .imagePullPolicy(RunTimeInputHandler.resolveImagePullPolicy(pluginStepInfo.getImagePullPolicy()))
        .build();
  }

  private ContainerResourceParams getStepContainerResource(ContainerResource resource, String stepType, String stepId) {
    return ContainerResourceParams.builder()
        .resourceRequestMilliCpu(STEP_REQUEST_MILLI_CPU)
        .resourceRequestMemoryMiB(STEP_REQUEST_MEMORY_MIB)
        .resourceLimitMilliCpu(getContainerCpuLimit(resource, stepType, stepId))
        .resourceLimitMemoryMiB(getContainerMemoryLimit(resource, stepType, stepId))
        .build();
  }

  private List<SecretNGVariable> getSecretVariables(StageElementConfig stageElementConfig) {
    IntegrationStageConfig integrationStageConfig = IntegrationStageUtils.getIntegrationStageConfig(stageElementConfig);
    if (isEmpty(stageElementConfig.getVariables())) {
      return Collections.emptyList();
    }

    return stageElementConfig.getVariables()
        .stream()
        .filter(variable -> variable.getType() == NGVariableType.SECRET)
        .map(customVariable -> (SecretNGVariable) customVariable)
        .collect(Collectors.toList());
  }

  private ParallelStepElementConfig getParallelStepElementConfig(ExecutionWrapperConfig executionWrapperConfig) {
    try {
      return YamlUtils.read(executionWrapperConfig.getParallel().toString(), ParallelStepElementConfig.class);
    } catch (Exception ex) {
      throw new CIStageExecutionException("Failed to deserialize ExecutionWrapperConfig parallel node", ex);
    }
  }

  private StepElementConfig getStepElementConfig(ExecutionWrapperConfig executionWrapperConfig) {
    try {
      return YamlUtils.read(executionWrapperConfig.getStep().toString(), StepElementConfig.class);
    } catch (Exception ex) {
      throw new CIStageExecutionException("Failed to deserialize ExecutionWrapperConfig step node", ex);
    }
  }

  private Map<String, K8BuildJobEnvInfo.ConnectorConversionInfo> getStepConnectorRefs(
      StageElementConfig stageElementConfig) {
    IntegrationStageConfig integrationStageConfig = IntegrationStageUtils.getIntegrationStageConfig(stageElementConfig);

    List<ExecutionWrapperConfig> executionWrappers = integrationStageConfig.getExecution().getSteps();
    if (isEmpty(executionWrappers)) {
      return Collections.emptyMap();
    }

    Map<String, K8BuildJobEnvInfo.ConnectorConversionInfo> map = new HashMap<>();
    for (ExecutionWrapperConfig executionWrapperConfig : executionWrappers) {
      if (executionWrapperConfig.getParallel() != null && !executionWrapperConfig.getParallel().isNull()) {
        ParallelStepElementConfig parallelStepElementConfig = getParallelStepElementConfig(executionWrapperConfig);
        for (ExecutionWrapperConfig executionWrapper : parallelStepElementConfig.getSections()) {
          if (executionWrapper.getStep() != null && !executionWrapper.getStep().isNull()) {
            StepElementConfig stepElementConfig = getStepElementConfig(executionWrapper);
            map.putAll(getStepConnectorConversionInfo(stepElementConfig));
          }
        }
      } else if (executionWrapperConfig.getStep() != null && !executionWrapperConfig.getStep().isNull()) {
        StepElementConfig stepElementConfig = getStepElementConfig(executionWrapperConfig);
        map.putAll(getStepConnectorConversionInfo(stepElementConfig));
      }
    }
    return map;
  }

  private Map<String, K8BuildJobEnvInfo.ConnectorConversionInfo> getStepConnectorConversionInfo(
      StepElementConfig stepElement) {
    Map<String, K8BuildJobEnvInfo.ConnectorConversionInfo> map = new HashMap<>();
    if (stepElement.getStepSpecType() instanceof PluginCompatibleStep) {
      PluginCompatibleStep step = (PluginCompatibleStep) stepElement.getStepSpecType();
      switch (stepElement.getType()) {
        case "BuildAndPushECR":
        case "RestoreCacheS3":
        case "SaveCacheS3":
        case "S3Upload":
          map.put(stepElement.getIdentifier(),
              K8BuildJobEnvInfo.ConnectorConversionInfo.builder()
                  .connectorRef(resolveStringParameter(
                      "connectorRef", stepElement.getType(), stepElement.getIdentifier(), step.getConnectorRef(), true))
                  .envToSecretEntry(EnvVariableEnum.AWS_ACCESS_KEY, PLUGIN_ACCESS_KEY)
                  .envToSecretEntry(EnvVariableEnum.AWS_SECRET_KEY, PLUGIN_SECRET_KEY)
                  .build());
          break;
        case "BuildAndPushGCR":
        case "GCSUpload":
        case "SaveCacheGCS":
        case "RestoreCacheGCS":
          map.put(stepElement.getIdentifier(),
              K8BuildJobEnvInfo.ConnectorConversionInfo.builder()
                  .connectorRef(resolveStringParameter(
                      "connectorRef", stepElement.getType(), stepElement.getIdentifier(), step.getConnectorRef(), true))
                  .envToSecretEntry(EnvVariableEnum.GCP_KEY, PLUGIN_JSON_KEY)
                  .build());

          break;
        case "BuildAndPushDockerRegistry":
          map.put(stepElement.getIdentifier(),
              K8BuildJobEnvInfo.ConnectorConversionInfo.builder()
                  .connectorRef(resolveStringParameter(
                      "connectorRef", stepElement.getType(), stepElement.getIdentifier(), step.getConnectorRef(), true))
                  .envToSecretEntry(EnvVariableEnum.DOCKER_USERNAME, PLUGIN_USERNAME)
                  .envToSecretEntry(EnvVariableEnum.DOCKER_PASSWORD, PLUGIN_PASSW)
                  .envToSecretEntry(EnvVariableEnum.DOCKER_REGISTRY, PLUGIN_REGISTRY)
                  .build());
          break;
        case "ArtifactoryUpload":
          map.put(stepElement.getIdentifier(),
              K8BuildJobEnvInfo.ConnectorConversionInfo.builder()
                  .connectorRef(resolveStringParameter(
                      "connectorRef", stepElement.getType(), stepElement.getIdentifier(), step.getConnectorRef(), true))
                  .envToSecretEntry(EnvVariableEnum.ARTIFACTORY_ENDPOINT, PLUGIN_URL)
                  .envToSecretEntry(EnvVariableEnum.ARTIFACTORY_USERNAME, PLUGIN_USERNAME)
                  .envToSecretEntry(EnvVariableEnum.ARTIFACTORY_PASSWORD, PLUGIN_PASSW)
                  .build());
          break;
        default:
          throw new IllegalStateException("Unexpected value: " + stepElement.getType());
      }
    }
    return map;
  }

  private Map<String, String> getEnvVariables(StageElementConfig stageElementConfig) {
    IntegrationStageConfig integrationStageConfig = IntegrationStageUtils.getIntegrationStageConfig(stageElementConfig);

    if (isEmpty(stageElementConfig.getVariables())) {
      return Collections.emptyMap();
    }

    return stageElementConfig.getVariables()
        .stream()
        .filter(customVariables -> customVariables.getType() == NGVariableType.STRING)
        .map(customVariable -> (StringNGVariable) customVariable)
        .collect(Collectors.toMap(ngVariable
            -> ngVariable.getName(),
            ngVariable
            -> resolveStringParameterWithDefaultValue("variableValue", "stage", stageElementConfig.getIdentifier(),
                ngVariable.getValue(), false, ngVariable.getDefaultValue())));
  }

  private Integer getContainerMemoryLimit(ContainerResource resource, String stepType, String stepId) {
    Integer memoryLimit = ciExecutionServiceConfig.getDefaultMemoryLimit();
    if (resource != null && resource.getLimits() != null && resource.getLimits().getMemory() != null) {
      String memoryLimitMemoryQuantity =
          resolveStringParameter("memory", stepType, stepId, resource.getLimits().getMemory(), false);
      if (isNotEmpty(memoryLimitMemoryQuantity) && !UNRESOLVED_PARAMETER.equals(memoryLimitMemoryQuantity)) {
        memoryLimit = QuantityUtils.getMemoryQuantityValueInUnit(memoryLimitMemoryQuantity, MemoryQuantityUnit.Mi);
      }
    }
    return memoryLimit;
  }

  private Integer getContainerCpuLimit(ContainerResource resource, String stepType, String stepId) {
    Integer cpuLimit = ciExecutionServiceConfig.getDefaultCPULimit();
    if (resource != null && resource.getLimits() != null && resource.getLimits().getCpu() != null) {
      String cpuLimitQuantity = resolveStringParameter("cpu", stepType, stepId, resource.getLimits().getCpu(), false);
      if (isNotEmpty(cpuLimitQuantity) && !UNRESOLVED_PARAMETER.equals(cpuLimitQuantity)) {
        cpuLimit = QuantityUtils.getCpuQuantityValueInUnit(cpuLimitQuantity, DecimalQuantityUnit.m);
      }
    }
    return cpuLimit;
  }

  private Integer getStageMemoryRequest(List<ExecutionWrapperConfig> steps) {
    Integer stageMemoryRequest = 0;
    for (ExecutionWrapperConfig step : steps) {
      Integer executionWrapperMemoryRequest = getExecutionWrapperMemoryRequest(step);
      stageMemoryRequest = Math.max(stageMemoryRequest, executionWrapperMemoryRequest);
    }
    return stageMemoryRequest;
  }

  private Integer getExecutionWrapperMemoryRequest(ExecutionWrapperConfig executionWrapper) {
    if (executionWrapper == null) {
      return 0;
    }

    Integer executionWrapperMemoryRequest = 0;
    if (executionWrapper.getStep() != null && !executionWrapper.getStep().isNull()) {
      StepElementConfig stepElementConfig = IntegrationStageUtils.getStepElementConfig(executionWrapper);
      executionWrapperMemoryRequest = getStepMemoryLimit(stepElementConfig);
    } else if (executionWrapper.getParallel() != null && !executionWrapper.getParallel().isNull()) {
      ParallelStepElementConfig parallel = IntegrationStageUtils.getParallelStepElementConfig(executionWrapper);
      if (isNotEmpty(parallel.getSections())) {
        for (ExecutionWrapperConfig wrapper : parallel.getSections()) {
          executionWrapperMemoryRequest += getExecutionWrapperMemoryRequest(wrapper);
        }
      }
    }
    return executionWrapperMemoryRequest;
  }

  private Integer getStepMemoryLimit(StepElementConfig stepElement) {
    Integer zeroMemory = 0;
    if (!(stepElement.getStepSpecType() instanceof CIStepInfo)) {
      return zeroMemory;
    }

    CIStepInfo ciStepInfo = (CIStepInfo) stepElement.getStepSpecType();
    switch (ciStepInfo.getNonYamlInfo().getStepInfoType()) {
      case RUN:
        return getContainerMemoryLimit(
            ((RunStepInfo) ciStepInfo).getResources(), stepElement.getType(), stepElement.getIdentifier());
      case PLUGIN:
        return getContainerMemoryLimit(
            ((PluginStepInfo) ciStepInfo).getResources(), stepElement.getType(), stepElement.getIdentifier());
      case RUN_TESTS:
        return getContainerMemoryLimit(
            ((RunTestsStepInfo) ciStepInfo).getResources(), stepElement.getType(), stepElement.getIdentifier());
      case GCR:
      case ECR:
      case DOCKER:
      case UPLOAD_ARTIFACTORY:
      case UPLOAD_GCS:
      case UPLOAD_S3:
      case RESTORE_CACHE_GCS:
      case RESTORE_CACHE_S3:
      case SAVE_CACHE_S3:
      case SAVE_CACHE_GCS:
        return getContainerMemoryLimit(
            ((PluginCompatibleStep) ciStepInfo).getResources(), stepElement.getType(), stepElement.getIdentifier());
      default:
        return zeroMemory;
    }
  }

  private Integer getStageCpuRequest(List<ExecutionWrapperConfig> steps) {
    Integer stageCpuRequest = 0;
    for (ExecutionWrapperConfig step : steps) {
      Integer executionWrapperCpuRequest = getExecutionWrapperCpuRequest(step);
      stageCpuRequest = Math.max(stageCpuRequest, executionWrapperCpuRequest);
    }
    return stageCpuRequest;
  }

  private Integer getExecutionWrapperCpuRequest(ExecutionWrapperConfig executionWrapper) {
    if (executionWrapper == null) {
      return 0;
    }

    Integer executionWrapperCpuRequest = 0;
    if (executionWrapper.getStep() != null && !executionWrapper.getStep().isNull()) {
      StepElementConfig stepElementConfig = IntegrationStageUtils.getStepElementConfig(executionWrapper);
      executionWrapperCpuRequest = getStepCpuLimit(stepElementConfig);
    } else if (executionWrapper.getParallel() != null && !executionWrapper.getParallel().isNull()) {
      ParallelStepElementConfig parallelStepElement =
          IntegrationStageUtils.getParallelStepElementConfig(executionWrapper);
      if (isNotEmpty(parallelStepElement.getSections())) {
        for (ExecutionWrapperConfig wrapper : parallelStepElement.getSections()) {
          executionWrapperCpuRequest += getExecutionWrapperCpuRequest(wrapper);
        }
      }
    }
    return executionWrapperCpuRequest;
  }

  private Integer getStepCpuLimit(StepElementConfig stepElement) {
    Integer zeroCpu = 0;
    if (!(stepElement.getStepSpecType() instanceof CIStepInfo)) {
      return zeroCpu;
    }

    CIStepInfo ciStepInfo = (CIStepInfo) stepElement.getStepSpecType();
    switch (ciStepInfo.getNonYamlInfo().getStepInfoType()) {
      case RUN:
        return getContainerCpuLimit(
            ((RunStepInfo) ciStepInfo).getResources(), stepElement.getType(), stepElement.getIdentifier());
      case PLUGIN:
        return getContainerCpuLimit(
            ((PluginStepInfo) ciStepInfo).getResources(), stepElement.getType(), stepElement.getIdentifier());
      case RUN_TESTS:
        return getContainerCpuLimit(
            ((RunTestsStepInfo) ciStepInfo).getResources(), stepElement.getType(), stepElement.getIdentifier());
      case GCR:
      case ECR:
      case DOCKER:
      case UPLOAD_ARTIFACTORY:
      case UPLOAD_GCS:
      case UPLOAD_S3:
      case RESTORE_CACHE_GCS:
      case RESTORE_CACHE_S3:
      case SAVE_CACHE_S3:
      case SAVE_CACHE_GCS:
        return getContainerCpuLimit(
            ((PluginCompatibleStep) ciStepInfo).getResources(), stepElement.getType(), stepElement.getIdentifier());
      default:
        return zeroCpu;
    }
  }
}