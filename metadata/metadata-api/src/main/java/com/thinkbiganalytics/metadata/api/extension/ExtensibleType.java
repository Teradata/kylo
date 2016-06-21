/**
 * 
 */
package com.thinkbiganalytics.metadata.api.extension;

import java.io.Serializable;
import java.util.Set;

import org.joda.time.DateTime;

/**
 *
 * @author Sean Felten
 */
public interface ExtensibleType {
    
    interface ID extends Serializable { }

    ID getId();
    
    ExtensibleType getSupertype();
    
    String getName();
    
    String getDiplayName();
    
    String getDesciption();
    
    DateTime getCreatedTime();
    
    DateTime getModifiedTime();
    
    Set<FieldDescriptor> getFieldDescriptors();

    FieldDescriptor getFieldDescriptor(String name);
}
