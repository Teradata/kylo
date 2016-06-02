/**
 * 
 */
package com.thinkbiganalytics.metadata.api.generic;

import java.io.Serializable;
import java.util.Map;

/**
 *
 * @author Sean Felten
 */
public interface GenericEntity {

    interface ID extends Serializable { }
    
    ID getId();
    
    String getTypeName();
    
    Map<String, Object> getProperties();
    
    Object getProperty(String name);
    
    void setProperty(String name, Object value);
}
