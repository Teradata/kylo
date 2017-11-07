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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Configuration Properties used for the Provenance reporting
 */
public class ConfigurationProperties {

    private static final Logger log = LoggerFactory.getLogger(ConfigurationProperties.class);

    public static final String DEFAULT_BACKUP_LOCATION = "/opt/nifi/feed-event-statistics.gz";
    public static final Integer DEFAULT_MAX_EVENTS = 10;
    public static final Long DEFAULT_RUN_INTERVAL_MILLIS = 3000L;
    public static final Integer DEFAULT_THROTTLE_STARTING_FEED_FLOWS_THRESHOLD = 15;
    public static final Integer DEFAULT_THROTTLE_STARTING_FEED_FLOWS_TIME_PERIOD_MILLIS = 1000;

    public static final String DEFAULT_ORPHAN_CHILD_FLOW_FILE_PROCESSORS = "{\"CLONE\":[\"ConvertCSVToAvro\"]}";

    public static final String BACKUP_LOCATION_KEY = "backupLocation";
    public static final String MAX_FEED_EVENTS_KEY = "maxFeedEvents";
    public static final String RUN_INTERVAL_KEY = "runInterval";
    public static final String ORPHAN_CHILD_FLOW_FILE_PROCESSORS_KEY="orphanChildFlowFileProcessors";

    private Properties properties = new Properties();

    private Long runInterval = DEFAULT_RUN_INTERVAL_MILLIS;
    private Integer maxFeedEvents = DEFAULT_MAX_EVENTS;
    private String backupLocation = DEFAULT_BACKUP_LOCATION;
    private Integer throttleStartingFeedFlowsThreshold = DEFAULT_THROTTLE_STARTING_FEED_FLOWS_THRESHOLD;
    private Integer throttleStartingFeedFlowsTimePeriodMillis = DEFAULT_THROTTLE_STARTING_FEED_FLOWS_TIME_PERIOD_MILLIS;

    //JSON MAP of eventType to processors that create children that are removed without provenance.
    private String orphanChildFlowFileProcessorsString;


    private static final ConfigurationProperties instance = new ConfigurationProperties();

    private ConfigurationProperties() {
        load();
    }

    public static ConfigurationProperties getInstance() {
        return instance;
    }


    private String getConfigLocation(){
        return  System.getProperty("kylo.nifi.configPath");
    }

    private FileInputStream getConfigFileInputStream(String location) throws Exception{
        return new FileInputStream(location + "/config.properties");
    }

    private File getConfigFile(String location){
        return new File(location + "/config.properties");
    }

    private Long lastModified = null;

    private void load() {
        String location = null;
        try {
            location = getConfigLocation();
            properties.clear();
            File file = getConfigFile(getConfigLocation());
            properties.load(getConfigFileInputStream(location));
            setValues();
            if(file.exists()) {
                lastModified = file.lastModified();
            }
        } catch (Exception e) {
            log.error("Unable to load properties for location: {} , {} ", location, e.getMessage(), e);
        }
    }

    private void setValues() {
        this.backupLocation = properties.getProperty("kylo.provenance.cache.location", DEFAULT_BACKUP_LOCATION);
        this.maxFeedEvents = new Integer(properties.getProperty("kylo.provenance.max.starting.events", DEFAULT_MAX_EVENTS + ""));
        this.runInterval = new Long(properties.getProperty("kylo.provenance.run.interval.millis", DEFAULT_RUN_INTERVAL_MILLIS + ""));

        this.throttleStartingFeedFlowsThreshold = new Integer(properties.getProperty("kylo.provenance.event.count.throttle.threshold", DEFAULT_THROTTLE_STARTING_FEED_FLOWS_THRESHOLD + ""));
        this.throttleStartingFeedFlowsTimePeriodMillis = new Integer(properties.getProperty("kylo.provenance.event.throttle.threshold.time.millis", DEFAULT_THROTTLE_STARTING_FEED_FLOWS_TIME_PERIOD_MILLIS + ""));
        orphanChildFlowFileProcessorsString = properties.getProperty("kylo.provenance.orphan.child.flowfile.processors", DEFAULT_ORPHAN_CHILD_FLOW_FILE_PROCESSORS);
        //only update this on the initial run.  Any changes will be detected and updated with the ConfigurationPropertiesRefresher
        if(lastModified == null) {
            FeedEventStatistics.getInstance().updateEventTypeProcessorTypeSkipChildren(orphanChildFlowFileProcessorsString);
        }
    }

