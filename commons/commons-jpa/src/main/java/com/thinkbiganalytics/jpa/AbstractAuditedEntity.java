/**
 * 
 */
package com.thinkbiganalytics.jpa;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

/**
 * Base type for entities that have standard timestamp auditing columns in their tables.
 * @author Sean Felten
 */
@MappedSuperclass
@EntityListeners(AuditTimestampListener.class)
public class AbstractAuditedEntity implements AuditedEntity {
    
    @Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    @Column(name="created_time")
    private DateTime createdTime;
    
    @Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    @Column(name="modified_time")
    private DateTime modifiedTime;

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.jpa.AuditedEntity#setCreatedTime(org.joda.time.DateTime)
     */
    @Override
    public void setCreatedTime(DateTime time) {
        this.createdTime = time;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.jpa.AuditedEntity#setModifiedTime(org.joda.time.DateTime)
     */
    @Override
    public void setModifiedTime(DateTime time) {
        this.modifiedTime = time;
    }
    
    public DateTime getCreatedTime() {
        return createdTime;
    }
    
    public DateTime getModifiedTime() {
        return modifiedTime;
    }

}
