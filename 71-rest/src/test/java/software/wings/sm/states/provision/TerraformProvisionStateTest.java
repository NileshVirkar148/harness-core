package software.wings.sm.states.provision;

import static io.harness.context.ContextElementType.TERRAFORM_INHERIT_PLAN;
import static io.harness.rule.OwnerRule.ABOSII;
import static io.harness.rule.OwnerRule.BOJANA;
import static io.harness.rule.OwnerRule.GEORGE;
import static io.harness.rule.OwnerRule.VAIBHAV_SI;
import static io.harness.rule.OwnerRule.YOGESH;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static software.wings.beans.Application.GLOBAL_APP_ID;
import static software.wings.utils.WingsTestConstants.ACCOUNT_ID;
import static software.wings.utils.WingsTestConstants.ACTIVITY_ID;
import static software.wings.utils.WingsTestConstants.APP_ID;
import static software.wings.utils.WingsTestConstants.PROVISIONER_ID;
import static software.wings.utils.WingsTestConstants.WORKFLOW_EXECUTION_ID;

import com.google.common.collect.ImmutableMap;

import io.harness.beans.DelegateTask;
import io.harness.beans.EmbeddedUser;
import io.harness.beans.ExecutionStatus;
import io.harness.beans.FileMetadata;
import io.harness.beans.SweepingOutputInstance;
import io.harness.category.element.UnitTests;
import io.harness.context.ContextElementType;
import io.harness.delegate.beans.ResponseData;
import io.harness.delegate.service.DelegateAgentFileService.FileBucket;
import io.harness.rule.Owner;
import io.harness.security.encryption.EncryptedDataDetail;
import io.harness.stream.BoundedInputStream;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;
import software.wings.WingsBaseTest;
import software.wings.api.ScriptStateExecutionData;
import software.wings.api.TerraformExecutionData;
import software.wings.api.TerraformOutputInfoElement;
import software.wings.api.TerraformPlanParam;
import software.wings.api.terraform.TerraformProvisionInheritPlanElement;
import software.wings.beans.Activity;
import software.wings.beans.Application;
import software.wings.beans.Environment;
import software.wings.beans.GitConfig;
import software.wings.beans.NameValuePair;
import software.wings.beans.TerraformInfrastructureProvisioner;
import software.wings.beans.delegation.TerraformProvisionParameters;
import software.wings.dl.WingsPersistence;
import software.wings.service.intfc.ActivityService;
import software.wings.service.intfc.DelegateService;
import software.wings.service.intfc.FileService;
import software.wings.service.intfc.InfrastructureProvisionerService;
import software.wings.service.intfc.security.SecretManager;
import software.wings.service.intfc.sweepingoutput.SweepingOutputInquiry;
import software.wings.service.intfc.sweepingoutput.SweepingOutputService;
import software.wings.settings.UsageRestrictions;
import software.wings.sm.ContextElement;
import software.wings.sm.ExecutionContext;
import software.wings.sm.ExecutionContextImpl;
import software.wings.sm.ExecutionResponse;
import software.wings.sm.StateExecutionContext;
import software.wings.sm.WorkflowStandardParams;
import software.wings.sm.states.ManagerExecutionLogCallback;
import software.wings.utils.GitUtilsManager;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collector;

public class TerraformProvisionStateTest extends WingsBaseTest {
  @Mock InfrastructureProvisionerService infrastructureProvisionerService;
  @Mock private DelegateService delegateService;
  @Mock private ActivityService activityService;
  @Mock private GitUtilsManager gitUtilsManager;
  @Mock private FileService fileService;
  @Mock private SecretManager secretManager;
  @Mock private SweepingOutputService sweepingOutputService;
  @Mock private ExecutionContextImpl executionContext;
  @Mock private WingsPersistence wingsPersistence;
  @InjectMocks private TerraformProvisionState state = new ApplyTerraformProvisionState("tf");
  @InjectMocks private TerraformProvisionState destroyProvisionState = new DestroyTerraformProvisionState("tf");

