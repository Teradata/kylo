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
import com.thinkbiganalytics.metadata.MetadataField;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.nifi.feedmgr.ConfigurationPropertyReplacer;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Resolves the values for NiFi processor properties using the following logic:
 * <ol>
 *     <li>Resolves {@code ${config.<NAME>}} to a property of the same name in the {@code application.properties} file.</li>
 *     <li>Looks for a property in {@code application.properties} that matches {@code nifi.<PROCESSOR TYPE>.<PROPERTY KEY>} or {@code nifi.all_processors.<PROPERTY_KEY>}.</li>
 *     <li>Resolves {@code ${metadata.<NAME>}} to a {@link MetadataField} property of the {@link FeedMetadata} class.</li>
 * </ol>
 *
 * <p>The {@code <PROCESSOR TYPE>} is the class name of the NiFi processor converted to lowercase. The {@code <PROPERTY KEY>} is the NiFi processor property key converted to lowercase and spaces
 * substituted with underscores.
 * See {@link ConfigurationPropertyReplacer} in the {@code nifi-rest-client} project for more information.</p>
 */
public class PropertyExpressionResolver {

    private static final Logger log = LoggerFactory.getLogger(PropertyExpressionResolver.class);

    /** Matches a variable in a property value */
    public static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{(.+?)\\}");

    /** Prefix for variable-type property replacement */
    public static String configPropertyPrefix = "config.";

    /** Prefix for {@link FeedMetadata} property replacement */
    public static String metadataPropertyPrefix = MetadataFieldAnnotationFieldNameResolver.metadataPropertyPrefix;

    /** Properties from the {@code application.properties} file */
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
    public boolean resolveExpression(@Nonnull final FeedMetadata metadata, @Nonnull final NifiProperty property) {
        final ResolveResult variableResult = resolveVariables(property, metadata);
        final ResolveResult staticConfigResult = (!variableResult.isFinal) ? resolveStaticConfigProperty(property) : new ResolveResult(false, false);
        return variableResult.isModified || staticConfigResult.isModified;
    }

