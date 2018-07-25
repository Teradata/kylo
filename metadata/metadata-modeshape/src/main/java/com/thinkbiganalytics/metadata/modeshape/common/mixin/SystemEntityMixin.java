/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.common.mixin;

import com.thinkbiganalytics.metadata.api.SystemEntity;
import com.thinkbiganalytics.metadata.modeshape.common.JcrPropertyConstants;

/**
 * To be implemented by JCR objects that have a system name and whose node types extend "mix:title".
 */
public interface SystemEntityMixin extends WrappedNodeMixin, SystemEntity {
    
    String SYSTEM_NAME = JcrPropertyConstants.SYSTEM_NAME;
    String TITLE = JcrPropertyConstants.TITLE;
    String DESCRIPTION = JcrPropertyConstants.DESCRIPTION;
    

    default String getSystemName() {
        return getProperty(SYSTEM_NAME, String.class);
    }
    
    default void setSystemName(String systemName) {
        setProperty(SYSTEM_NAME, systemName);
    }
    
    default String getDescription() {
        return getProperty(DESCRIPTION, String.class);
    }

    default void setDescription(String description) {
        setProperty(DESCRIPTION, description);
    }

    default String getTitle() {
        return getProperty(TITLE, String.class);
    }

    default void setTitle(String title) {
        setProperty(TITLE, title);
    }

}
