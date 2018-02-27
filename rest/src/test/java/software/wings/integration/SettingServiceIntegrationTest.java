package software.wings.integration;

import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static software.wings.beans.ResponseMessage.Level.ERROR;
import static software.wings.beans.ResponseMessage.aResponseMessage;
import static software.wings.beans.SettingAttribute.Builder.aSettingAttribute;

import io.harness.rule.RepeatRule.Repeat;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import software.wings.beans.BambooConfig;
import software.wings.beans.DockerConfig;
import software.wings.beans.ErrorCode;
import software.wings.beans.JenkinsConfig;
import software.wings.beans.RestResponse;
import software.wings.beans.SettingAttribute;
import software.wings.beans.SettingAttribute.Category;
import software.wings.beans.config.NexusConfig;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

/**
 * Created by anubhaw on 5/3/17.
 */
public class SettingServiceIntegrationTest extends BaseIntegrationTest {
  @Before
  public void setUp() throws Exception {
    loginAdminUser();
  }

  @Test
  @Repeat(times = 5)
  public void shouldSaveJenkinsConfig() {
    RestResponse<SettingAttribute> restResponse =
        getRequestBuilderWithAuthHeader(getListWebTarget(accountId))
            .post(entity(aSettingAttribute()
                             .withName("Wings Jenkins" + System.currentTimeMillis())
                             .withCategory(Category.CONNECTOR)
                             .withAccountId(accountId)
                             .withValue(JenkinsConfig.builder()
                                            .accountId(accountId)
                                            .jenkinsUrl(JENKINS_URL)
                                            .username(JENKINS_USERNAME)
                                            .password(JENKINS_PASSWORD)
                                            .build())
                             .build(),
                      APPLICATION_JSON),
                new GenericType<RestResponse<SettingAttribute>>() {});

    assertThat(restResponse.getResource())
        .isInstanceOf(SettingAttribute.class)
        .extracting("value")
        .extracting("jenkinsUrl", "username", "password", "accountId")
        .contains(tuple(JENKINS_URL, JENKINS_USERNAME, null, accountId));
  }

  @Test
  public void shouldThrowExceptionForUnreachableJenkinsUrl() {
    Response response = getRequestBuilderWithAuthHeader(getListWebTarget(accountId))
                            .post(entity(aSettingAttribute()
                                             .withName("Wings Jenkins" + System.currentTimeMillis())
                                             .withCategory(Category.CONNECTOR)
                                             .withAccountId(accountId)
                                             .withValue(JenkinsConfig.builder()
                                                            .accountId(accountId)
                                                            .jenkinsUrl("BAD_URL")
                                                            .username(JENKINS_USERNAME)
                                                            .password(JENKINS_PASSWORD)
                                                            .build())
                                             .build(),
                                APPLICATION_JSON));

    assertThat(response.getStatus()).isEqualTo(400);
    assertThat(response.readEntity(RestResponse.class).getResponseMessages())
        .containsExactly(aResponseMessage()
                             .code(ErrorCode.INVALID_ARTIFACT_SERVER)
                             .message("Jenkins URL must be a valid URL")
                             .level(ERROR)
                             .build());
  }

  @Test
  public void shouldSaveNexusConfig() {
    RestResponse<SettingAttribute> restResponse =
        getRequestBuilderWithAuthHeader(getListWebTarget(accountId))
            .post(entity(aSettingAttribute()
                             .withName("Wings Nexus" + System.currentTimeMillis())
                             .withCategory(Category.CONNECTOR)
                             .withAccountId(accountId)
                             .withValue(NexusConfig.builder()
                                            .nexusUrl(NEXUS_URL)
                                            .username(NEXUS_USERNAME)
                                            .password(NEXUS_PASSWORD)
                                            .accountId(accountId)
                                            .build())
                             .build(),
                      APPLICATION_JSON),
                new GenericType<RestResponse<SettingAttribute>>() {});
    assertThat(restResponse.getResource())
        .isInstanceOf(SettingAttribute.class)
        .extracting("value")
        .extracting("nexusUrl", "username", "password")
        .contains(tuple(NEXUS_URL, NEXUS_USERNAME, null));
  }

  @Test
  public void shouldSaveBambooConfig() {
    RestResponse<SettingAttribute> restResponse =
        getRequestBuilderWithAuthHeader(getListWebTarget(accountId))
            .post(entity(aSettingAttribute()
                             .withName("Wings Bamboo" + System.currentTimeMillis())
                             .withCategory(Category.CONNECTOR)
                             .withAccountId(accountId)
                             .withValue(BambooConfig.builder()
                                            .accountId(accountId)
                                            .bambooUrl(BAMBOO_URL)
                                            .username(BAMBOO_USERNAME)
                                            .password(BAMBOO_PASSWORD)
                                            .build())
                             .build(),
                      APPLICATION_JSON),
                new GenericType<RestResponse<SettingAttribute>>() {});
    assertThat(restResponse.getResource())
        .isInstanceOf(SettingAttribute.class)
        .extracting("value")
        .extracting("bambooUrl", "username", "password", "accountId")
        .contains(tuple(BAMBOO_URL, BAMBOO_USERNAME, null, accountId));
  }

  @Test
  @Ignore
  @Repeat(times = 5, successes = 1)
  public void shouldSaveDockerConfig() {
    RestResponse<SettingAttribute> restResponse =
        getRequestBuilderWithAuthHeader(getListWebTarget(accountId))
            .post(entity(aSettingAttribute()
                             .withName("Wings Docker Registry" + System.currentTimeMillis())
                             .withCategory(Category.CONNECTOR)
                             .withAccountId(accountId)
                             .withValue(DockerConfig.builder()
                                            .accountId(accountId)
                                            .dockerRegistryUrl(DOCKER_REGISTRY_URL)
                                            .username(DOCKER_USERNAME)
                                            .password(DOCKER_PASSOWRD)
                                            .build())
                             .build(),
                      APPLICATION_JSON),
                new GenericType<RestResponse<SettingAttribute>>() {});
    assertThat(restResponse.getResource())
        .isInstanceOf(SettingAttribute.class)
        .extracting("value")
        .extracting("dockerRegistryUrl", "username", "password", "accountId")
        .contains(tuple(DOCKER_REGISTRY_URL, DOCKER_USERNAME, null, accountId));
  }

  private WebTarget getListWebTarget(String accountId) {
    return client.target(String.format("%s/settings/?accountId=%s", API_BASE, accountId));
  }
}
