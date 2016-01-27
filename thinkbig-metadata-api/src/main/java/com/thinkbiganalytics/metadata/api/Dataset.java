/**
 * 
 */
package com.thinkbiganalytics.metadata.api;

import java.util.List;

import org.joda.time.DateTime;

/**
 *
 * @author Sean Felten
 */
public interface Dataset {
    
    String getName();
    
    String getDescription();
    
    DateTime getCreationTime();
    
    List<ChangeSet> getChangeSets();

}
