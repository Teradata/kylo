/**
 * 
 */
package com.thinkbiganalytics.metadata.api.extension;

import java.io.Serializable;
import java.util.Map;

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
    
    void setProperty(String name, Object value);
}
