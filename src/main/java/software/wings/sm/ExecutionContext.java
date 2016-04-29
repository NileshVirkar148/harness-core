package software.wings.sm;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Describes execution context for a state machine execution.
 * @author Rishi
 */
public class ExecutionContext implements Serializable {
  private static final long serialVersionUID = 1L;
  private String stateMachineId;
  private Map<String, Serializable> params = new HashMap<>();
  private Map<String, Serializable> summary = new HashMap<>();
  private Map<String, StateExecutionData> stateExecutionMap;
  private Map<RepeatElementType, RepeatElement> repeatElementMap = new HashMap<>();

  private transient SmInstance smInstance;
  private boolean dirty = false;

  public Map<String, Serializable> getParams() {
    return params;
  }

  public void setParams(Map<String, Serializable> params) {
    this.params = params;
    dirty = true;
  }

  public void setParam(String paramName, Serializable paramValue) {
    params.put(paramName, paramValue);
    dirty = true;
  }

  public Map<String, Serializable> getSummary() {
    return summary;
  }

  public void setSummary(Map<String, Serializable> summary) {
    this.summary = summary;
    dirty = true;
  }

  public String getStateMachineId() {
    return stateMachineId;
  }

  public void setStateMachineId(String stateMachineId) {
    this.stateMachineId = stateMachineId;
  }

  public Map<String, StateExecutionData> getStateExecutionMap() {
    return stateExecutionMap;
  }

  public void setStateExecutionMap(Map<String, StateExecutionData> stateExecutionMap) {
    this.stateExecutionMap = stateExecutionMap;
    dirty = true;
  }

  public SmInstance getSmInstance() {
    return smInstance;
  }

  public void setSmInstance(SmInstance smInstance) {
    this.smInstance = smInstance;
  }

  public RepeatElement getRepeatElement(RepeatElementType repeatElementType) {
    return repeatElementMap.get(repeatElementType);
  }
  public Map<RepeatElementType, RepeatElement> getRepeatElementMap() {
    return repeatElementMap;
  }

  public void setRepeatElementMap(Map<RepeatElementType, RepeatElement> repeatElementMap) {
    this.repeatElementMap = repeatElementMap;
  }

  public Object evaluateExpression(String expression) {
    return null;
  }

  public List evaluateExpressionAsList(String expression) {
    return null;
  }

  public Map evaluateExpressionAsMap(String expression) {
    return null;
  }

  public List<RepeatElement> evaluateRepeatExpression(
      RepeatElementType repeatElementType, String repeatElementExpression) {
    return null;
  }

  public boolean isDirty() {
    return dirty;
  }

  public void setDirty(boolean dirty) {
    this.dirty = dirty;
  }
}
