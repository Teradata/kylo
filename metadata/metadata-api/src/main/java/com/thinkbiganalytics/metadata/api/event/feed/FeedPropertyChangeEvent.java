package com.thinkbiganalytics.metadata.api.event.feed;

import com.thinkbiganalytics.metadata.api.event.AbstractMetadataEvent;
import com.thinkbiganalytics.metadata.api.feed.Feed;

import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Jeremy Merrifield on 10/7/16.
 */
public class FeedPropertyChangeEvent extends AbstractMetadataEvent<String> {

    private Properties oldProperties;
    private Properties newProperties;

    public FeedPropertyChangeEvent(Map<String, Object> oldProperties, Map<String, Object> newProperties) {
        super("");
        this.oldProperties = convertMapToProperties(oldProperties);
        this.newProperties = convertMapToProperties(newProperties);
    }

    public Properties getOldProperties() {
        return oldProperties;
    }

    public Properties getNewProperties() {
        return newProperties;
    }

    private Properties convertMapToProperties(Map<String, Object> map) {
        Properties properties = new Properties();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            properties.setProperty(entry.getKey(), String.valueOf(entry.getValue()));
        }
        return properties;
    }
}
