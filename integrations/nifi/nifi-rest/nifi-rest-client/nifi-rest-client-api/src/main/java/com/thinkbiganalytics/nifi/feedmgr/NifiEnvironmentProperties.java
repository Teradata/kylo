package com.thinkbiganalytics.nifi.feedmgr;

/*-
 * #%L
 * thinkbig-nifi-rest-client-api
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

import com.google.common.base.CaseFormat;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * Utility to allow for application.properties to work with Nifi.
 */
public class NifiEnvironmentProperties {

    public static final String ENVIRONMENT_PROPERTY_SERVICE_PREFIX = "nifi.service.";


    public NifiEnvironmentProperties() {

    }


    public static String getPrefix() {
        return ENVIRONMENT_PROPERTY_SERVICE_PREFIX;
    }


    /**
     * return the property prefix along with the service name
     *
     * @param serviceName the name of the service
     * @return the returned key for inspection with the application.properties
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


    /**
     * return the environment property in the format that can be used with NiFi
     *
     * @param envProperty the property from the application.properties
     * @return the formatted property to work with NiFi
     */
    private static String environmentPropertyToNifi(String envProperty) {
        String name = envProperty.replaceAll("_", " ");
        name = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name);
        return name;
    }

    /**
     * Return the property key from NiFi that can work with the application.properties
     *
     * @param nifiPropertyKey the Nifi property
     * @return the formatted property to work with the application.properties
     */
    private static String nifiPropertyToEnvironmentProperty(String nifiPropertyKey) {
        String name = nifiPropertyKey.toLowerCase().trim().replaceAll(" +", "_");
        name = name.toLowerCase();
        return name;
    }


    /**
     * Returns a collection of the Service Name along with the Map of nifi Properties stripping the ENVIRONMENT_PROPERTY_SERVICE_PREFIX from the properties
     *
     * @param envProperties the map of environment (.properties) properties
     * @return the table of nifi ready properties
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

    /**
     * Returns a map of properties for a given Nifi service
     *
     * @param envProperties the .properties
     * @param serviceName   the service name to use/look for as the key
     * @return the map of properties ready for NiFi use, inspected from the environment properties
     */
    public static Map<String, String> getEnvironmentControllerServiceProperties(Map<String, String> envProperties, String serviceName) {
        return getEnvironmentControllerServiceProperties(envProperties).row(nifiPropertyToEnvironmentProperty(serviceName));
    }

}
