package software.wings.beans.command;

import io.harness.delegate.command.CommandExecutionResult.CommandExecutionStatus;
import io.harness.delegate.service.DelegateAgentFileService.FileBucket;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.tuple.Pair;
import software.wings.beans.artifact.ArtifactStreamAttributes;
import software.wings.beans.command.CopyConfigCommandUnit.ConfigFileMetaData;
import software.wings.core.BaseScriptExecutor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
public class ShellCommandExecutionContext extends CommandExecutionContext {
  private BaseScriptExecutor executor;

  public ShellCommandExecutionContext(CommandExecutionContext other) {
    super(other);
  }

  public CommandExecutionStatus copyGridFsFiles(
      String destinationDirectoryPath, FileBucket fileBucket, List<Pair<String, String>> fileNamesIds) {
    return executor.copyGridFsFiles(evaluateVariable(destinationDirectoryPath), fileBucket, fileNamesIds);
  }

  public CommandExecutionStatus copyConfigFiles(ConfigFileMetaData configFileMetaData) {
    configFileMetaData.setDestinationDirectoryPath(evaluateVariable(configFileMetaData.getDestinationDirectoryPath()));
    return executor.copyConfigFiles(configFileMetaData);
  }

  public CommandExecutionStatus copyFiles(String destinationDirectoryPath, List<String> files) {
    return executor.copyFiles(evaluateVariable(destinationDirectoryPath), files);
  }

  public CommandExecutionStatus copyFiles(String destinationDirectoryPath,
      ArtifactStreamAttributes artifactStreamAttributes, String accountId, String appId, String activityId,
      String commandUnitName, String hostName) {
    return executor.copyFiles(evaluateVariable(destinationDirectoryPath), artifactStreamAttributes, accountId, appId,
        activityId, commandUnitName, hostName);
  }

  public CommandExecutionStatus executeCommandString(String commandString) {
    return executor.executeCommandString(commandString, false);
  }

  public CommandExecutionStatus executeCommandString(String commandString, boolean displayCommand) {
    return executor.executeCommandString(commandString, displayCommand);
  }

  public CommandExecutionStatus executeCommandString(String commandString, StringBuffer output) {
    return executor.executeCommandString(commandString, output);
  }

  public void setExecutor(BaseScriptExecutor executor) {
    this.executor = executor;
  }
}
