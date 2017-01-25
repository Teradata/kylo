package com.thinkbiganalytics.jobrepo.query.model;

/*-
 * #%L
 * thinkbig-job-repository-core
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.Map;

/**
 * JSON Friendly version of StepExecution to return spring batch data through a RESTful interface
 */
@SuppressWarnings("UnusedDeclaration")
public class DefaultExecutedStep implements Serializable, ExecutedStep {

  private static final long serialVersionUID = -1190571546947137596L;

  private String stepName;
  private ExecutionStatus status;
  private long readCount;
  private long writeCount;
  private long commitCount;
  private long rollbackCount;
  private long readSkipCount;
  private long processSkipCount;
  private long writeSkipCount;
  private DateTime startTime;
  private DateTime endTime;
  private DateTime lastUpdateTime;
  private Map<String, Object> executionContext;
  private String exitCode;
  private String exitDescription;
  private long id;
  private int version;
  private Long nifiEventId;

  private Long runTime;
  private Long timeSinceEndTime;
  private boolean running;

  @Override
  public String getStepName() {
    return stepName;
  }

  @Override
  public void setStepName(final String stepName) {
    this.stepName = stepName;
  }

  @Override
  public ExecutionStatus getStatus() {
    return status;
  }

  @Override
  public void setStatus(final ExecutionStatus status) {
    this.status = status;
  }

  @Override
  public long getReadCount() {
    return readCount;
  }

  @Override
  public void setReadCount(final long readCount) {
    this.readCount = readCount;
  }

  @Override
  public long getWriteCount() {
    return writeCount;
  }

  @Override
  public void setWriteCount(final long writeCount) {
    this.writeCount = writeCount;
  }

  @Override
  public long getCommitCount() {
    return commitCount;
  }

  @Override
  public void setCommitCount(final long commitCount) {
    this.commitCount = commitCount;
  }

  @Override
  public long getRollbackCount() {
    return rollbackCount;
  }

  @Override
  public void setRollbackCount(final long rollbackCount) {
    this.rollbackCount = rollbackCount;
  }

  @Override
  public long getReadSkipCount() {
    return readSkipCount;
  }

  @Override
  public void setReadSkipCount(final long readSkipCount) {
    this.readSkipCount = readSkipCount;
  }

  @Override
  public long getProcessSkipCount() {
    return processSkipCount;
  }

  @Override
  public void setProcessSkipCount(final long processSkipCount) {
    this.processSkipCount = processSkipCount;
  }

  @Override
  public long getWriteSkipCount() {
    return writeSkipCount;
  }

  @Override
  public void setWriteSkipCount(final long writeSkipCount) {
    this.writeSkipCount = writeSkipCount;
  }

  @Override
  public DateTime getStartTime() {
    return startTime;
  }

  @Override
  public void setStartTime(final DateTime startTime) {
    this.startTime = startTime;
  }

  @Override
  public DateTime getEndTime() {
    return endTime;
  }

  @Override
  public void setEndTime(final DateTime endTime) {
    this.endTime = endTime;
  }

  @Override
  public DateTime getLastUpdateTime() {
    return lastUpdateTime;
  }

  @Override
  public void setLastUpdateTime(final DateTime lastUpdateTime) {
    this.lastUpdateTime = lastUpdateTime;
  }

  @Override
  public Map<String, Object> getExecutionContext() {
    return executionContext;
  }

  @Override
  public void setExecutionContext(final Map<String, Object> executionContext) {
    this.executionContext = executionContext;
  }

  @Override
  public String getExitCode() {
    return exitCode;
  }

  @Override
  public void setExitCode(final String exitCode) {
    this.exitCode = exitCode;
  }

  @Override
  public String getExitDescription() {
    return exitDescription;
  }

  @Override
  public void setExitDescription(final String exitDescription) {
    this.exitDescription = exitDescription;
  }

  @Override
  public long getId() {
    return id;
  }

  @Override
  public void setId(final long id) {
    this.id = id;
  }

  @Override
  public int getVersion() {
    return version;
  }

  @Override
  public void setVersion(final int version) {
    this.version = version;
  }


  @Override
  public Long getRunTime() {
    return runTime;
  }

  @Override
  public void setRunTime(Long runTime) {
    this.runTime = runTime;
  }

  @Override
  public Long getTimeSinceEndTime() {
    return timeSinceEndTime;
  }

  @Override
  public void setTimeSinceEndTime(Long timeSinceEndTime) {
    this.timeSinceEndTime = timeSinceEndTime;
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  @Override
  public void setRunning(boolean running) {
    this.running = running;
  }

  public Long getNifiEventId() {
    return nifiEventId;
  }

  public void setNifiEventId(Long nifiEventId) {
    this.nifiEventId = nifiEventId;
  }
}
