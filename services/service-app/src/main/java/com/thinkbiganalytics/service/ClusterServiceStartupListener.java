package com.thinkbiganalytics.service;

/*-
 * #%L
 * thinkbig-service-app
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

import com.thinkbiganalytics.app.ServicesApplicationStartup;
import com.thinkbiganalytics.app.ServicesApplicationStartupListener;
import com.thinkbiganalytics.cluster.ClusterService;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

@Service
public class ClusterServiceStartupListener implements ServicesApplicationStartupListener {

    private static final Logger log = LoggerFactory.getLogger(ClusterServiceStartupListener.class);

    @Inject
    ServicesApplicationStartup startup;

    @Inject
    ClusterService clusterService;

    public void onStartup(DateTime startTime) {
        try {
            clusterService.start();
        } catch (Exception e) {
            log.error("Error with startup", e);
        }
    }

    @PostConstruct
    private void init() {
        startup.subscribe(this);
    }


    @PreDestroy
    private void destroy() {
        try {
            clusterService.stop();
        } catch (Exception e) {
            log.error("Error with destroy", e);
        }
    }

}
