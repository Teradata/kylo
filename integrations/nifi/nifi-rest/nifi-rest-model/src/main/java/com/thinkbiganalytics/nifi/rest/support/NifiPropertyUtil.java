package com.thinkbiganalytics.nifi.rest.support;

/*-
 * #%L
 * thinkbig-nifi-rest-model
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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.thinkbiganalytics.nifi.rest.model.NiFiAllowableValue;
import com.thinkbiganalytics.nifi.rest.model.NiFiPropertyDescriptor;
import com.thinkbiganalytics.nifi.rest.model.NiFiPropertyDescriptorTransform;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.ControllerServiceDTO;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.apache.nifi.web.api.dto.PropertyDescriptorDTO;
import org.apache.nifi.web.api.dto.TemplateDTO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Uitlity to extract properties and property info from NiFi
 */
public class NifiPropertyUtil {

    /**
     * map the incoming list of properties to a key,value map
     *
     * @param propertyList a list of properties
     * @return a map with the {@link NifiProperty#getKey()} as the key and the {@link NifiProperty} as the value
     */
    public static Map<String, NifiProperty> propertiesAsMap(List<NifiProperty> propertyList) {
        Map<String, NifiProperty> map = new HashMap<String, NifiProperty>();
        for (NifiProperty property : propertyList) {
            map.put(property.getKey(), property);
        }
        return map;
    }

    /**
     * For a given template object return the list of properties
     *
     * @param parentProcessGroup          the parent group associated with the template
     * @param dto                         the template
     * @param propertyDescriptorTransform transformation utility
     * @return the list of properties
     */
    public static List<NifiProperty> getPropertiesForTemplate(ProcessGroupDTO parentProcessGroup, TemplateDTO dto, NiFiPropertyDescriptorTransform propertyDescriptorTransform) {
        return getPropertiesForTemplate(parentProcessGroup, dto, propertyDescriptorTransform, false);
    }

    /**
     * For a given template object return the list of properties. optionally choose to exclude any input processors from the property list
     *
     * @param parentProcessGroup          the parent process group associated wiht the template
     * @param dto                         the template
     * @param propertyDescriptorTransform transformation utility
     * @param excludeInputProcessors      {@code true} removes the properties part of the input processors, {@code false} will include all properties in all processors of the template
     */
    public static List<NifiProperty> getPropertiesForTemplate(ProcessGroupDTO parentProcessGroup, TemplateDTO dto, NiFiPropertyDescriptorTransform propertyDescriptorTransform,
                                                              boolean excludeInputProcessors) {
        List<NifiProperty> properties = new ArrayList<NifiProperty>();
        if (dto != null) {
            List<ProcessorDTO> inputs = NifiTemplateUtil.getInputProcessorsForTemplate(dto);
            Set<ProcessorDTO> processorDTOSet = NifiProcessUtil.getProcessors(dto, excludeInputProcessors);
            Map<String, ProcessGroupDTO> groupMap = NifiProcessUtil.getProcessGroupsMap(dto);

            for (ProcessorDTO processor : processorDTOSet) {
                ProcessGroupDTO group = groupMap.get(processor.getParentGroupId());
                if (group == null) {
                    group = parentProcessGroup;
                }
                List<NifiProperty> propertyList = getPropertiesForProcessor(group, processor, propertyDescriptorTransform);
                //assign the property as an input property if it is one
                if (NifiProcessUtil.findFirstProcessorsById(inputs, processor.getId()) != null) {
                    for (NifiProperty property : propertyList) {
                        property.setInputProperty(true);
                    }
                }
                properties.addAll(propertyList);
            }
        }
        return properties;
    }

