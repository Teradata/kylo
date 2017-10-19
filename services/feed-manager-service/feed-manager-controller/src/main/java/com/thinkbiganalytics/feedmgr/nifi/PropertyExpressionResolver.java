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


import com.thinkbiganalytics.annotations.AnnotatedFieldProperty;
import com.thinkbiganalytics.feedmgr.MetadataFieldAnnotationFieldNameResolver;
import com.thinkbiganalytics.feedmgr.MetadataFields;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.metadata.MetadataField;
import com.thinkbiganalytics.nifi.feedmgr.ConfigurationPropertyReplacer;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.spring.SpringEnvironmentProperties;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.text.StrLookup;
import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Resolves the values for NiFi processor properties using the following logic:
 * <ol>
 * <li>Resolves {@code ${config.<NAME>}} to a property of the same name in the {@code application.properties} file.</li>
 * <li>Looks for a property in {@code application.properties} that matches {@code nifi.<PROCESSOR TYPE>.<PROPERTY KEY>} or {@code nifi.all_processors.<PROPERTY_KEY>}.</li>
 * <li>Resolves {@code ${metadata.<NAME>}} to a {@link MetadataField} property of the {@link FeedMetadata} class.</li>
 * </ol>
 *
 * <p>The {@code <PROCESSOR TYPE>} is the class name of the NiFi processor converted to lowercase. The {@code <PROPERTY KEY>} is the NiFi processor property key converted to lowercase and spaces
 * substituted with underscores.
 * See {@link ConfigurationPropertyReplacer} in the {@code nifi-rest-client} project for more information.</p>
 */
public class PropertyExpressionResolver {

    /**
     * Matches a variable in a property value
     */
    private static final Logger log = LoggerFactory.getLogger(PropertyExpressionResolver.class);

    private static final String VAR_PREFIX = "${";

    /**
     * Prefix for variable-type property replacement
     */
    public static final String configPropertyPrefix = "config.";

    /**
     * Prefix for {@link FeedMetadata} property replacement
     */
    private static String metadataPropertyPrefix = MetadataFieldAnnotationFieldNameResolver.metadataPropertyPrefix;

    /**
     * Properties from the {@code application.properties} file
     */
    @Autowired
    private SpringEnvironmentProperties environmentProperties;

    /**
     * Resolves the values for all properties of the specified feed.
     *
     * @param metadata the feed
     * @return the modified properties
     */
    @Nonnull
    public List<NifiProperty> resolvePropertyExpressions(@Nullable final FeedMetadata metadata) {
        if (metadata != null) {
            return resolvePropertyExpressions(metadata.getProperties(), metadata);
        } else {
            return Collections.emptyList();
        }
    }

