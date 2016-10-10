/*
 * Copyright (c) 2016.
 */

package com.thinkbiganalytics.nifi.rest.support;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.ControllerServiceDTO;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.apache.nifi.web.api.dto.TemplateDTO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by sr186054 on 1/11/16.
 */
public class NifiPropertyUtil {

    public static enum PROPERTY_MATCH_AND_UPDATE_MODE{
        DONT_UPDATE,UPDATE_ALL_PROPERTIES, UPDATE_NON_EXPRESSION_PROPERTIES;

        public boolean performUpdate() {
            return !DONT_UPDATE.equals(this);
        }
    }

    public static Map<String,NifiProperty> propertiesAsMap(List<NifiProperty> propertyList){
        Map<String,NifiProperty> map = new HashMap<String, NifiProperty>();
        for(NifiProperty property: propertyList){
            map.put(property.getKey(),property);
        }
        return map;
    }

    public static List<NifiProperty> getPropertiesForTemplate(ProcessGroupDTO parentProcessGroup, TemplateDTO dto){
        return getPropertiesForTemplate(parentProcessGroup,dto,false);
    }

    public static List<NifiProperty> getPropertiesForTemplate(ProcessGroupDTO parentProcessGroup, TemplateDTO dto, boolean excludeInputProcessors){
        List<NifiProperty> properties = new ArrayList<NifiProperty>();
        if(dto != null){
             List<ProcessorDTO> inputs = NifiTemplateUtil.getInputProcessorsForTemplate(dto);
             Set<ProcessorDTO> processorDTOSet = NifiProcessUtil.getProcessors(dto,excludeInputProcessors);
             Map<String,ProcessGroupDTO> groupMap = NifiProcessUtil.getProcessGroupsMap(dto);

            for(ProcessorDTO processor : processorDTOSet) {
                ProcessGroupDTO group = groupMap.get(processor.getParentGroupId());
                if(group == null) {
                    group = parentProcessGroup;
                }
                List<NifiProperty> propertyList = getPropertiesForProcessor(group, processor);
                //assign the property as an input property if it is one
                if(NifiProcessUtil.findFirstProcessorsById(inputs,processor.getId()) != null) {
                    for(NifiProperty property: propertyList){
                        property.setInputProperty(true);
                    }
                }
                properties.addAll(propertyList);
            }
        }
        return properties;
    }

    public static NifiProperty getProperty(final String processorName, final String propertyName, List<NifiProperty> properties){
       NifiProperty property = Iterables.tryFind(properties, new Predicate<NifiProperty>() {
            @Override
            public boolean apply(NifiProperty property) {
                return property.getProcessorName().equalsIgnoreCase(processorName) && property.getKey().equalsIgnoreCase(propertyName);
            }
        }).orNull();
        return property;
    }

    public static List<NifiProperty> copyProperties(List<NifiProperty>properties){
        List<NifiProperty> copyList = new ArrayList<>();
        for(NifiProperty property: properties){
            copyList.add(new NifiProperty(property));
        }
        return copyList;
    }


    public static List<NifiProperty> getPropertiesForService(ControllerServiceDTO service) {
        return service.getProperties().entrySet().stream()
                .map(entry -> new NifiProperty(service.getParentGroupId(), service.getId(), entry.getKey(),
                        entry.getValue(), service.getDescriptors().get(entry.getKey())))
                .collect(Collectors.toList());
    }

    public static List<NifiProperty> getPropertiesForProcessor(ProcessGroupDTO processGroup, ProcessorDTO processor) {
        List<NifiProperty> properties = new ArrayList<>();
        for(Map.Entry<String,String> entry : processor.getConfig().getProperties().entrySet()) {
            NifiProperty property = new NifiProperty(processor.getParentGroupId(),processor.getId(),entry.getKey(),entry.getValue(),processor.getConfig().getDescriptors().get(entry.getKey()));
           // property.setProcessor(processor);
            property.setProcessGroupName(processGroup.getName());
            property.setProcessorName(processor.getName());
            property.setProcessorType(processor.getType());
            properties.add(property);
        }
        return properties;
    }

