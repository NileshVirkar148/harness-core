package software.wings.service.impl.yaml.handler.setting.verificationprovider;

import software.wings.beans.DynaTraceConfig;
import software.wings.beans.DynaTraceYaml;
import software.wings.beans.SettingAttribute;
import software.wings.beans.yaml.ChangeContext;

import java.util.List;

/**
 * Created by rsingh on 2/12/18.
 */
public class DynaTraceConfigYamlHandler extends VerificationProviderYamlHandler<DynaTraceYaml, DynaTraceConfig> {
  @Override
  public DynaTraceYaml toYaml(SettingAttribute settingAttribute, String appId) {
    DynaTraceConfig config = (DynaTraceConfig) settingAttribute.getValue();

    DynaTraceYaml yaml = DynaTraceYaml.builder()
                             .harnessApiVersion(getHarnessApiVersion())
                             .type(config.getType())
                             .apiToken(getEncryptedYamlRef(config.getAccountId(), config.getEncryptedApiToken()))
                             .dynaTraceUrl(config.getDynaTraceUrl())
                             .build();
    toYaml(yaml, settingAttribute, appId);
    return yaml;
  }

  @Override
  protected SettingAttribute toBean(
      SettingAttribute previous, ChangeContext<DynaTraceYaml> changeContext, List<ChangeContext> changeSetContext) {
    String uuid = previous != null ? previous.getUuid() : null;
    DynaTraceYaml yaml = changeContext.getYaml();
    String accountId = changeContext.getChange().getAccountId();

    DynaTraceConfig config = DynaTraceConfig.builder()
                                 .accountId(accountId)
                                 .dynaTraceUrl(yaml.getDynaTraceUrl())
                                 .encryptedApiToken(yaml.getApiToken())
                                 .build();

    return buildSettingAttribute(accountId, changeContext.getChange().getFilePath(), uuid, config);
  }

  @Override
  public Class getYamlClass() {
    return DynaTraceYaml.class;
  }
}
