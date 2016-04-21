package com.thinkbiganalytics.feedmgr.nifi;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by sr186054 on 1/25/16.
 */
public class NifiConfigurationPropertiesService {

    private static class LazyHolder {
        static final NifiConfigurationPropertiesService INSTANCE = new NifiConfigurationPropertiesService();
    }

    public static NifiConfigurationPropertiesService getInstance() {
        return LazyHolder.INSTANCE;
    }

    private NifiConfigurationPropertiesService() {
        loadProperties();
    }

    private Properties properties  = null;

    private Properties propertiesWithConfigPrefix = null;

    public void loadProperties(){


        final Properties properties = new Properties();
        try (final InputStream stream =
                     this.getClass().getClassLoader().getResourceAsStream("nifi-configuration.properties")) {
            properties.load(stream);
    /* or properties.loadFromXML(...) */
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.properties = properties;
        this.propertiesWithConfigPrefix= getPropertiesWithPrefix(PropertyExpressionResolver.configPropertyPrefix);


    }

    public Properties getProperties(){
        return properties;
    }

    public Properties getPropertiesWithConfigPrefix() {
        return propertiesWithConfigPrefix;
    }

    public Properties getPropertiesWithPrefix(String prefix){
        Properties p = new Properties();
        if(properties != null) {
            for (String str : properties.stringPropertyNames()) {
                p.put(prefix + str, properties.getProperty(str));
            }
        }
        return p;

    }

    public boolean hasProperty(String key){
        return getPropertyValue(key) != null;
    }

    public String getPropertyValue(String key){
        return properties.getProperty(key);
    }


}
