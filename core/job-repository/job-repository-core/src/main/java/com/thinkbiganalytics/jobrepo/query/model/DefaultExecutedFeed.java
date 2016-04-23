package com.thinkbiganalytics.jobrepo.query.model;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.List;

/**
 * Represents all of the information about a feed that was executed in a JSON friendly format.  The native spring objects for this
 * are not JSON friendly.
 */
@SuppressWarnings("UnusedDeclaration")
public class DefaultExecutedFeed implements Serializable, ExecutedFeed {

  private static final long serialVersionUID = 2227858119326404976L;
  private String name;
  private long feedInstanceId;
  private long feedExecutionId;
  private List<Throwable> exceptions;
  private DateTime endTime;
  private String exitCode;
  private String exitStatus;
  private DateTime startTime;
  private ExecutionStatus status;
  private List<ExecutedJob> executedJobs;
  private Long runTime;
  private Long timeSinceEndTime;
  private boolean isLatest;

  public DefaultExecutedFeed() {

  }

  public DefaultExecutedFeed(ExecutedFeed feed) {
    this.name = feed.getName();
    this.feedInstanceId = feed.getFeedInstanceId();
    this.feedExecutionId = feed.getFeedExecutionId();
    this.exceptions = feed.getExceptions();
    this.endTime = feed.getEndTime();
    this.exitCode = feed.getExitCode();
    this.exitStatus = feed.getExitStatus();
    this.startTime = feed.getStartTime();
    this.status = feed.getStatus();
    this.executedJobs = feed.getExecutedJobs();
    this.runTime = feed.getRunTime();
    this.timeSinceEndTime = feed.getTimeSinceEndTime();
    this.isLatest = feed.isLatest();
  }

  public static long getSerialVersionUID() {
    return serialVersionUID;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public long getFeedInstanceId() {
    return feedInstanceId;
  }

  @Override
  public void setFeedInstanceId(long feedInstanceId) {
    this.feedInstanceId = feedInstanceId;
  }

  @Override
  public long getFeedExecutionId() {
    return feedExecutionId;
  }

  @Override
  public void setFeedExecutionId(long feedExecutionId) {
    this.feedExecutionId = feedExecutionId;
  }

  @Override
  public List<Throwable> getExceptions() {
    return exceptions;
  }

  @Override
  public void setExceptions(List<Throwable> exceptions) {
    this.exceptions = exceptions;
  }

  @Override
  public DateTime getEndTime() {
    return endTime;
  }

  @Override
  public void setEndTime(DateTime endTime) {
    this.endTime = endTime;
  }

  @Override
  public String getExitCode() {
    return exitCode;
  }

  @Override
  public void setExitCode(String exitCode) {
    this.exitCode = exitCode;
  }

  @Override
  public String getExitStatus() {
    return exitStatus;
  }

  @Override
  public void setExitStatus(String exitStatus) {
    this.exitStatus = exitStatus;
  }

  @Override
  public DateTime getStartTime() {
    return startTime;
  }

  @Override
  public void setStartTime(DateTime startTime) {
    this.startTime = startTime;
  }

  @Override
  public ExecutionStatus getStatus() {
    return status;
  }

  @Override
  public void setStatus(ExecutionStatus status) {
    this.status = status;
  }

  @Override
  public List<ExecutedJob> getExecutedJobs() {
    return executedJobs;
  }

  @Override
  public void setExecutedJobs(List<ExecutedJob> executedJobs) {
    this.executedJobs = executedJobs;
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
  public boolean isLatest() {
    return isLatest;
  }

  @Override
  public void setIsLatest(boolean isLatest) {
    this.isLatest = isLatest;
  }
}
