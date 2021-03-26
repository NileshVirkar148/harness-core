package software.wings.infra;

import static io.harness.annotations.dev.HarnessTeam.CDP;

import static software.wings.beans.AmiDeploymentType.AWS_ASG;
import static software.wings.beans.AwsAmiInfrastructureMapping.Builder.anAwsAmiInfrastructureMapping;
import static software.wings.beans.InfrastructureType.AWS_AMI;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;

import io.harness.annotations.dev.OwnedBy;
import io.harness.data.structure.EmptyPredicate;
import io.harness.exception.InvalidRequestException;

import software.wings.annotation.IncludeFieldMap;
import software.wings.api.CloudProviderType;
import software.wings.beans.AmiDeploymentType;
import software.wings.beans.AwsAmiInfrastructureMapping;
import software.wings.beans.InfrastructureMapping;
import software.wings.beans.InfrastructureMappingType;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.lang3.StringUtils;

@JsonTypeName("AWS_AMI")
@Data
@Builder
@FieldNameConstants(innerTypeName = "AwsAmiInfrastructureKeys")
@OwnedBy(CDP)
public class AwsAmiInfrastructure
    implements InfraMappingInfrastructureProvider, FieldKeyValMapProvider, ProvisionerAware {
  private String cloudProviderId;
  @IncludeFieldMap private String region;

  // Base AutoScaling Group
  private String autoScalingGroupName;
  private List<String> classicLoadBalancers;
  private List<String> targetGroupArns;
  private String hostNameConvention;

  // Variables for B/G type Ami deployment
  private List<String> stageClassicLoadBalancers;
  private List<String> stageTargetGroupArns;

  // Right now ONLY regular Asg OR SpotInst
  // This field can't be modified once Infra is created
  @IncludeFieldMap @Builder.Default private AmiDeploymentType amiDeploymentType = AWS_ASG;

  // Variables used for SpotInst Deployment type
  private String spotinstElastiGroupJson;
  private String spotinstCloudProvider;
  private boolean asgIdentifiesWorkload;

  private boolean useTrafficShift;

  private Map<String, String> expressions;

  public AmiDeploymentType getAmiDeploymentType() {
    return amiDeploymentType != null ? amiDeploymentType : AmiDeploymentType.AWS_ASG;
  }

  @Override
  public InfrastructureMapping getInfraMapping() {
    return anAwsAmiInfrastructureMapping()
        .withComputeProviderSettingId(cloudProviderId)
        .withInfraMappingType(InfrastructureMappingType.AWS_AMI.name())
        .withRegion(region)
        .withAutoScalingGroupName(autoScalingGroupName)
        .withClassicLoadBalancers(classicLoadBalancers)
        .withTargetGroupArns(targetGroupArns)
        .withHostNameConvention(hostNameConvention)
        .withStageClassicLoadBalancers(stageClassicLoadBalancers)
        .withStageTargetGroupArns(stageTargetGroupArns)
        .withInfraMappingType(InfrastructureMappingType.AWS_AMI.name())
        .withAmiDeploymentType(getAmiDeploymentType())
        .withSpotinstCloudProvider(spotinstCloudProvider)
        .withSpotinstElastiGroupJson(spotinstElastiGroupJson)
        .build();
  }

  @Override
  public Class<AwsAmiInfrastructureMapping> getMappingClass() {
    return AwsAmiInfrastructureMapping.class;
  }

  @Override
  public String getInfrastructureType() {
    return AWS_AMI;
  }

  @Override
  public Set<String> getSupportedExpressions() {
    return ImmutableSet.of(AwsAmiInfrastructureKeys.region, AwsAmiInfrastructureKeys.autoScalingGroupName,
        AwsAmiInfrastructureKeys.classicLoadBalancers, AwsAmiInfrastructureKeys.targetGroupArns,
        AwsAmiInfrastructureKeys.stageClassicLoadBalancers, AwsAmiInfrastructureKeys.stageTargetGroupArns);
  }

  @Override
  public void applyExpressions(
      Map<String, Object> resolvedExpressions, String appId, String envId, String infraDefinitionId) {
    if (AWS_ASG != getAmiDeploymentType()) {
      // Should never happen
      throw new InvalidRequestException("Provisioning ONLY supported for AWS_ASG type AMI deployments");
    }
    // Clear the existing values
    setRegion(StringUtils.EMPTY);
    setAutoScalingGroupName(StringUtils.EMPTY);
    setClassicLoadBalancers(emptyList());
    setTargetGroupArns(emptyList());
    setStageClassicLoadBalancers(emptyList());
    setStageTargetGroupArns(emptyList());

    for (Entry<String, Object> entry : resolvedExpressions.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();
      switch (key) {
        case "region": {
          setRegion((String) value);
          break;
        }
        case "autoScalingGroupName": {
          setAutoScalingGroupName((String) value);
          break;
        }
        case "classicLoadBalancers": {
          setClassicLoadBalancers(getList(value));
          break;
        }
        case "targetGroupArns": {
          setTargetGroupArns(getList(value));
          break;
        }
        case "stageClassicLoadBalancers": {
          setStageClassicLoadBalancers(getList(value));
          break;
        }
        case "stageTargetGroupArns": {
          setStageTargetGroupArns(getList(value));
          break;
        }
        default: {
          throw new InvalidRequestException(format("Unknown expression : [%s]", entry.getKey()));
        }
      }
    }
    if (EmptyPredicate.isEmpty(getRegion())) {
      throw new InvalidRequestException("Region is required");
    }
    if (EmptyPredicate.isEmpty(getAutoScalingGroupName())) {
      throw new InvalidRequestException("Base Asg is required");
    }
  }

  @Override
  public Set<String> getUserDefinedUniqueInfraFields() {
    return isAsgIdentifiesWorkload()
        ? new HashSet<>(Collections.singletonList(AwsAmiInfrastructureKeys.autoScalingGroupName))
        : emptySet();
  }

  @Override
  public CloudProviderType getCloudProviderType() {
    return CloudProviderType.AWS;
  }
}
