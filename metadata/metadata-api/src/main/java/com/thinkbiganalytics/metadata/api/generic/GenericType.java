/**
 * 
 */
package com.thinkbiganalytics.metadata.api.generic;

import java.util.Map;

/**
 *
 * @author Sean Felten
 */
public interface GenericType {
    
    enum PropertyType { STRING, BOOLEAN, INTEGER, LONG, DOUBLE, PATH, ENTITY } // TODO need more like DATE

    String getName();
    
    GenericType getParentType();
    
    Map<String, GenericType.PropertyType> getProperyTypes();

    GenericType.PropertyType getPropertyType(String name);
}
