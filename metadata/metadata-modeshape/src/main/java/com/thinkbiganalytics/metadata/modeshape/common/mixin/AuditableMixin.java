/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.common.mixin;

import com.thinkbiganalytics.metadata.api.Auditable;
import com.thinkbiganalytics.metadata.modeshape.common.JcrPropertyConstants;

import org.joda.time.DateTime;

/**
 * A mixin interface to be implemented by classes that wrap nodes extending the "mix:lastModified" and "mix:created" mixin types.
 */
public interface AuditableMixin extends WrappedNodeMixin, Auditable {
    
    String CREATED_TIME = JcrPropertyConstants.CREATED_TIME;
    String CREATED_BY = JcrPropertyConstants.CREATED_BY;
    String MODIFIED_TIME = JcrPropertyConstants.MODIFIED_TIME;
    String MODIFIED_BY = JcrPropertyConstants.MODIFIED_BY;

    
    default DateTime getCreatedTime() {
        return getProperty(CREATED_TIME, DateTime.class);
    }

    default DateTime getModifiedTime() {
        return getProperty(MODIFIED_TIME, DateTime.class);
    }

    default String getCreatedBy() {
        return getProperty(CREATED_BY, String.class);
    }

    default String getModifiedBy() {
        return getProperty(MODIFIED_BY, String.class);
    }

}
