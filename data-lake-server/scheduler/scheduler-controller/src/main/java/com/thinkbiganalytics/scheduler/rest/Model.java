package com.thinkbiganalytics.scheduler.rest;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.thinkbiganalytics.scheduler.JobIdentifier;
import com.thinkbiganalytics.scheduler.JobInfo;
import com.thinkbiganalytics.scheduler.TriggerIdentifier;
import com.thinkbiganalytics.scheduler.model.DefaultJobIdentifier;
import com.thinkbiganalytics.scheduler.model.DefaultTriggerIdentifier;
import com.thinkbiganalytics.scheduler.model.DefaultTriggerInfo;
import com.thinkbiganalytics.scheduler.rest.model.ScheduleIdentifier;
import com.thinkbiganalytics.scheduler.rest.model.ScheduledJob;
import com.thinkbiganalytics.scheduler.rest.model.TriggerInfo;
import com.thinkbiganalytics.scheduler.support.ScheduledJobState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by sr186054 on 4/14/16.
 */
public class Model {


  public static final Function<TriggerIdentifier, ScheduleIdentifier>
      DOMAIN_TO_TRIGGER_IDENTIFIER =
      new Function<TriggerIdentifier, ScheduleIdentifier>() {
        @Override
        public ScheduleIdentifier apply(TriggerIdentifier triggerIdentifier) {
          ScheduleIdentifier
              scheduleIdentifier =
              new ScheduleIdentifier(triggerIdentifier.getName(), triggerIdentifier.getGroup());
          return scheduleIdentifier;
        }
      };

  public static final Function<JobIdentifier, ScheduleIdentifier>
      DOMAIN_TO_JOB_IDENTIFIER =
      new Function<JobIdentifier, ScheduleIdentifier>() {
        @Override
        public ScheduleIdentifier apply(JobIdentifier jobIdentifier) {
          ScheduleIdentifier scheduleIdentifier = new ScheduleIdentifier(jobIdentifier.getName(), jobIdentifier.getGroup());
          return scheduleIdentifier;
        }
      };


  public static final Function<ScheduleIdentifier, TriggerIdentifier>
      TRIGGER_IDENTIFIER_TO_DOMAIN =
      new Function<ScheduleIdentifier, TriggerIdentifier>() {
        @Override
        public TriggerIdentifier apply(ScheduleIdentifier scheduleIdentifier) {
          TriggerIdentifier
              triggerIdentifier =
              new DefaultTriggerIdentifier(scheduleIdentifier.getName(), scheduleIdentifier.getGroup());
          return triggerIdentifier;
        }
      };

  public static final Function<ScheduleIdentifier, JobIdentifier>
      JOB_IDENTIFIER_TO_DOMAIN =
      new Function<ScheduleIdentifier, JobIdentifier>() {
        @Override
        public JobIdentifier apply(ScheduleIdentifier scheduleIdentifier) {
          JobIdentifier jobIdentifier = new DefaultJobIdentifier(scheduleIdentifier.getName(), scheduleIdentifier.getGroup());
          return jobIdentifier;
        }
      };


  public static final Function<TriggerInfo, com.thinkbiganalytics.scheduler.model.DefaultTriggerInfo>
      TRIGGER_INFO_TO_DOMAIN =
      new Function<TriggerInfo, DefaultTriggerInfo>() {
        @Override
        public DefaultTriggerInfo apply(TriggerInfo triggerInfo) {
          JobIdentifier jobIdentifier = JOB_IDENTIFIER_TO_DOMAIN.apply(triggerInfo.getJobIdentifier());
          TriggerIdentifier triggerIdentifier = TRIGGER_IDENTIFIER_TO_DOMAIN.apply(triggerInfo.getTriggerIdentifier());
          DefaultTriggerInfo domain = new DefaultTriggerInfo(jobIdentifier, triggerIdentifier);
          return domain;
        }
      };


  public static final Function<com.thinkbiganalytics.scheduler.TriggerInfo, TriggerInfo>
      DOMAIN_TO_TRIGGER_INFO =
      new Function<com.thinkbiganalytics.scheduler.TriggerInfo, TriggerInfo>() {
        @Override
        public TriggerInfo apply(com.thinkbiganalytics.scheduler.TriggerInfo domain) {
          ScheduleIdentifier jobIdentifier = DOMAIN_TO_JOB_IDENTIFIER.apply(domain.getJobIdentifier());
          ScheduleIdentifier triggerIdentifier = DOMAIN_TO_TRIGGER_IDENTIFIER.apply(domain.getTriggerIdentifier());
          TriggerInfo triggerInfo = new TriggerInfo(jobIdentifier, triggerIdentifier);
          return triggerInfo;
        }
      };


  public static List<TriggerInfo> domainToTriggerInfo(Collection<com.thinkbiganalytics.scheduler.TriggerInfo> triggerInfos) {
    return new ArrayList<>(Collections2.transform(triggerInfos, DOMAIN_TO_TRIGGER_INFO));
  }

  public static final Function<JobInfo, ScheduledJob> DOMAIN_TO_SCHEDULED_JOB = new Function<JobInfo, ScheduledJob>() {

    @Override
    public ScheduledJob apply(JobInfo jobInfo) {
      ScheduledJob jobBean = new ScheduledJob();
      jobBean.setJobIdentifier(Model.DOMAIN_TO_JOB_IDENTIFIER.apply(jobInfo.getJobIdentifier()));
      jobBean.setJobGroup(jobInfo.getJobIdentifier().getGroup());
      jobBean.setJobName(jobInfo.getJobIdentifier().getName());
      jobBean.setTriggers(Model.domainToTriggerInfo(jobInfo.getTriggers()));
      jobBean.setIsPaused(ScheduledJobState.isPaused(jobInfo.getTriggers()));
      jobBean.setIsScheduled(ScheduledJobState.isScheduled(jobInfo.getTriggers()));
      jobBean.setIsRunning(ScheduledJobState.isRunning(jobInfo.getTriggers()));
      jobBean.setCronExpressionData();
      jobBean.setState();
      return jobBean;
    }

  };


}
