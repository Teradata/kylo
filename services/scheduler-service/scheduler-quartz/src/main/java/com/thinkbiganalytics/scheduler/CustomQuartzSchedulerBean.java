package com.thinkbiganalytics.scheduler;

import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;

/**
 * Created by sr186054 on 9/21/15.
 */
public class CustomQuartzSchedulerBean implements InitializingBean {

  private QuartzScheduler quartzScheduler;

  private CronTriggerFactoryBean cronTriggerFactoryBean;

  private MethodInvokingJobDetailFactoryBean methodInvokingJobDetailFactoryBean;

  private JobDetail jobDetail;

  private Trigger trigger;

  public void setQuartzScheduler(QuartzScheduler quartzScheduler) {
    this.quartzScheduler = quartzScheduler;
  }

  public void setCronTriggerFactoryBean(CronTriggerFactoryBean cronTriggerFactoryBean) {
    this.cronTriggerFactoryBean = cronTriggerFactoryBean;
  }

  public void setMethodInvokingJobDetailFactoryBean(MethodInvokingJobDetailFactoryBean methodInvokingJobDetailFactoryBean) {
    this.methodInvokingJobDetailFactoryBean = methodInvokingJobDetailFactoryBean;
  }

  public void setJobDetail(JobDetail jobDetail) {
    this.jobDetail = jobDetail;
  }

  public void setTrigger(Trigger trigger) {
    this.trigger = trigger;
  }

  @Override
  public void afterPropertiesSet() throws Exception {

    if (jobDetail != null && trigger != null) {
      quartzScheduler.scheduleJob(jobDetail, trigger);
    } else {

      if (methodInvokingJobDetailFactoryBean != null) {
        quartzScheduler.scheduleJob(methodInvokingJobDetailFactoryBean, cronTriggerFactoryBean);
      } else {
        quartzScheduler.scheduleJob(jobDetail, cronTriggerFactoryBean);
      }
    }

  }
}
