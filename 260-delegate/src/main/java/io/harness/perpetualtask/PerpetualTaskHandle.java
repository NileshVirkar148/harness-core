/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.perpetualtask;

import io.harness.annotations.dev.HarnessModule;
import io.harness.annotations.dev.TargetModule;

import java.util.concurrent.Future;
import lombok.Value;

@Value
@TargetModule(HarnessModule._420_DELEGATE_AGENT)
public class PerpetualTaskHandle {
  private Future<?> taskHandle;
  private PerpetualTaskLifecycleManager taskLifecycleManager;
}
