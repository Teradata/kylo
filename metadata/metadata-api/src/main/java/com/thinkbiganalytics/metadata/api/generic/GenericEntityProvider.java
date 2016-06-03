/**
 * 
 */
package com.thinkbiganalytics.metadata.api.generic;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Sean Felten
 */
public interface GenericEntityProvider {

    GenericType createType(String name, Map<String, GenericType.PropertyType> props);
    
    GenericType createType(String name, GenericEntity supertype, Map<String, GenericType.PropertyType> props);
    
    GenericType getType(String name);
    
    List<GenericType> getTypes();
    
//    List<GenericType> getTypes(GenericType type);
    
    GenericEntity createEntity(GenericType type, Map<String, Object> props);
    
    GenericEntity getEntity(GenericEntity.ID id);
    
    List<GenericEntity> getEntities();
    
    
}
