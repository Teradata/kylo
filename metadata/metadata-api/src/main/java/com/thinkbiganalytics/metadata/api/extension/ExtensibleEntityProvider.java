/**
 * 
 */
package com.thinkbiganalytics.metadata.api.extension;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Sean Felten
 */
public interface ExtensibleEntityProvider {

    ExtensibleType createType(String name, Map<String, ExtensibleType.PropertyType> props);
    
    ExtensibleType createType(String name, ExtensibleType supertype, Map<String, ExtensibleType.PropertyType> props);
    
    ExtensibleType getType(String name);
    
    List<ExtensibleType> getTypes();
    
//    List<ExtensibleType> getTypes(ExtensibleType type);
    
    ExtensibleEntity createEntity(ExtensibleType type, Map<String, Object> props);
    
    ExtensibleEntity getEntity(ExtensibleEntity.ID id);
    
    List<ExtensibleEntity> getEntities();

    Map<String,ExtensibleType.PropertyType> getPropertyTypes(String nodeType);
    
    
}