  @Before
  public void setup() {
    BiFunction<String, Collector, Answer> extractVariablesOfType = (type, collector) -> {
      return invocation -> {
        List<NameValuePair> input = invocation.getArgumentAt(0, List.class);
        return input.stream().filter(value -> type.equals(value.getValueType())).collect(collector);
      };
    };
    Answer doExtractTextVariables =
        extractVariablesOfType.apply("TEXT", toMap(NameValuePair::getName, NameValuePair::getValue));
    Answer doExtractEncryptedVariables = extractVariablesOfType.apply("ENCRYPTED_TEXT",
        toMap(NameValuePair::getName, entry -> EncryptedDataDetail.builder().fieldName(entry.getName()).build()));
    Answer<String> doReturnSameValue = invocation -> invocation.getArgumentAt(0, String.class);

    doReturn(Activity.builder().uuid("uuid").build()).when(activityService).save(any(Activity.class));
    doAnswer(doExtractTextVariables)
        .when(infrastructureProvisionerService)
        .extractTextVariables(anyListOf(NameValuePair.class), any(ExecutionContext.class));
    doAnswer(doExtractTextVariables)
        .when(infrastructureProvisionerService)
        .extractUnresolvedTextVariables(anyListOf(NameValuePair.class));
    doAnswer(doExtractEncryptedVariables)
        .when(infrastructureProvisionerService)
        .extractEncryptedTextVariables(anyListOf(NameValuePair.class), anyString());
    doAnswer(doReturnSameValue).when(executionContext).renderExpression(anyString());
    doAnswer(doReturnSameValue).when(executionContext).renderExpression(anyString(), any(StateExecutionContext.class));
    doReturn(APP_ID).when(executionContext).getAppId();
    doReturn(Environment.Builder.anEnvironment().build()).when(executionContext).getEnv();
    doReturn(Application.Builder.anApplication().appId(APP_ID).build()).when(executionContext).getApp();
    doReturn(WorkflowStandardParams.Builder.aWorkflowStandardParams()
                 .withCurrentUser(EmbeddedUser.builder().name("name").build())
                 .build())
        .when(executionContext)
        .getContextElement(any(ContextElementType.class));
  }

  @Test
  @Owner(developers = GEORGE)
  @Category(UnitTests.class)
  public void shouldParseOutputs() throws IOException {
    assertThat(TerraformProvisionState.parseOutputs(null).size()).isEqualTo(0);
    assertThat(TerraformProvisionState.parseOutputs("").size()).isEqualTo(0);
    assertThat(TerraformProvisionState.parseOutputs("  ").size()).isEqualTo(0);

    ClassLoader classLoader = getClass().getClassLoader();
    File file = new File(classLoader.getResource("software/wings/sm/states/provision/terraform_output.json").getFile());
    String json = FileUtils.readFileToString(file, Charset.defaultCharset());

    final Map<String, Object> stringObjectMap = TerraformProvisionState.parseOutputs(json);
    assertThat(stringObjectMap.size()).isEqualTo(4);
  }

  @Test
  @Owner(developers = VAIBHAV_SI)
  @Category(UnitTests.class)
  public void shouldUpdateProvisionerWorkspaces() {
    when(infrastructureProvisionerService.update(any())).thenReturn(null);
    TerraformInfrastructureProvisioner provisioner = TerraformInfrastructureProvisioner.builder().build();
    state.updateProvisionerWorkspaces(provisioner, "w1");
    assertThat(provisioner.getWorkspaces().size() == 1 && provisioner.getWorkspaces().contains("w1")).isTrue();
    state.updateProvisionerWorkspaces(provisioner, "w2");
    assertThat(provisioner.getWorkspaces().size() == 2 && provisioner.getWorkspaces().equals(Arrays.asList("w1", "w2")))
        .isTrue();
    state.updateProvisionerWorkspaces(provisioner, "w2");
    assertThat(provisioner.getWorkspaces().size() == 2 && provisioner.getWorkspaces().equals(Arrays.asList("w1", "w2")))
        .isTrue();
  }

  @Test
  @Owner(developers = GEORGE)
  @Category(UnitTests.class)
  public void shouldHandleDefaultWorkspace() {
    assertThat(state.handleDefaultWorkspace(null) == null).isTrue();
    assertThat(state.handleDefaultWorkspace("default") == null).isTrue();
    assertThat(state.handleDefaultWorkspace("abc").equals("abc")).isTrue();
  }

