package software.wings.helpers.ext.bamboo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import lombok.Builder;
import lombok.Data;

/**
 * Created by sgurubelli on 8/29/17.
 */
@Data
@Builder
@OwnedBy(HarnessTeam.CDC)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Status {
  boolean finished;
  String prettyQueuedTime;
}
