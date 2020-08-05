package software.wings.delegatetasks.validation;

import static io.harness.annotations.dev.HarnessTeam.CDC;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import com.google.inject.Inject;

import io.harness.annotations.dev.OwnedBy;
import software.wings.beans.DelegateTaskPackage;
import software.wings.beans.SmbConfig;
import software.wings.service.impl.SmbHelperService;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

@OwnedBy(CDC)
public class SmbValidation extends AbstractDelegateValidateTask {
  @Inject SmbHelperService smbHelperService;
  public SmbValidation(String delegateId, DelegateTaskPackage delegateTaskPackage,
      Consumer<List<DelegateConnectionResult>> postExecute) {
    super(delegateId, delegateTaskPackage, postExecute);
  }

  @Override
  public List<String> getCriteria() {
    return singletonList(Arrays.stream(getParameters())
                             .filter(o -> o instanceof SmbConfig)
                             .map(config -> ((SmbConfig) config).getSmbUrl())
                             .findFirst()
                             .orElse(null));
  }

  @Override
  public List<DelegateConnectionResult> validate() {
    try {
      String criteria = getCriteria().get(0);
      String connectionHost = smbHelperService.getSMBConnectionHost(criteria);
      return singletonList(DelegateConnectionResult.builder()
                               .criteria(criteria)
                               .validated(smbHelperService.isConnectibleSOBServer(connectionHost))
                               .build());
    } catch (Exception e) {
      return emptyList();
    }
  }
}
