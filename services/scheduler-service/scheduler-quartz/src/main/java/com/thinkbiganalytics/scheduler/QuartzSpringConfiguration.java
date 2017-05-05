package com.thinkbiganalytics.scheduler;

/*-
 * #%L
 * thinkbig-scheduler-quartz
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.inject.Inject;
import javax.sql.DataSource;

/**
 * Spring configuration to setup the Quartz scheduler
 */
@Configuration
public class QuartzSpringConfiguration {

    private static final Logger log = LoggerFactory.getLogger(QuartzSpringConfiguration.class);

    @Inject
    private ApplicationContext applicationContext;

    @Bean(name = "schedulerFactoryBean")
    public SchedulerFactoryBean schedulerFactoryBean( @Qualifier( "dataSource") DataSource dataSource) {
        SchedulerFactoryBean scheduler = new SchedulerFactoryBean();
        scheduler.setApplicationContextSchedulerContextKey("applicationContext");
        Resource resource = new ClassPathResource("quartz.properties");
        if(resource.exists()) {
            scheduler.setConfigLocation(resource);
            scheduler.setDataSource(dataSource);
        }
        //Enable autowiring of Beans inside each QuartzJobBean
        AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        scheduler.setJobFactory(jobFactory);

        return scheduler;
    }


    @Bean
    public QuartzClusterMessageReceiver quartzClusterMessageReceiver(){
        return new QuartzClusterMessageReceiver();
    }

    @Bean
    public QuartzClusterMessageSender quartzClusterMessageSender(){
        return new QuartzClusterMessageSender();
    }

    @Bean
    public QuartzSchedulerClusterListener quartzSchedulerClusterListener() {
        return new QuartzSchedulerClusterListener();
    }
}
