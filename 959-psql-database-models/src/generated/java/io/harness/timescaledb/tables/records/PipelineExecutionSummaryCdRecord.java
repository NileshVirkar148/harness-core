/*
 * This file is generated by jOOQ.
 */
package io.harness.timescaledb.tables.records;

import io.harness.timescaledb.tables.PipelineExecutionSummaryCd;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record21;
import org.jooq.Row21;
import org.jooq.impl.UpdatableRecordImpl;

/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class PipelineExecutionSummaryCdRecord extends UpdatableRecordImpl<PipelineExecutionSummaryCdRecord>
    implements Record21<String, String, String, String, String, String, String, String, Long, Long, String, String,
        String, String, String, String, String, String, String, String, String> {
  private static final long serialVersionUID = 1L;

  /**
   * Setter for <code>public.pipeline_execution_summary_cd.id</code>.
   */
  public PipelineExecutionSummaryCdRecord setId(String value) {
    set(0, value);
    return this;
  }

  /**
   * Getter for <code>public.pipeline_execution_summary_cd.id</code>.
   */
  public String getId() {
    return (String) get(0);
  }

  /**
   * Setter for <code>public.pipeline_execution_summary_cd.accountid</code>.
   */
  public PipelineExecutionSummaryCdRecord setAccountid(String value) {
    set(1, value);
    return this;
  }

  /**
   * Getter for <code>public.pipeline_execution_summary_cd.accountid</code>.
   */
  public String getAccountid() {
    return (String) get(1);
  }

  /**
   * Setter for <code>public.pipeline_execution_summary_cd.orgidentifier</code>.
   */
  public PipelineExecutionSummaryCdRecord setOrgidentifier(String value) {
    set(2, value);
    return this;
  }

  /**
   * Getter for <code>public.pipeline_execution_summary_cd.orgidentifier</code>.
   */
  public String getOrgidentifier() {
    return (String) get(2);
  }

  /**
   * Setter for <code>public.pipeline_execution_summary_cd.projectidentifier</code>.
   */
  public PipelineExecutionSummaryCdRecord setProjectidentifier(String value) {
    set(3, value);
    return this;
  }

  /**
   * Getter for <code>public.pipeline_execution_summary_cd.projectidentifier</code>.
   */
  public String getProjectidentifier() {
    return (String) get(3);
  }

  /**
   * Setter for <code>public.pipeline_execution_summary_cd.pipelineidentifier</code>.
   */
  public PipelineExecutionSummaryCdRecord setPipelineidentifier(String value) {
    set(4, value);
    return this;
  }

  /**
   * Getter for <code>public.pipeline_execution_summary_cd.pipelineidentifier</code>.
   */
  public String getPipelineidentifier() {
    return (String) get(4);
  }

  /**
   * Setter for <code>public.pipeline_execution_summary_cd.name</code>.
   */
  public PipelineExecutionSummaryCdRecord setName(String value) {
    set(5, value);
    return this;
  }

  /**
   * Getter for <code>public.pipeline_execution_summary_cd.name</code>.
   */
  public String getName() {
    return (String) get(5);
  }

  /**
   * Setter for <code>public.pipeline_execution_summary_cd.status</code>.
   */
  public PipelineExecutionSummaryCdRecord setStatus(String value) {
    set(6, value);
    return this;
  }

  /**
   * Getter for <code>public.pipeline_execution_summary_cd.status</code>.
   */
  public String getStatus() {
    return (String) get(6);
  }

  /**
   * Setter for <code>public.pipeline_execution_summary_cd.moduleinfo_type</code>.
   */
  public PipelineExecutionSummaryCdRecord setModuleinfoType(String value) {
    set(7, value);
    return this;
  }

  /**
   * Getter for <code>public.pipeline_execution_summary_cd.moduleinfo_type</code>.
   */
  public String getModuleinfoType() {
    return (String) get(7);
  }

  /**
   * Setter for <code>public.pipeline_execution_summary_cd.startts</code>.
   */
  public PipelineExecutionSummaryCdRecord setStartts(Long value) {
    set(8, value);
    return this;
  }

  /**
   * Getter for <code>public.pipeline_execution_summary_cd.startts</code>.
   */
  public Long getStartts() {
    return (Long) get(8);
  }

  /**
   * Setter for <code>public.pipeline_execution_summary_cd.endts</code>.
   */
  public PipelineExecutionSummaryCdRecord setEndts(Long value) {
    set(9, value);
    return this;
  }

  /**
   * Getter for <code>public.pipeline_execution_summary_cd.endts</code>.
   */
  public Long getEndts() {
    return (Long) get(9);
  }

  /**
   * Setter for <code>public.pipeline_execution_summary_cd.trigger_type</code>.
   */
  public PipelineExecutionSummaryCdRecord setTriggerType(String value) {
    set(10, value);
    return this;
  }

  /**
   * Getter for <code>public.pipeline_execution_summary_cd.trigger_type</code>.
   */
  public String getTriggerType() {
    return (String) get(10);
  }

  /**
   * Setter for <code>public.pipeline_execution_summary_cd.author_name</code>.
   */
  public PipelineExecutionSummaryCdRecord setAuthorName(String value) {
    set(11, value);
    return this;
  }

  /**
   * Getter for <code>public.pipeline_execution_summary_cd.author_name</code>.
   */
  public String getAuthorName() {
    return (String) get(11);
  }

  /**
   * Setter for <code>public.pipeline_execution_summary_cd.author_avatar</code>.
   */
  public PipelineExecutionSummaryCdRecord setAuthorAvatar(String value) {
    set(12, value);
    return this;
  }

  /**
   * Getter for <code>public.pipeline_execution_summary_cd.author_avatar</code>.
   */
  public String getAuthorAvatar() {
    return (String) get(12);
  }

  /**
   * Setter for <code>public.pipeline_execution_summary_cd.moduleinfo_repository</code>.
   */
  public PipelineExecutionSummaryCdRecord setModuleinfoRepository(String value) {
    set(13, value);
    return this;
  }

  /**
   * Getter for <code>public.pipeline_execution_summary_cd.moduleinfo_repository</code>.
   */
  public String getModuleinfoRepository() {
    return (String) get(13);
  }

  /**
   * Setter for <code>public.pipeline_execution_summary_cd.moduleinfo_branch_name</code>.
   */
  public PipelineExecutionSummaryCdRecord setModuleinfoBranchName(String value) {
    set(14, value);
    return this;
  }

  /**
   * Getter for <code>public.pipeline_execution_summary_cd.moduleinfo_branch_name</code>.
   */
  public String getModuleinfoBranchName() {
    return (String) get(14);
  }

  /**
   * Setter for <code>public.pipeline_execution_summary_cd.source_branch</code>.
   */
  public PipelineExecutionSummaryCdRecord setSourceBranch(String value) {
    set(15, value);
    return this;
  }

  /**
   * Getter for <code>public.pipeline_execution_summary_cd.source_branch</code>.
   */
  public String getSourceBranch() {
    return (String) get(15);
  }

  /**
   * Setter for <code>public.pipeline_execution_summary_cd.moduleinfo_event</code>.
   */
  public PipelineExecutionSummaryCdRecord setModuleinfoEvent(String value) {
    set(16, value);
    return this;
  }

  /**
   * Getter for <code>public.pipeline_execution_summary_cd.moduleinfo_event</code>.
   */
  public String getModuleinfoEvent() {
    return (String) get(16);
  }

  /**
   * Setter for <code>public.pipeline_execution_summary_cd.moduleinfo_branch_commit_id</code>.
   */
  public PipelineExecutionSummaryCdRecord setModuleinfoBranchCommitId(String value) {
    set(17, value);
    return this;
  }

  /**
   * Getter for <code>public.pipeline_execution_summary_cd.moduleinfo_branch_commit_id</code>.
   */
  public String getModuleinfoBranchCommitId() {
    return (String) get(17);
  }

  /**
   * Setter for <code>public.pipeline_execution_summary_cd.moduleinfo_branch_commit_message</code>.
   */
  public PipelineExecutionSummaryCdRecord setModuleinfoBranchCommitMessage(String value) {
    set(18, value);
    return this;
  }

  /**
   * Getter for <code>public.pipeline_execution_summary_cd.moduleinfo_branch_commit_message</code>.
   */
  public String getModuleinfoBranchCommitMessage() {
    return (String) get(18);
  }

  /**
   * Setter for <code>public.pipeline_execution_summary_cd.moduleinfo_author_id</code>.
   */
  public PipelineExecutionSummaryCdRecord setModuleinfoAuthorId(String value) {
    set(19, value);
    return this;
  }

  /**
   * Getter for <code>public.pipeline_execution_summary_cd.moduleinfo_author_id</code>.
   */
  public String getModuleinfoAuthorId() {
    return (String) get(19);
  }

  /**
   * Setter for <code>public.pipeline_execution_summary_cd.planexecutionid</code>.
   */
  public PipelineExecutionSummaryCdRecord setPlanexecutionid(String value) {
    set(20, value);
    return this;
  }

  /**
   * Getter for <code>public.pipeline_execution_summary_cd.planexecutionid</code>.
   */
  public String getPlanexecutionid() {
    return (String) get(20);
  }

  // -------------------------------------------------------------------------
  // Primary key information
  // -------------------------------------------------------------------------

  @Override
  public Record1<String> key() {
    return (Record1) super.key();
  }

  // -------------------------------------------------------------------------
  // Record21 type implementation
  // -------------------------------------------------------------------------

  @Override
  public Row21<String, String, String, String, String, String, String, String, Long, Long, String, String, String,
      String, String, String, String, String, String, String, String>
  fieldsRow() {
    return (Row21) super.fieldsRow();
  }

  @Override
  public Row21<String, String, String, String, String, String, String, String, Long, Long, String, String, String,
      String, String, String, String, String, String, String, String>
  valuesRow() {
    return (Row21) super.valuesRow();
  }

  @Override
  public Field<String> field1() {
    return PipelineExecutionSummaryCd.PIPELINE_EXECUTION_SUMMARY_CD.ID;
  }

  @Override
  public Field<String> field2() {
    return PipelineExecutionSummaryCd.PIPELINE_EXECUTION_SUMMARY_CD.ACCOUNTID;
  }

  @Override
  public Field<String> field3() {
    return PipelineExecutionSummaryCd.PIPELINE_EXECUTION_SUMMARY_CD.ORGIDENTIFIER;
  }

  @Override
  public Field<String> field4() {
    return PipelineExecutionSummaryCd.PIPELINE_EXECUTION_SUMMARY_CD.PROJECTIDENTIFIER;
  }

  @Override
  public Field<String> field5() {
    return PipelineExecutionSummaryCd.PIPELINE_EXECUTION_SUMMARY_CD.PIPELINEIDENTIFIER;
  }

  @Override
  public Field<String> field6() {
    return PipelineExecutionSummaryCd.PIPELINE_EXECUTION_SUMMARY_CD.NAME;
  }

  @Override
  public Field<String> field7() {
    return PipelineExecutionSummaryCd.PIPELINE_EXECUTION_SUMMARY_CD.STATUS;
  }

  @Override
  public Field<String> field8() {
    return PipelineExecutionSummaryCd.PIPELINE_EXECUTION_SUMMARY_CD.MODULEINFO_TYPE;
  }

  @Override
  public Field<Long> field9() {
    return PipelineExecutionSummaryCd.PIPELINE_EXECUTION_SUMMARY_CD.STARTTS;
  }

  @Override
  public Field<Long> field10() {
    return PipelineExecutionSummaryCd.PIPELINE_EXECUTION_SUMMARY_CD.ENDTS;
  }

  @Override
  public Field<String> field11() {
    return PipelineExecutionSummaryCd.PIPELINE_EXECUTION_SUMMARY_CD.TRIGGER_TYPE;
  }

  @Override
  public Field<String> field12() {
    return PipelineExecutionSummaryCd.PIPELINE_EXECUTION_SUMMARY_CD.AUTHOR_NAME;
  }

  @Override
  public Field<String> field13() {
    return PipelineExecutionSummaryCd.PIPELINE_EXECUTION_SUMMARY_CD.AUTHOR_AVATAR;
  }

  @Override
  public Field<String> field14() {
    return PipelineExecutionSummaryCd.PIPELINE_EXECUTION_SUMMARY_CD.MODULEINFO_REPOSITORY;
  }

  @Override
  public Field<String> field15() {
    return PipelineExecutionSummaryCd.PIPELINE_EXECUTION_SUMMARY_CD.MODULEINFO_BRANCH_NAME;
  }

  @Override
  public Field<String> field16() {
    return PipelineExecutionSummaryCd.PIPELINE_EXECUTION_SUMMARY_CD.SOURCE_BRANCH;
  }

  @Override
  public Field<String> field17() {
    return PipelineExecutionSummaryCd.PIPELINE_EXECUTION_SUMMARY_CD.MODULEINFO_EVENT;
  }

  @Override
  public Field<String> field18() {
    return PipelineExecutionSummaryCd.PIPELINE_EXECUTION_SUMMARY_CD.MODULEINFO_BRANCH_COMMIT_ID;
  }

  @Override
  public Field<String> field19() {
    return PipelineExecutionSummaryCd.PIPELINE_EXECUTION_SUMMARY_CD.MODULEINFO_BRANCH_COMMIT_MESSAGE;
  }

  @Override
  public Field<String> field20() {
    return PipelineExecutionSummaryCd.PIPELINE_EXECUTION_SUMMARY_CD.MODULEINFO_AUTHOR_ID;
  }

  @Override
  public Field<String> field21() {
    return PipelineExecutionSummaryCd.PIPELINE_EXECUTION_SUMMARY_CD.PLANEXECUTIONID;
  }

  @Override
  public String component1() {
    return getId();
  }

  @Override
  public String component2() {
    return getAccountid();
  }

  @Override
  public String component3() {
    return getOrgidentifier();
  }

  @Override
  public String component4() {
    return getProjectidentifier();
  }

  @Override
  public String component5() {
    return getPipelineidentifier();
  }

  @Override
  public String component6() {
    return getName();
  }

  @Override
  public String component7() {
    return getStatus();
  }

  @Override
  public String component8() {
    return getModuleinfoType();
  }

  @Override
  public Long component9() {
    return getStartts();
  }

  @Override
  public Long component10() {
    return getEndts();
  }

  @Override
  public String component11() {
    return getTriggerType();
  }

  @Override
  public String component12() {
    return getAuthorName();
  }

  @Override
  public String component13() {
    return getAuthorAvatar();
  }

  @Override
  public String component14() {
    return getModuleinfoRepository();
  }

  @Override
  public String component15() {
    return getModuleinfoBranchName();
  }

  @Override
  public String component16() {
    return getSourceBranch();
  }

  @Override
  public String component17() {
    return getModuleinfoEvent();
  }

  @Override
  public String component18() {
    return getModuleinfoBranchCommitId();
  }

  @Override
  public String component19() {
    return getModuleinfoBranchCommitMessage();
  }

  @Override
  public String component20() {
    return getModuleinfoAuthorId();
  }

  @Override
  public String component21() {
    return getPlanexecutionid();
  }

  @Override
  public String value1() {
    return getId();
  }

  @Override
  public String value2() {
    return getAccountid();
  }

  @Override
  public String value3() {
    return getOrgidentifier();
  }

  @Override
  public String value4() {
    return getProjectidentifier();
  }

  @Override
  public String value5() {
    return getPipelineidentifier();
  }

  @Override
  public String value6() {
    return getName();
  }

  @Override
  public String value7() {
    return getStatus();
  }

  @Override
  public String value8() {
    return getModuleinfoType();
  }

  @Override
  public Long value9() {
    return getStartts();
  }

  @Override
  public Long value10() {
    return getEndts();
  }

  @Override
  public String value11() {
    return getTriggerType();
  }

  @Override
  public String value12() {
    return getAuthorName();
  }

  @Override
  public String value13() {
    return getAuthorAvatar();
  }

  @Override
  public String value14() {
    return getModuleinfoRepository();
  }

  @Override
  public String value15() {
    return getModuleinfoBranchName();
  }

  @Override
  public String value16() {
    return getSourceBranch();
  }

  @Override
  public String value17() {
    return getModuleinfoEvent();
  }

  @Override
  public String value18() {
    return getModuleinfoBranchCommitId();
  }

  @Override
  public String value19() {
    return getModuleinfoBranchCommitMessage();
  }

  @Override
  public String value20() {
    return getModuleinfoAuthorId();
  }

  @Override
  public String value21() {
    return getPlanexecutionid();
  }

  @Override
  public PipelineExecutionSummaryCdRecord value1(String value) {
    setId(value);
    return this;
  }

  @Override
  public PipelineExecutionSummaryCdRecord value2(String value) {
    setAccountid(value);
    return this;
  }

  @Override
  public PipelineExecutionSummaryCdRecord value3(String value) {
    setOrgidentifier(value);
    return this;
  }

  @Override
  public PipelineExecutionSummaryCdRecord value4(String value) {
    setProjectidentifier(value);
    return this;
  }

  @Override
  public PipelineExecutionSummaryCdRecord value5(String value) {
    setPipelineidentifier(value);
    return this;
  }

  @Override
  public PipelineExecutionSummaryCdRecord value6(String value) {
    setName(value);
    return this;
  }

  @Override
  public PipelineExecutionSummaryCdRecord value7(String value) {
    setStatus(value);
    return this;
  }

  @Override
  public PipelineExecutionSummaryCdRecord value8(String value) {
    setModuleinfoType(value);
    return this;
  }

  @Override
  public PipelineExecutionSummaryCdRecord value9(Long value) {
    setStartts(value);
    return this;
  }

  @Override
  public PipelineExecutionSummaryCdRecord value10(Long value) {
    setEndts(value);
    return this;
  }

  @Override
  public PipelineExecutionSummaryCdRecord value11(String value) {
    setTriggerType(value);
    return this;
  }

  @Override
  public PipelineExecutionSummaryCdRecord value12(String value) {
    setAuthorName(value);
    return this;
  }

  @Override
  public PipelineExecutionSummaryCdRecord value13(String value) {
    setAuthorAvatar(value);
    return this;
  }

  @Override
  public PipelineExecutionSummaryCdRecord value14(String value) {
    setModuleinfoRepository(value);
    return this;
  }

  @Override
  public PipelineExecutionSummaryCdRecord value15(String value) {
    setModuleinfoBranchName(value);
    return this;
  }

  @Override
  public PipelineExecutionSummaryCdRecord value16(String value) {
    setSourceBranch(value);
    return this;
  }

  @Override
  public PipelineExecutionSummaryCdRecord value17(String value) {
    setModuleinfoEvent(value);
    return this;
  }

  @Override
  public PipelineExecutionSummaryCdRecord value18(String value) {
    setModuleinfoBranchCommitId(value);
    return this;
  }

  @Override
  public PipelineExecutionSummaryCdRecord value19(String value) {
    setModuleinfoBranchCommitMessage(value);
    return this;
  }

  @Override
  public PipelineExecutionSummaryCdRecord value20(String value) {
    setModuleinfoAuthorId(value);
    return this;
  }

  @Override
  public PipelineExecutionSummaryCdRecord value21(String value) {
    setPlanexecutionid(value);
    return this;
  }

  @Override
  public PipelineExecutionSummaryCdRecord values(String value1, String value2, String value3, String value4,
      String value5, String value6, String value7, String value8, Long value9, Long value10, String value11,
      String value12, String value13, String value14, String value15, String value16, String value17, String value18,
      String value19, String value20, String value21) {
    value1(value1);
    value2(value2);
    value3(value3);
    value4(value4);
    value5(value5);
    value6(value6);
    value7(value7);
    value8(value8);
    value9(value9);
    value10(value10);
    value11(value11);
    value12(value12);
    value13(value13);
    value14(value14);
    value15(value15);
    value16(value16);
    value17(value17);
    value18(value18);
    value19(value19);
    value20(value20);
    value21(value21);
    return this;
  }

  // -------------------------------------------------------------------------
  // Constructors
  // -------------------------------------------------------------------------

  /**
   * Create a detached PipelineExecutionSummaryCdRecord
   */
  public PipelineExecutionSummaryCdRecord() {
    super(PipelineExecutionSummaryCd.PIPELINE_EXECUTION_SUMMARY_CD);
  }

  /**
   * Create a detached, initialised PipelineExecutionSummaryCdRecord
   */
  public PipelineExecutionSummaryCdRecord(String id, String accountid, String orgidentifier, String projectidentifier,
      String pipelineidentifier, String name, String status, String moduleinfoType, Long startts, Long endts,
      String triggerType, String authorName, String authorAvatar, String moduleinfoRepository,
      String moduleinfoBranchName, String sourceBranch, String moduleinfoEvent, String moduleinfoBranchCommitId,
      String moduleinfoBranchCommitMessage, String moduleinfoAuthorId, String planexecutionid) {
    super(PipelineExecutionSummaryCd.PIPELINE_EXECUTION_SUMMARY_CD);

    setId(id);
    setAccountid(accountid);
    setOrgidentifier(orgidentifier);
    setProjectidentifier(projectidentifier);
    setPipelineidentifier(pipelineidentifier);
    setName(name);
    setStatus(status);
    setModuleinfoType(moduleinfoType);
    setStartts(startts);
    setEndts(endts);
    setTriggerType(triggerType);
    setAuthorName(authorName);
    setAuthorAvatar(authorAvatar);
    setModuleinfoRepository(moduleinfoRepository);
    setModuleinfoBranchName(moduleinfoBranchName);
    setSourceBranch(sourceBranch);
    setModuleinfoEvent(moduleinfoEvent);
    setModuleinfoBranchCommitId(moduleinfoBranchCommitId);
    setModuleinfoBranchCommitMessage(moduleinfoBranchCommitMessage);
    setModuleinfoAuthorId(moduleinfoAuthorId);
    setPlanexecutionid(planexecutionid);
  }
}