package com.thinkbiganalytics.feedmgr.nifi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.*;

import java.util.*;

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
           MutablePropertySources sources = environment.getPropertySources();
            while(sources.iterator().hasNext()){
                PropertySource propertySource = sources.iterator().next();
                addAll(result, getAllProperties(propertySource));
            }
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
            for(PropertySource propertySource: cps.getPropertySources()){
                addAll(result, getAllProperties(propertySource));
            }

            return result;
        }

        if ( aPropSource instanceof EnumerablePropertySource<?>)
        {
            EnumerablePropertySource<?> ps = (EnumerablePropertySource<?>) aPropSource;
            if(ps != null && ps.getPropertyNames() != null) {
                List<String> keys = Arrays.asList(ps.getPropertyNames());
                for (String key : keys) {
                    result.put(key, ps.getProperty(key));
                }
            }
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
