/*
 * Copyright 2020 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package software.wings.beans;

import static io.harness.annotations.dev.HarnessTeam.CDC;

import io.harness.annotations.dev.OwnedBy;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@OwnedBy(CDC)
@EqualsAndHashCode(callSuper = true)
public class ResourceConstraintNotification extends Notification {
  @Getter @Setter private String displayText;

  public ResourceConstraintNotification() {
    super(NotificationType.INFORMATION);
  }
}