    /**
     * Return a property matching a given processor name and property name
     *
     * @param processorName the processor name to match
     * @param propertyName  the name of hte {@link NifiProperty#getKey()}
     * @param properties    a list of properties to inspect
     * @return the first property matching the processorName nad propertyName
     */
    public static NifiProperty getProperty(final String processorName, final String propertyName, List<NifiProperty> properties) {
        NifiProperty property = Iterables.tryFind(properties, new Predicate<NifiProperty>() {
            @Override
            public boolean apply(NifiProperty property) {
                return property.getProcessorName().equalsIgnoreCase(processorName) && property.getKey().equalsIgnoreCase(propertyName);
            }
        }).orNull();
        return property;
    }

    /**
     * Create a deep copy of properties to a new List
     *
     * @param properties a list of properties
     * @return a new list containing the newly copied properties.
     */
    public static List<NifiProperty> copyProperties(List<NifiProperty> properties) {
        List<NifiProperty> copyList = new ArrayList<>();
        for (NifiProperty property : properties) {
            copyList.add(new NifiProperty(property));
        }
        return copyList;
    }

    /**
     * Return all properties assocated with a given controller service
     *
     * @param service                     the service to inspect
     * @param propertyDescriptorTransform the transform utility
     * @return a list of properties on the service
     */
    public static List<NifiProperty> getPropertiesForService(ControllerServiceDTO service, NiFiPropertyDescriptorTransform propertyDescriptorTransform) {
        return service.getProperties().entrySet().stream()
            .map(entry -> {
                final NiFiPropertyDescriptor propertyDescriptor = propertyDescriptorTransform.toNiFiPropertyDescriptor(service.getDescriptors().get(entry.getKey()));
                return new NifiProperty(service.getParentGroupId(), service.getId(), entry.getKey(), entry.getValue(), propertyDescriptor);
            })
            .collect(Collectors.toList());
    }

    /**
     * Return a list of properties on a gi en processor
     *
     * @param processGroup                the processors group
     * @param processor                   the processor
     * @param propertyDescriptorTransform the transform utility
     * @return the list of properties on the processor
     */
    public static List<NifiProperty> getPropertiesForProcessor(ProcessGroupDTO processGroup, ProcessorDTO processor, NiFiPropertyDescriptorTransform propertyDescriptorTransform) {
        List<NifiProperty> properties = new ArrayList<>();
        for (Map.Entry<String, String> entry : processor.getConfig().getProperties().entrySet()) {
            PropertyDescriptorDTO descriptorDTO = processor.getConfig().getDescriptors().get(entry.getKey());
            if (descriptorDTO != null) {
                final NiFiPropertyDescriptor propertyDescriptor = propertyDescriptorTransform.toNiFiPropertyDescriptor(processor.getConfig().getDescriptors().get(entry.getKey()));
                final NifiProperty property = new NifiProperty(processor.getParentGroupId(), processor.getId(), entry.getKey(), entry.getValue(), propertyDescriptor);
                property.setProcessGroupName(processGroup.getName());
                property.setProcessorName(processor.getName());
                property.setProcessorType(processor.getType());
                properties.add(property);
            }
        }
        return properties;
    }

    /**
     * Return all properties for a given process group
     *
     * @param processGroupDTO             the process group to inspect
     * @param propertyDescriptorTransform the transform utility
     * @return the list of properties
     */
    public static List<NifiProperty> getProperties(ProcessGroupDTO processGroupDTO, NiFiPropertyDescriptorTransform propertyDescriptorTransform) {
        List<NifiProperty> properties = new ArrayList<NifiProperty>();
        if (processGroupDTO != null) {

            if (processGroupDTO.getContents().getProcessors() != null && !processGroupDTO.getContents().getProcessors().isEmpty()) {
                for (ProcessorDTO processorDTO : processGroupDTO.getContents().getProcessors()) {
                    properties.addAll(NifiPropertyUtil.getPropertiesForProcessor(processGroupDTO, processorDTO, propertyDescriptorTransform));
                }
            }

            if (processGroupDTO.getContents().getProcessGroups() != null && !processGroupDTO.getContents().getProcessGroups().isEmpty()) {
                for (ProcessGroupDTO groupDTO : processGroupDTO.getContents().getProcessGroups()) {
                    properties.addAll(NifiPropertyUtil.getProperties(groupDTO, propertyDescriptorTransform));
                }
            }
        }
        return properties;
    }

