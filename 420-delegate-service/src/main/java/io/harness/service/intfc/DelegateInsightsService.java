/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Shield 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/06/PolyForm-Shield-1.0.0.txt.
 */

package io.harness.service.intfc;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.delegate.beans.DelegateInsightsDetails;

@OwnedBy(HarnessTeam.DEL)
public interface DelegateInsightsService {
  DelegateInsightsDetails retrieveDelegateInsightsDetails(
      String accountId, String delegateGroupId, long startTimestamp);
}
