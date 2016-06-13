/**
 * 
 */
package com.thinkbiganalytics.metadata.api.extension;

import java.io.Serializable;
import java.util.Set;

/**
 *
 * @author Sean Felten
 */
public interface ExtensibleType {
    
    interface ID extends Serializable { }

    ID getId();
    
    String getName();
    
    ExtensibleType getParentType();
    
    Set<FieldDescriptor> getFieldDescriptors();

    FieldDescriptor getFieldDescriptor(String name);
}
