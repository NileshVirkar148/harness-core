package software.wings.yaml.handler.workflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static software.wings.utils.WingsTestConstants.ACCOUNT_ID;
import static software.wings.utils.WingsTestConstants.APP_ID;

import com.google.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import software.wings.beans.Workflow;
import software.wings.beans.yaml.ChangeContext;
import software.wings.exception.HarnessException;
import software.wings.service.impl.yaml.handler.workflow.MultiServiceWorkflowYamlHandler;
import software.wings.yaml.workflow.MultiServiceWorkflowYaml;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author rktummala on 1/10/18
 */
public class MultiServiceWorkflowYamlHandlerTest extends BaseWorkflowYamlHandlerTest {
  private String validYamlContent = "envName: ENV_NAME\n"
      + "templatized: false\n"
      + "phases:\n"
      + "  - name: Phase 1\n"
      + "    infraMappingName: direct_Kubernetes\n"
      + "    serviceName: SERVICE_NAME\n"
      + "    computeProviderName: COMPUTE_PROVIDER_ID\n"
      + "    provisionNodes: false\n"
      + "    phaseSteps:\n"
      + "      - name: Setup Container\n"
      + "        stepsInParallel: false\n"
      + "        steps:\n"
      + "          - name: Kubernetes Service Setup\n"
      + "            type: KUBERNETES_REPLICATION_CONTROLLER_SETUP\n"
      + "        type: CONTAINER_SETUP\n"
      + "      - name: Deploy Containers\n"
      + "        stepsInParallel: false\n"
      + "        steps:\n"
      + "          - name: Upgrade Containers\n"
      + "            properties:\n"
      + "                commandName: Resize Replication Controller\n"
      + "                instanceUnitType: COUNT\n"
      + "                instanceCount: 1\n"
      + "            type: KUBERNETES_REPLICATION_CONTROLLER_DEPLOY\n"
      + "        type: CONTAINER_DEPLOY\n"
      + "      - name: Verify Service\n"
      + "        stepsInParallel: false\n"
      + "        type: VERIFY_SERVICE\n"
      + "      - name: Wrap Up\n"
      + "        stepsInParallel: false\n"
      + "        type: WRAP_UP\n"
      + "    type: KUBERNETES\n"
      + "  - name: Phase 2\n"
      + "    infraMappingName: direct_Kubernetes\n"
      + "    serviceName: SERVICE_NAME\n"
      + "    computeProviderName: COMPUTE_PROVIDER_ID\n"
      + "    provisionNodes: false\n"
      + "    phaseSteps:\n"
      + "      - name: Setup Container\n"
      + "        stepsInParallel: false\n"
      + "        steps:\n"
      + "          - name: Kubernetes Service Setup\n"
      + "            type: KUBERNETES_REPLICATION_CONTROLLER_SETUP\n"
      + "        type: CONTAINER_SETUP\n"
      + "      - name: Deploy Containers\n"
      + "        stepsInParallel: false\n"
      + "        steps:\n"
      + "          - name: Upgrade Containers\n"
      + "            properties:\n"
      + "                commandName: Resize Replication Controller\n"
      + "                instanceUnitType: COUNT\n"
      + "                instanceCount: 1\n"
      + "            type: KUBERNETES_REPLICATION_CONTROLLER_DEPLOY\n"
      + "        type: CONTAINER_DEPLOY\n"
      + "      - name: Verify Service\n"
      + "        stepsInParallel: false\n"
      + "        type: VERIFY_SERVICE\n"
      + "      - name: Wrap Up\n"
      + "        stepsInParallel: false\n"
      + "        type: WRAP_UP\n"
      + "    type: KUBERNETES\n"
      + "rollbackPhases:\n"
      + "  - name: Rollback Phase 1\n"
      + "    infraMappingName: direct_Kubernetes\n"
      + "    serviceName: SERVICE_NAME\n"
      + "    computeProviderName: COMPUTE_PROVIDER_ID\n"
      + "    provisionNodes: false\n"
      + "    phaseNameForRollback: Phase 1\n"
      + "    phaseSteps:\n"
      + "      - name: Deploy Containers\n"
      + "        statusForRollback: SUCCESS\n"
      + "        stepsInParallel: false\n"
      + "        steps:\n"
      + "          - name: Rollback Containers\n"
      + "            type: KUBERNETES_REPLICATION_CONTROLLER_ROLLBACK\n"
      + "        phaseStepNameForRollback: Deploy Containers\n"
      + "        type: CONTAINER_DEPLOY\n"
      + "      - name: Wrap Up\n"
      + "        stepsInParallel: false\n"
      + "        type: WRAP_UP\n"
      + "    type: KUBERNETES\n"
      + "  - name: Rollback Phase 2\n"
      + "    infraMappingName: direct_Kubernetes\n"
      + "    serviceName: SERVICE_NAME\n"
      + "    computeProviderName: COMPUTE_PROVIDER_ID\n"
      + "    provisionNodes: false\n"
      + "    phaseNameForRollback: Phase 2\n"
      + "    phaseSteps:\n"
      + "      - name: Deploy Containers\n"
      + "        statusForRollback: SUCCESS\n"
      + "        stepsInParallel: false\n"
      + "        steps:\n"
      + "          - name: Rollback Containers\n"
      + "            type: KUBERNETES_REPLICATION_CONTROLLER_ROLLBACK\n"
      + "        phaseStepNameForRollback: Deploy Containers\n"
      + "        type: CONTAINER_DEPLOY\n"
      + "      - name: Wrap Up\n"
      + "        stepsInParallel: false\n"
      + "        type: WRAP_UP\n"
      + "    type: KUBERNETES\n"
      + "notificationRules:\n"
      + "  - conditions:\n"
      + "      - FAILED\n"
      + "    executionScope: WORKFLOW\n"
      + "    notificationGroups:\n"
      + "      - accountId: ACCOUNT_ID\n"
      + "        name: Account Administrator\n"
      + "        editable: false\n"
      + "    batchNotifications: false\n"
      + "    active: true\n"
      + "failureStrategies:\n"
      + "  - failureTypes:\n"
      + "      - APPLICATION_ERROR\n"
      + "    executionScope: WORKFLOW\n"
      + "    repairActionCode: ROLLBACK_WORKFLOW\n"
      + "    retryCount: 0\n"
      + "harnessApiVersion: '1.0'\n"
      + "type: MULTI_SERVICE";
  private String validYamlFilePath = "Setup/Applications/APP_NAME/Workflows/multiService.yaml";
  private String invalidYamlContent = "envName: env1\nphaseInvalid: phase1\ntype: MULTI_SERVICE";
  private String invalidYamlFilePath = "Setup/Applications/APP_NAME/WorkflowsInvalid/multiService.yaml";
  private String workflowName = "multiService";

