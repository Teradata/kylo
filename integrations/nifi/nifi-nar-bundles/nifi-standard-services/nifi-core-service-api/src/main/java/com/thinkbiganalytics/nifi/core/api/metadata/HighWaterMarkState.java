/**
 * 
 */
package com.thinkbiganalytics.nifi.core.api.metadata;

import java.io.Serializable;

import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.ProcessSession;

/**
 *
 * @author Sean Felten
 */
public interface HighWaterMarkState {

    <W extends Serializable> W loadWaterMark(ProcessSession session, FlowFile ff, String key) throws WaterMarkActiveException;
    
    <W extends Serializable> FlowFile recordWaterMark(ProcessSession session, 
                                                      FlowFile ff, 
                                                      String key, 
                                                      W markValue);
    
    FlowFile commitWaterMark(ProcessSession session, FlowFile ff, String key);
    
    FlowFile commitAllWaterMarks(ProcessSession session, FlowFile ff);
    
    FlowFile releaseWaterMark(ProcessSession session, FlowFile ff, String key);
    
    FlowFile releaseAllWaterMarks(ProcessSession session, FlowFile ff);
    
}
