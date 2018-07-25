/**
 * 
 */
package com.thinkbiganalytics.metadata.api;

import org.joda.time.DateTime;

/**
 * Identifies an entity that tracks creation and modification times, and 
 * the creator and modifier.
 */
public interface Auditable {
    
    DateTime getCreatedTime();

    DateTime getModifiedTime();

    String getCreatedBy();

    String getModifiedBy();

}
