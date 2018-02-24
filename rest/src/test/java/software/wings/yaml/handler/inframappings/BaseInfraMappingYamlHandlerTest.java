package software.wings.yaml.handler.inframappings;

import static java.util.Collections.singletonList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static software.wings.beans.Application.Builder.anApplication;
import static software.wings.beans.Environment.Builder.anEnvironment;
import static software.wings.beans.Service.Builder.aService;
import static software.wings.beans.ServiceTemplate.Builder.aServiceTemplate;
import static software.wings.beans.SettingAttribute.Builder.aSettingAttribute;
import static software.wings.beans.yaml.YamlConstants.PATH_DELIMITER;
import static software.wings.utils.WingsTestConstants.ACCOUNT_ID;
import static software.wings.utils.WingsTestConstants.APP_ID;
import static software.wings.utils.WingsTestConstants.APP_NAME;
import static software.wings.utils.WingsTestConstants.ENV_ID;
import static software.wings.utils.WingsTestConstants.ENV_NAME;
import static software.wings.utils.WingsTestConstants.HOST_CONN_ATTR_ID;
import static software.wings.utils.WingsTestConstants.SERVICE_ID;
import static software.wings.utils.WingsTestConstants.SERVICE_NAME;
import static software.wings.utils.WingsTestConstants.SETTING_ID;
import static software.wings.utils.WingsTestConstants.TEMPLATE_ID;

import org.mockito.Mock;
import org.mongodb.morphia.Key;
import software.wings.beans.AwsConfig;
import software.wings.beans.Environment;
import software.wings.beans.Service;
import software.wings.beans.ServiceTemplate;
import software.wings.beans.SettingAttribute;
import software.wings.beans.yaml.ChangeContext;
import software.wings.beans.yaml.GitFileChange;
import software.wings.beans.yaml.YamlType;
import software.wings.service.impl.yaml.handler.BaseYamlHandler;
import software.wings.service.impl.yaml.handler.YamlHandlerFactory;
import software.wings.service.impl.yaml.service.YamlHelper;
import software.wings.service.intfc.AppService;
import software.wings.service.intfc.EnvironmentService;
import software.wings.service.intfc.ServiceResourceService;
import software.wings.service.intfc.ServiceTemplateService;
import software.wings.service.intfc.SettingsService;
import software.wings.utils.ArtifactType;
import software.wings.yaml.BaseYaml;
import software.wings.yaml.handler.BaseYamlHandlerTest;

public class BaseInfraMappingYamlHandlerTest extends BaseYamlHandlerTest {
  protected Service service = aService()
                                  .withName(SERVICE_NAME)
                                  .withAppId(APP_ID)
                                  .withUuid(SERVICE_ID)
                                  .withArtifactType(ArtifactType.DOCKER)
                                  .build();

  protected Environment environment = anEnvironment().withName(ENV_NAME).withAppId(APP_ID).withUuid(ENV_ID).build();

  @Mock protected YamlHelper yamlHelper;
  @Mock protected YamlHandlerFactory yamlHandlerFactory;

  @Mock protected AppService appService;
  @Mock protected ServiceResourceService serviceResourceService;
  @Mock protected ServiceTemplateService serviceTemplateService;
  @Mock protected EnvironmentService environmentService;
  @Mock protected SettingsService settingsService;
  private SettingAttribute setting = aSettingAttribute()
                                         .withUuid(SETTING_ID)
                                         .withAccountId(ACCOUNT_ID)
                                         .withName("aws")
                                         .withValue(AwsConfig.builder().build())
                                         .build();

  private SettingAttribute connAttr =
      aSettingAttribute().withUuid(HOST_CONN_ATTR_ID).withAccountId(ACCOUNT_ID).withName("Wings Key").build();

  protected void setup(String yamlFilePath, String infraMappingName) {
    when(appService.getAppByName(anyString(), anyString()))
        .thenReturn(anApplication().withName(APP_NAME).withUuid(APP_ID).build());

    when(yamlHelper.getAppId(anyString(), anyString())).thenReturn(APP_ID);
    when(yamlHelper.getEnvironmentId(anyString(), anyString())).thenReturn(ENV_ID);
    when(yamlHelper.getNameFromYamlFilePath(yamlFilePath)).thenReturn(infraMappingName);
    when(yamlHelper.extractEntityNameFromYamlPath(
             YamlType.INFRA_MAPPING.getPathExpression(), yamlFilePath, PATH_DELIMITER))
        .thenReturn(infraMappingName);

    when(environmentService.getEnvironmentByName(anyString(), anyString())).thenReturn(environment);
    when(environmentService.get(APP_ID, ENV_ID, false)).thenReturn(environment);

    when(serviceResourceService.getServiceByName(anyString(), anyString())).thenReturn(service);
    when(serviceResourceService.get(anyString(), anyString())).thenReturn(service);
    when(serviceTemplateService.getTemplateRefKeysByService(APP_ID, SERVICE_ID, ENV_ID))
        .thenReturn(singletonList(new Key<>(ServiceTemplate.class, "serviceTemplate", TEMPLATE_ID)));
    when(serviceTemplateService.get(APP_ID, TEMPLATE_ID)).thenReturn(aServiceTemplate().withUuid(TEMPLATE_ID).build());

    when(settingsService.getByName(ACCOUNT_ID, APP_ID, "aws")).thenReturn(setting);
    when(settingsService.get(SETTING_ID)).thenReturn(setting);
    when(settingsService.getSettingAttributeByName(ACCOUNT_ID, "Wings Key")).thenReturn(connAttr);
    when(settingsService.get(HOST_CONN_ATTR_ID)).thenReturn(connAttr);
  }

  protected <Y extends BaseYaml, H extends BaseYamlHandler> ChangeContext<Y> getChangeContext(
      String yamlContent, String yamlFilePath, H yamlHandler) {
    // Invalid yaml path
    GitFileChange gitFileChange = GitFileChange.Builder.aGitFileChange()
                                      .withAccountId(ACCOUNT_ID)
                                      .withFilePath(yamlFilePath)
                                      .withFileContent(yamlContent)
                                      .build();

    ChangeContext<Y> changeContext = new ChangeContext();
    changeContext.setChange(gitFileChange);
    changeContext.setYamlType(YamlType.INFRA_MAPPING);
    changeContext.setYamlSyncHandler(yamlHandler);
    return changeContext;
  }
}