    public static List<NifiProperty> getProperties(ProcessGroupDTO processGroupDTO) {
        List<NifiProperty> properties = new ArrayList<NifiProperty>();
        if (processGroupDTO != null) {

            if (processGroupDTO.getContents().getProcessors() != null && !processGroupDTO.getContents().getProcessors().isEmpty()) {
                for (ProcessorDTO processorDTO : processGroupDTO.getContents().getProcessors()) {
                    properties.addAll(NifiPropertyUtil.getPropertiesForProcessor(processGroupDTO, processorDTO));
                }
            }

            if (processGroupDTO.getContents().getProcessGroups() != null && !processGroupDTO.getContents().getProcessGroups().isEmpty()) {
                for (ProcessGroupDTO groupDTO : processGroupDTO.getContents().getProcessGroups()) {
                    properties.addAll(NifiPropertyUtil.getProperties(groupDTO));
                }
            }
        }
        return properties;
    }

    public static  Map<String,Map<String,List<NifiProperty>>>  groupPropertiesByProcessGroupAndProcessor(List<NifiProperty> properties){
        Map<String,Map<String,List<NifiProperty>>> processGroupProperties = new HashMap();
        for(NifiProperty property : properties){
            String processGroup = property.getProcessGroupId();
            String processorId = property.getProcessorId();

            if(!processGroupProperties.containsKey(processGroup)){
                processGroupProperties.put(processGroup,new HashMap<String, List<NifiProperty>>());
            }
            if(!processGroupProperties.get(processGroup).containsKey(processorId)){
                processGroupProperties.get(processGroup).put(processorId,new ArrayList<NifiProperty>());
            }

            processGroupProperties.get(processGroup).get(processorId).add(property);
        }
        return processGroupProperties;
    }



    public static List<NifiProperty> getPropertiesForProcessor(List<NifiProperty> properties, final String processorId){
        return Lists.newArrayList(Iterables.filter(properties, new Predicate<NifiProperty>() {
            @Override
            public boolean apply(NifiProperty property) {
                return property.getProcessorId().equalsIgnoreCase(processorId);
            }
        }));
    }

    /**
     * Attempts to match incoming properties against the activeProperties using the ProcessGroup Id/Name and Processor Id/Name along with the Property Key
     * If a match is found sets the activeProperty value
     * @param activeProperties
     * @param properties
     * @return
     */
    public static List<NifiProperty> matchAndSetPropertyValues(String rootProcesGroupName, String activeProcessGroupName,List<NifiProperty> activeProperties, List<NifiProperty> properties ) {

        //try to match against processgroup and processor
        //if not then match against processgroup name and processor name
        Map<String,NifiProperty> idMap = new HashMap<>();

        Map<String,NifiProperty> nameMap = new HashMap<>();


        Map<String,String> processGroupIdMap = new HashMap<>();

        Map<String,String> processorIdMap = new HashMap<>();

        for(NifiProperty property : activeProperties){
            idMap.put(property.getIdKey(),property);
            nameMap.put(property.getNameKey(),property);
            processGroupIdMap.put(property.getProcessGroupName(),property.getProcessGroupId());
            processorIdMap.put(property.getProcessorName(),property.getProcessorId());
        }
        List<NifiProperty> modifiedProperties = new ArrayList<>();
        //copy the properites to modify them

if(properties != null) {
    for (NifiProperty property : properties) {
        //copy off the template property and save it so its snapshotted
        NifiProperty templateProperty = new NifiProperty(property);
        property.setTemplateProperty(templateProperty);

        //if this properties processGroup == the root template, change it so it matches this new processgroup
        if (property.getProcessGroupName().equalsIgnoreCase(rootProcesGroupName)) {
            property.setProcessGroupName(activeProcessGroupName);
        }

        NifiProperty propertyToUpdate = idMap.get(property.getIdKey());
        if (propertyToUpdate == null) {
            propertyToUpdate = nameMap.get(property.getNameKey());
        }
        if (propertyToUpdate != null) {
            property.setProcessorId(processorIdMap.get(propertyToUpdate.getProcessorName()));
            property.setProcessGroupId(processGroupIdMap.get(propertyToUpdate.getProcessGroupName()));
            propertyToUpdate.setValue(property.getValue());
            modifiedProperties.add(propertyToUpdate);
        }

    }
}
        return modifiedProperties;

    }
    public static List<NifiProperty> matchAndSetPropertyByIdKey(Collection<NifiProperty> templateProperties, List<NifiProperty> nifiProperties,PROPERTY_MATCH_AND_UPDATE_MODE updateMode){
        List<NifiProperty> matchedProperties = new ArrayList<>();

        if(nifiProperties != null && !nifiProperties.isEmpty()) {
            for (NifiProperty nifiProperty : nifiProperties) {
            NifiProperty matched =    matchPropertyByIdKey(templateProperties, nifiProperty, updateMode);
                if(matched != null) {
                    matchedProperties.add(matched);
                }
            }
        }
        return matchedProperties;
    }

