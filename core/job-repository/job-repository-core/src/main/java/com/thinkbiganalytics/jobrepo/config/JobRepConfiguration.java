package com.thinkbiganalytics.jobrepo.config;

import com.thinkbiganalytics.jobrepo.JobRepoApplicationStartupListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by sr186054 on 5/9/16.
 */
@Configuration
public class JobRepConfiguration {

    private static final Logger log = LoggerFactory.getLogger(JobRepConfiguration.class);

    @Bean
    public JobRepoApplicationStartupListener jobRepoApplicationStartupListener() {
        return new JobRepoApplicationStartupListener();
    }

}