    public String getFeedEventStatisticsBackupLocation() {
        return StringUtils.isBlank(backupLocation) ? DEFAULT_BACKUP_LOCATION : backupLocation;
    }

    /**
     * The Max allowed feed flow files to send through to ops manager per the processing run interval
     */
    public Integer getFeedProcessorMaxEvents() {
        return maxFeedEvents == null ? DEFAULT_MAX_EVENTS : maxFeedEvents;
    }

    public Integer getThrottleStartingFeedFlowsThreshold() {
        return throttleStartingFeedFlowsThreshold == null ? DEFAULT_THROTTLE_STARTING_FEED_FLOWS_THRESHOLD : throttleStartingFeedFlowsThreshold;
    }

    public Integer getDefaultThrottleStartingFeedFlowsTimePeriodMillis(){
        return throttleStartingFeedFlowsTimePeriodMillis == null ? DEFAULT_THROTTLE_STARTING_FEED_FLOWS_THRESHOLD : throttleStartingFeedFlowsTimePeriodMillis;
    }

    public Long getFeedProcessingRunInterval() {
        return runInterval == null ? DEFAULT_RUN_INTERVAL_MILLIS : runInterval;
    }



    public void populateChanges(Map<String, PropertyChange> changes, boolean old) {
        changes.computeIfAbsent(BACKUP_LOCATION_KEY, key -> new PropertyChange(key)).setValue(backupLocation, old);
        changes.computeIfAbsent(MAX_FEED_EVENTS_KEY, key -> new PropertyChange(key)).setValue(maxFeedEvents + "", old);
        changes.computeIfAbsent(RUN_INTERVAL_KEY, key -> new PropertyChange(key)).setValue(runInterval + "", old);
        changes.computeIfAbsent(ORPHAN_CHILD_FLOW_FILE_PROCESSORS_KEY,key -> new PropertyChange(key)).setValue(orphanChildFlowFileProcessorsString, old);
    }

    public Map<String, PropertyChange> refresh() {
            Map<String, PropertyChange> changes = new HashMap<>();
            populateChanges(changes, true);
            load();
            populateChanges(changes, false);
        return changes.values().stream().filter(propertyChange -> propertyChange.changed()).collect(Collectors.toMap(change -> change.getPropertyName(), Function.identity()));
    }

    public boolean isModified(){
        File file = getConfigFile(getConfigLocation());
        return (lastModified != null && file.exists() && file.lastModified() != lastModified  );
    }


    public class PropertyChange {

        private String propertyName;
        private String oldValue;
        private String newValue;

        public PropertyChange(String propertyName) {
            this.propertyName = propertyName;
        }

        public PropertyChange(String propertyName, String oldValue, String newValue) {
            this.propertyName = propertyName;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public void setPropertyName(String propertyName) {
            this.propertyName = propertyName;
        }

        public String getOldValue() {
            return oldValue;
        }

        public void setOldValue(String oldValue) {
            this.oldValue = oldValue;
        }

        public String getNewValue() {
            return newValue;
        }

        public void setNewValue(String newValue) {
            this.newValue = newValue;
        }

        public boolean changed() {
            return !oldValue.equals(newValue);
        }

        public void setValue(String value, boolean old) {
            if (old) {
                setOldValue(value);
            } else {
                setNewValue(value);
            }

        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("PropertyChange{");
            sb.append("propertyName='").append(propertyName).append('\'');
            sb.append(", oldValue='").append(oldValue).append('\'');
            sb.append(", newValue='").append(newValue).append('\'');
            sb.append('}');
            return sb.toString();
        }
    }
}
