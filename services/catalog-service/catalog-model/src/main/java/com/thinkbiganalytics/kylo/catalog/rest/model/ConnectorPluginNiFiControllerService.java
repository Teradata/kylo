package com.thinkbiganalytics.kylo.catalog.rest.model;

/*-
 * #%L
 * kylo-catalog-model
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * Instructions for creating a NiFi Controller Service from a Data Source.
 */
public class ConnectorPluginNiFiControllerService {

    /**
     * Map of controller service property to value
     */
    private Map<String, String> properties;

    private List<String> identityProperties;


    /**
     * Map of controller service property key to propertydescriptors
     */
    private Map<String, ConnectorPluginNiFiControllerServicePropertyDescriptor> propertyDescriptors;

    /**
     * Type of controller service
     */
    private String type;

    public ConnectorPluginNiFiControllerService() {
    }

    public ConnectorPluginNiFiControllerService(@Nonnull final ConnectorPluginNiFiControllerService other) {
        properties = (other.properties != null) ? new HashMap<>(other.properties) : null;
        propertyDescriptors = copyDescriptorMap(other.propertyDescriptors);
        type = other.type;
        identityProperties = other.identityProperties != null ? new ArrayList(other.identityProperties) : null;
    }

    private Map<String, ConnectorPluginNiFiControllerServicePropertyDescriptor>  copyDescriptorMap(Map<String, ConnectorPluginNiFiControllerServicePropertyDescriptor> map){
        if(map != null){
            Map<String, ConnectorPluginNiFiControllerServicePropertyDescriptor> copy = new HashMap<>();
            for(Map.Entry<String,ConnectorPluginNiFiControllerServicePropertyDescriptor> entry: map.entrySet()){
                copy.put(entry.getKey(),new ConnectorPluginNiFiControllerServicePropertyDescriptor(entry.getValue()));
            }
            return copy;
        }
        return null;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, ConnectorPluginNiFiControllerServicePropertyDescriptor> getPropertyDescriptors() {
        return propertyDescriptors;
    }

    public void setPropertyDescriptors(Map<String, ConnectorPluginNiFiControllerServicePropertyDescriptor> propertyDescriptors) {
        this.propertyDescriptors = propertyDescriptors;
    }

    public List<String> getIdentityProperties() {
        return identityProperties;
    }

    public void setIdentityProperties(List<String> identityProperties) {
        this.identityProperties = identityProperties;
    }
}
