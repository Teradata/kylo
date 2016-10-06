/**
 * 
 */
package com.thinkbiganalytics.nifi.v2.core.metadata;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.ProcessSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.thinkbiganalytics.metadata.rest.client.MetadataClient;
import com.thinkbiganalytics.metadata.rest.model.feed.Feed;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataConstants;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataRecorder;
import com.thinkbiganalytics.nifi.core.api.metadata.WaterMarkActiveException;

/**
 *
 * @author Sean Felten
 */
public class MetadataClientRecorder implements MetadataRecorder {
    
    private static final Logger log = LoggerFactory.getLogger(MetadataClientRecorder.class);
    private final ObjectReader WATER_MARKS_READER = new ObjectMapper().reader().forType(Map.class);
    private final ObjectWriter WATER_MARKS_WRITER = new ObjectMapper().writer().forType(Map.class);
    
    private MetadataClient client;
//    private Set<String> activeWaterMarks = Collections.synchronizedSet(new HashSet<>());
    // TODO: Remove this
    public Map<String, String> waterMarkValues = new HashMap<>();
    
    // TODO: Remove this
    public Map<String, Boolean> workaroundRegistration = new HashMap<>();

    
    public MetadataClientRecorder() {
        this(URI.create("http://localhost:8420/api/metadata"));
    }
    
    public MetadataClientRecorder(URI baseUri) {
        this(new MetadataClient(baseUri));
    }
    
    public MetadataClientRecorder(MetadataClient client) {
        this.client = client;
    }
    
    

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.nifi.core.api.metadata.MetadataRecorder#loadWaterMark(org.apache.nifi.processor.ProcessSession, org.apache.nifi.flowfile.FlowFile, java.lang.String, java.lang.String)
     */
    @Override
    public FlowFile loadWaterMark(ProcessSession session, FlowFile ff, String feedId, String waterMarkName, String parameterName, String defaultValue) throws WaterMarkActiveException {
        FlowFile resultFF = addActiveWaterMark(session, ff, waterMarkName, parameterName);
        String value = getHighWaterMarkValue(feedId, waterMarkName).orElse(defaultValue);
        return session.putAttribute(resultFF, parameterName, value);
    }
    
    private FlowFile addActiveWaterMark(ProcessSession session, FlowFile ff, String waterMarkName, String parameterName) throws WaterMarkActiveException {
        Map<String, String> actives = getActiveWaterMarks(ff);
        
        if (actives.containsKey(waterMarkName)) {
            throw new WaterMarkActiveException(waterMarkName);
        } else {
            actives.put(waterMarkName, parameterName);
            return setActiveWaterMarks(session, ff, actives);
        }
    }

    public FlowFile setActiveWaterMarks(ProcessSession session, FlowFile ff, Map<String, String> actives) {
        try {
            return session.putAttribute(ff, "activeWaterMarks", WATER_MARKS_WRITER.writeValueAsString(actives));
        } catch (Exception e) {
            // Should never happen.
            throw new IllegalStateException(e);
        }
    }
    
    private Map<String, String> getActiveWaterMarks(FlowFile ff) {
        try {
            String activeStr = ff.getAttribute("activeWaterMarks");
            
            if (activeStr == null) {
                return new HashMap<>();
            } else {
                return WATER_MARKS_READER.readValue(activeStr);
            }
        } catch (Exception e) {
            // Should never happen.
            throw new IllegalStateException(e);
        }
    }