    public static List<NifiProperty> matchAndSetPropertyByProcessorName(Collection<NifiProperty> templateProperties, List<NifiProperty> nifiProperties, PROPERTY_MATCH_AND_UPDATE_MODE updateMode) {
        List<NifiProperty> matchedProperties = new ArrayList<>();
        if(nifiProperties != null && !nifiProperties.isEmpty()) {
            for (NifiProperty nifiProperty : nifiProperties) {
                NifiProperty matched = matchPropertyByProcessorName(templateProperties, nifiProperty, updateMode);
                if (matched != null) {
                    matchedProperties.add(matched);
                }
            }
        }
        return matchedProperties;
    }

    public static void matchAndSetTemplatePropertiesWithSavedProperties(Collection<NifiProperty> templateProperties, List<NifiProperty> savedProperties){
        if(savedProperties != null && !savedProperties.isEmpty()) {

           final Map<String,NifiProperty> map = new HashMap<>();
            for(NifiProperty p: savedProperties) {
                if((p.getTemplateProperty() != null)) {
                    map.put(p.getTemplateProperty().getIdKey(),p);
                }
            }

            for(NifiProperty property: templateProperties){
                if(map.containsKey(property.getIdKey())){

                    updateProperty(property,map.get(property.getIdKey()));
                }
            }
        }
    }

    public static NifiProperty findPropertyByProcessorType(Collection<NifiProperty> properties,final String processorType, final String propertyKey){
        List<NifiProperty> matchingProperties = Lists.newArrayList(Iterables.filter(properties, new Predicate<NifiProperty>() {
            @Override
            public boolean apply(NifiProperty property) {
                return property.getKey().equalsIgnoreCase(propertyKey) && property.getProcessorType().equalsIgnoreCase(processorType);
            }
        }));
        if(matchingProperties !=null && !matchingProperties.isEmpty()){
            return matchingProperties.get(0);
        }
        return null;
    }

    public static NifiProperty findPropertyByProcessorName(Collection<NifiProperty> properties,final String processorName, final String propertyKey){
        List<NifiProperty> matchingProperties = Lists.newArrayList(Iterables.filter(properties, new Predicate<NifiProperty>() {
            @Override
            public boolean apply(NifiProperty property) {
                return property.getKey().equalsIgnoreCase(propertyKey) && property.getProcessorName().equalsIgnoreCase(processorName);
            }
        }));
        if(matchingProperties !=null && !matchingProperties.isEmpty()){
            return matchingProperties.get(0);
        }
        return null;
    }

    private static void updateProperty(NifiProperty propertyToUpdate, NifiProperty nifiProperty){
        propertyToUpdate.setValue(nifiProperty.getValue());
        propertyToUpdate.setInputProperty(nifiProperty.isInputProperty());
        propertyToUpdate.setUserEditable(nifiProperty.isUserEditable());
        propertyToUpdate.setSelected(nifiProperty.isSelected());
        propertyToUpdate.setRenderType(nifiProperty.getRenderType());
    }

