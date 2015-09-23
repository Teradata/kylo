package com.thinkbiganalytics.scheduler.quartz;

import com.thinkbiganalytics.scheduler.quartz.dto.JobDetailDTO;
import com.thinkbiganalytics.scheduler.quartz.dto.JobTriggerDetail;
import com.thinkbiganalytics.scheduler.quartz.dto.TriggerDTO;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.quartz.*;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.*;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

@Service
public class QuartzScheduler {


	  public enum QUARTZ_MODE {
		SHUTDOWN, STARTED, STANDBY
	}

	private static Logger LOG = LoggerFactory.getLogger(QuartzScheduler.class);

	@Autowired
	@Qualifier("schedulerFactoryBean")
	private SchedulerFactoryBean schedulerFactoryBean;



	private QuartzScheduler()
	{

	}


	public void scheduleJob(JobDetail jobDetail, CronTrigger cronTrigger) throws SchedulerException{
		getScheduler().scheduleJob(jobDetail, cronTrigger);
	}

	public void scheduleJob(MethodInvokingJobDetailFactoryBean methodInvokingJobDetailFactoryBean, CronTriggerFactoryBean cronTriggerFactoryBean)  throws SchedulerException{
	   JobDetail job = methodInvokingJobDetailFactoryBean.getObject();
		CronTrigger trigger = cronTriggerFactoryBean.getObject();
		scheduleJob(job, trigger);
	}

	public void scheduleJob(JobDetail job, CronTriggerFactoryBean cronTriggerFactoryBean)  throws SchedulerException{
		CronTrigger trigger = cronTriggerFactoryBean.getObject();
		scheduleJob(job, trigger);
	}


	public void scheduleJob(String groupName, String jobName,Class<? extends QuartzJobBean> clazz, String cronExpression,Map<String,Object> jobData)  throws SchedulerException{
		JobDataMap jobDataMap = new JobDataMap(jobData);
		JobDetail job = newJob(clazz)
				.withIdentity(jobName, groupName)
				.requestRecovery(false)
				.setJobData(jobDataMap)
				.build();
		CronTrigger trigger = newTrigger()
				.withIdentity("triggerFor_"+jobName, groupName)
				.withSchedule(cronSchedule(cronExpression).inTimeZone(TimeZone.getTimeZone("UTC"))
						.withMisfireHandlingInstructionFireAndProceed())
				.forJob(job.getKey())
				.build();
		scheduleJob(job, trigger);
	}


	public Scheduler getScheduler() {
		return schedulerFactoryBean.getScheduler();
	}

	
	public void startScheduler() throws SchedulerException{
		Scheduler sched = getScheduler();
		if (sched != null) {
			LOG.info("------- Starting Scheduler ---------------------");
				sched.start();
		}
	}

	
	public void restartScheduler() throws SchedulerException {
		Scheduler sched = getScheduler();
		if (sched != null) {
			if (sched.isInStandbyMode()) {
				sched.start();
			}
		}
	}

	public boolean deleteJob(String jobName, String jobGroup) throws SchedulerException {
		Scheduler sched = getScheduler();
		boolean success = false;
		if (sched != null) {
			success = sched.deleteJob(new JobKey(jobName,jobGroup));
		}
		return success;
	}

