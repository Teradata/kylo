package com.thinkbiganalytics.feedmgr.nifi;

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

import com.thinkbiganalytics.nifi.feedmgr.NifiEnvironmentProperties;
import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiClientRuntimeException;
import com.thinkbiganalytics.nifi.rest.client.NifiComponentNotFoundException;
import com.thinkbiganalytics.spring.SpringEnvironmentProperties;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.nifi.web.api.dto.ControllerServiceDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Helper class to get Controller Service Properties from Nifi and also merge any of these properties specified in our .properties file
 */
@Service
public class NifiControllerServiceProperties {

    private static final Logger log = LoggerFactory.getLogger(NifiControllerServiceProperties.class);


    @Autowired
    SpringEnvironmentProperties environmentProperties;

    @Autowired
    LegacyNifiRestClient nifiRestClient;


    /**
     * Call out to Nifi and get all the Properties for a service by Service Name
     */
    public Map<String, String> getPropertiesForServiceName(String serviceName) {
        ControllerServiceDTO controllerService = getControllerServiceByName(serviceName);
        if (controllerService != null) {
            return controllerService.getProperties();
        }
        return null;
    }

    /**
     * Call out to Nifi and get all the Properties for a service by Service Id
     */
    public Map<String, String> getPropertiesForServiceId(String serviceId) {
        ControllerServiceDTO controllerService = getControllerServiceById(serviceId);
        if (controllerService != null) {
            return controllerService.getProperties();
        }
        return null;
    }

    /**
     * Call out to Nifi and get the Controller Service Properties and then merge it with any properties in our environment properties file.
     * Env service properties need to start with the ENVIRONMENT_PROPERTY_SERVICE_PREFIX  ("nifi.service.")
     */
    public Map<String, String> getPropertiesForServiceIdMergedWithEnvironmentProperties(String serviceId) {
        ControllerServiceDTO controllerService = getControllerServiceById(serviceId);
        if (controllerService != null) {
            String serviceName = controllerService.getName();
            Map<String, String> properties = controllerService.getProperties();
            properties = mergeNifiAndEnvProperties(properties, serviceName);
            return properties;
        }
        return null;
    }


    public Map<String, String> getPropertiesForServiceNameMergedWithEnvironmentProperties(String serviceName) {
        Map<String, String> properties = getPropertiesForServiceName(serviceName);
        properties = mergeNifiAndEnvProperties(properties, serviceName);
        return properties;
    }

    public Map<String, String> mergeNifiAndEnvProperties(Map<String, String> nifiProperties, String serviceName) {
        if (nifiProperties != null) {
            CaseInsensitiveMap propertyMap = new CaseInsensitiveMap(nifiProperties);
            String servicePrefix = NifiEnvironmentProperties.getEnvironmentControllerServicePropertyPrefix(serviceName);
            Map<String, Object> map = environmentProperties.getPropertiesStartingWith(servicePrefix);
            if (map != null && !map.isEmpty()) {
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    String key = NifiEnvironmentProperties.environmentPropertyToControllerServiceProperty(entry.getKey());
                    if (propertyMap.containsKey(key) && entry.getValue() != null) {
                        propertyMap.put(key, entry.getValue());
                    }
                }
            }
            return propertyMap;
        }
        return null;
    }

    /**
     * for a given nifi controller service return the application.properties value for the passed in key suffix
     * example
     * mysql, password
     * will return the value from the property  nifi.service.mysql.password
     */
    public String getEnvironmentPropertyValueForControllerService(String serviceName, String envPropertyKeySuffix) {

        String servicePrefix = NifiEnvironmentProperties.getEnvironmentControllerServicePropertyPrefix(serviceName);
        return environmentProperties.getPropertyValueAsString(servicePrefix + "." + envPropertyKeySuffix);

    }

    /**
     * returns all properties configured with the prefix
     */
    public Map<String, Object> getAllServiceProperties() {
        return environmentProperties.getPropertiesStartingWith(NifiEnvironmentProperties.getPrefix());
    }


    /**
     * Find a Controller service with a given display name
     *
     * @param serviceName a controller service name
     * @return a Controller service with a given display name or null if not found
     */
    public ControllerServiceDTO getControllerServiceByName(String serviceName) {
        ControllerServiceDTO controllerService = null;
        try {
            controllerService = nifiRestClient.getControllerServiceByName(null, serviceName);
        } catch (NifiClientRuntimeException e) {
            log.error("Unable to find Nifi Controller Service with name: " + serviceName + ".  " + e.getMessage(), e);
        }

        return controllerService;
    }

    /**
     * Find a controller service with a given NiFi id or null if not found
     *
     * @param serviceId a controller service id
     * @return a controller service with a given NiFi id or null if not found
     */
    public ControllerServiceDTO getControllerServiceById(String serviceId) {
        ControllerServiceDTO controllerService = null;
        try {
            ControllerServiceDTO entity = nifiRestClient.getControllerService(null, serviceId);
            if (entity != null) {
                controllerService = entity;
            }
        } catch (NifiComponentNotFoundException e) {
            log.error("Unable to find Nifi Controller Service with ID: " + serviceId + ".  " + e.getMessage(), e);
        }
        return controllerService;
    }

    public ControllerServiceDTO getControllerService(String serviceId, String serviceName) {
        ControllerServiceDTO controllerService = getControllerServiceById(serviceId);
        if (controllerService == null) {
            controllerService = getControllerServiceByName(serviceName);
        }
        return controllerService;
    }

    /**
     * return the property prepended with the prefix used in the .properties file to denote nifi controller service settings.
     * the default prefix is 'nifi.'
     *
     * @param serviceName a service name
     * @return the property prepended with the prefix used in the .properties file to denote nifi controller service settings.
     */
    public String getEnvironmentControllerServicePropertyPrefix(String serviceName) {
        return NifiEnvironmentProperties.getEnvironmentControllerServicePropertyPrefix(serviceName);
    }

}