  @Test
  @Owner(developers = YOGESH)
  @Category(UnitTests.class)
  public void testValidateAndFilterVariables() {
    NameValuePair prov_var_1 = NameValuePair.builder().name("access_key").valueType("TEXT").build();
    NameValuePair prov_var_2 = NameValuePair.builder().name("secret_key").valueType("TEXT").build();

    NameValuePair wf_var_1 = NameValuePair.builder().name("access_key").valueType("TEXT").value("value-1").build();
    NameValuePair wf_var_2 = NameValuePair.builder().name("secret_key").valueType("TEXT").value("value-2").build();
    NameValuePair wf_var_3 = NameValuePair.builder().name("region").valueType("TEXT").value("value-3").build();

    final List<NameValuePair> workflowVars = Arrays.asList(wf_var_1, wf_var_2, wf_var_3);
    final List<NameValuePair> provVars = Arrays.asList(prov_var_1, prov_var_2);

    List<NameValuePair> filteredVars_1 = TerraformProvisionState.validateAndFilterVariables(workflowVars, provVars);

    final List<NameValuePair> expected_1 = Arrays.asList(wf_var_1, wf_var_2);
    assertThat(filteredVars_1).isEqualTo(expected_1);

    wf_var_1.setValueType("ENCRYPTED_TEXT");

    final List<NameValuePair> filteredVars_2 =
        TerraformProvisionState.validateAndFilterVariables(workflowVars, provVars);

    final List<NameValuePair> expected_2 = Arrays.asList(wf_var_1, wf_var_2);
    assertThat(filteredVars_2).isEqualTo(expected_2);
  }

  @Test
  @Owner(developers = YOGESH)
  @Category(UnitTests.class)
  public void testStateTimeout() {
    testTimeoutInternal(new ApplyTerraformProvisionState("tf"));
    testTimeoutInternal(new AdjustTerraformProvisionState("tf"));
    testTimeoutInternal(new DestroyTerraformProvisionState("tf"));
    testTimeoutInternal(new TerraformRollbackState("tf"));
    testTimeoutInternal(new ApplyTerraformState("tf"));
  }

  private void testTimeoutInternal(TerraformProvisionState state) {
    state.setTimeoutMillis(null);
    assertThat(state.getTimeoutMillis()).isNull();

    state.setTimeoutMillis(500);
    assertThat(state.getTimeoutMillis()).isEqualTo(500);
  }

  @Owner(developers = ABOSII)
  @Category(UnitTests.class)
  public void testExecuteTerraformDestroyStateWithConfiguration() {
    destroyProvisionState.setVariables(getTerraformVariables());
    destroyProvisionState.setBackendConfigs(getTerraformBackendConfigs());
    destroyProvisionState.setProvisionerId(PROVISIONER_ID);
    TerraformInfrastructureProvisioner provisioner = TerraformInfrastructureProvisioner.builder()
                                                         .appId(APP_ID)
                                                         .path("current/working/directory")
                                                         .variables(getTerraformVariables())
                                                         .build();
    GitConfig gitConfig = GitConfig.builder().branch("master").build();

    doReturn(provisioner).when(infrastructureProvisionerService).get(APP_ID, PROVISIONER_ID);
    doReturn("taskId").when(delegateService).queueTask(any(DelegateTask.class));
    doReturn(gitConfig).when(gitUtilsManager).getGitConfig(anyString());
    ExecutionResponse response = destroyProvisionState.execute(executionContext);

    ArgumentCaptor<DelegateTask> taskCaptor = ArgumentCaptor.forClass(DelegateTask.class);
    verify(delegateService).queueTask(taskCaptor.capture());
    DelegateTask createdTask = taskCaptor.getValue();

    assertThat(response.getDelegateTaskId()).isEqualTo("taskId");
    assertThat(response.isAsync()).isTrue();
    assertThat(createdTask.getAppId()).isEqualTo(APP_ID);
    assertThat(createdTask.getData().getParameters()).isNotEmpty();
    TerraformProvisionParameters parameters = (TerraformProvisionParameters) createdTask.getData().getParameters()[0];
    assertThat(parameters.getVariables()).isNotEmpty();
    assertThat(parameters.getBackendConfigs()).isNotEmpty();
    assertParametersVariables(parameters);
    assertParametersBackendConfigs(parameters);
  }