    /**
     * Return a map of processGroupId to a map of that groups processors and its respective propeties
     *
     * @param properties the properties to inspect
     * @return a map with the key being the processGroupId and the value being a map of properties with its key being the processorId
     */
    public static Map<String, Map<String, List<NifiProperty>>> groupPropertiesByProcessGroupAndProcessor(List<NifiProperty> properties) {
        Map<String, Map<String, List<NifiProperty>>> processGroupProperties = new HashMap();
        for (NifiProperty property : properties) {
            String processGroup = property.getProcessGroupId();
            String processorId = property.getProcessorId();

            if (!processGroupProperties.containsKey(processGroup)) {
                processGroupProperties.put(processGroup, new HashMap<String, List<NifiProperty>>());
            }
            if (!processGroupProperties.get(processGroup).containsKey(processorId)) {
                processGroupProperties.get(processGroup).put(processorId, new ArrayList<NifiProperty>());
            }

            processGroupProperties.get(processGroup).get(processorId).add(property);
        }
        return processGroupProperties;
    }

    /**
     * Groups the properties by their {@see NifiProperty#getIdKey}
     * @param properties the properties to inspect
     * @return a map with the property idKey (the processgroup+processorId+propertyKey, property)
     */
    public static Map<String,NifiProperty> groupPropertiesByIdKey(List<NifiProperty> properties) {
        Map<String, NifiProperty> map = new HashMap();
        if(properties != null){
            map = properties.stream().collect(Collectors.toMap(p -> p.getIdKey(), p-> p));
        }
        return map;
    }

    /**
     * Return all properties for a given processor
     *
     * @param properties  the properties to inspect
     * @param processorId the processor id to match
     * @return the list of properties for the processorId
     */
    public static List<NifiProperty> getPropertiesForProcessor(List<NifiProperty> properties, final String processorId) {
        return Lists.newArrayList(Iterables.filter(properties, new Predicate<NifiProperty>() {
            @Override
            public boolean apply(NifiProperty property) {
                return property.getProcessorId().equalsIgnoreCase(processorId);
            }
        }));
    }

    /**
     * Updates the values of the destination properties that match the specified source properties.
     *
     * <p>Matches are made using the processor ID or name and the property key.</p>
     *
     * @param sourceGroupName       name of the source process group
     * @param destinationGroupName  name of the destination process group
     * @param destinationProperties properties of processors in the destination group
     * @param sourceProperties      properties of processors in the source group
     * @return modified properties from the destination group
     */
    @Nonnull
    public static List<NifiProperty> matchAndSetPropertyValues(@Nonnull final String sourceGroupName, @Nonnull final String destinationGroupName,
                                                               @Nonnull final List<NifiProperty> destinationProperties, @Nullable final List<NifiProperty> sourceProperties) {
        // Shortcut if there are no properties
        if (destinationProperties.isEmpty() || sourceProperties == null || sourceProperties.isEmpty()) {
            return Collections.emptyList();
        }

        // Create mappings for destination properties
        final Map<String, NifiProperty> propertyById = new HashMap<>(destinationProperties.size());
        final Map<String, NifiProperty> propertyByName = new HashMap<>(destinationProperties.size());
        final Map<String, String> groupIdByName = new HashMap<>(destinationProperties.size());
        final Map<String, String> processorIdByName = new HashMap<>(destinationProperties.size());

        for (final NifiProperty property : destinationProperties) {
            propertyById.put(property.getIdKey(), property);
            propertyByName.put(property.getNameKey(), property);
            groupIdByName.put(property.getProcessGroupName(), property.getProcessGroupId());
            processorIdByName.put(property.getProcessorName(), property.getProcessorId());
        }

        // Match and update destination properties
        final List<NifiProperty> modifiedProperties = new ArrayList<>(destinationProperties.size());

        for (final NifiProperty sourceProperty : sourceProperties) {
            // Update source property to match destination
            sourceProperty.setTemplateProperty(new NifiProperty(sourceProperty));

            if (sourceProperty.getProcessGroupName().equalsIgnoreCase(sourceGroupName)) {
                sourceProperty.setProcessGroupName(destinationGroupName);
            }

            // Find destination property
            NifiProperty destinationProperty = propertyById.get(sourceProperty.getIdKey());
            if (destinationProperty == null) {
                destinationProperty = propertyByName.get(sourceProperty.getNameKey());
            }

            if (destinationProperty != null) {
                sourceProperty.setProcessGroupId(groupIdByName.get(destinationProperty.getProcessGroupName()));
                sourceProperty.setProcessorId(processorIdByName.get(destinationProperty.getProcessorName()));

                final String sourceValue = sourceProperty.getValue();
                if (isValidPropertyValue(destinationProperty, sourceValue)) {
                    destinationProperty.setValue(sourceValue);
                    modifiedProperties.add(destinationProperty);
                }
            }
        }

        return modifiedProperties;
    }

