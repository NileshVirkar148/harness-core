package software.wings.service.impl.yaml.handler.command;

import software.wings.beans.command.ResizeCommandUnit;
import software.wings.beans.command.ResizeCommandUnit.Yaml;
import software.wings.beans.command.ResizeCommandUnit.Yaml.Builder;
import software.wings.beans.yaml.Change.ChangeType;
import software.wings.beans.yaml.ChangeContext;
import software.wings.exception.HarnessException;

import java.util.List;

/**
 * @author rktummala on 11/13/17
 */
public class ResizeCommandUnitYamlHandler extends CommandUnitYamlHandler<Yaml, ResizeCommandUnit, Builder> {
  @Override
  public ResizeCommandUnit upsertFromYaml(ChangeContext<Yaml> changeContext, List<ChangeContext> changeSetContext)
      throws HarnessException {
    if (changeContext.getChange().getChangeType().equals(ChangeType.ADD)) {
      return createFromYaml(changeContext, changeSetContext);
    } else {
      return updateFromYaml(changeContext, changeSetContext);
    }
  }

  @Override
  public Class getYamlClass() {
    return Yaml.class;
  }

  @Override
  protected Builder getYamlBuilder() {
    return Builder.aYaml();
  }

  @Override
  protected ResizeCommandUnit getCommandUnit() {
    return new ResizeCommandUnit();
  }
}
