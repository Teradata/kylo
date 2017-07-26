package com.thinkbiganalytics.nifi.provenance.repo;

/*-
 * #%L
 * thinkbig-nifi-provenance-repo
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

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

/**
 * Ability for NiFi to refresh the config properties..
 * Disabled for now
 */
public class ConfigurationPropertiesRefresher {

    private static final Logger log = LoggerFactory.getLogger(ConfigurationPropertiesRefresher.class);

    ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

    @PostConstruct
    private void init() {
        initTimerThread();
    }

    public void checkAndRefreshProperties() {
        if(ConfigurationProperties.getInstance().isModified()) {
            Map<String, ConfigurationProperties.PropertyChange> changes = ConfigurationProperties.getInstance().refresh();

            if (changes != null && !changes.isEmpty()) {
                ConfigurationProperties.PropertyChange runInterval = changes.get(ConfigurationProperties.RUN_INTERVAL_KEY);
                if (runInterval != null) {
                    FeedStatisticsManager.getInstance().resetStatisticsInterval(new Long(runInterval.getNewValue()));
                    log.info("Reset {} ", runInterval);
                }
                ConfigurationProperties.PropertyChange maxEvents = changes.get(ConfigurationProperties.MAX_FEED_EVENTS_KEY);
                if (maxEvents != null) {
                    FeedStatisticsManager.getInstance().resetMaxEvents(new Integer(maxEvents.getNewValue()));
                    log.info("Reset {} ", maxEvents);
                }
                ConfigurationProperties.PropertyChange backupLocation = changes.get(ConfigurationProperties.BACKUP_LOCATION_KEY);
                if (backupLocation != null) {
                    FeedEventStatistics.getInstance().setBackupLocation(backupLocation.getNewValue());
                    log.info("Reset {} ", backupLocation);
                }
                ConfigurationProperties.PropertyChange orphanChildFlowFiles = changes.get(ConfigurationProperties.ORPHAN_CHILD_FLOW_FILE_PROCESSORS_KEY);
                if (orphanChildFlowFiles != null) {
                    FeedEventStatistics.getInstance().updateEventTypeProcessorTypeSkipChildren(orphanChildFlowFiles.getNewValue());
                }

            }
        }
    }




    private void initTimerThread() {
        log.info("Timer thread running every {} seconds to detect changes in the config.properties ",30);
        service.scheduleAtFixedRate(() -> {
            checkAndRefreshProperties();
        }, 30, 30, TimeUnit.SECONDS);

    }

}
