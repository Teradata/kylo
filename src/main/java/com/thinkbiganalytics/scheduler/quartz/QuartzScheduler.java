package com.thinkbiganalytics.scheduler.quartz;

import com.thinkbiganalytics.scheduler.JobIdentifier;
import com.thinkbiganalytics.scheduler.JobSchedulerExecption;
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

	private static Logger LOG = LoggerFactory.getLogger(QuartzScheduler.class);

	@Autowired
	@Qualifier("schedulerFactoryBean")
	private SchedulerFactoryBean schedulerFactoryBean;



	private QuartzScheduler()
	{

	}

	private JobDetail getJobDetail(JobIdentifier jobIdentifier, Object task, String runMethod) throws NoSuchMethodException, ClassNotFoundException {
		MethodInvokingJobDetailFactoryBean bean = new MethodInvokingJobDetailFactoryBean();
		bean.setTargetObject(task);
		bean.setTargetMethod(runMethod);
		bean.setName(jobIdentifier.getName());
		bean.setGroup(jobIdentifier.getGroupName());
		bean.afterPropertiesSet();
		return bean.getObject();
	}


	public void scheduleWithCronExpression(JobIdentifier jobIdentifier, Runnable task, String cronExpression) throws JobSchedulerExecption{
		scheduleWithCronExpressionInTimeZone(jobIdentifier, task, "run", cronExpression, null);
	}

	void scheduleWithCronExpressionInTimeZone(JobIdentifier jobIdentifier, Runnable task, String cronExpression, TimeZone timeZone) throws JobSchedulerExecption {
		scheduleWithCronExpressionInTimeZone(jobIdentifier, task, "run", cronExpression,timeZone);
	}
	public void scheduleWithCronExpression(JobIdentifier jobIdentifier,Object task, String runMethod, String cronExpression) throws JobSchedulerExecption {
		scheduleWithCronExpressionInTimeZone(jobIdentifier, task, runMethod, cronExpression,null);
	}

	public void scheduleWithCronExpressionInTimeZone(JobIdentifier jobIdentifier,Object task, String runMethod, String cronExpression, TimeZone timeZone) throws JobSchedulerExecption {
		try {
			JobDetail jobDetail = getJobDetail(jobIdentifier,task,runMethod);
			if(timeZone == null){
				timeZone = TimeZone.getDefault();
			}

			Trigger trigger = TriggerBuilder.newTrigger()
					.withIdentity(new TriggerKey("trigger_"+jobIdentifier.getUniqueName(), jobIdentifier.getGroupName()))
					.forJob(jobDetail)
					.withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)
							.inTimeZone(timeZone)
							.withMisfireHandlingInstructionFireAndProceed()).build();


			scheduleJob(jobDetail,trigger);
		}
		catch(Exception e){
			throw new JobSchedulerExecption();
		}
	}


	public  void schedule(JobIdentifier jobIdentifier, Runnable task, Date startTime) throws JobSchedulerExecption{
		schedule(jobIdentifier, task, "run", startTime);
	}

	public  void schedule(JobIdentifier jobIdentifier,Object task, String runMethod, Date startTime) throws JobSchedulerExecption {

		scheduleWithFixedDelay(jobIdentifier, task, runMethod, startTime, 0L);
	}


	public  void scheduleWithFixedDelay(JobIdentifier jobIdentifier,Object task, String runMethod, Date startTime, long startDelay) throws JobSchedulerExecption {
		try {
			JobDetail jobDetail = getJobDetail(jobIdentifier, task, runMethod);
			Date triggerStartTime = startTime;
			if(startDelay > 0L || startTime == null) {
				triggerStartTime = new Date(System.currentTimeMillis() + startDelay);
			}
			Trigger trigger = newTrigger().withIdentity(new TriggerKey(jobIdentifier.getName(), jobIdentifier.getGroupName())).forJob(jobDetail).startAt(triggerStartTime).build();
			getScheduler().scheduleJob(jobDetail, trigger);
		} catch (Exception e) {
			throw new JobSchedulerExecption();
		}
	}


	public  void scheduleWithFixedDelay(JobIdentifier jobIdentifier, Runnable runnable, Date startTime, long startDelay) throws JobSchedulerExecption {
		scheduleWithFixedDelay(jobIdentifier, runnable, "run", startTime, startDelay);
	}



	public  void scheduleAtFixedRate(JobIdentifier jobIdentifier,Object task, String runMethod, Date startTime, long period) throws JobSchedulerExecption {
		scheduleAtFixedRateWithDelay(jobIdentifier, task, runMethod, startTime, period, 0L);
	}

	public  void scheduleAtFixedRate(JobIdentifier jobIdentifier, Runnable runnable, Date startTime, long period) throws JobSchedulerExecption {
		scheduleAtFixedRate(jobIdentifier, runnable, "run", startTime, period);
	}


	public  void scheduleAtFixedRateWithDelay(JobIdentifier jobIdentifier,Object task, String runMethod, Date startTime, long period, long startDelay) throws JobSchedulerExecption {
		try {
			JobDetail jobDetail = getJobDetail(jobIdentifier, task, runMethod);
			Date triggerStartTime = startTime;
			if(startDelay > 0L || startTime == null) {
				triggerStartTime = new Date(System.currentTimeMillis() + startDelay);
			}
			Trigger trigger = TriggerBuilder.newTrigger().withIdentity(new TriggerKey(jobIdentifier.getName(), jobIdentifier.getGroupName())).forJob(jobDetail).startAt(triggerStartTime).withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInMilliseconds(period).repeatForever()).build();
			getScheduler().scheduleJob(jobDetail, trigger);
		} catch (Exception e) {
			throw new JobSchedulerExecption();
		}
	}

	public  void scheduleAtFixedRateWithDelay(JobIdentifier jobIdentifier, Runnable runnable, Date startTime, long period, long startDelay) throws JobSchedulerExecption {
		scheduleAtFixedRateWithDelay(jobIdentifier, runnable, startTime, period, startDelay);
	}


	/** Quartz Specific Methods **/










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

	public boolean deleteJob(String jobName, String jobGroup) throws SchedulerException {
		Scheduler sched = getScheduler();
		boolean success = false;
		if (sched != null) {
			success = sched.deleteJob(new JobKey(jobName,jobGroup));
		}
		return success;
	}

	public void pauseScheduler()  throws SchedulerException {
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
