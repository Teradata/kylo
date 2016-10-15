package com.thinkbiganalytics.metadata.modeshape.common;

import javax.jcr.Node;

import org.joda.time.DateTime;

/**
 * Created by sr186054 on 6/6/16.
 */
public class AbstractJcrAuditableSystemEntity extends AbstractJcrSystemEntity {

    public AbstractJcrAuditableSystemEntity(Node node) {
        super(node);
    }


    public DateTime getCreatedTime() {
        return getProperty(JcrPropertyConstants.CREATED_TIME, DateTime.class);
    }

    public DateTime getModifiedTime() {
        return getProperty(JcrPropertyConstants.MODIFIED_TIME, DateTime.class);
    }

    public String getCreatedBy() {
        return getProperty(JcrPropertyConstants.CREATED_BY, String.class);
    }

    public String getModifiedBy() {
        return getProperty(JcrPropertyConstants.MODIFIED_BY, String.class);
    }

    public void setCreatedTime(DateTime createdTime) {
       // setProperty(JcrPropertyConstants.CREATED_TIME, createdTime);
    }

    public void setModifiedTime(DateTime modifiedTime) {
       // setProperty(JcrPropertyConstants.CREATED_TIME, modifiedTime);
    }

}
