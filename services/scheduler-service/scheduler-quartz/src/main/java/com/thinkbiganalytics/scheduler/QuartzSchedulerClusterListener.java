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

import com.thinkbiganalytics.cluster.ClusterService;
import com.thinkbiganalytics.cluster.ClusterServiceListener;

import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.inject.Inject;


public class QuartzSchedulerClusterListener implements ClusterServiceListener {

    private static final Logger log = LoggerFactory.getLogger(QuartzSchedulerClusterListener.class);

    @Inject
    ClusterService clusterService;

    @PostConstruct
    private void init(){
        clusterService.subscribe(this);
    }

    @Override
    public void onClusterMembershipChanged(List<String> previousMembers, List<String> currentMembers) {

    }

    @Override
    public void onConnected(List<String> currentMembers) {
        validateQuartzClusterConfiguration();
    }

    @Override
    public void onDisconnected(List<String> currentMembers) {

    }

    @Override
    public void onClosed(List<String> currentMembers) {

    }


    private void validateQuartzClusterConfiguration() {
        Resource resource = new ClassPathResource("quartz.properties");
            if (resource.exists()) {
                try {
                    Properties properties = PropertiesLoaderUtils.loadProperties(resource);
                    Boolean isClustered = BooleanUtils.toBoolean(properties.getProperty("org.quartz.jobStore.isClustered"));
                    boolean isValid = isClustered;
                    if(!isValid) {
                        log.error("Kylo is running in clustered mode by Quartz scheduler is not configured for clustered mode.  Please ensure the Quartz is configured for database persistence in clustered mode ");
                    }
                } catch (IOException e) {
                    log.error("Kylo is running in Clustered mode the system cannot find the 'quartz.properties' file.  Please ensure the Quartz is configured for database persistence in clustered mode", e);
                }
            } else {
                log.error("Kylo is running in Clustered mode the system cannot find the 'quartz.properties' file. Please ensure the Quartz is configured for database persistence in clustered mode");
            }

    }
}