  @InjectMocks @Inject private MultiServiceWorkflowYamlHandler yamlHandler;

  @Before
  public void runBeforeTest() {
    setup(validYamlFilePath, workflowName);
  }

  @Test
  public void testCRUDAndGet() throws HarnessException, IOException {
    ChangeContext<MultiServiceWorkflowYaml> changeContext =
        getChangeContext(validYamlContent, validYamlFilePath, yamlHandler);

    MultiServiceWorkflowYaml yamlObject =
        (MultiServiceWorkflowYaml) getYaml(validYamlContent, MultiServiceWorkflowYaml.class, false);
    changeContext.setYaml(yamlObject);

    Workflow workflow = yamlHandler.upsertFromYaml(changeContext, Arrays.asList(changeContext));
    assertNotNull(workflow);
    assertEquals(workflow.getName(), workflowName);

    MultiServiceWorkflowYaml yaml = yamlHandler.toYaml(workflow, APP_ID);
    assertNotNull(yaml);
    assertEquals(yaml.getType(), "MULTI_SERVICE");

    String yamlContent = getYamlContent(yaml);
    assertNotNull(yamlContent);
    yamlContent = yamlContent.substring(0, yamlContent.length() - 1);
    assertEquals(validYamlContent, yamlContent);

    Workflow savedWorkflow = workflowService.readWorkflowByName(APP_ID, workflowName);
    // TODO find out why this couldn't be called
    //    Workflow savedWorkflow = yamlHandler.get(ACCOUNT_ID, validYamlFilePath);
    assertNotNull(savedWorkflow);
    assertEquals(savedWorkflow.getName(), workflowName);

    yamlHandler.delete(changeContext);

    Workflow deletedWorkflow = yamlHandler.get(ACCOUNT_ID, validYamlFilePath);
    assertNull(deletedWorkflow);
  }

  @Test
  public void testFailures() throws HarnessException, IOException {
    testFailures(validYamlContent, validYamlFilePath, invalidYamlContent, invalidYamlFilePath, yamlHandler,
        MultiServiceWorkflowYaml.class);
  }
}
