/**
 * 
 */
package com.thinkbiganalytics.metadata.api.extension;

import java.util.List;
import java.util.Set;

/**
 *
 * @author Sean Felten
 */
public interface ExtensibleTypeProvider {
    
    ExtensibleTypeBuilder buildType(String name);

//    ExtensibleType createType(String name, Set<FieldDescriptor> props);
//    
//    ExtensibleType createType(String name, ExtensibleType supertype, Set<FieldDescriptor> props);
    
    ExtensibleType getType(String name);
    
    List<ExtensibleType> getTypes();
    
    List<ExtensibleType> getTypes(ExtensibleType type);
    
    Set<FieldDescriptor> getPropertyDescriptors(String nodeType);
    
    
}
