/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Shield 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/06/PolyForm-Shield-1.0.0.txt.
 */

package io.harness.cdng.creator.plan.manifest;

import com.google.inject.Inject;
import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.cdng.artifact.steps.ArtifactStep;
import io.harness.cdng.creator.plan.PlanCreatorConstants;
import io.harness.cdng.manifest.ManifestType;
import io.harness.cdng.manifest.steps.ManifestStep;
import io.harness.cdng.manifest.steps.ManifestStepParameters;
import io.harness.cdng.manifest.yaml.ManifestConfig;
import io.harness.cdng.visitor.YamlTypes;
import io.harness.data.structure.UUIDGenerator;
import io.harness.pms.contracts.facilitators.FacilitatorObtainment;
import io.harness.pms.contracts.facilitators.FacilitatorType;
import io.harness.pms.execution.OrchestrationFacilitatorType;
import io.harness.pms.sdk.core.plan.PlanNode;
import io.harness.pms.sdk.core.plan.creation.beans.PlanCreationContext;
import io.harness.pms.sdk.core.plan.creation.beans.PlanCreationResponse;
import io.harness.pms.sdk.core.plan.creation.creators.PartialPlanCreator;
import io.harness.serializer.KryoSerializer;

import java.util.*;

@OwnedBy(HarnessTeam.CDC)
public class IndividualManifestPlanCreator implements PartialPlanCreator<ManifestConfig> {
  @Inject KryoSerializer kryoSerializer;
  @Override
  public Class<ManifestConfig> getFieldClass() {
    return ManifestConfig.class;
  }

  @Override
  public Map<String, Set<String>> getSupportedTypes() {
    return Collections.singletonMap(YamlTypes.MANIFEST_CONFIG,
        new HashSet<>(Arrays.asList(ManifestType.K8Manifest, ManifestType.K8Manifest,
                ManifestType.VALUES,ManifestType.OpenshiftTemplate,ManifestType.OpenshiftTemplate,ManifestType.KustomizePatches,ManifestType.Kustomize,ManifestType.HelmChart,ManifestType.CONFIG_FILE)));
  }

  @Override
  public PlanCreationResponse createPlanForField(PlanCreationContext ctx, ManifestConfig manifestConfig) {
    String manifestId = (String) kryoSerializer.asInflatedObject(
        ctx.getDependency().getMetadataMap().get(YamlTypes.UUID).toByteArray());
    ManifestStepParameters stepParameters = (ManifestStepParameters) kryoSerializer.asInflatedObject(
        ctx.getDependency().getMetadataMap().get(PlanCreatorConstants.MANIFEST_STEP_PARAMETER).toByteArray());

    PlanNode manifestPlanNode =
            PlanNode.builder()
                    .uuid(manifestId)
                    .stepType(ManifestStep.STEP_TYPE)
                    .name(PlanCreatorConstants.MANIFEST_NODE_NAME)
                    .identifier(stepParameters.getIdentifier())
                    .stepParameters(stepParameters)
                    .facilitatorObtainment(
                            FacilitatorObtainment.newBuilder()
                                    .setType(FacilitatorType.newBuilder().setType(OrchestrationFacilitatorType.SYNC).build())
                                    .build())
                    .skipExpressionChain(false)
                    .build();

    return PlanCreationResponse.builder().planNode(manifestPlanNode).build();
  }
}