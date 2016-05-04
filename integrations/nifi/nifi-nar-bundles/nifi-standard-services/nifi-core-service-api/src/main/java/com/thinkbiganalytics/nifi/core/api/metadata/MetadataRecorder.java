/**
 * 
 */
package com.thinkbiganalytics.nifi.core.api.metadata;

import org.apache.nifi.flowfile.FlowFile;
import org.joda.time.DateTime;

/**
 *
 * @author Sean Felten
 */
public interface MetadataRecorder {
    
    FlowFile recordLastLoadTime(FlowFile ff, String destId);
    
    DateTime getLastLoadTime(FlowFile ff, String destId);
    
    // TODO Other forms or high-water mark recording besides time-based (last file, record ID, etc.)?
    
    boolean isFeedInitialized();
    
    void recoredFeedInitialized();
    
    void updateFeedStatus(String statusMsg);

}