  @Test
  @Owner(developers = ABOSII)
  @Category(UnitTests.class)
  public void testExecuteTerraformDestroyWithConfigurationAndStateFile() {
    destroyProvisionState.setVariables(getTerraformVariables());
    destroyProvisionState.setBackendConfigs(getTerraformBackendConfigs());
    destroyProvisionState.setProvisionerId(PROVISIONER_ID);
    TerraformInfrastructureProvisioner provisioner = TerraformInfrastructureProvisioner.builder()
                                                         .appId(APP_ID)
                                                         .path("current/working/directory")
                                                         .variables(getTerraformVariables())
                                                         .build();
    GitConfig gitConfig = GitConfig.builder().branch("master").build();
    FileMetadata fileMetadata =
        FileMetadata.builder()
            .metadata(ImmutableMap.of("variables", ImmutableMap.of("region", "us-west"), "backend_configs",
                ImmutableMap.of("bucket", "tf-remote-state", "key", "old_terraform.tfstate")))
            .build();

    doReturn("fileId").when(fileService).getLatestFileId(anyString(), eq(FileBucket.TERRAFORM_STATE));
    doReturn(fileMetadata).when(fileService).getFileMetadata("fileId", FileBucket.TERRAFORM_STATE);
    doReturn(provisioner).when(infrastructureProvisionerService).get(APP_ID, PROVISIONER_ID);
    doReturn("taskId").when(delegateService).queueTask(any(DelegateTask.class));
    doReturn(gitConfig).when(gitUtilsManager).getGitConfig(anyString());
    ExecutionResponse response = destroyProvisionState.execute(executionContext);

    ArgumentCaptor<DelegateTask> taskCaptor = ArgumentCaptor.forClass(DelegateTask.class);
    verify(delegateService).queueTask(taskCaptor.capture());
    DelegateTask createdTask = taskCaptor.getValue();

    assertThat(response.getDelegateTaskId()).isEqualTo("taskId");
    assertThat(response.isAsync()).isTrue();
    assertThat(createdTask.getAppId()).isEqualTo(APP_ID);
    assertThat(createdTask.getData().getParameters()).isNotEmpty();
    TerraformProvisionParameters parameters = (TerraformProvisionParameters) createdTask.getData().getParameters()[0];
    assertThat(parameters.getVariables()).isNotEmpty();
    assertThat(parameters.getBackendConfigs()).isNotEmpty();
    assertParametersVariables(parameters);
    assertParametersBackendConfigs(parameters);
  }

  @Test
  @Owner(developers = ABOSII)
  @Category(UnitTests.class)
  public void testExecuteTerraformDestroyUsingFileMetaData() {
    destroyProvisionState.setProvisionerId(PROVISIONER_ID);
    TerraformInfrastructureProvisioner provisioner = TerraformInfrastructureProvisioner.builder()
                                                         .appId(APP_ID)
                                                         .path("current/working/directory")
                                                         .variables(getTerraformVariables())
                                                         .build();
    GitConfig gitConfig = GitConfig.builder().branch("master").build();
    FileMetadata fileMetadata =
        FileMetadata.builder()
            .metadata(ImmutableMap.of("variables", ImmutableMap.of("region", "us-east", "vpc_id", "vpc-id"),
                "encrypted_variables", ImmutableMap.of("access_key", "access_key", "secret_key", "secret_key"),
                "backend_configs", ImmutableMap.of("bucket", "tf-remote-state", "key", "terraform.tfstate"),
                "encrypted_backend_configs", ImmutableMap.of("access_token", "access_token")))
            .build();

    doReturn("fileId").when(fileService).getLatestFileId(anyString(), eq(FileBucket.TERRAFORM_STATE));
    doReturn(fileMetadata).when(fileService).getFileMetadata("fileId", FileBucket.TERRAFORM_STATE);
    doReturn(provisioner).when(infrastructureProvisionerService).get(APP_ID, PROVISIONER_ID);
    doReturn("taskId").when(delegateService).queueTask(any(DelegateTask.class));
    doReturn(gitConfig).when(gitUtilsManager).getGitConfig(anyString());
    ExecutionResponse response = destroyProvisionState.execute(executionContext);

    ArgumentCaptor<DelegateTask> taskCaptor = ArgumentCaptor.forClass(DelegateTask.class);
    verify(delegateService).queueTask(taskCaptor.capture());
    DelegateTask createdTask = taskCaptor.getValue();

    assertThat(response.getDelegateTaskId()).isEqualTo("taskId");
    assertThat(response.isAsync()).isTrue();
    assertThat(createdTask.getAppId()).isEqualTo(APP_ID);
    assertThat(createdTask.getData().getParameters()).isNotEmpty();
    TerraformProvisionParameters parameters = (TerraformProvisionParameters) createdTask.getData().getParameters()[0];
    assertThat(parameters.getVariables()).isNotEmpty();
    assertThat(parameters.getBackendConfigs()).isNotEmpty();
    assertParametersVariables(parameters);
    assertParametersBackendConfigs(parameters);
  }