    /**
     * Validates that the specified value is valid for the property.
     *
     * @param property the property
     * @param value    the value to validate
     * @return {@code true} if the value is valid for the property, or {@code false} otherwise
     */
    private static boolean isValidPropertyValue(@Nonnull final NifiProperty property, final String value) {
        // Check for list of allowable values
        final Optional<List<NiFiAllowableValue>> allowableValues = Optional.of(property)
            .map(NifiProperty::getPropertyDescriptor)
            .map(NiFiPropertyDescriptor::getAllowableValues);
        if (allowableValues.isPresent()) {
            return allowableValues.get().stream()
                .filter(allowableValue -> allowableValue.getValue().equals(value))
                .findAny()
                .isPresent();
        }
        return true;
    }

    /**
     * find all properties whoes internal {@link NifiProperty#getIdKey()} matches that of the template properties
     *
     * @param templateProperties the properties that are part of the template
     * @param nifiProperties     the properties to inspect and match
     * @param updateMode         a mode indicating what should be inspected and updated
     * @return a list of properties matching the templateProperties and the nifiProperties based upon their {@link NifiProperty#getIdKey()}
     */
    public static List<NifiProperty> matchAndSetPropertyByIdKey(Collection<NifiProperty> templateProperties, List<NifiProperty> nifiProperties, PROPERTY_MATCH_AND_UPDATE_MODE updateMode) {
        List<NifiProperty> matchedProperties = new ArrayList<>();

        if (nifiProperties != null && !nifiProperties.isEmpty()) {
            for (NifiProperty nifiProperty : nifiProperties) {
                NifiProperty matched = matchPropertyByIdKey(templateProperties, nifiProperty, updateMode);
                if (matched != null) {
                    matchedProperties.add(matched);
                }
            }
        }
        return matchedProperties;
    }

    /**
     * Find all properties that have the same name and property key
     *
     * @param templateProperties the properties that are part of the template.  These properties will get updated from the {@code nifiProperties} passed in if they match
     * @param nifiProperties     the properties to inspect and match
     * @param updateMode         a mode indicating what should be inspected and updated
     * @return a list of matched properties
     */
    public static List<NifiProperty> matchAndSetPropertyByProcessorName(Collection<NifiProperty> templateProperties, List<NifiProperty> nifiProperties, PROPERTY_MATCH_AND_UPDATE_MODE updateMode) {
        List<NifiProperty> matchedProperties = new ArrayList<>();
        if (nifiProperties != null && !nifiProperties.isEmpty()) {
            for (NifiProperty nifiProperty : nifiProperties) {
                NifiProperty matched = matchPropertyByProcessorName(templateProperties, nifiProperty, updateMode);
                if (matched != null) {
                    matchedProperties.add(matched);
                }
            }
        }
        return matchedProperties;
    }