    public String getMetadataPropertyValue(FeedMetadata metadata, String variableName) throws Exception {
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

    public String getConfigurationPropertyValue(NifiProperty property, String propertyKey) {
        if (StringUtils.isNotBlank(propertyKey) && propertyKey.startsWith(configPropertyPrefix)) {
            return environmentProperties.getPropertyValueAsString(propertyKey);
        } else {
            //see if the processorType is configured
            String processorTypeProperty = ConfigurationPropertyReplacer.getProcessorPropertyConfigName(property);
            String value = environmentProperties.getPropertyValueAsString(processorTypeProperty);
            if (StringUtils.isBlank(value)) {
                String globalPropertyType = ConfigurationPropertyReplacer.getGlobalAllProcessorsPropertyConfigName(property);
                value = environmentProperties.getPropertyValueAsString(globalPropertyType);
            }
            return value;
        }
    }

    public List<AnnotatedFieldProperty> getMetadataProperties() {
        return MetadataFields.getInstance().getProperties(FeedMetadata.class);
    }

    /**
     * Resolves the value of the specified property using static configuration properties.
     *
     * @param property the property
     * @return the result of the transformation
     */
    private ResolveResult resolveStaticConfigProperty(@Nonnull final NifiProperty property) {
        final String name = ConfigurationPropertyReplacer.getProcessorPropertyConfigName(property);
        final String processorTypePropertyValue = environmentProperties.getPropertyValueAsString(name);

        final String globalName = ConfigurationPropertyReplacer.getGlobalAllProcessorsPropertyConfigName(property);
        final String globalPropertyValue = environmentProperties.getPropertyValueAsString(globalName);

        final String value = StringUtils.isBlank(processorTypePropertyValue) ? globalPropertyValue : processorTypePropertyValue;

        if (value != null) {
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

        // Look for a match, else return
        final Matcher matcher = VARIABLE_PATTERN.matcher(value);
        if (!matcher.find()) {
            return new ResolveResult(false, false);
        }

        // Replace matches with resolved values
        boolean hasConfig = false;
        boolean isModified = false;
        final StringBuffer result = new StringBuffer(value.length() * 2);

        do {
            final String variable = matcher.group(1);

            // Resolve configuration variables
            final String configValue = getConfigurationPropertyValue(property, variable);
            if (configValue != null) {
                hasConfig = true;
                isModified = true;
                matcher.appendReplacement(result, Matcher.quoteReplacement(configValue));
                continue;
            }

            // Resolve metadata variables
            try {
                final String metadataValue = getMetadataPropertyValue(metadata, variable);
                if (metadataValue != null) {
                    isModified = true;
                    matcher.appendReplacement(result, Matcher.quoteReplacement(metadataValue));
                }
            } catch (Exception e) {
                log.error("Unable to resolve variable: " + variable, e);
            }
        } while (matcher.find());

        // Replace property value
        matcher.appendTail(result);
        property.setValue(StringUtils.trim(result.toString()));

        return new ResolveResult(hasConfig, isModified);
    }

    /**
     * The result of resolving a NiFi property value.
     */
    private static class ResolveResult {
        /** Indicates that the value should not be resolved further */
        final boolean isFinal;

        /** Indicates that the value was modified */
        final boolean isModified;

        /**
         * Constructs a {@code ResultResult} with the specified attributes.
         *
         * @param isFinal {@code true} if the value should not be resolved further, or {@code false} otherwise
         * @param isModified {@code true} if the value was modified, or {@code false} otherwise
         */
        ResolveResult(final boolean isFinal, final boolean isModified) {
            this.isFinal = isFinal;
            this.isModified = isModified;
        }
    }

    public boolean containsVariablesPatterns(String str) {
        final Matcher matcher = VARIABLE_PATTERN.matcher(str);
        return (matcher.find());
    }

    public static class ResolvedVariables {

        private Map<String, String> resolvedVariables;

        private String str;

        private String resolvedString;

        public ResolvedVariables(String str) {
            this.str = str;
            this.resolvedString = str;
            this.resolvedVariables = new HashMap<>();
        }

        public Map<String, String> getResolvedVariables() {
            return resolvedVariables;
        }

        public void setResolvedVariables(Map<String, String> resolvedVariables) {
            this.resolvedVariables = resolvedVariables;
        }

        public String getResolvedString() {
            return resolvedString;
        }

        public void setResolvedString(String resolvedString) {
            this.resolvedString = resolvedString;
        }
    }


    /**
     * Replace any property in the str  ${var} with the respective value in the map of vars
     */
    public String resolveVariables(String str, Map<String, String> vars) {

        String replacedString = str;
        if (str != null) {
            // Look for a match, else return
            final Matcher matcher = VARIABLE_PATTERN.matcher(str);
            if (!matcher.find()) {
                return replacedString;
            }

            final StringBuffer result = new StringBuffer(str.length() * 2);

            do {
                final String variable = matcher.group(1);

                String replacement = vars.get(variable);
                if (replacement != null) {
                    matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
                }
            } while (matcher.find());

            // Replace property value
            matcher.appendTail(result);
            replacedString = StringUtils.trim(result.toString());
        }
        return replacedString;
    }

    public String replaceAll(String str, String replacement) {
        if (str != null) {
            final Matcher matcher = VARIABLE_PATTERN.matcher(str);
            return matcher.replaceAll(replacement);
        }
        return str;
    }

    /**
     * Resolve the str with values from the supplied {@code properties} This will recursively fill out the str looking back at the properties to get the correct value. NOTE the property values will be
     * overwritten if replacement is found!
     */
    public ResolvedVariables resolveVariables(String str, List<NifiProperty> properties) {
        return resolveVariables(str, properties, new HashMap<>());
    }

    private ResolvedVariables resolveVariables(String str, List<NifiProperty> properties, Map<String, Set<String>> resolvedValues) {
        ResolvedVariables variables = new ResolvedVariables(str);

        if (str != null) {
            // Look for a match, else return
            final Matcher matcher = PropertyExpressionResolver.VARIABLE_PATTERN.matcher(str);
            if (!matcher.find()) {
                return variables;
            }

            final StringBuffer result = new StringBuffer(str.length() * 2);

            do {
                final String variable = matcher.group(1);
                NifiProperty property = properties.stream().filter(p -> p.getKey().equals(variable)).findFirst().orElse(null);
                if (property != null) {
                    String replacedString = property.getValue();
                    //cannot contain itself

                    if (replacedString != null && !replacedString.contains("${" + property.getKey() + "}") && (!resolvedValues.containsKey(property.getKey()) || (!resolvedValues.get(property.getKey())
                        .contains(replacedString)))) {
                        resolvedValues.computeIfAbsent(property.getKey(), (key) -> new HashSet<String>()).add(replacedString);
                        ResolvedVariables resolvedVariables = resolveVariables(replacedString, properties, resolvedValues);
                        replacedString = resolvedVariables.getResolvedString();
                        property.setValue(resolvedVariables.getResolvedString());
                        variables.getResolvedVariables().put(property.getKey(), replacedString);

                    }
                    if (replacedString != null) {
                        matcher.appendReplacement(result, Matcher.quoteReplacement(replacedString));
                    }
                }

            } while (matcher.find());

            // Replace property value
            matcher.appendTail(result);
            String replacement = StringUtils.trim(result.toString());
            variables.setResolvedString(replacement);
            return variables;

        } else {
            return null;
        }
    }







}