  @Test
  @Owner(developers = ABOSII)
  @Category(UnitTests.class)
  public void testExecuteTerraformDestroyWithOnlyBackendConfigs() {
    destroyProvisionState.setProvisionerId(PROVISIONER_ID);
    destroyProvisionState.setBackendConfigs(getTerraformBackendConfigs());
    TerraformInfrastructureProvisioner provisioner = TerraformInfrastructureProvisioner.builder()
                                                         .appId(APP_ID)
                                                         .path("current/working/directory")
                                                         .variables(getTerraformVariables())
                                                         .build();
    GitConfig gitConfig = GitConfig.builder().branch("master").build();

    doReturn(provisioner).when(infrastructureProvisionerService).get(APP_ID, PROVISIONER_ID);
    doReturn("taskId").when(delegateService).queueTask(any(DelegateTask.class));
    doReturn(gitConfig).when(gitUtilsManager).getGitConfig(anyString());
    ExecutionResponse response = destroyProvisionState.execute(executionContext);

    ArgumentCaptor<DelegateTask> taskCaptor = ArgumentCaptor.forClass(DelegateTask.class);
    verify(delegateService).queueTask(taskCaptor.capture());
    DelegateTask createdTask = taskCaptor.getValue();

    assertThat(response.getDelegateTaskId()).isEqualTo("taskId");
    assertThat(response.isAsync()).isTrue();
    assertThat(createdTask.getAppId()).isEqualTo(APP_ID);
    assertThat(createdTask.getData().getParameters()).isNotEmpty();
    TerraformProvisionParameters parameters = (TerraformProvisionParameters) createdTask.getData().getParameters()[0];
    assertThat(parameters.getBackendConfigs()).isNotEmpty();
    assertThat(parameters.getVariables()).isEmpty();
    assertParametersBackendConfigs(parameters);
  }

  @Test
  @Owner(developers = BOJANA)
  @Category(UnitTests.class)
  public void testExecuteInheritApprovedPlan() {
    state.setInheritApprovedPlan(true);
    List<ContextElement> terraformProvisionInheritPlanElements = new ArrayList<>();
    TerraformProvisionInheritPlanElement terraformProvisionInheritPlanElement =
        TerraformProvisionInheritPlanElement.builder()
            .provisionerId(PROVISIONER_ID)
            .workspace("workspace")
            .sourceRepoReference("sourceRepoReference")
            .backendConfigs(getTerraformBackendConfigs())
            .targets(Arrays.asList("target1"))
            .variables(getTerraformVariables())
            .build();
    terraformProvisionInheritPlanElements.add(terraformProvisionInheritPlanElement);
    when(executionContext.getContextElementList(TERRAFORM_INHERIT_PLAN))
        .thenReturn(terraformProvisionInheritPlanElements);

    when(executionContext.getAppId()).thenReturn(APP_ID);
    doReturn(Environment.Builder.anEnvironment().build()).when(executionContext).getEnv();
    state.setProvisionerId(PROVISIONER_ID);
    TerraformInfrastructureProvisioner provisioner = TerraformInfrastructureProvisioner.builder()
                                                         .appId(APP_ID)
                                                         .path("current/working/directory")
                                                         .sourceRepoBranch("sourceRepoBranch")
                                                         .build();
    GitConfig gitConfig = GitConfig.builder().branch("master").build();
    doReturn(provisioner).when(infrastructureProvisionerService).get(APP_ID, PROVISIONER_ID);
    doReturn("fileId").when(fileService).getLatestFileId(anyString(), eq(FileBucket.TERRAFORM_STATE));
    doReturn(gitConfig).when(gitUtilsManager).getGitConfig(anyString());
    when(executionContext.getWorkflowExecutionId()).thenReturn(WORKFLOW_EXECUTION_ID);
    when(executionContext.prepareSweepingOutputInquiryBuilder()).thenReturn(SweepingOutputInquiry.builder());
    List<EncryptedDataDetail> encryptedDataDetails = new ArrayList<>();
    when(secretManager.getEncryptionDetails(gitConfig, GLOBAL_APP_ID, WORKFLOW_EXECUTION_ID))
        .thenReturn(encryptedDataDetails);
    ExecutionResponse executionResponse = state.execute(executionContext);

    verify(infrastructureProvisionerService, times(1)).get(APP_ID, PROVISIONER_ID);
    verify(fileService, times(1)).getLatestFileId(anyString(), any(FileBucket.class));
    verify(gitUtilsManager, times(1)).getGitConfig(anyString());
    verify(infrastructureProvisionerService, times(1)).extractTextVariables(anyList(), any(ExecutionContext.class));
    verify(infrastructureProvisionerService, times(2)).extractEncryptedTextVariables(anyList(), eq(APP_ID));
    verify(infrastructureProvisionerService, times(1)).extractUnresolvedTextVariables(anyList());
    verify(secretManager, times(1)).getEncryptionDetails(any(GitConfig.class), anyString(), anyString());
    assertThat(executionResponse.getCorrelationIds().get(0)).isEqualTo("uuid");
    assertThat(((ScriptStateExecutionData) executionResponse.getStateExecutionData()).getActivityId())
        .isEqualTo("uuid");
  }

