package com.thinkbiganalytics.feedmgr.nifi;

import com.google.common.base.CaseFormat;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClient;
import com.thinkbiganalytics.rest.JerseyClientException;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.ControllerServiceDTO;
import org.apache.nifi.web.api.entity.ControllerServiceEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Created by sr186054 on 5/3/16.
 * Helper class to get Controller Service Properties from Nifi and also merge any of these properties specified in our .properties file
 */
@Service
public class NifiControllerServiceProperties {

    private static String ENVIRONMENT_PROPERTY_SERVICE_PREFIX = "nifi.service.";

    @Autowired
    SpringEnvironmentProperties environmentProperties;

    @Autowired
    NifiRestClient nifiRestClient;

    /**
     * Call out to Nifi and get all the Properties for a service by Service Name
     * @param serviceName
     * @return
     */
    public Map<String,String> getPropertiesForServiceName(String serviceName){
        ControllerServiceDTO controllerService = getControllerServiceByName(serviceName);
        if(controllerService != null){
            return controllerService.getProperties();
        }
        return null;
    }

    /**
     * Call out to Nifi and get all the Properties for a service by Service Id
     * @param serviceId
     * @return
     */
    public Map<String,String> getPropertiesForServiceId(String serviceId){
        ControllerServiceDTO controllerService = getControllerServiceById(serviceId);
        if(controllerService != null){
            return controllerService.getProperties();
        }
        return null;
    }

    /**
     * Call out to Nifi and get the Controller Service Properties and then merge it with any properties in our environment properties file.
     * Env service properties need to start with the ENVIRONMENT_PROPERTY_SERVICE_PREFIX  ("nifi.service.")
     * @param serviceId
     * @return
     */
    public  Map<String,String> getPropertiesForServiceIdMergedWithEnvironmentProperties(String serviceId) {
        ControllerServiceDTO controllerService = getControllerServiceById(serviceId);
        if(controllerService != null){
            String serviceName = controllerService.getName();
            Map<String,String>  properties = controllerService.getProperties();
            properties =  mergeNifiAndEnvProperties(properties,serviceName);
            return properties;
        }
        return null;
    }


    public  Map<String,String> getPropertiesForServiceNameMergedWithEnvironmentProperties(String serviceName) {
        Map<String,String> properties = getPropertiesForServiceName(serviceName);
        properties =   mergeNifiAndEnvProperties(properties,serviceName);
        return properties;
    }

    public Map<String,String> mergeNifiAndEnvProperties(Map<String,String> nifiProperties, String serviceName){
        if(nifiProperties != null){
            CaseInsensitiveMap propertyMap = new CaseInsensitiveMap(nifiProperties);
            String servicePrefix = getEnvironmentControllerServicePropertyPrefix(serviceName);
            Map<String,Object> map = environmentProperties.getPropertiesStartingWith(servicePrefix);
            if(map != null && !map.isEmpty()) {
                for(Map.Entry<String,Object> entry: map.entrySet()) {
                    String key = environmentPropertyToControllerServiceProperty(entry.getKey());
                    if(propertyMap.containsKey(key) && entry.getValue() != null){
                        propertyMap.put(key,entry.getValue());
                    }
                }
            }
            return propertyMap;
        }
        return null;
    }

    private String getEnvironmentControllerServicePropertyPrefix(String serviceName){
        return ENVIRONMENT_PROPERTY_SERVICE_PREFIX+nifiPropertyToEnvironmentProperty(serviceName);
    }

    private String environmentPropertyToControllerServiceProperty(String envProperty){
        String prop = envProperty;
        prop = StringUtils.substringAfter(prop,ENVIRONMENT_PROPERTY_SERVICE_PREFIX);
        String serviceName = StringUtils.substringBefore(prop,".");
        prop = StringUtils.substringAfter(prop, ".");
        prop = environmentPropertyToNifi(prop);
        return prop;
    }

    private String environmentPropertyToNifi(String envProperty){
        String  name = envProperty.replaceAll("_", " ");
        name = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL,name);
        return name;
    }

    private String nifiPropertyToEnvironmentProperty(String nifiPropertyKey){
        String  name = nifiPropertyKey.toLowerCase().trim().replaceAll(" +","_");
        name = name.toLowerCase();
        return name;
    }



    public ControllerServiceDTO getControllerServiceByName(String serviceName) {
        ControllerServiceDTO controllerService = null;
            try {
                controllerService = nifiRestClient.getControllerServiceByName("NODE", serviceName);
            }catch (JerseyClientException e) {

            }

        return controllerService;
    }

    public ControllerServiceDTO getControllerServiceById(String serviceId) {
        ControllerServiceDTO controllerService = null;
        try {
            ControllerServiceEntity entity = nifiRestClient.getControllerService("NODE", serviceId);
            if (entity != null && entity.getControllerService() != null) {
                controllerService = entity.getControllerService();
            }
        }catch(JerseyClientException e) {

        }
        return controllerService;
    }

}
