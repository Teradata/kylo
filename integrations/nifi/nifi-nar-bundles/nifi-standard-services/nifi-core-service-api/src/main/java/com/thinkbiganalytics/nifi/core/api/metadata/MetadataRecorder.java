/**
 * 
 */
package com.thinkbiganalytics.nifi.core.api.metadata;

import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.ProcessSession;
import org.joda.time.DateTime;

/**
 *
 * @author Sean Felten
 */
public interface MetadataRecorder {
    
    
    FlowFile recordLastLoadTime(ProcessSession session, FlowFile ff, String destId, DateTime time);
    
    DateTime getLastLoadTime(ProcessSession session, FlowFile ff, String destId);
    
    // TODO Other forms or high-water mark recording besides time-based (last file, record ID, etc.)?
    
    boolean isFeedInitialized(FlowFile ff);
    
    void recordFeedInitialization(ProcessSession session, FlowFile ff, boolean flag);
    
    void updateFeedStatus(ProcessSession session, FlowFile ff, String statusMsg);

    // TODO: Remove all following when working

    void recordFeedInitialization(String systemCategory, String feedName);

    boolean isFeedInitialized(String systemCategory, String feedName);

    void recordLastLoadTime(String systemCategory, String feedName, DateTime time);

    DateTime getLastLoadTime(String systemCategory, String feedName);

}
