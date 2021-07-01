package io.harness.event.handlers;

import static io.harness.springdata.SpringDataMongoUtils.setUnset;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.engine.executions.node.NodeExecutionService;
import io.harness.execution.NodeExecution.NodeExecutionKeys;
import io.harness.pms.contracts.execution.events.HandleProgressRequest;
import io.harness.pms.contracts.execution.events.SdkResponseEventProto;
import io.harness.pms.serializer.recaster.RecastOrchestrationUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bson.Document;

@Singleton
@OwnedBy(HarnessTeam.PIPELINE)
public class HandleProgressRequestProcessor implements SdkResponseProcessor {
  @Inject private NodeExecutionService nodeExecutionService;

  @Override
  public void handleEvent(SdkResponseEventProto event) {
    HandleProgressRequest progressRequest = event.getProgressRequest();
    Document progressDoc = RecastOrchestrationUtils.toDocumentFromJson(progressRequest.getProgressJson());
    nodeExecutionService.update(
        event.getNodeExecutionId(), ops -> setUnset(ops, NodeExecutionKeys.progressData, progressDoc));
  }
}
