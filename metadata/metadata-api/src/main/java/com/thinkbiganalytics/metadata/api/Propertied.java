/**
 * 
 */
package com.thinkbiganalytics.metadata.api;

import java.util.Map;

/**
 *
 * @author Sean Felten
 */
public interface Propertied {
    
    Map<String, Object> getProperties();
    
    void setProperties(Map<String, Object> props);
    
    Map<String, Object> mergeProperties(Map<String, Object> props);
    
    void setProperty(String key, Object value);

    void removeProperty(String key);
}
