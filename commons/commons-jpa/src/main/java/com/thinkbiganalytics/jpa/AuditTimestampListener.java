/**
 * 
 */
package com.thinkbiganalytics.jpa;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import org.joda.time.DateTime;

/**
 *
 * @author Sean Felten
 */
public class AuditTimestampListener {

    @PrePersist
    public void setCreatedTime(AuditedEntity entity) {
        DateTime now = DateTime.now();
        entity.setCreatedTime(now);
        entity.setModifiedTime(now);
    }

    @PreUpdate
    public void setModifiedTime(AuditedEntity entity) {
        entity.setModifiedTime(DateTime.now());
    }
    
}
