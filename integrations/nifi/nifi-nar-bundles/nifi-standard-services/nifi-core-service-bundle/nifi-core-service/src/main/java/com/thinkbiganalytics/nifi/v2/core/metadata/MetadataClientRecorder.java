/**
 * 
 */
package com.thinkbiganalytics.nifi.v2.core.metadata;

import com.thinkbiganalytics.metadata.rest.client.MetadataClient;
import com.thinkbiganalytics.metadata.rest.model.Formatters;
import com.thinkbiganalytics.metadata.rest.model.feed.Feed;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataConstants;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataRecorder;

import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.ProcessSession;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Sean Felten
 */
public class MetadataClientRecorder implements MetadataRecorder {
    
    private static final Logger log = LoggerFactory.getLogger(MetadataClientRecorder.class);
    
    private MetadataClient client;

    // TODO: Remove this
    public Map<String,Boolean> workaroundRegistration = new HashMap<>();

    // TODO: remove this (
    public Map<String,DateTime> workaroundWatermark = new HashMap<>();


    public MetadataClientRecorder() {
        this(URI.create("http://localhost:8077/api/metadata"));
    }
    
    public MetadataClientRecorder(URI baseUri) {
        this(new MetadataClient(baseUri));
    }
    
    public MetadataClientRecorder(MetadataClient client) {
        this.client = client;
    }
    
    

    @Override
    public FlowFile recordLastLoadTime(ProcessSession session, FlowFile ff, String destination, DateTime time) {
        return session.putAttribute(ff, MetadataConstants.LAST_LOAD_TIME_PROP + "." + destination, Formatters.TIME_FORMATTER.print(time));
    }

    @Override
    public DateTime getLastLoadTime(ProcessSession session, FlowFile ff, String destination) {
        String timeStr = ff.getAttribute(MetadataConstants.LAST_LOAD_TIME_PROP + "." + destination);
        
        if (timeStr != null) {
            return Formatters.TIME_FORMATTER.parseDateTime(timeStr);
        } else {
            return null;
        }
    }

    @Override
    public boolean isFeedInitialized(FlowFile ff) {
        String feedId = ff.getAttribute(MetadataConstants.FEED_ID_PROP);
        
        if (feedId != null) {
            Feed feed = this.client.getFeed(feedId);
            
            if (feed != null) {
                return feed.isInitialized();
            } else {
                log.info("Could not confirm feed initialization - no feed exists with ID: {}", feedId);
                return false;
            } 
        } else {
            log.info("Could not confirm feed initialization - no feed ID in flow file", feedId);
            return false;
        } 
    }

    @Override
    public void recordFeedInitialization(ProcessSession session, FlowFile ff, boolean flag) {
        String feedId = ff.getAttribute(MetadataConstants.FEED_ID_PROP);
        
        if (feedId != null) {
            Feed feed = this.client.getFeed(feedId);
            
            if (feed != null) {
                feed.setInitialized(flag);
                this.client.updateFeed(feed);
            }
        }
    }



    @Override
    public void updateFeedStatus(ProcessSession session, FlowFile ff, String statusMsg) {
        // TODO Auto-generated method stub
        
    }

    @Override
    // TODO: Remove workaroundRegistration
    public void recordFeedInitialization(String systemCategory, String feedName) {
        String key = feedKey(systemCategory, feedName);
        log.warn("recordFeedInit feed {} size {}", key, workaroundRegistration.size());
        workaroundRegistration.put(key, true);
    }

    @Override
    // TODO: Remove workaroundRegistration
    public boolean isFeedInitialized(String systemCategory, String feedName) {
        String key = feedKey(systemCategory,feedName);
        Boolean result = workaroundRegistration.get(key);
        log.warn("isFeedInitialized feed {} size {} result {}", key, workaroundRegistration.size(), result);
        return (result == null ? false : result);
    }

    @Override
    // TODO: Remove workaroundwatermark
    public void recordLastLoadTime(String systemCategory, String feedName, DateTime time) {
        String key = feedKey(systemCategory,feedName);
        workaroundWatermark.put(key, time);
    }

    @Override
    // TODO: Remove workaroundwatermark
    public DateTime getLastLoadTime(String systemCategory, String feedName) {
        String key = feedKey(systemCategory,feedName);
        DateTime dt =  workaroundWatermark.get(key);
        return (dt == null ? new DateTime(0L) : dt);
    }

    // TODO: Remove workaround
    private String feedKey(String systemCategory, String feedName) {
        return systemCategory+"."+feedName;
    }

}