    public List<NifiProperty> resolvePropertyExpressions(List<NifiProperty> properties, @Nullable final FeedMetadata metadata) {
        if (metadata != null && properties != null) {
            return properties.stream()
                .filter(property -> resolveExpression(metadata, property))
                .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    public Map<String, Object> getStaticConfigProperties() {
        Map<String, Object> props = environmentProperties.getPropertiesStartingWith(configPropertyPrefix);
        if(props == null){
            props = new HashMap<>();
        }
        Map<String, Object> nifiProps = environmentProperties.getPropertiesStartingWith("nifi.");
        if (nifiProps != null && !nifiProps.isEmpty()) {
            //copy it to a new map
            props = new HashMap<>(props);
            props.putAll(nifiProps);
        }
        return props;
    }

    public List<AnnotatedFieldProperty> getMetadataProperties() {
        return MetadataFields.getInstance().getProperties(FeedMetadata.class);
    }

    /**
     * @return only those properties that were updated
     */
    public List<NifiProperty> resolveStaticProperties(@Nullable final List<NifiProperty> allProperties) {

        if (allProperties != null) {
            return allProperties.stream().filter(property -> resolveStaticConfigProperty(property).isModified).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }


    /**
     * Resolves the value of the specified property of the specified feed.
     *
     * @param metadata the feed
     * @param property the property
     * @return {@code true} if the property was modified, or {@code false} otherwise
     */
    boolean resolveExpression(@Nonnull final FeedMetadata metadata, @Nonnull final NifiProperty property) {
        final ResolveResult variableResult = resolveVariables(property, metadata);
        final ResolveResult staticConfigResult = (!variableResult.isFinal) ? resolveStaticConfigProperty(property) : new ResolveResult(false, false);
        if (variableResult.isModified || staticConfigResult.isModified) {
            if (StringUtils.isEmpty(property.getValue())) {
                property.setValue(null);
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean containsVariablesPatterns(String str) {
        return str.contains(VAR_PREFIX);
    }

    public static class ResolvedVariables {

        private Map<String, String> resolvedVariables;

        private String resolvedString;

        ResolvedVariables(String str) {
            this.resolvedString = str;
            this.resolvedVariables = new HashMap<>();
        }

        public Map<String, String> getResolvedVariables() {
            return resolvedVariables;
        }

        public String getResolvedString() {
            return resolvedString;
        }

        void setResolvedString(String resolvedString) {
            this.resolvedString = resolvedString;
        }
    }


    /**
     * Replace any property in the str  ${var} with the respective value in the map of vars
     */
    public String resolveVariables(String str, Map<String, String> vars) {
        StrSubstitutor ss = new StrSubstitutor(vars);
        return ss.replace(str);
    }

    public String replaceAll(String str, String replacement) {
        if (str != null) {
            StrLookup lookup = new StrLookup() {
                @Override
                public String lookup(String key) {
                    return replacement;
                }
            };
            StrSubstitutor ss = new StrSubstitutor(lookup);
            return ss.replace(str);
        }
        return null;
    }

    /**
     * Resolve the str with values from the supplied {@code properties} This will recursively fill out the str looking back at the properties to get the correct value. NOTE the property values will be
     * overwritten if replacement is found!
     */
    public ResolvedVariables resolveVariables(String str, List<NifiProperty> properties) {
        ResolvedVariables variables = new ResolvedVariables(str);

        StrLookup resolver = new StrLookup() {
            @Override
            public String lookup(String key) {
                Optional<NifiProperty> optional = properties.stream().filter(prop -> key.equals(prop.getKey())).findFirst();
                if (optional.isPresent()) {
                    NifiProperty property = optional.get();
                    String value = StringUtils.isNotBlank(property.getValue()) ? property.getValue().trim() : "";
                    variables.getResolvedVariables().put(property.getKey(), value);
                    return value;
                } else {
                    return null;
                }
            }
        };

        StrSubstitutor ss = new StrSubstitutor(resolver);
        variables.setResolvedString(ss.replace(str));

        Map<String, String> map = variables.getResolvedVariables();
        Map<String, String> vars = map.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> ss.replace(entry.getValue())));
        variables.getResolvedVariables().putAll(vars);

        return variables;
    }

    private String getMetadataPropertyValue(FeedMetadata metadata, String variableName) throws Exception {
        String fieldPathName = StringUtils.substringAfter(variableName, metadataPropertyPrefix);
        Object obj = null;
        try {
            obj = BeanUtils.getProperty(metadata, fieldPathName);
        } catch (Exception e) {
            //    throw new RuntimeException(e);
        }
        //check to see if the path has a Metadata annotation with a matching field
        String matchingProperty = MetadataFields.getInstance().getMatchingPropertyDescriptor(metadata, variableName);
        if (obj == null && matchingProperty != null) {
            matchingProperty = StringUtils.substringAfter(matchingProperty, metadataPropertyPrefix);
            obj = BeanUtils.getProperty(metadata, matchingProperty);
        }
        if (obj != null) {
            return obj.toString();
        } else {
            return null;
        }
    }

    /**
     * Find a property in the env properties matching one of the following:
     * 1) nifi.<processorType>[<processorName>].<property key>
     * 2)    nifi.<processorType>.<property key>
     * 3) nifi.all_processors.<property key>
     * @param property the property
     * @param propertyKey a config. environment property to match on
     * @return
     */
    private String getConfigurationPropertyValue(NifiProperty property, String propertyKey) {
        if (StringUtils.isNotBlank(propertyKey) && propertyKey.startsWith(configPropertyPrefix)) {
            return ConfigurationPropertyReplacer.fixNiFiExpressionPropertyValue(environmentProperties.getPropertyValueAsString(propertyKey));
        } else {
            //see if the processorType is configured
            String processorTypeWithProcessorNameProperty = ConfigurationPropertyReplacer.getProcessorNamePropertyConfigName(property);
            String value = environmentProperties.getPropertyValueAsString(processorTypeWithProcessorNameProperty);
            if(StringUtils.isBlank(value)) {
                String processorTypeProperty = ConfigurationPropertyReplacer.getProcessorPropertyConfigName(property);
                value = environmentProperties.getPropertyValueAsString(processorTypeProperty);
                if (StringUtils.isBlank(value)) {
                    String globalPropertyType = ConfigurationPropertyReplacer.getGlobalAllProcessorsPropertyConfigName(property);
                    value = environmentProperties.getPropertyValueAsString(globalPropertyType);
                }
            }
            return ConfigurationPropertyReplacer.fixNiFiExpressionPropertyValue(value);
        }
    }



    /**
     * Resolves the value of the specified property using static configuration properties.
     *
     * @param property the property
     * @return the result of the transformation
     */
    private ResolveResult resolveStaticConfigProperty(@Nonnull final NifiProperty property) {
        final String processTypeAndProcessNameProperty = ConfigurationPropertyReplacer.getProcessorNamePropertyConfigName(property);
        final String processTypeAndProcessNamePropertyValue = environmentProperties.getPropertyValueAsString(processTypeAndProcessNameProperty);


        final String processorTypeProperty = ConfigurationPropertyReplacer.getProcessorPropertyConfigName(property);
        final String processorTypePropertyValue = environmentProperties.getPropertyValueAsString(processorTypeProperty);

        final String globalName = ConfigurationPropertyReplacer.getGlobalAllProcessorsPropertyConfigName(property);
        final String globalPropertyValue = environmentProperties.getPropertyValueAsString(globalName);

        //value first == processorTypeAndProcessNamePropertyValue, thne processorTypePropertyValue,  finally globalPropertyValue
        String value = StringUtils.isBlank(processTypeAndProcessNamePropertyValue) ?  ( StringUtils.isBlank(processorTypePropertyValue) ? globalPropertyValue : processorTypePropertyValue) : processTypeAndProcessNamePropertyValue;

        if (value != null) {
            value = ConfigurationPropertyReplacer.fixNiFiExpressionPropertyValue(value);
            property.setValue(value);
            return new ResolveResult(true, true);
        }
        return new ResolveResult(false, false);
    }


    /**
     * Resolves the variables in the value of the specified property.
     *
     * @param property the property
     * @param metadata the feed
     * @return the result of the transformation
     */
    private ResolveResult resolveVariables(@Nonnull final NifiProperty property, @Nonnull final FeedMetadata metadata) {
        // Filter blank values
        final String value = property.getValue();
        if (StringUtils.isBlank(value)) {
            return new ResolveResult(false, false);
        }

        final boolean[] hasConfig = {false};
        final boolean[] isModified = {false};

        StrLookup resolver = new StrLookup() {
            @Override
            public String lookup(String variable) {
                // Resolve configuration variables
                final String configValue = getConfigurationPropertyValue(property, variable);
                if (configValue != null && property.getValue() != null && !property.getValue().equalsIgnoreCase(configValue)) {
                    hasConfig[0] = true;
                    isModified[0] = true;
                    //if this is the first time we found the config var, set the template value correctly
                    if (!property.isContainsConfigurationVariables()) {
                        property.setTemplateValue(property.getValue());
                        property.setContainsConfigurationVariables(true);
                    }
                    return configValue;

                }

                // Resolve metadata variables
                try {
                    final String metadataValue = getMetadataPropertyValue(metadata, variable);
                    if (metadataValue != null) {
                        isModified[0] = true;
                        return metadataValue;
                    }
                } catch (Exception e) {
                    log.error("Unable to resolve variable: " + variable, e);
                }

                return null;
            }
        };
        StrSubstitutor ss = new StrSubstitutor(resolver);
        ss.setEnableSubstitutionInVariables(true);
        //escape
        String val = StringUtils.trim(ss.replace(value));
        //fix
        property.setValue(val);

        return new ResolveResult(hasConfig[0], isModified[0]);
    }

    /**
     * The result of resolving a NiFi property value.
     */
    private static class ResolveResult {

        /**
         * Indicates that the value should not be resolved further
         */
        final boolean isFinal;

        /**
         * Indicates that the value was modified
         */
        final boolean isModified;

        /**
         * Constructs a {@code ResultResult} with the specified attributes.
         *
         * @param isFinal    {@code true} if the value should not be resolved further, or {@code false} otherwise
         * @param isModified {@code true} if the value was modified, or {@code false} otherwise
         */
        ResolveResult(final boolean isFinal, final boolean isModified) {
            this.isFinal = isFinal;
            this.isModified = isModified;
        }
    }
}
