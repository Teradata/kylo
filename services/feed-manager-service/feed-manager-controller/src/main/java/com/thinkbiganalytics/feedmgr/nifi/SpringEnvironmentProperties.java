package com.thinkbiganalytics.feedmgr.nifi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Consumer;

/**
 * Created by sr186054 on 5/3/16.
 *
 * Helper class to get Environment Properties
 */
public class SpringEnvironmentProperties {

    private  Map<String,Object> properties;

    public SpringEnvironmentProperties() {

    }

    @Autowired
    private ConfigurableEnvironment environment;

    /**
     * Get All Properties that start with a prefix
     * @param key
     * @return
     */
    public Map<String,Object> getPropertiesStartingWith(String key){
        Map<String,Object> props = getAllProperties();
        if(props != null) {
            NavigableMap m = new TreeMap(props);
            return m.subMap( key, key + Character.MAX_VALUE );
        }
        return null;
    }

    /**
     * get All properties
     * @return
     */
    public  Map<String,Object> getAllProperties(  )
    {
        if(properties == null) {
            final Map<String, Object> result = new HashMap<>();
            environment.getPropertySources().forEach(new Consumer<PropertySource<?>>() {
                @Override
                public void accept(PropertySource<?> propertySource) {
                    addAll(result, getAllProperties(propertySource));
                }
            });
            properties = result;
        }
        return properties;
    }

    private  Map<String,Object> getAllProperties( PropertySource<?> aPropSource )
    {
        final Map<String,Object> result = new HashMap<>();

        if ( aPropSource instanceof CompositePropertySource)
        {
            CompositePropertySource cps = (CompositePropertySource) aPropSource;
            cps.getPropertySources().forEach(new Consumer<PropertySource<?>>() {
                @Override
                public void accept(PropertySource<?> propertySource) {
                    addAll(result, getAllProperties(propertySource));
                }
            });
            return result;
        }

        if ( aPropSource instanceof EnumerablePropertySource<?>)
        {
           final  EnumerablePropertySource<?> ps = (EnumerablePropertySource<?>) aPropSource;
            Arrays.asList(ps.getPropertyNames()).forEach(new Consumer<String>() {
                @Override
                public void accept(String key) {
                    result.put(key, ps.getProperty(key));
                }
            });
            return result;
        }
        return result;

    }

    private  void addAll( Map<String, Object> aBase, Map<String, Object> aToBeAdded )
    {
        for (Map.Entry<String, Object> entry : aToBeAdded.entrySet())
        {
            if ( aBase.containsKey( entry.getKey() ) )
            {
                continue;
            }

            aBase.put( entry.getKey(), entry.getValue() );
        }
    }

}
