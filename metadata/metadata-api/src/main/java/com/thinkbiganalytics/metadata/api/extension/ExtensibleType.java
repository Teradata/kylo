/**
 * 
 */
package com.thinkbiganalytics.metadata.api.extension;

import java.util.Set;

/**
 *
 * @author Sean Felten
 */
public interface ExtensibleType {
    
    String getName();
    
    ExtensibleType getParentType();
    
    Set<FieldDescriptor> getPropertyDescriptors();

    FieldDescriptor getPropertyDescriptor(String name);
}
