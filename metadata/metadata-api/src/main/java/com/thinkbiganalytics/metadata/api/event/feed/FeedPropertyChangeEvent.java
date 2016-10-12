package com.thinkbiganalytics.metadata.api.event.feed;

import com.thinkbiganalytics.datalake.authorization.model.HadoopAuthorizationGroup;
import com.thinkbiganalytics.metadata.api.event.AbstractMetadataEvent;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.security.HadoopSecurityGroup;

import java.util.List;
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
    private String feedCategory;
    private String feedName;
    private List<String> hadoopSecurityGroupNames;

    public FeedPropertyChangeEvent(String feedCategory, String feedName, List<String> hadoopSecurityGroupNames, Map<String, Object> oldProperties, Map<String, Object> newProperties) {
        super("");
        this.oldProperties = convertMapToProperties(oldProperties);
        this.newProperties = convertMapToProperties(newProperties);
        this.feedCategory = feedCategory;
        this.feedName = feedName;
        this.hadoopSecurityGroupNames = hadoopSecurityGroupNames;
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

    public String getFeedCategory() {
        return feedCategory;
    }

    public String getFeedName() {
        return feedName;
    }

    public List<String> getHadoopSecurityGroupNames() {
        return hadoopSecurityGroupNames;
    }
}
