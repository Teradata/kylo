package com.thinkbiganalytics.spring;

/*-
 * #%L
 * thinkbig-feed-manager-controller
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * Helper class to get Environment Properties
 * reference for RefreshScope: http://cloud.spring.io/spring-cloud-static/docs/1.0.x/spring-cloud.html#_refresh_scope
 */
@RefreshScope
public class SpringEnvironmentProperties {

    private static final Logger log = LoggerFactory.getLogger(SpringEnvironmentProperties.class);

    private Map<String, Object> properties = new HashMap<>();

    private Map<String, Map<String, Object>> propertiesStartingWith = new HashMap<>();
    @Autowired
    private Environment env;

    public SpringEnvironmentProperties() {

    }

    /**
     * Get All Properties that start with a prefix
     */
    public Map<String, Object> getPropertiesStartingWith(String key) {

        if (propertiesStartingWith.containsKey(key)) {
            return propertiesStartingWith.get(key);
        } else {
            Map<String, Object> props = getAllProperties();
            if (props != null) {
                NavigableMap m = new TreeMap(props);
                Map<String, Object> properties = m.subMap(key, key + Character.MAX_VALUE);
                Map<String, Object> decryptedProperties = new HashMap<>();
                if (properties != null && !properties.isEmpty()) {
                    properties.keySet().stream().forEach(k -> {
                                                             decryptedProperties.put(k, env.getProperty(k));
                                                         }
                    );
                }
                propertiesStartingWith.put(key, decryptedProperties);
                return properties;

            }
            return null;
        }
    }

    public Object getPropertyValue(String key) {
        return getAllProperties().get(key);
    }


    public String getPropertyValueAsString(String key) {
        Object obj = getPropertyValue(key);
        if (obj != null) {
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
     */
    public Map<String, Object> getAllProperties() {
        if (properties == null || properties.isEmpty()) {
            Map<String, Object> map = new HashMap();
            for (Iterator it = ((AbstractEnvironment) env).getPropertySources().iterator(); it.hasNext(); ) {
                PropertySource propertySource = (PropertySource) it.next();
                if (propertySource instanceof MapPropertySource) {
                    map.putAll(((MapPropertySource) propertySource).getSource());
                }
            }
            //decrypt
            Map<String, Object> decryptedMap = new HashMap();
            map.keySet().forEach(k -> {
                decryptedMap.put(k, env.getProperty(k));
            });

            properties = decryptedMap;
        }
        return properties;
    }

}