	public void standbyScheduler()  throws SchedulerException {
		Scheduler sched = getScheduler();
		if (sched != null) {
			sched.standby();
		}
	}

	
	public void shutdownScheduler() {
		Scheduler sched = getScheduler();
		if (sched != null) {
			LOG.info("------- Shutting Down ---------------------");
			try {
				sched.shutdown(true);

				LOG.info("------- Shutdown Complete -----------------");

				// display some stats about the schedule that just ran
				SchedulerMetaData metaData = sched.getMetaData();
				LOG.info("Executed " + metaData.getNumberOfJobsExecuted() + " jobs.");
			} catch (SchedulerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}


	public Date scheduleJob(JobDetail job, Trigger trigger) throws SchedulerException {
		Scheduler sched = getScheduler();
		if (sched != null) {
			try {
				return sched.scheduleJob(job, trigger);
			}
			catch (ObjectAlreadyExistsException existsExc)
			{
				System.out.println("Someone has already scheduled such job/trigger. " + job.getKey() + " : " + trigger.getKey());
			}
		}
		return null;
	}


	public void executeJob(JobDetail job) throws SchedulerException {
		// jobs can be fired directly... (rather than waiting for a trigger)
		Scheduler sched = getScheduler();
		if (sched != null) {
			sched.addJob(job, true);
		}
	}

	
	public void triggerJob(String name, String group) throws SchedulerException {
		Scheduler sched = getScheduler();
		if (sched != null) {
			sched.triggerJob(new JobKey(name, group));
		}
	}

	
	public void interruptJob(String name, String group) throws SchedulerException {
		Scheduler sched = getScheduler();
		if (sched != null) {
			sched.interrupt(new JobKey(name, group));
		}
	}

	
	public void unscheduleJob(String triggerName, String triggerGroup) throws SchedulerException {
		Scheduler sched = getScheduler();
		if (sched != null) {
			sched.unscheduleJob(new TriggerKey(triggerName, triggerGroup));
		}
	}

	


	
	public boolean isSchedulerStopped() throws SchedulerException {
		Scheduler sched = getScheduler();
		if (sched != null) {
			return sched.isShutdown();
		}
		return true;
	}

	
	public boolean isSchedulerStarted() throws SchedulerException {
		Scheduler sched = getScheduler();
		if (sched != null) {
			return sched.isStarted();
		}
		return true;
	}

	public boolean isInStandbyMode() throws SchedulerException {
		Scheduler sched = getScheduler();
		if (sched != null) {
			return sched.isInStandbyMode();
		}
		return true;
	}

	
	public void pauseAll() throws SchedulerException {
		Scheduler sched = getScheduler();
		if (sched != null) {
			sched.pauseAll();
		}
	}

	
	public void resumeAll() throws SchedulerException {
		Scheduler sched = getScheduler();
		if (sched != null) {
			sched.resumeAll();
		}
	}

	public QUARTZ_MODE getMode() throws SchedulerException {
		Scheduler sched = getScheduler();
		if (sched != null) {
			if (sched.isInStandbyMode()) {
				return QUARTZ_MODE.STANDBY;
			} else if (sched.isStarted()) {
				return QUARTZ_MODE.STARTED;
			} else if (sched.isShutdown()) {
				return QUARTZ_MODE.SHUTDOWN;
			}

		}

		return null;

	}

	public void updateTrigger(String name, String group, String cronExpression) throws  SchedulerException {

		CronTrigger trigger = newTrigger().withIdentity(name, group).withSchedule(cronSchedule(cronExpression)).build();
		updateTrigger(name, group, trigger);
	}

	public void pauseTrigger(String name, String group) throws  SchedulerException {
		Scheduler sched = getScheduler();
		if (sched != null) {
			sched.pauseTrigger(new TriggerKey(name, group));
		}
	}
	public void resumeTrigger(String name, String group) throws  SchedulerException {
		Scheduler sched = getScheduler();
		if (sched != null) {
			sched.resumeTrigger(new TriggerKey(name, group));
		}
	}

	public void updateTrigger(String name, String group, Trigger trigger) throws SchedulerException {
		Scheduler sched = getScheduler();
		if (sched != null) {
			// tell the scheduler to remove the old trigger with the given key,
			// and put the new one in its place
			sched.rescheduleJob(new TriggerKey(name, group), trigger);
		}
	}

	
	public SchedulerMetaData getMetaData()  {
		Scheduler sched = getScheduler();
		if (sched != null) {
		try {
			SchedulerMetaData metaData = sched.getMetaData();
			return metaData;
		}catch(SchedulerException e){
			LOG.error("Error getting Scheduler Metadata",e);
		}
	
		}
		return null;
	}

	public List<JobDetail> getScheduledJobs() throws SchedulerException {
		List<JobDetail> list = new ArrayList<JobDetail>();
		Scheduler sched = getScheduler();
		if (sched != null) {
			// enumerate each job group
			for (String group : sched.getJobGroupNames()) {
				// enumerate each job in group

				for (JobKey jobKey : sched.getJobKeys(GroupMatcher.jobGroupEquals(group))) {
					JobDetail detail = sched.getJobDetail(jobKey);
					list.add(detail);
				}

			}

		}
		return list;
	}


	
	public List<JobTriggerDetail> getJobTriggerDetails() throws SchedulerException {
		List<JobTriggerDetail> list = new ArrayList<JobTriggerDetail>();
		Map<TriggerKey, TriggerDTO> triggerDtoMap = new HashMap<TriggerKey, TriggerDTO>();
		Scheduler sched = getScheduler();
		if (sched != null) {
			// enumerate each job group
			for (String group : sched.getJobGroupNames()) {
				// enumerate each job in group

				for (JobKey jobKey : sched.getJobKeys(GroupMatcher.jobGroupEquals(group))) {
					JobTriggerDetail detail = new JobTriggerDetail();
					list.add(detail);
					detail.setGroupName(group);
					JobDetail jobDetail = sched.getJobDetail(jobKey);
					detail.setJobDetail(new JobDetailDTO(jobDetail));

					List<? extends Trigger> jobTriggers = sched.getTriggersOfJob(jobKey);

					List<TriggerDTO> dtoList = new ArrayList<TriggerDTO>();
					if (jobTriggers != null) {
						for (Trigger trigger : jobTriggers) {
							Trigger.TriggerState state = sched.getTriggerState(trigger.getKey());
							TriggerDTO dto = new TriggerDTO(trigger);
							dto.setState(state.name());
							if(Trigger.TriggerState.PAUSED.equals(state)){
								dto.paused = true;
							}
							dtoList.add(dto);
							triggerDtoMap.put(trigger.getKey(), dto);
						}
					}
					detail.setTriggers(dtoList);
				}

			}
			Set<String> pausedTriggers = sched.getPausedTriggerGroups();
			if (pausedTriggers != null) {

				for (String group : pausedTriggers) {
					for (TriggerKey triggerKey : sched.getTriggerKeys(GroupMatcher.triggerGroupEquals(group))) {
						if (triggerDtoMap.containsKey(triggerKey)) {
							triggerDtoMap.get(triggerKey).setPaused(true);
						}
					}
				}
			}
		}
		return list;

	}

	public List<? extends Trigger> getJobTriggers(JobKey jobKey) throws SchedulerException {
		Scheduler sched = getScheduler();
		if (sched != null) {
			return sched.getTriggersOfJob(jobKey);
		}
		return null;
	}

	public List<Trigger> getScheduledTriggers() throws SchedulerException {
		List<Trigger> list = new ArrayList<Trigger>();
		Scheduler sched = getScheduler();
		if (sched != null) {
			for (String group : sched.getTriggerGroupNames()) {
				for (TriggerKey triggerKey : sched.getTriggerKeys(GroupMatcher.triggerGroupEquals(group))) {
					Trigger trigger = sched.getTrigger(triggerKey);
					list.add(trigger);
				}
			}
		}
		return list;
	}

}