  @Test
  @Owner(developers = BOJANA)
  @Category(UnitTests.class)
  public void testHandleAsyncResponseInternalRunPlanOnly() {
    state.setRunPlanOnly(true);
    state.setProvisionerId(PROVISIONER_ID);
    when(executionContext.getAppId()).thenReturn(APP_ID);
    Map<String, ResponseData> response = new HashMap<>();
    TerraformExecutionData terraformExecutionData =
        TerraformExecutionData.builder().executionStatus(ExecutionStatus.SUCCESS).build();
    response.put("activityId", terraformExecutionData);
    TerraformInfrastructureProvisioner provisioner = TerraformInfrastructureProvisioner.builder().appId(APP_ID).build();
    doReturn(provisioner).when(infrastructureProvisionerService).get(APP_ID, PROVISIONER_ID);

    ExecutionResponse executionResponse = state.handleAsyncResponse(executionContext, response);
    assertThat(executionResponse.getStateExecutionData()).isEqualTo(terraformExecutionData);
    assertThat(executionResponse.getExecutionStatus()).isEqualTo(ExecutionStatus.SUCCESS);
    assertThat(
        ((TerraformProvisionInheritPlanElement) executionResponse.getContextElements().get(0)).getProvisionerId())
        .isEqualTo(PROVISIONER_ID);
    verify(infrastructureProvisionerService, times(1)).get(APP_ID, PROVISIONER_ID);
  }

  @Test
  @Owner(developers = BOJANA)
  @Category(UnitTests.class)
  public void testHandleAsyncResponseSavePlan() {
    state.setRunPlanOnly(true);
    state.setExportPlanToApplyStep(true);
    Map<String, ResponseData> response = new HashMap<>();
    response.put("activityId", TerraformExecutionData.builder().tfPlanFile("TFPlanFileContent".getBytes()).build());
    doReturn("workflowExecutionId").when(executionContext).getWorkflowExecutionId();
    state.setProvisionerId(PROVISIONER_ID);
    doReturn(SweepingOutputInquiry.builder()).when(executionContext).prepareSweepingOutputInquiryBuilder();
    doReturn(TerraformInfrastructureProvisioner.builder().build())
        .when(infrastructureProvisionerService)
        .get(APP_ID, PROVISIONER_ID);
    doReturn(SweepingOutputInstance.builder())
        .when(executionContext)
        .prepareSweepingOutputBuilder(SweepingOutputInstance.Scope.WORKFLOW);
    ExecutionResponse executionResponse = state.handleAsyncResponse(executionContext, response);
    assertThat(
        ((TerraformProvisionInheritPlanElement) executionResponse.getContextElements().get(0)).getProvisionerId())
        .isEqualTo(PROVISIONER_ID);
    verify(secretManager, times(1))
        .saveFile(anyString(), anyString(), anyString(), anyInt(), any(UsageRestrictions.class),
            any(BoundedInputStream.class), anyBoolean());
    verify(sweepingOutputService, times(1)).save(any(SweepingOutputInstance.class));
    verify(executionContext, times(1)).prepareSweepingOutputBuilder(any(SweepingOutputInstance.Scope.class));
  }

