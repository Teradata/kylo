/**
 * 
 */
package com.thinkbiganalytics.metadata.api.extension;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Sean Felten
 */
public interface ExtensibleEntity {

    interface ID extends Serializable { }
    
    ID getId();
    
    String getTypeName();
    
    Map<String, Object> getProperties();
    
    Object getProperty(String name);

    <T> Set<T> getPropertyAsSet(String name, Class<T> objectType);
    
    void setProperty(String name, Object value);
}
