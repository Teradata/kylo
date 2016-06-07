/**
 * 
 */
package com.thinkbiganalytics.metadata.api.extension;

import java.util.Map;

/**
 *
 * @author Sean Felten
 */
public interface ExtensibleType {
    
    enum PropertyType { STRING, BOOLEAN, INTEGER, LONG, DOUBLE, PATH, ENTITY } // TODO need more like DATE

    String getName();
    
    ExtensibleType getParentType();
    
    Map<String, ExtensibleType.PropertyType> getProperyTypes();

    ExtensibleType.PropertyType getPropertyType(String name);
}
