package io.harness.ccm.commons.entities;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AzureConfig {
  private String azureAppClientId;
}
