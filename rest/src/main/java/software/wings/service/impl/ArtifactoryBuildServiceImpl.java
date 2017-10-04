package software.wings.service.impl;

import static software.wings.utils.HttpUtil.connectableHttpUrl;
import static software.wings.utils.HttpUtil.validUrl;
import static software.wings.utils.Validator.equalCheck;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.wings.beans.ErrorCode;
import software.wings.beans.artifact.ArtifactStreamAttributes;
import software.wings.beans.artifact.ArtifactStreamType;
import software.wings.beans.config.ArtifactoryConfig;
import software.wings.exception.WingsException;
import software.wings.helpers.ext.artifactory.ArtifactoryService;
import software.wings.helpers.ext.jenkins.BuildDetails;
import software.wings.helpers.ext.jenkins.JobDetails;
import software.wings.service.intfc.ArtifactoryBuildService;
import software.wings.utils.ArtifactType;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by sgurubelli on 6/28/17.
 */
@Singleton
public class ArtifactoryBuildServiceImpl implements ArtifactoryBuildService {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Inject private ArtifactoryService artifactoryService;

  @Override
  public List<BuildDetails> getBuilds(
      String appId, ArtifactStreamAttributes artifactStreamAttributes, ArtifactoryConfig artifactoryConfig) {
    equalCheck(artifactStreamAttributes.getArtifactStreamType(), ArtifactStreamType.ARTIFACTORY.name());
    if (artifactStreamAttributes.getArtifactType().equals(ArtifactType.DOCKER)) {
      return artifactoryService.getBuilds(
          artifactoryConfig, artifactStreamAttributes.getJobName(), artifactStreamAttributes.getImageName(), 50);
    } else {
      return artifactoryService.getFilePaths(artifactoryConfig, artifactStreamAttributes.getJobName(),
          artifactStreamAttributes.getGroupId(), artifactStreamAttributes.getArtifactPattern(),
          artifactStreamAttributes.getArtifactType(), 50);
    }
  }

  @Override
  public List<JobDetails> getJobs(ArtifactoryConfig config, Optional<String> parentJobName) {
    return null;
  }

  @Override
  public List<String> getArtifactPaths(String jobName, String groupId, ArtifactoryConfig config) {
    if (StringUtils.isEmpty(groupId)) {
      logger.info("Retrieving {} repo paths.", jobName);
      List<String> repoPaths = artifactoryService.getRepoPaths(config, jobName);
      logger.info("Retrieved {} repo paths.", repoPaths.size());
      return repoPaths;
    } else {
      return artifactoryService.getArtifactIds(config, jobName, groupId);
    }
  }

  @Override
  public BuildDetails getLastSuccessfulBuild(
      String appId, ArtifactStreamAttributes artifactStreamAttributes, ArtifactoryConfig artifactoryConfig) {
    return artifactoryService.getLatestVersion(artifactoryConfig, artifactStreamAttributes.getJobName(),
        artifactStreamAttributes.getGroupId(), artifactStreamAttributes.getArtifactName());
  }

  @Override
  public Map<String, String> getPlans(ArtifactoryConfig config) {
    return artifactoryService.getRepositories(config);
  }
  @Override
  public Map<String, String> getPlans(ArtifactoryConfig config, ArtifactType artifactType) {
    return artifactoryService.getRepositories(config, artifactType);
  }

  @Override
  public List<String> getGroupIds(String repoType, ArtifactoryConfig config) {
    logger.info("Retrieving {} Group Ids.", repoType);
    List<String> repoPaths = artifactoryService.getRepoPaths(config, repoType);
    logger.info("Retrieved {} Group Ids.", repoPaths.size());
    return repoPaths;
  }

  @Override
  public boolean validateArtifactServer(ArtifactoryConfig config) {
    if (!validUrl(config.getArtifactoryUrl())) {
      throw new WingsException(ErrorCode.INVALID_ARTIFACT_SERVER, "message", "Artifactory URL must be a valid URL");
    }
    if (!connectableHttpUrl(config.getArtifactoryUrl())) {
      throw new WingsException(ErrorCode.INVALID_ARTIFACT_SERVER, "message",
          "Could not reach Artifactory Server at : " + config.getArtifactoryUrl());
    }
    return artifactoryService.getRepositories(config) != null;
  }

  @Override
  public boolean validateArtifactSource(ArtifactoryConfig config, ArtifactStreamAttributes artifactStreamAttributes) {
    return false;
  }
}
