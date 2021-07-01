package io.harness.pms.exception;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.data.structure.EmptyPredicate;
import io.harness.data.structure.HarnessStringUtils;
import io.harness.exception.FilterCreatorException;
import io.harness.exception.PlanCreatorException;
import io.harness.pms.contracts.plan.Dependencies;
import io.harness.pms.contracts.plan.ErrorResponse;
import io.harness.pms.contracts.plan.YamlFieldBlob;
import io.harness.pms.yaml.YamlField;
import io.harness.serializer.JsonUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;

@OwnedBy(HarnessTeam.PIPELINE)
@UtilityClass
public class PmsExceptionUtils {
  public String getUnresolvedDependencyErrorMessage(Collection<YamlFieldBlob> yamlFieldBlobs) throws IOException {
    return String.format("Following Nodes could not be parsed: %s.",
        getYamlNodeErrorInfo(yamlFieldBlobs).stream().map(JsonUtils::asJson).collect(Collectors.joining(",")));
  }

  public String getUnresolvedDependencyPathsErrorMessage(Dependencies dependencies) {
    return String.format(
        "Following yaml paths could not be parsed: %s", String.join(",", dependencies.getDependenciesMap().values()));
  }

  private List<YamlNodeErrorInfo> getYamlNodeErrorInfo(Collection<YamlFieldBlob> yamlFieldBlobs) throws IOException {
    List<YamlNodeErrorInfo> yamlNodeErrorInfos = new ArrayList<>();
    for (YamlFieldBlob yamlFieldBlob : yamlFieldBlobs) {
      YamlField yamlField = YamlField.fromFieldBlob(yamlFieldBlob);
      yamlNodeErrorInfos.add(YamlNodeErrorInfo.fromField(yamlField));
    }
    return yamlNodeErrorInfos;
  }

  public void checkAndThrowFilterCreatorException(List<ErrorResponse> errorResponses) {
    if (EmptyPredicate.isEmpty(errorResponses)) {
      return;
    }
    List<String> messages =
        errorResponses.stream().flatMap(resp -> resp.getMessagesList().stream()).collect(Collectors.toList());
    throw new FilterCreatorException(HarnessStringUtils.join(",", messages));
  }

  public void checkAndThrowPlanCreatorException(List<ErrorResponse> errorResponses) {
    if (EmptyPredicate.isEmpty(errorResponses)) {
      return;
    }
    List<String> messages =
        errorResponses.stream().flatMap(resp -> resp.getMessagesList().stream()).collect(Collectors.toList());
    throw new PlanCreatorException(String.format("Error creating Plan: %s", HarnessStringUtils.join(",", messages)));
  }
}
