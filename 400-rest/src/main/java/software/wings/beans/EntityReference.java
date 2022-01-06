/*
 * Copyright 2020 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package software.wings.beans;

import lombok.Builder;
import lombok.Data;

/**
 * Generic entity reference
 * @author rktummala on 12/08/17
 */
@Data
@Builder
public class EntityReference {
  private String id;
  private String name;
  private String appId;
  private String entityType;
}