    /**
     * Return the first property matching a given property key that is part of a processor with the supplied processorType
     *
     * @param properties    a collection of properties to inspect
     * @param processorType the type of processor to match
     * @param propertyKey   the name of the property to find
     * @return the first property matching a given property key that is part of a processor with the supplied processorType
     */
    public static NifiProperty findPropertyByProcessorType(Collection<NifiProperty> properties, final String processorType, final String propertyKey) {
        List<NifiProperty> matchingProperties = Lists.newArrayList(Iterables.filter(properties, new Predicate<NifiProperty>() {
            @Override
            public boolean apply(NifiProperty property) {
                return property.getKey().equalsIgnoreCase(propertyKey) && property.getProcessorType().equalsIgnoreCase(processorType);
            }
        }));
        if (matchingProperties != null && !matchingProperties.isEmpty()) {
            return matchingProperties.get(0);
        }
        return null;
    }

    /**
     * Return the first property matching a given property key that is part of a processor with the supplied name
     *
     * @param properties    a collection of properties to inspect
     * @param processorName the name of processor to match
     * @param propertyKey   the name of the property to find
     * @return the first property matching a given property key that is part of a processor with the supplied name
     */
    public static NifiProperty findPropertyByProcessorName(Collection<NifiProperty> properties, final String processorName, final String propertyKey) {
        List<NifiProperty> matchingProperties = Lists.newArrayList(Iterables.filter(properties, new Predicate<NifiProperty>() {
            @Override
            public boolean apply(NifiProperty property) {
                return property.getKey().equalsIgnoreCase(propertyKey) && property.getProcessorName().equalsIgnoreCase(processorName);
            }
        }));
        if (matchingProperties != null && !matchingProperties.isEmpty()) {
            return matchingProperties.get(0);
        }
        return null;
    }

    /**
     * update a given property with the values from the nifiProperty
     *
     * @param propertyToUpdate a property to update
     * @param nifiProperty     a property with values that will be set to the 'propertyToUpdate'
     */
    private static void updateProperty(NifiProperty propertyToUpdate, NifiProperty nifiProperty, PROPERTY_MATCH_AND_UPDATE_MODE updateMode) {
        propertyToUpdate.setValue(nifiProperty.getValue());
        if(!PROPERTY_MATCH_AND_UPDATE_MODE.FEED_DETAILS_MATCH_TEMPLATE.equals(updateMode)){
            //if its Not updating for editing a Feed then attempt to set the render type properties.
            //otherwise the 'propertyToUpdate' will be the registeredTemplate  property and should not be updated from the feed values as the template should drive how the feed is rendered.
            propertyToUpdate.setInputProperty(nifiProperty.isInputProperty());
            propertyToUpdate.setUserEditable(nifiProperty.isUserEditable());
            propertyToUpdate.setSelected(nifiProperty.isSelected());
            propertyToUpdate.setRenderType(nifiProperty.getRenderType());
            propertyToUpdate.setSensitive(nifiProperty.isSensitive());
            propertyToUpdate.setRequired(nifiProperty.isRequired());
            if(nifiProperty.getPropertyDescriptor() != null) {
                propertyToUpdate.setPropertyDescriptor(nifiProperty.getPropertyDescriptor());
            }
            if (nifiProperty.getRenderOptions() != null) {
                propertyToUpdate.setRenderOptions(nifiProperty.getRenderOptions());
            }
        }

    }

    /**
     * Return the first property matching the supplied nifiProperty {@link NifiProperty#getIdKey()}
     *
     * @param properties   a collection of properties to inspect
     * @param nifiProperty a property to check against
     * @return the first property to match the supplied nifiProperty {@link NifiProperty#getIdKey()}
     */
    public static NifiProperty findPropertyByIdKey(Collection<NifiProperty> properties, final NifiProperty nifiProperty) {
        List<NifiProperty> matchingProperties = Lists.newArrayList(Iterables.filter(properties, new Predicate<NifiProperty>() {
            @Override
            public boolean apply(NifiProperty property) {
                return property.matchesIdKey(nifiProperty);
            }
        }));
        if (matchingProperties != null && !matchingProperties.isEmpty()) {
            return matchingProperties.get(0);
        }
        return null;
    }

