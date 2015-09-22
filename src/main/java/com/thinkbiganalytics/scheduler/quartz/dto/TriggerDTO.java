package com.thinkbiganalytics.scheduler.quartz.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.quartz.CronTrigger;
import org.quartz.Trigger;
@JsonIgnoreProperties(ignoreUnknown=true)
public class TriggerDTO {

	private Date nextFireTime;
	private Date previousFireTime;
	private String jobName;
	private String jobGroup;
	private String triggerName;
	private String triggerGroup;
	private Date startTime;
	private Date endTime;
	private Date finalFireTime;
	private String cronExpression;
	private String cronExpressionSummary;
	private String description;
	public boolean paused;

	public TriggerDTO() {

	}

	public TriggerDTO(Trigger trigger) {
		if (trigger instanceof CronTrigger) {
			CronTrigger ct = (CronTrigger) trigger;
			this.cronExpression = ct.getCronExpression();
			this.cronExpressionSummary = ct.getExpressionSummary();
		}
		this.description = trigger.getDescription();
		this.nextFireTime = trigger.getNextFireTime();
		this.jobName = trigger.getJobKey().getName();
		this.jobGroup = trigger.getJobKey().getGroup();
		this.triggerName = trigger.getKey().getName();
		this.triggerGroup = trigger.getKey().getGroup();
		this.startTime = trigger.getStartTime();
		this.endTime = trigger.getEndTime();
		this.finalFireTime = trigger.getFinalFireTime();
		this.previousFireTime = trigger.getPreviousFireTime();
	}

	public Date getNextFireTime() {
		return nextFireTime;
	}

	public void setNextFireTime(Date nextFireTime) {
		this.nextFireTime = nextFireTime;
	}

	public Date getPreviousFireTime() {
		return previousFireTime;
	}

	public void setPreviousFireTime(Date previousFireTime) {
		this.previousFireTime = previousFireTime;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getJobGroup() {
		return jobGroup;
	}

	public void setJobGroup(String jobGroup) {
		this.jobGroup = jobGroup;
	}

	public String getTriggerName() {
		return triggerName;
	}

	public void setTriggerName(String triggerName) {
		this.triggerName = triggerName;
	}

	public String getTriggerGroup() {
		return triggerGroup;
	}

	public void setTriggerGroup(String triggerGroup) {
		this.triggerGroup = triggerGroup;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Date getFinalFireTime() {
		return finalFireTime;
	}

	public void setFinalFireTime(Date finalFireTime) {
		this.finalFireTime = finalFireTime;
	}

	public String getCronExpression() {
		return cronExpression;
	}

	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}

	public String getCronExpressionSummary() {
		return cronExpressionSummary;
	}

	public void setCronExpressionSummary(String cronExpressionSummary) {
		this.cronExpressionSummary = cronExpressionSummary;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isPaused() {
		return paused;
	}

	public void setPaused(boolean paused) {
		this.paused = paused;
	}

}
