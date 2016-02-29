package com.thinkbiganalytics.nifi.provenance;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by sr186054 on 2/25/16.
 */
public class PropertiesLoader {

    private Properties properties  = null;

    private static class LazyHolder {
        static final PropertiesLoader INSTANCE = new PropertiesLoader();
    }

    public static PropertiesLoader getInstance() {
        return LazyHolder.INSTANCE;
    }

    public static final String DATASOURCE_URL = "thinkbig.provenance.datasource.url";
    public static final String DATASOURCE_USERNAME = "thinkbig.provenance.datasource.username";
    public static final String DATASOURCE_PASSWORD = "thinkbig.provenance.datasource.password";
    public static final String DATABASE_DRIVER_CLASS_NAME = "thinkbig.provenance.datasource.driverClassName";

    private PropertiesLoader() {
        loadProperties();
    }

    private void loadProperties(){


        final Properties properties = new Properties();
        try (final InputStream stream =
                     this.getClass().getClassLoader().getResourceAsStream("config.properties")) {
            properties.load(stream);
    /* or properties.loadFromXML(...) */
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.properties = properties;

    }
    public String getProperty(String key){
        return properties.getProperty(key);
    }



}