    private Optional<String> getHighWaterMarkValue(String feedId, String waterMarkName) {
        return Optional.ofNullable(this.waterMarkValues.get(waterMarkName));
//        return Optional.ofNullable(this.client.getHighWaterMark(feedId, waterMarkName));
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.nifi.core.api.metadata.MetadataRecorder#recordWaterMark(org.apache.nifi.processor.ProcessSession, org.apache.nifi.flowfile.FlowFile, java.lang.String, java.lang.String, java.io.Serializable)
     */
    @Override
    public FlowFile recordWaterMark(ProcessSession session, FlowFile ff, String feedId, String waterMarkName, String parameterName, String newValue) {
        Map<String, String> actives = getActiveWaterMarks(ff);
        
        if (actives.containsKey(waterMarkName)) {
            return session.putAttribute(ff, parameterName, newValue);
        } else {
            throw new IllegalStateException("No active high-water mark named \"" + waterMarkName + "\"");
        }
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.nifi.core.api.metadata.MetadataRecorder#commitWaterMark(org.apache.nifi.processor.ProcessSession, org.apache.nifi.flowfile.FlowFile, java.lang.String, java.lang.String)
     */
    @Override
    public FlowFile commitWaterMark(ProcessSession session, FlowFile ff, String feedId, String waterMarkName) {
        Map<String, String> actives = getActiveWaterMarks(ff);
        
        if (actives.containsKey(waterMarkName)) {
            String parameterName = actives.remove(waterMarkName);
            String value = ff.getAttribute(parameterName);
            
            updateHighWaterMarkValue(feedId, waterMarkName, value);
            return setActiveWaterMarks(session, ff, actives);
        } else {
            throw new IllegalStateException("No active high-water mark named \"" + waterMarkName + "\"");
        }
    }

    public void updateHighWaterMarkValue(String feedId, String waterMarkName, String value) {
        this.waterMarkValues.put(waterMarkName, value);
//        this.client.updateHighWaterMark(feedId, waterMarkName, value);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.nifi.core.api.metadata.MetadataRecorder#commitAllWaterMarks(org.apache.nifi.processor.ProcessSession, org.apache.nifi.flowfile.FlowFile, java.lang.String)
     */
    @Override
    public FlowFile commitAllWaterMarks(ProcessSession session, FlowFile ff, String feedId) {
        Map<String, String> actives = getActiveWaterMarks(ff);
        FlowFile resultFF = ff;
        
        // TODO do more efficiently
        for (String waterMarkName : new HashSet<String>(actives.keySet())) {
            resultFF = commitWaterMark(session, resultFF, feedId, waterMarkName);
        }
        
        return resultFF;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.nifi.core.api.metadata.MetadataRecorder#releaseWaterMark(org.apache.nifi.processor.ProcessSession, org.apache.nifi.flowfile.FlowFile, java.lang.String, java.lang.String)
     */
    @Override
    public FlowFile releaseWaterMark(ProcessSession session, FlowFile ff, String feedId, String waterMarkName) {
        Map<String, String> actives = getActiveWaterMarks(ff);
        FlowFile resultFF = ff;
        
        if (actives.containsKey(waterMarkName)) {
            String parameterName = actives.remove(waterMarkName);
            String value = getHighWaterMarkValue(feedId, waterMarkName)
                            .orElse(actives.get(defaultName(parameterName)));
            
            resultFF = session.putAttribute(resultFF, parameterName, value);
            return setActiveWaterMarks(session, resultFF, actives);
        } else {
            throw new IllegalStateException("No active high-water mark named \"" + waterMarkName + "\"");
        }
    }

    /**
     * @param parameterName
     * @return
     */
    private Object defaultName(String parameterName) {
        return parameterName + ".default";
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.nifi.core.api.metadata.MetadataRecorder#releaseAllWaterMarks(org.apache.nifi.processor.ProcessSession, org.apache.nifi.flowfile.FlowFile, java.lang.String)
     */
    @Override
    public FlowFile releaseAllWaterMarks(ProcessSession session, FlowFile ff, String feedId) {
        Map<String, String> actives = getActiveWaterMarks(ff);
        FlowFile resultFF = ff;
        
        for (String waterMarkName : new HashSet<String>(actives.keySet())) {
            resultFF = releaseWaterMark(session, resultFF, feedId, waterMarkName);
        }
        
        return resultFF;
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

//    @Override
//    // TODO: Remove workaroundwatermark
//    public void recordLastLoadTime(String systemCategory, String feedName, DateTime time) {
//        String key = feedKey(systemCategory,feedName);
//        workaroundWatermark.put(key, time);
//    }
//
//    @Override
//    // TODO: Remove workaroundwatermark
//    public DateTime getLastLoadTime(String systemCategory, String feedName) {
//        String key = feedKey(systemCategory,feedName);
//        DateTime dt =  workaroundWatermark.get(key);
//        return (dt == null ? new DateTime(0L) : dt);
//    }

    // TODO: Remove workaround
    private String feedKey(String systemCategory, String feedName) {
        return systemCategory+"."+feedName;
    }

}
