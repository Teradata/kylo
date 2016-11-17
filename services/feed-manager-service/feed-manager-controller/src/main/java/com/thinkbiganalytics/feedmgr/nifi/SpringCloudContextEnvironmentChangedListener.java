package com.thinkbiganalytics.feedmgr.nifi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationListener;

/**
 * Created by sr186054 on 11/16/16.
 */
public class SpringCloudContextEnvironmentChangedListener implements ApplicationListener<EnvironmentChangeEvent> {

    @Autowired
    SpringEnvironmentProperties springEnvironmentProperties;

    @Override
    public void onApplicationEvent(EnvironmentChangeEvent event) {
        // reset the cache so the properties can partke in the @RefreshEvent
        //http://cloud.spring.io/spring-cloud-static/docs/1.0.x/spring-cloud.html#_refresh_scope
        springEnvironmentProperties.reset();
    }
}
