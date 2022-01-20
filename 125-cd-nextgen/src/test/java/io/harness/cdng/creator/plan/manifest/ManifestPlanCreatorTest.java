/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.cdng.creator.plan.manifest;

import static io.harness.rule.OwnerRule.*;
import static io.harness.rule.OwnerRule.PRASHANTSHARMA;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.category.element.UnitTests;
import io.harness.cdng.CDNGTestBase;
import io.harness.cdng.artifact.bean.yaml.SidecarArtifact;
import io.harness.cdng.manifest.ManifestConfigType;
import io.harness.cdng.manifest.ManifestsListConfigWrapper;
import io.harness.cdng.manifest.yaml.ManifestConfig;
import io.harness.cdng.manifest.yaml.ManifestConfigWrapper;
import io.harness.cdng.service.beans.KubernetesServiceSpec;
import io.harness.cdng.service.beans.ServiceConfig;
import io.harness.cdng.service.beans.ServiceDefinition;
import io.harness.cdng.service.beans.StageOverridesConfig;
import io.harness.cdng.visitor.YamlTypes;
import io.harness.delegate.task.artifacts.ArtifactSourceConstants;
import io.harness.exception.InvalidRequestException;
import io.harness.pms.contracts.plan.Dependency;
import io.harness.pms.plan.creation.PlanCreatorUtils;
import io.harness.pms.sdk.core.plan.creation.beans.PlanCreationContext;
import io.harness.pms.sdk.core.plan.creation.beans.PlanCreationResponse;
import io.harness.pms.yaml.YamlField;
import io.harness.pms.yaml.YamlUtils;
import io.harness.rule.Owner;
import io.harness.serializer.KryoSerializer;

import com.google.inject.Inject;
import com.google.protobuf.ByteString;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.InjectMocks;

@OwnedBy(HarnessTeam.CDC)
public class ManifestPlanCreatorTest extends CDNGTestBase {
  @Inject private KryoSerializer kryoSerializer;
  @Inject @InjectMocks ManifestsPlanCreator manifestsPlanCreator;

  private YamlField getYamlFieldFromGivenFileName(String file) throws IOException {
    ClassLoader classLoader = this.getClass().getClassLoader();
    InputStream yamlFile = classLoader.getResourceAsStream(file);
    assertThat(yamlFile).isNotNull();

    String yaml = new Scanner(yamlFile, "UTF-8").useDelimiter("\\A").next();
    yaml = YamlUtils.injectUuid(yaml);
    YamlField yamlField = YamlUtils.readTree(yaml);
    return yamlField;
  }
  @Test
  @Owner(developers = ACASIAN)
  @Category(UnitTests.class)
  public void shouldNotAllowDuplicateManifestIdentifiers() throws IOException {
    ManifestConfigWrapper k8sManifest =
        ManifestConfigWrapper.builder()
            .manifest(ManifestConfig.builder().identifier("test").type(ManifestConfigType.K8_MANIFEST).build())
            .build();
    ManifestConfigWrapper valuesManifest =
        ManifestConfigWrapper.builder()
            .manifest(ManifestConfig.builder().identifier("test").type(ManifestConfigType.VALUES).build())
            .build();

    ServiceConfig serviceConfig =
        ServiceConfig.builder()
            .serviceDefinition(
                ServiceDefinition.builder()
                    .serviceSpec(
                        KubernetesServiceSpec.builder().manifests(Arrays.asList(k8sManifest, valuesManifest)).build())
                    .build())
            .build();

    Map<String, ByteString> metadataDependency = new HashMap<>();
    metadataDependency.put(
        YamlTypes.SERVICE_CONFIG, ByteString.copyFrom(kryoSerializer.asDeflatedBytes(serviceConfig)));

    Dependency dependency = Dependency.newBuilder().putAllMetadata(metadataDependency).build();
    YamlField manifestsYamlField = getYamlFieldFromGivenFileName("cdng/plan/manifests/manifests.yml");
    PlanCreationContext ctx =
        PlanCreationContext.builder().currentField(manifestsYamlField).dependency(dependency).build();
    assertThatExceptionOfType(InvalidRequestException.class)
        .isThrownBy(() -> manifestsPlanCreator.createPlanForChildrenNodes(ctx, null))
        .withMessageContaining("Duplicate identifier: [test] in manifests");
  }

  @Test
  @Owner(developers = ABOSII)
  @Category(UnitTests.class)
  public void shouldCreateWithProperOrder() throws IOException {
    ServiceConfig serviceConfig =
        ServiceConfig.builder()
            .serviceDefinition(
                ServiceDefinition.builder()
                    .serviceSpec(KubernetesServiceSpec.builder()
                                     .manifests(Arrays.asList(manifestWith("m1", ManifestConfigType.K8_MANIFEST),
                                         manifestWith("m2", ManifestConfigType.VALUES),
                                         manifestWith("m3", ManifestConfigType.VALUES)))
                                     .build())
                    .build())
            .stageOverrides(
                StageOverridesConfig.builder()
                    .manifests(Arrays.asList(manifestWith("m1", ManifestConfigType.K8_MANIFEST),
                        manifestWith("m4", ManifestConfigType.VALUES), manifestWith("m2", ManifestConfigType.VALUES),
                        manifestWith("m5", ManifestConfigType.VALUES), manifestWith("m6", ManifestConfigType.VALUES),
                        manifestWith("m3", ManifestConfigType.VALUES)))
                    .build())
            .build();

    Map<String, ByteString> metadataDependency = new HashMap<>();
    metadataDependency.put(
        YamlTypes.SERVICE_CONFIG, ByteString.copyFrom(kryoSerializer.asDeflatedBytes(serviceConfig)));

    Dependency dependency = Dependency.newBuilder().putAllMetadata(metadataDependency).build();
    YamlField manifestsYamlField = getYamlFieldFromGivenFileName("cdng/plan/manifests/manifests.yml");

    PlanCreationContext ctx =
        PlanCreationContext.builder().currentField(manifestsYamlField).dependency(dependency).build();

    LinkedHashMap<String, PlanCreationResponse> response = manifestsPlanCreator.createPlanForChildrenNodes(ctx, null);
    assertThat(response.size()).isEqualTo(6);
  }

  private ManifestConfigWrapper manifestWith(String identifier, ManifestConfigType type) {
    return ManifestConfigWrapper.builder()
        .manifest(ManifestConfig.builder().identifier(identifier).type(type).build())
        .build();
  }

  @Test
  @Owner(developers = PRASHANTSHARMA)
  @Category(UnitTests.class)
  public void testGetFieldClass() {
    assertThat(manifestsPlanCreator.getFieldClass()).isEqualTo(ManifestsListConfigWrapper.class);
  }

  @Test
  @Owner(developers = PRASHANTSHARMA)
  @Category(UnitTests.class)
  public void testGetSupportedTypes() {
    Map<String, Set<String>> supportedTypes = manifestsPlanCreator.getSupportedTypes();
    assertThat(supportedTypes.containsKey(YamlTypes.MANIFEST_LIST_CONFIG)).isEqualTo(true);
    assertThat(supportedTypes.get(YamlTypes.MANIFEST_LIST_CONFIG).contains(PlanCreatorUtils.ANY_TYPE)).isEqualTo(true);
  }
}
