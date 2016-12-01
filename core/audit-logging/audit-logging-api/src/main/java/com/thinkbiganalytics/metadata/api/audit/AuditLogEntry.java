/**
 * 
 */
package com.thinkbiganalytics.metadata.api.audit;

import java.io.Serializable;
import java.security.Principal;
import java.util.UUID;

import org.joda.time.DateTime;

/**
 * An audit log entry describing a metadata change or operation attempt.
 * @author Sean Felten
 */
public interface AuditLogEntry {

    interface ID extends Serializable { }
    
    ID getId();
    
    DateTime getCreatedTime();
    
    Principal getUser();
    
    String getType();
    
    String getDescription();
    
    UUID getEntityId();
}
