package com.thinkbiganalytics.feedmgr.nifi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Created by sr186054 on 5/3/16.
 *
 * Helper class to get Environment Properties
 */
@RefreshScope
public class SpringEnvironmentProperties {

    private Map<String, Object> properties = new HashMap<>();

    private Map<String,Map<String,Object>> propertiesStartingWith = new HashMap<>();



    public SpringEnvironmentProperties() {

    }

    @Autowired
    private Environment env;

    /**
     * Get All Properties that start with a prefix
     * @param key
     * @return
     */
    public Map<String,Object> getPropertiesStartingWith(String key){

        if(propertiesStartingWith.containsKey(key)){
            return propertiesStartingWith.get(key);
        }
        else {
            Map<String, Object> props = getAllProperties();
            if (props != null) {
                NavigableMap m = new TreeMap(props);
                Map<String,Object> properties = m.subMap(key, key + Character.MAX_VALUE);
                propertiesStartingWith.put(key,properties);
                return properties;

            }
            return null;
        }
    }

    public Object getPropertyValue(String key){
        return getAllProperties().get(key);
    }

    public String getPropertyValueAsString(String key){
        Object obj =  getPropertyValue(key);
        if(obj != null){
            return obj.toString();
        }
        return null;
    }

    public void reset() {
        this.propertiesStartingWith.clear();
        this.properties.clear();
    }


    /**
     * get All properties
     * @return
     */
    public  Map<String,Object> getAllProperties(  )
    {
        if (properties == null || properties.isEmpty()) {
            Map<String, Object> map = new HashMap();
            for(Iterator it = ((AbstractEnvironment) env).getPropertySources().iterator(); it.hasNext(); ) {
                PropertySource propertySource = (PropertySource) it.next();
                if (propertySource instanceof MapPropertySource) {
                    map.putAll(((MapPropertySource) propertySource).getSource());
                }
            }

            properties = map;
        }
        return properties;
    }

}
