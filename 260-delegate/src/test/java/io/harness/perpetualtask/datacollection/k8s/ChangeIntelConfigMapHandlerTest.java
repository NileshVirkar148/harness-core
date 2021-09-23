package io.harness.perpetualtask.datacollection.k8s;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.harness.category.element.UnitTests;
import io.harness.cvng.beans.change.ChangeEventDTO;
import io.harness.cvng.beans.change.KubernetesChangeEventMetadata;
import io.harness.rule.Owner;
import io.harness.rule.OwnerRule;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1ConfigMapBuilder;
import io.kubernetes.client.openapi.models.V1OwnerReferenceBuilder;
import java.time.Instant;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class ChangeIntelConfigMapHandlerTest extends ChangeIntelHandlerTestBase {
  private ChangeIntelConfigMapHandler handler;
  @Before
  public void setup() throws Exception {
    super.setup();
    handler = ChangeIntelConfigMapHandler.builder()
                  .accountId(accountId)
                  .dataCollectionInfo(dataCollectionInfo)
                  .k8sHandlerUtils(new K8sHandlerUtils())
                  .build();
    FieldUtils.writeField(handler, "cvNextGenServiceClient", cvNextGenServiceClient, true);
    FieldUtils.writeField(handler, "cvngRequestExecutor", cvngRequestExecutor, true);
  }

  @Test
  @Owner(developers = OwnerRule.PRAVEEN)
  @Category({UnitTests.class})
  public void testOnAdd() {
    V1ConfigMap configMap = buildConfigMap();
    handler.onAdd(configMap);
    ChangeEventDTO eventDTO = verifyAndValidate();
    assertThat(eventDTO.getMetadata()).isNotNull();
    KubernetesChangeEventMetadata changeEventMetadata = (KubernetesChangeEventMetadata) eventDTO.getMetadata();
    assertThat(changeEventMetadata.getAction().name()).isEqualTo(KubernetesChangeEventMetadata.Action.Add.name());
    assertThat(changeEventMetadata.getResourceType().name())
        .isEqualTo(KubernetesChangeEventMetadata.KubernetesResourceType.ConfigMap.name());
    assertThat(changeEventMetadata.getNewYaml()).isNotEmpty();
    assertThat(changeEventMetadata.getNamespace()).isEqualTo(configMap.getMetadata().getNamespace());
    assertThat(changeEventMetadata.getWorkload()).isEqualTo(configMap.getMetadata().getName());
  }

  @Test
  @Owner(developers = OwnerRule.PRAVEEN)
  @Category({UnitTests.class})
  public void testOnAdd_withOwnerReference() {
    V1ConfigMap configMap = buildConfigMap();
    configMap.getMetadata().addOwnerReferencesItem(new V1OwnerReferenceBuilder().withController(true).build());
    handler.onAdd(configMap);
    verify(cvNextGenServiceClient, times(0)).saveChangeEvent(anyString(), any());
  }

  @Test
  @Owner(developers = OwnerRule.PRAVEEN)
  @Category({UnitTests.class})
  public void testOnUpdate() {
    V1ConfigMap oldConfigMap = buildConfigMap();
    V1ConfigMap newConfigMap = buildConfigMap();
    newConfigMap.getData().put("key2", "value2");
    handler.onUpdate(oldConfigMap, newConfigMap);
    ChangeEventDTO eventDTO = verifyAndValidate();
    assertThat(eventDTO.getMetadata()).isNotNull();
    KubernetesChangeEventMetadata changeEventMetadata = (KubernetesChangeEventMetadata) eventDTO.getMetadata();
    assertThat(changeEventMetadata.getAction().name()).isEqualTo(KubernetesChangeEventMetadata.Action.Update.name());
    assertThat(changeEventMetadata.getResourceType().name())
        .isEqualTo(KubernetesChangeEventMetadata.KubernetesResourceType.ConfigMap.name());
    assertThat(changeEventMetadata.getOldYaml()).isNotEmpty();
    assertThat(changeEventMetadata.getNewYaml()).isNotEmpty();
    assertThat(changeEventMetadata.getNamespace()).isEqualTo(newConfigMap.getMetadata().getNamespace());
    assertThat(changeEventMetadata.getWorkload()).isEqualTo(newConfigMap.getMetadata().getName());
  }

  @Test
  @Owner(developers = OwnerRule.PRAVEEN)
  @Category({UnitTests.class})
  public void testOnUpdate_noChanges() {
    V1ConfigMap configMap = buildConfigMap();
    handler.onUpdate(configMap, configMap);
    verify(cvNextGenServiceClient, times(0)).saveChangeEvent(anyString(), any());
  }

  @Test
  @Owner(developers = OwnerRule.PRAVEEN)
  @Category({UnitTests.class})
  public void testOnDelete() {
    V1ConfigMap configMap = buildConfigMap();
    handler.onDelete(configMap, false);
    ChangeEventDTO eventDTO = verifyAndValidate();
    assertThat(eventDTO.getMetadata()).isNotNull();
    KubernetesChangeEventMetadata changeEventMetadata = (KubernetesChangeEventMetadata) eventDTO.getMetadata();
    assertThat(changeEventMetadata.getAction().name()).isEqualTo(KubernetesChangeEventMetadata.Action.Delete.name());
    assertThat(changeEventMetadata.getResourceType().name())
        .isEqualTo(KubernetesChangeEventMetadata.KubernetesResourceType.ConfigMap.name());
    assertThat(changeEventMetadata.getOldYaml()).isNotEmpty();
    assertThat(changeEventMetadata.getNamespace()).isEqualTo(configMap.getMetadata().getNamespace());
    assertThat(changeEventMetadata.getWorkload()).isEqualTo(configMap.getMetadata().getName());
  }

  @Test
  @Owner(developers = OwnerRule.PRAVEEN)
  @Category({UnitTests.class})
  public void testOnDelete_finalStateUnknown() {
    V1ConfigMap configMap = buildConfigMap();
    handler.onDelete(configMap, true);
    verify(cvNextGenServiceClient, times(0)).saveChangeEvent(anyString(), any());
  }

  private V1ConfigMap buildConfigMap() {
    return new V1ConfigMapBuilder()
        .withNewMetadata()
        .withName("test-name")
        .withNamespace("test-namespace")
        .withNewCreationTimestamp(Instant.now().toEpochMilli())
        .withUid("test-uid")
        .endMetadata()
        .withData(Maps.newHashMap(ImmutableMap.of("key1", "value1")))
        .build();
  }
}