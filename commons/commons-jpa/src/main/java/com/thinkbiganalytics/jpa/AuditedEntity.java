/**
 * 
 */
package com.thinkbiganalytics.jpa;

import org.joda.time.DateTime;

/**
 *
 * @author Sean Felten
 */
public interface AuditedEntity {

    void setCreatedTime(DateTime time);

    void setModifiedTime(DateTime time);
}