    /**
     * Return a list of all the input properties ( properties that are part of processors that dont have an incoming connections)
     *
     * @param properties a collection of properties to inspect
     * @return a list of all the input properties ( properties that are part of processors that dont have an incoming connections)
     */
    public static List<NifiProperty> findInputProperties(Collection<NifiProperty> properties) {
        return Lists.newArrayList(Iterables.filter(properties, new Predicate<NifiProperty>() {
            @Override
            public boolean apply(NifiProperty property) {
                return property.isInputProperty();
            }
        }));

    }

    /**
     * Return a list of all the input properties ( properties that are part of processors that dont have an incoming connections) that match the supplied processorType
     *
     * @param properties    a collection of properties to inspect
     * @param processorType the type of processor to match
     * @return a list of all the input properties ( properties that are part of processors that dont have an incoming connections) that match the supplied processorType
     */
    public static List<NifiProperty> findInputPropertyMatchingType(Collection<NifiProperty> properties, final String processorType) {

        return Lists.newArrayList(Iterables.filter(properties, new Predicate<NifiProperty>() {
            @Override
            public boolean apply(NifiProperty property) {
                return property.isInputProperty() && property.getProcessorType().equalsIgnoreCase(processorType);
            }
        }));
    }

    /**
     * Return the first property in the collection that matches the {@link NifiProperty#getKey()}
     *
     * @param properties  a collection of properties to inspect
     * @param propertyKey the key to match
     * @return the first property in the collection that matches the {@link NifiProperty#getKey()}
     */
    public static NifiProperty findFirstPropertyMatchingKey(Collection<NifiProperty> properties, final String propertyKey) {
        if (properties != null) {
            return Iterables.tryFind(properties, new Predicate<NifiProperty>() {
                @Override
                public boolean apply(NifiProperty property) {
                    return property.getKey().equalsIgnoreCase(propertyKey);
                }
            }).orNull();
        } else {
            return null;
        }
    }

    /**
     * find properties in the supplied list, using the supplied predicate
     *
     * @param list1     a collection of properties
     * @param predicate a predicate to filter
     * @return the resulting filtered list
     */
    public static List<NifiProperty> findProperties(Collection<NifiProperty> list1, Predicate<NifiProperty> predicate) {
        return Lists.newArrayList(Iterables.filter(list1, predicate));
    }

    /**
     * Find the first property in the collection that has a given processor name
     *
     * @param properties   a collection of properties
     * @param nifiProperty a property to check against looking at the {@link NifiProperty#processorName}
     * @return a matching property
     */
    public static NifiProperty findPropertyByProcessorName(Collection<NifiProperty> properties, final NifiProperty nifiProperty) {
        List<NifiProperty> matchingProperties = Lists.newArrayList(Iterables.filter(properties, new Predicate<NifiProperty>() {
            @Override
            public boolean apply(NifiProperty property) {
                return property.getKey().equalsIgnoreCase(nifiProperty.getKey()) && property.getProcessorName().equalsIgnoreCase(nifiProperty.getProcessorName());
            }
        }));
        if (matchingProperties != null && !matchingProperties.isEmpty()) {
            return matchingProperties.get(0);
        }
        return null;
    }

    /**
     * update the templateProperties with those that match the nifiProperty using the {@link NifiProperty#getIdKey()} to make the match
     *
     * @param templateProperties the properties to update
     * @param nifiProperty       the property to check
     * @param updateMode         a mode to update
     * @return the property, or updated property if matched
     */
    private static NifiProperty matchPropertyByIdKey(Collection<NifiProperty> templateProperties, final NifiProperty nifiProperty, PROPERTY_MATCH_AND_UPDATE_MODE updateMode) {
        NifiProperty matchingProperty = findPropertyByIdKey(templateProperties, nifiProperty);
        if (matchingProperty != null) {
            updateMatchingProperty(matchingProperty, nifiProperty, updateMode);
        }
        return matchingProperty;
    }

