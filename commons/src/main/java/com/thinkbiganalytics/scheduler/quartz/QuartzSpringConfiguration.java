package com.thinkbiganalytics.scheduler.quartz;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.inject.Inject;

/**
 * Created by sr186054 on 9/20/15.
 */
@Configuration
public class QuartzSpringConfiguration {

    @Inject
    private ApplicationContext applicationContext;

    @Bean(name="schedulerFactoryBean")
    public SchedulerFactoryBean schedulerFactoryBean() {
        SchedulerFactoryBean scheduler = new SchedulerFactoryBean();
        scheduler.setApplicationContextSchedulerContextKey("applicationContext");
        //Enable autowiring of Beans inside each QuartzJobBean
        AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        scheduler.setJobFactory(jobFactory);

        return scheduler;
    }


}
