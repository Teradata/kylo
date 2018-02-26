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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
     * return the application.properties property keys for the specific property using the proocessor type as the key reference
     *
     * @param property the nifi property
     * @return the property keys prepended with the "nifi.<processor type>."
     */
    public static List<String> getProcessorPropertyConfigNames(NifiProperty property) {
        return   getConfigPropertyKeysForNiFi(property).stream().map(k -> "nifi." + toPropertyName(StringUtils.substringAfterLast(property.getProcessorType(), ".") + "." +k)).collect(Collectors.toList());
    }


    /**
     * return the application.properties property keys for the specific property using the proocessor type as the key reference
     *
     * @param property the nifi property
     * @return the list of possible property key prepended with the "nifi.<processor type>[<processor name>]."
     */
    public static List<String> getProcessorNamePropertyConfigNames(NifiProperty property) {

        return getConfigPropertyKeysForNiFi(property).stream().map(k -> "nifi." + toPropertyName(StringUtils.substringAfterLast(property.getProcessorType(), ".") + "["+property.getProcessorName()+"]." + k)).collect(Collectors.toList());
    }

    private static Set<String> getConfigPropertyKeysForNiFi(NifiProperty property){
        Set<String> keys = new HashSet<>();
        if(property.getPropertyDescriptor() != null && StringUtils.isNotBlank(property.getPropertyDescriptor().getDisplayName())) {
            String key = toPropertyName(property.getPropertyDescriptor().getDisplayName());
            keys.add(key);
        }
        keys.add(property.getKey());
        return keys;
    }


    /**
     * Return the application.properties property keys for 'all_processors' matching the supplied NiFi property.
     *
     * @param property the nifi property to inspect
     * @return the property keys prepended with the "nifi.all_processors."
     */
    public static List<String> getGlobalAllProcessorsPropertyConfigNames(NifiProperty property) {
       return getConfigPropertyKeysForNiFi(property).stream().map(k ->"nifi.all_processors." + toPropertyName(k)).collect(Collectors.toList());
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
        if(configProperties != null) {

            Optional<String> value = findMatchingConfigurationProperty(property,configProperties);
            if(value.isPresent()){
                String updatedPropertyValue = fixNiFiExpressionPropertyValue(value.get());
                return updatedPropertyValue;
            }
        }
        return null;


    }

    private static Optional<String> getConfigPropertyValue(List<String> properties, Map<String,Object> configProperties){
        return properties.stream().filter(p-> configProperties.containsKey(p)).map(p -> configProperties.get(p).toString()).findFirst();
    }

    private static Optional<String> findMatchingConfigurationProperty(NifiProperty property,Map<String,Object> configProperties)
    {
        Optional<String> value = getConfigPropertyValue(ConfigurationPropertyReplacer.getProcessorNamePropertyConfigNames(property),configProperties);
        if(!value.isPresent()) {
            value = getConfigPropertyValue(ConfigurationPropertyReplacer.getProcessorPropertyConfigNames(property),configProperties);
            if (!value.isPresent()) {
                value = getConfigPropertyValue(ConfigurationPropertyReplacer.getGlobalAllProcessorsPropertyConfigNames(property),configProperties);
            }
        }
        return value;
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
