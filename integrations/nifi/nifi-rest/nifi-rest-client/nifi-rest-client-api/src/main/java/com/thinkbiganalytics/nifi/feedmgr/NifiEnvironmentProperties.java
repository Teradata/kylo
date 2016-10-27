package com.thinkbiganalytics.nifi.feedmgr;

import com.google.common.base.CaseFormat;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * Created by sr186054 on 8/11/16.
 */
public class NifiEnvironmentProperties {

    public static String ENVIRONMENT_PROPERTY_SERVICE_PREFIX = "nifi.service.";


    public NifiEnvironmentProperties() {

    }


    public static String getPrefix() {
        return ENVIRONMENT_PROPERTY_SERVICE_PREFIX;
    }

    /**
     * return the property prefix along with the service name
     */
    public static String getEnvironmentControllerServicePropertyPrefix(String serviceName) {
        return ENVIRONMENT_PROPERTY_SERVICE_PREFIX + nifiPropertyToEnvironmentProperty(serviceName);
    }

    /**
     * for a given property return the serviceName
     */
    public static String serviceNameForEnvironmentProperty(String envProperty) {
        String prop = envProperty;
        prop = StringUtils.substringAfter(prop, getPrefix());
        String serviceName = StringUtils.substringBefore(prop, ".");
        return serviceName;
    }

    /**
     * resolve the Nifi Property from the  env controllerServiceProperty
     */
    public static String environmentPropertyToControllerServiceProperty(String envProperty) {
        String prop = envProperty;
        prop = StringUtils.substringAfter(prop, getPrefix());
        String serviceName = StringUtils.substringBefore(prop, ".");
        prop = StringUtils.substringAfter(prop, ".");
        prop = environmentPropertyToNifi(prop);
        return prop;
    }


    private static String environmentPropertyToNifi(String envProperty) {
        String name = envProperty.replaceAll("_", " ");
        name = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name);
        return name;
    }

    private static String nifiPropertyToEnvironmentProperty(String nifiPropertyKey) {
        String name = nifiPropertyKey.toLowerCase().trim().replaceAll(" +", "_");
        name = name.toLowerCase();
        return name;
    }


    /**
     * Returns a collection of the Service Name along with the Map of nifi Properties stripping the ENVIRONMENT_PROPERTY_SERVICE_PREFIX from the properties
     */
    public static Table<String, String, String> getEnvironmentControllerServiceProperties(Map<String, String> envProperties) {
        Table<String, String, String> allProps = HashBasedTable.create();
        if (envProperties != null && !envProperties.isEmpty()) {
            for (Map.Entry<String, String> entry : envProperties.entrySet()) {
                if (entry.getKey().startsWith(getPrefix())) {
                    String key = environmentPropertyToControllerServiceProperty(entry.getKey());
                    String serviceName = serviceNameForEnvironmentProperty(entry.getKey());
                    allProps.put(serviceName, key, entry.getValue());
                }
            }
        }
        return allProps;
    }

    public static Map<String, String> getEnvironmentControllerServiceProperties(Map<String, String> envProperties, String serviceName) {
        return getEnvironmentControllerServiceProperties(envProperties).row(nifiPropertyToEnvironmentProperty(serviceName));
    }

}