  @Test
  @Owner(developers = BOJANA)
  @Category(UnitTests.class)
  public void testHandleAsyncResponseInternalRegularFail() {
    state.setProvisionerId(PROVISIONER_ID);
    when(executionContext.getAppId()).thenReturn(APP_ID);
    Map<String, ResponseData> response = new HashMap<>();
    TerraformExecutionData terraformExecutionData =
        TerraformExecutionData.builder().executionStatus(ExecutionStatus.FAILED).build();
    response.put("activityId", terraformExecutionData);
    TerraformInfrastructureProvisioner provisioner = TerraformInfrastructureProvisioner.builder().appId(APP_ID).build();
    doReturn(provisioner).when(infrastructureProvisionerService).get(APP_ID, PROVISIONER_ID);

    when(executionContext.prepareSweepingOutputInquiryBuilder()).thenReturn(SweepingOutputInquiry.builder());
    doReturn(SweepingOutputInstance.builder())
        .when(executionContext)
        .prepareSweepingOutputBuilder(SweepingOutputInstance.Scope.WORKFLOW);

    ExecutionResponse executionResponse = state.handleAsyncResponse(executionContext, response);

    verify(infrastructureProvisionerService, times(1)).get(APP_ID, PROVISIONER_ID);
    assertThat(executionResponse.getStateExecutionData()).isEqualTo(terraformExecutionData);
    assertThat(executionResponse.getExecutionStatus()).isEqualTo(ExecutionStatus.FAILED);
  }

  @Test
  @Owner(developers = BOJANA)
  @Category(UnitTests.class)
  public void testGetTerraformPlanFromSecretManager() {
    String terraformPlanSecretManagerId = "terraformPlanSecretManagerId";
    when(executionContext.getWorkflowExecutionId()).thenReturn(WORKFLOW_EXECUTION_ID);
    when(executionContext.getAccountId()).thenReturn(ACCOUNT_ID);
    SweepingOutputInstance sweepingOutputInstance =
        SweepingOutputInstance.builder()
            .value(TerraformPlanParam.builder().terraformPlanSecretManagerId(terraformPlanSecretManagerId).build())
            .build();
    when(executionContext.prepareSweepingOutputInquiryBuilder()).thenReturn(SweepingOutputInquiry.builder());
    when(sweepingOutputService.find(any(SweepingOutputInquiry.class))).thenReturn(sweepingOutputInstance);
    byte[] terraformPlanContent = "terraformPlanContent".getBytes();
    when(secretManager.getFileContents(anyString(), anyString())).thenReturn(terraformPlanContent);
    byte[] retrievedFileContent = state.getTerraformPlanFromSecretManager(executionContext);
    assertThat(retrievedFileContent).isEqualTo(terraformPlanContent);
    verify(secretManager, times(1)).getFileContents(ACCOUNT_ID, terraformPlanSecretManagerId);
    verify(sweepingOutputService, times(1)).find(any(SweepingOutputInquiry.class));
  }

  @Test
  @Owner(developers = BOJANA)
  @Category(UnitTests.class)
  public void testHandleAsyncResponseInternalRegularDestroy() {
    TerraformProvisionState destroyProvisionStateSpy = spy(destroyProvisionState);
    destroyProvisionStateSpy.setProvisionerId(PROVISIONER_ID);
    destroyProvisionStateSpy.setTargets(Arrays.asList("target1"));
    when(executionContext.getAppId()).thenReturn(APP_ID);
    String outputs = "{\n"
        + "\"key\": {\n"
        + "\"value\": \"value1\"\n"
        + "}\n"
        + "}";
    Map<String, ResponseData> response = new HashMap<>();
    TerraformExecutionData terraformExecutionData = TerraformExecutionData.builder()
                                                        .workspace("workspace")
                                                        .executionStatus(ExecutionStatus.SUCCESS)
                                                        .activityId(ACTIVITY_ID)
                                                        .outputs(outputs)
                                                        .build();
    response.put("activityId", terraformExecutionData);
    TerraformInfrastructureProvisioner provisioner = TerraformInfrastructureProvisioner.builder().appId(APP_ID).build();
    doReturn(provisioner).when(infrastructureProvisionerService).get(APP_ID, PROVISIONER_ID);

    when(executionContext.getContextElement(ContextElementType.TERRAFORM_PROVISION))
        .thenReturn(TerraformOutputInfoElement.builder().build());
    when(infrastructureProvisionerService.getManagerExecutionCallback(anyString(), anyString(), anyString()))
        .thenReturn(mock(ManagerExecutionLogCallback.class));
    when(executionContext.prepareSweepingOutputInquiryBuilder()).thenReturn(SweepingOutputInquiry.builder());

    ExecutionResponse executionResponse = destroyProvisionStateSpy.handleAsyncResponse(executionContext, response);

    verify(infrastructureProvisionerService, times(1))
        .regenerateInfrastructureMappings(
            anyString(), any(ExecutionContext.class), anyMap(), any(Optional.class), any(Optional.class));
    verify(infrastructureProvisionerService, times(1)).get(APP_ID, PROVISIONER_ID);
    assertThat(executionResponse.getStateExecutionData()).isEqualTo(terraformExecutionData);
    assertThat(executionResponse.getExecutionStatus()).isEqualTo(ExecutionStatus.SUCCESS);

    TerraformOutputInfoElement terraformOutputInfoElement =
        (TerraformOutputInfoElement) executionResponse.getContextElements().get(0);
    assertThat(terraformOutputInfoElement.paramMap(executionContext)).containsKeys("terraform");
  }

