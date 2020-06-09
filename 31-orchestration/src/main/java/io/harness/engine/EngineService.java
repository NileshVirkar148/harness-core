package io.harness.engine;

import static io.harness.annotations.dev.HarnessTeam.CDC;

import io.harness.annotations.Redesign;
import io.harness.annotations.dev.OwnedBy;
import io.harness.beans.EmbeddedUser;
import io.harness.execution.PlanExecution;
import io.harness.plan.Plan;
import io.harness.plan.input.InputArgs;

import javax.validation.Valid;

@OwnedBy(CDC)
@Redesign
public interface EngineService {
  PlanExecution startExecution(@Valid Plan plan, EmbeddedUser createdBy);
  PlanExecution startExecution(@Valid Plan plan, InputArgs inputArgs, EmbeddedUser createdBy);
}
