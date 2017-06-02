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


import com.thinkbiganalytics.nifi.rest.model.NiFiPropertyDescriptorTransform;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.ControllerServiceDTO;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Auto Inject Property Values stored in the application.properties file
 * 3 use cases are supported
 * 1) store properties in the file starting with the prefix defined in the "PropertyExpressionResolver class"  default = config.
 * 2) store properties in the file starting with "nifi.<PROCESSORTYPE>.<PROPERTY_KEY>   where PROCESSORTYPE and PROPERTY_KEY are all lowercase and the spaces are substituted with underscore
 * 3) Global property replacement.  properties starting with "nifi.all_processors.<PROPERTY_KEY> will globally replace the value when the template is instantiated
 */
public class ConfigurationPropertyReplacer {


    public static final String NIF_EL_PROPERTY_REPLACEMENT_PREFIX = "$nifi{";

    /**
     * returns a new str in lowercase replacing spaces with _
     * @param str a string to parse
     * @return  a new str in lowercase replacing spaces with _
     */
    private static String toPropertyName(String str) {
        return str.toLowerCase().trim().replaceAll(" +", "_");
    }



    /**
     * return the application.properties property key for the specific property using the proocessor type as the key reference
     *
     * @param property the nifi property
     * @return the property key prepended with the "nifi.<processor type>."
     */
    public static String getProcessorPropertyConfigName(NifiProperty property) {
        String processorTypeName = "nifi." + toPropertyName(StringUtils.substringAfterLast(property.getProcessorType(), ".") + "." + property.getKey());
        return processorTypeName;
    }


    /**
     * return the application.properties property key for the specific property using the proocessor type as the key reference
     *
     * @param property the nifi property
     * @return the property key prepended with the "nifi.<processor type>[<processor name>]."
     */
    public static String getProcessorNamePropertyConfigName(NifiProperty property) {
        String processorTypeName = "nifi." + toPropertyName(StringUtils.substringAfterLast(property.getProcessorType(), ".") + "["+property.getProcessorName()+"]." + property.getKey());
        return processorTypeName;
    }


    /**
     * Return the application.properties property key for 'all_processors' matching the supplied NiFi property.
     *
     * @param property the nifi property to inspect
     * @return the property key prepended with the "nifi.all_processors."
     */
    public static String getGlobalAllProcessorsPropertyConfigName(NifiProperty property) {
        String processorTypeName = "nifi.all_processors." + toPropertyName(property.getKey());
        return processorTypeName;
    }

    /**
     * Replace the $nifi{} with ${}
     * @param value the property value
     * @return the replaced value
     */
    public static String fixNiFiExpressionPropertyValue(String value){
        if(StringUtils.isNotBlank(value)) {
            return StringUtils.replace(value, ConfigurationPropertyReplacer.NIF_EL_PROPERTY_REPLACEMENT_PREFIX, "${");
        }
        return value;
    }

    /**
     * This will replace the Map of Properties in the DTO but not persist back to Nifi.  You need to call the rest client to persist the change
     *
     * @param controllerServiceDTO        the controller service
     * @param properties                  the properties to set
     * @param propertyDescriptorTransform transformer
     * @return {@code true} if the properties were updated, {@code false} if not
     */
    public static boolean replaceControllerServiceProperties(ControllerServiceDTO controllerServiceDTO, Map<String, String> properties, NiFiPropertyDescriptorTransform propertyDescriptorTransform) {
        Set<String> changedProperties = new HashSet<>();
        if (controllerServiceDTO != null) {
            //check both Nifis Internal Key name as well as the Displayname to match the properties
            CaseInsensitiveMap propertyMap = new CaseInsensitiveMap(properties);
            Map<String, String> controllerServiceProperties = controllerServiceDTO.getProperties();

            controllerServiceProperties.entrySet().stream().filter(
                entry -> (propertyMap.containsKey(entry.getKey()) || (controllerServiceDTO.getDescriptors().get(entry.getKey()) != null && propertyMap
                    .containsKey(controllerServiceDTO.getDescriptors().get(entry.getKey()).getDisplayName().toLowerCase())))).
                forEach(entry -> {
                    boolean isSensitive = propertyDescriptorTransform.isSensitive(controllerServiceDTO.getDescriptors().get(entry.getKey()));
                    String value = (String) propertyMap.get(entry.getKey());
                    if (StringUtils.isBlank(value)) {
                        value = (String) propertyMap.get(controllerServiceDTO.getDescriptors().get(entry.getKey()).getDisplayName().toLowerCase());
                    }
                    if (!isSensitive || (isSensitive && StringUtils.isNotBlank(value))) {
                        entry.setValue(value);
                        changedProperties.add(entry.getKey());
                    }

                });
        }
        return !changedProperties.isEmpty();
    }

    private static String resolveValue(NifiProperty property, Map<String,Object> configProperties){
        Object value = null;
        if(configProperties != null) {
            //see if the processorType is configured
            String processorTypeWithProcessorNameProperty = getProcessorNamePropertyConfigName(property);
            value = configProperties.get(processorTypeWithProcessorNameProperty);
            if (value == null || StringUtils.isBlank(value.toString())) {
                String processorTypeProperty = getProcessorPropertyConfigName(property);
                value = configProperties.get(processorTypeProperty);
                if (value == null || StringUtils.isBlank(value.toString())) {
                    String globalPropertyType = getGlobalAllProcessorsPropertyConfigName(property);
                    value = configProperties.get(globalPropertyType);
                }
            }
            if(value != null){
                String updatedPropertyValue = fixNiFiExpressionPropertyValue(value.toString());
                return updatedPropertyValue;
            }
        }
        return null;


    }


    /**
     * @param property         the NifiProperty to replace
     * @param configProperties a Map of properties which will be looked to to match against the property key
     */
    public static boolean resolveStaticConfigurationProperty(NifiProperty property, Map<String, Object> configProperties) {
        String value = property.getValue();
        StringBuffer sb = null;

        if (configProperties != null && !configProperties.isEmpty()) {

            if (StringUtils.isNotBlank(value)) {
                Pattern variablePattern = Pattern.compile("\\$\\{(.*?)\\}");
                Matcher matchVariablePattern = variablePattern.matcher(value);
                while (matchVariablePattern.find()) {
                    if (sb == null) {
                        sb = new StringBuffer();
                    }
                    String group = matchVariablePattern.group();
                    int groupCount = matchVariablePattern.groupCount();
                    if (groupCount == 1) {

                        String variable = matchVariablePattern.group(1);
                        //lookup the variable
                        //first look at configuration properties
                        Object resolvedValue = configProperties.get(variable);
                        if (resolvedValue != null) {
                            matchVariablePattern.appendReplacement(sb, resolvedValue.toString());
                        }
                    }
                }
                if (sb != null) {
                    matchVariablePattern.appendTail(sb);
                }
            }
        }

        if (sb == null) {

            String updatedValue = resolveValue(property,configProperties);
            if(StringUtils.isNotBlank(updatedValue)) {
                sb = new StringBuffer();
                sb.append(updatedValue);
            }

        }
        if (sb != null) {
            property.setValue(StringUtils.trim(sb.toString()));
        }

        return sb != null;
    }

}
