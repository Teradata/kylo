/**
 * 
 */
package com.thinkbiganalytics.metadata.api;

import org.joda.time.DateTime;

/**
 * Specifies a set of audit-related properties that most entities should provided.
 * 
 * @author Sean Felten
 */
public interface AuditedEntity {
    
    DateTime getCreatedTime();

    DateTime getModifiedTime();

}
