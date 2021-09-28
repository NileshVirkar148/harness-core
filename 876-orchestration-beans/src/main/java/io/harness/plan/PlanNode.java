package io.harness.plan;

import static io.harness.annotations.dev.HarnessTeam.PIPELINE;

import io.harness.annotations.dev.OwnedBy;
import io.harness.pms.contracts.advisers.AdviserObtainment;
import io.harness.pms.contracts.facilitators.FacilitatorObtainment;
import io.harness.pms.contracts.plan.PlanNodeProto;
import io.harness.pms.contracts.refobjects.RefObject;
import io.harness.pms.contracts.steps.SkipType;
import io.harness.pms.contracts.steps.StepType;
import io.harness.pms.data.OrchestrationMap;
import io.harness.pms.data.stepparameters.PmsStepParameters;
import io.harness.timeout.contracts.TimeoutObtainment;

import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.TypeAlias;

@Value
@Builder(toBuilder = true)
@FieldNameConstants(innerTypeName = "PlanNodeKeys")
@OwnedBy(PIPELINE)
@TypeAlias("planNode")
public class PlanNode implements Node {
  // Identifiers
  @NotNull String uuid;
  @NotNull String name;
  @NotNull StepType stepType;
  @NotNull String identifier;
  String group;

  // Input/Outputs
  PmsStepParameters stepParameters;
  OrchestrationMap stepInputs;
  @Singular List<RefObject> refObjects;

  // Hooks
  @Singular List<AdviserObtainment> adviserObtainments;
  @Singular List<FacilitatorObtainment> facilitatorObtainments;
  @Singular List<TimeoutObtainment> timeoutObtainments;

  String serviceName;

  // Skip
  String skipCondition;
  String whenCondition;

  // Config
  boolean skipExpressionChain;
  @Builder.Default SkipType skipGraphType = SkipType.NOOP;
  boolean skipUnresolvedExpressionsCheck;

  public static PlanNode fromPlanNodeProto(PlanNodeProto planNodeProto) {
    if (planNodeProto == null) {
      return null;
    }
    return PlanNode.builder()
        .uuid(planNodeProto.getUuid())
        .name(planNodeProto.getName())
        .stepType(planNodeProto.getStepType())
        .identifier(planNodeProto.getIdentifier())
        .group(planNodeProto.getGroup())
        .stepParameters(PmsStepParameters.parse(planNodeProto.getStepParameters()))
        .refObjects(planNodeProto.getRebObjectsList())
        .adviserObtainments(planNodeProto.getAdviserObtainmentsList())
        .facilitatorObtainments(planNodeProto.getFacilitatorObtainmentsList())
        .timeoutObtainments(planNodeProto.getTimeoutObtainmentsList())
        .skipCondition(planNodeProto.getSkipCondition())
        .whenCondition(planNodeProto.getWhenCondition())
        .skipExpressionChain(planNodeProto.getSkipExpressionChain())
        .skipGraphType(planNodeProto.getSkipType())
        .skipUnresolvedExpressionsCheck(planNodeProto.getSkipUnresolvedExpressionsCheck())
        .serviceName(planNodeProto.getServiceName())
        .stepInputs(OrchestrationMap.parse(planNodeProto.getStepInputs()))
        .build();
  }

  @Override
  public NodeType getNodeType() {
    return NodeType.PLAN_NODE;
  }
}