  @Test
  @Owner(developers = BOJANA)
  @Category(UnitTests.class)
  public void testDeleteTerraformPlanFromSecretManager() {
    String terraformPlanSecretManagerId = "terraformPlanSecretManagerId";
    String sweepingOutputInstanceUUID = "sweepingOutputInstanceUUID";
    when(executionContext.getWorkflowExecutionId()).thenReturn(WORKFLOW_EXECUTION_ID);
    when(executionContext.getAccountId()).thenReturn(ACCOUNT_ID);
    when(executionContext.getAppId()).thenReturn(APP_ID);
    SweepingOutputInstance sweepingOutputInstance =
        SweepingOutputInstance.builder()
            .uuid(sweepingOutputInstanceUUID)
            .value(TerraformPlanParam.builder().terraformPlanSecretManagerId(terraformPlanSecretManagerId).build())
            .build();
    when(executionContext.prepareSweepingOutputInquiryBuilder()).thenReturn(SweepingOutputInquiry.builder());
    when(sweepingOutputService.find(any(SweepingOutputInquiry.class))).thenReturn(sweepingOutputInstance);
    state.deleteTerraformPlanFromSecretManager(executionContext);
    verify(sweepingOutputService, times(1)).find(any(SweepingOutputInquiry.class));
    verify(secretManager, times(1)).deleteFile(ACCOUNT_ID, terraformPlanSecretManagerId);
    verify(sweepingOutputService, times(1)).deleteById(APP_ID, sweepingOutputInstanceUUID);
  }

  private void assertParametersVariables(TerraformProvisionParameters parameters) {
    assertThat(parameters.getVariables().keySet()).containsExactlyInAnyOrder("region", "vpc_id");
    assertThat(parameters.getVariables().values()).containsExactlyInAnyOrder("us-east", "vpc-id");
    assertThat(parameters.getEncryptedVariables().keySet()).containsExactlyInAnyOrder("access_key", "secret_key");
  }

  private void assertParametersBackendConfigs(TerraformProvisionParameters parameters) {
    assertThat(parameters.getBackendConfigs().keySet()).containsExactlyInAnyOrder("key", "bucket");
    assertThat(parameters.getBackendConfigs().values())
        .containsExactlyInAnyOrder("terraform.tfstate", "tf-remote-state");
    assertThat(parameters.getEncryptedBackendConfigs().keySet()).containsExactlyInAnyOrder("access_token");
  }

  private List<NameValuePair> getTerraformVariables() {
    return Arrays.asList(NameValuePair.builder().name("region").value("us-east").valueType("TEXT").build(),
        NameValuePair.builder().name("vpc_id").value("vpc-id").valueType("TEXT").build(),
        NameValuePair.builder().name("access_key").value("access_key").valueType("ENCRYPTED_TEXT").build(),
        NameValuePair.builder().name("secret_key").value("secret_key").valueType("ENCRYPTED_TEXT").build());
  }

  private List<NameValuePair> getTerraformBackendConfigs() {
    return Arrays.asList(NameValuePair.builder().name("key").value("terraform.tfstate").valueType("TEXT").build(),
        NameValuePair.builder().name("bucket").value("tf-remote-state").valueType("TEXT").build(),
        NameValuePair.builder().name("access_token").value("access_token").valueType("ENCRYPTED_TEXT").build());
  }
}