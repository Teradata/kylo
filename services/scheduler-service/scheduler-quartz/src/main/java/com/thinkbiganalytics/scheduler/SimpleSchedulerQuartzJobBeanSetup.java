package com.thinkbiganalytics.scheduler;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Map;

import javax.annotation.PostConstruct;

/**
 * Generic Class to be used as a Bean to help Schedule any QuartzJobBean Class via Java @Configuration or XML
 *
 * If using xml us the util:map to inject the map of data into the dataMap property (example) <util:map id="job1.dataMap"
 * map-class="java.util.HashMap"> <entry key="x" value="y"/> <entry key="y"><ref bean="X"/></entry> </util:map>
 *
 * <bean id="schedule.job1" class="com.thinkbiganalytics.scheduler.quartz.SimpleSchedulerQuartzJobBeanSetup"> <property
 * name="dataMap" ref="job1.dataMap" /> <property name="....
 */


public class SimpleSchedulerQuartzJobBeanSetup {

  private String cronExpresson;
  private String groupName;
  private String jobName;
  private String quartzJobBean;
  private Map<String, Object> dataMap;
  private boolean fireImmediately;
  private static final Logger LOG = LoggerFactory.getLogger(SimpleSchedulerQuartzJobBeanSetup.class);

  @Autowired
  @Qualifier("quartzScheduler")
  private QuartzScheduler quartzScheduler;

  @PostConstruct
  private void schedule() {
    try {
      scheduleMetadataJob();
    } catch (SchedulerException e) {
      throw new RuntimeException(e);
    }
  }

  public void scheduleMetadataJob() throws SchedulerException {
    try {
      Class clazz = Class.forName(quartzJobBean);
      quartzScheduler.scheduleJob("Podium", jobName, clazz, cronExpresson, dataMap, fireImmediately);
    } catch (ClassNotFoundException e) {
      //throw new RuntimeException(e);
    }
  }

  public void setCronExpresson(String cronExpresson) {
    this.cronExpresson = cronExpresson;
  }

  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }

  public void setJobName(String jobName) {
    this.jobName = jobName;
  }

  public void setQuartzJobBean(String quartzJobBean) {
    this.quartzJobBean = quartzJobBean;
  }

  public void setDataMap(Map<String, Object> dataMap) {
    this.dataMap = dataMap;
  }

  public void setQuartzScheduler(QuartzScheduler quartzScheduler) {
    this.quartzScheduler = quartzScheduler;
  }

  public void setFireImmediately(boolean fireImmediately) {
    this.fireImmediately = fireImmediately;
  }
}
