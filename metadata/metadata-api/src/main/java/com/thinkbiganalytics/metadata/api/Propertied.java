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
    
    Map<String, String> getProperties();
    
    void setProperties(Map<String, String> props);
    
    Map<String, String> mergeProperties(Map<String, String> props);
    
    String setProperty(String key, String value);

    String removeProperty(String key);
}
