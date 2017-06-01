package com.thinkbiganalytics.feedmgr.nifi;

/*-
 * #%L
 * thinkbig-feed-manager-controller
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationListener;

import com.thinkbiganalytics.spring.SpringEnvironmentProperties;

/**
 * Listen for when the spring application context is refreshed
 */
public class SpringCloudContextEnvironmentChangedListener implements ApplicationListener<EnvironmentChangeEvent> {

    @Autowired
    SpringEnvironmentProperties springEnvironmentProperties;

    @Override
    public void onApplicationEvent(EnvironmentChangeEvent event) {
        // reset the cache so the properties can partake in the @RefreshEvent
        //http://cloud.spring.io/spring-cloud-static/docs/1.0.x/spring-cloud.html#_refresh_scope
        springEnvironmentProperties.reset();
    }
}