    public static NifiProperty findPropertyByIdKey(Collection<NifiProperty> properties,final NifiProperty nifiProperty){
        List<NifiProperty> matchingProperties = Lists.newArrayList(Iterables.filter(properties, new Predicate<NifiProperty>() {
            @Override
            public boolean apply(NifiProperty property) {
                return property.matchesIdKey(nifiProperty);
            }
        }));
        if(matchingProperties !=null && !matchingProperties.isEmpty()){
            return matchingProperties.get(0);
        }
        return null;
    }

    public static List<NifiProperty> findInputProperties(Collection<NifiProperty> properties){
      return Lists.newArrayList(Iterables.filter(properties, new Predicate<NifiProperty>() {
            @Override
            public boolean apply(NifiProperty property) {
                return property.isInputProperty();
            }
        }));

    }

    public static List<NifiProperty> findInputPropertyMatchingType(Collection<NifiProperty> properties, final String processorType){

        return Lists.newArrayList(Iterables.filter(properties, new Predicate<NifiProperty>() {
            @Override
            public boolean apply(NifiProperty property) {
                return property.isInputProperty() && property.getProcessorType().equalsIgnoreCase(processorType);
            }
        }));
    }

    public static NifiProperty findFirstPropertyMatchingKey(Collection<NifiProperty> properties, final String propertyKey){
        if(properties != null) {
            return Iterables.tryFind(properties, new Predicate<NifiProperty>() {
                @Override
                public boolean apply(NifiProperty property) {
                    return property.getKey().equalsIgnoreCase(propertyKey);
                }
            }).orNull();
        }
        else {
            return null;
        }
    }

    public static List<NifiProperty> findProperties(Collection<NifiProperty> list1,Predicate<NifiProperty> predicate){
     return  Lists.newArrayList(Iterables.filter(list1, predicate));
    }


    public static NifiProperty findPropertyByProcessorName(Collection<NifiProperty> properties,final NifiProperty nifiProperty){
        List<NifiProperty> matchingProperties = Lists.newArrayList(Iterables.filter(properties, new Predicate<NifiProperty>() {
            @Override
            public boolean apply(NifiProperty property) {
                return property.getKey().equalsIgnoreCase(nifiProperty.getKey()) && property.getProcessorName().equalsIgnoreCase(nifiProperty.getProcessorName());
            }
        }));
        if(matchingProperties !=null && !matchingProperties.isEmpty()){
            return matchingProperties.get(0);
        }
        return null;
    }

    private static NifiProperty matchPropertyByIdKey(Collection<NifiProperty> templateProperties, final NifiProperty nifiProperty, PROPERTY_MATCH_AND_UPDATE_MODE updateMode){
        NifiProperty matchingProperty = findPropertyByIdKey(templateProperties, nifiProperty);
        if(matchingProperty != null){
               updateMatchingProperty(matchingProperty, nifiProperty,updateMode);
        }
        return matchingProperty;
    }

    private static NifiProperty matchPropertyByProcessorName(Collection<NifiProperty> templateProperties, final NifiProperty nifiProperty, PROPERTY_MATCH_AND_UPDATE_MODE updateMode){
        NifiProperty matchingProperty = findPropertyByProcessorName(templateProperties, nifiProperty);
        if(matchingProperty != null){
            updateMatchingProperty(matchingProperty, nifiProperty,updateMode);
        }
        return matchingProperty;
    }

    private static void updateMatchingProperty(NifiProperty matchingProperty, NifiProperty nifiProperty, PROPERTY_MATCH_AND_UPDATE_MODE updateMode){
        if(updateMode.performUpdate()) {
            if (matchingProperty.getValue() == null || (matchingProperty.getValue() != null && (PROPERTY_MATCH_AND_UPDATE_MODE.UPDATE_ALL_PROPERTIES.equals(updateMode) || (PROPERTY_MATCH_AND_UPDATE_MODE.UPDATE_NON_EXPRESSION_PROPERTIES.equals(updateMode) && (
                !matchingProperty.getValue().contains("${metadata.") || (matchingProperty.getValue().contains("${metadata.") && nifiProperty.getValue().contains("${metadata."))))))) {
                updateProperty(matchingProperty, nifiProperty);
            }
        }
    }

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
}