    /**
     * update the templateProperties with those that match the nifiProperty using the {@link NifiProperty#getProcessorName()}  to make the match
     *
     * @param templateProperties the properties to update
     * @param nifiProperty       the property to check
     * @param updateMode         a mode to update
     * @return the property, or updated property if matched
     */
    private static NifiProperty matchPropertyByProcessorName(Collection<NifiProperty> templateProperties, final NifiProperty nifiProperty, PROPERTY_MATCH_AND_UPDATE_MODE updateMode) {
        NifiProperty matchingProperty = findPropertyByProcessorName(templateProperties, nifiProperty);
        if (matchingProperty != null) {
            //
            if(PROPERTY_MATCH_AND_UPDATE_MODE.FEED_DETAILS_MATCH_TEMPLATE.equals(updateMode)){
                //copy the property
                NifiProperty copy = new NifiProperty(matchingProperty);
                templateProperties.remove(matchingProperty);
                templateProperties.add(copy);
                matchingProperty = copy;
            }
            updateMatchingProperty(matchingProperty, nifiProperty, updateMode);
        }
        return matchingProperty;
    }

    /**
     * Perform the update of the matching property, setting the property values to that of the supplied 'nifiProperty'
     *
     * @param matchingProperty a property to updated
     * @param nifiProperty     the property to use to update the matchingProperty
     * @param updateMode       a mode to update
     */
    public static void updateMatchingProperty(NifiProperty matchingProperty, NifiProperty nifiProperty, PROPERTY_MATCH_AND_UPDATE_MODE updateMode) {
        if (updateMode.performUpdate()) {
            if (matchingProperty.getValue() == null || (matchingProperty.getValue() != null && (PROPERTY_MATCH_AND_UPDATE_MODE.UPDATE_ALL_PROPERTIES.equals(updateMode) || PROPERTY_MATCH_AND_UPDATE_MODE.FEED_DETAILS_MATCH_TEMPLATE.equals(updateMode) || (
                PROPERTY_MATCH_AND_UPDATE_MODE.UPDATE_NON_EXPRESSION_PROPERTIES.equals(updateMode) && (
                    (!matchingProperty.getValue().contains("${metadata.")) || (matchingProperty.getValue()
                                                                                   .contains("${metadata.") && nifiProperty.getValue() != null && nifiProperty.getValue()
                                                                                   .contains("${metadata."))))))) {
                updateProperty(matchingProperty, nifiProperty, updateMode);
            }
        }
    }

    /**
     * Check to see if a collection of properties contains properties of a supplied processorType
     *
     * @param properties    a collection of properties
     * @param processorType a processor type to match
     * @return {@code true} if the collection of properties contains the supplied processorType, {@code false} if the properties do not contain the processorType
     */
    public static boolean containsPropertiesForProcessorMatchingType(Collection<NifiProperty> properties, final String processorType) {
        if (StringUtils.isBlank(processorType)) {
            return false;
        }
        return Iterables.tryFind(properties, new Predicate<NifiProperty>() {
            @Override
            public boolean apply(NifiProperty property) {
                return processorType.equalsIgnoreCase(property.getProcessorType());
            }
        }).orNull() != null;
    }

    /**
     * various modes used for updating properties
     */
    public static enum PROPERTY_MATCH_AND_UPDATE_MODE {
        /**
         * this mode will not update any properties
         */
        DONT_UPDATE,
        /**
         * this mode will update all the properties
         */
        UPDATE_ALL_PROPERTIES,
        /**
         * this mode will skip over any properties with the ${metadata. prefix in the value string of the property
         */
        UPDATE_NON_EXPRESSION_PROPERTIES,

        /**
         * update the template properties with the Feed properties
         */
        FEED_DETAILS_MATCH_TEMPLATE;

        /**
         * @return true if the update should happen, false if not
         */
        public boolean performUpdate() {
            return !DONT_UPDATE.equals(this);
        }
    }
}
