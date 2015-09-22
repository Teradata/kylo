package com.thinkbiganalytics.scheduler.quartz.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.quartz.JobKey;

@JsonIgnoreProperties(ignoreUnknown=true)
public class JobTriggerDetail {

	public String groupName;
	public JobKey jobKey;
	public JobDetailDTO jobDetail;
	public List<TriggerDTO> triggers;

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public JobKey getJobKey() {
		return jobKey;
	}

	public void setJobKey(JobKey jobKey) {
		this.jobKey = jobKey;
	}

	public JobDetailDTO getJobDetail() {
		return jobDetail;
	}

	public void setJobDetail(JobDetailDTO jobDetail) {
		this.jobDetail = jobDetail;
	}

	public List<TriggerDTO> getTriggers() {
		return triggers;
	}

	public void setTriggers(List<TriggerDTO> jobTriggers) {
		this.triggers = jobTriggers;
	}

}
