package com.thinkbiganalytics.feedmgr.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.nifi.rest.support.NifiProcessUtil;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by sr186054 on 1/26/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisteredTemplate {
    private List<NifiProperty> properties;

    private List<Processor> nonInputProcessors;

    private List<Processor> inputProcessors;

    private String id;

    private String nifiTemplateId;
    private String templateName;
    private Date updateDate;
    private Date createDate;
    private String icon;
    private String iconColor;
    private String description;

    private boolean defineTable;
    @JsonProperty("allowPreconditions")
    private boolean allowPreconditions;
    @JsonProperty("dataTransformation")
    private boolean dataTransformation;

    private boolean reusableTemplate;

    private List<ReusableTemplateConnectionInfo> reusableTemplateConnections;

    public RegisteredTemplate(){

    }

    public RegisteredTemplate(RegisteredTemplate registeredTemplate){
        this.id = registeredTemplate.getId();
        this.nifiTemplateId = registeredTemplate.getNifiTemplateId();
        this.templateName = registeredTemplate.getTemplateName();
        this.defineTable = registeredTemplate.isDefineTable();
        this.updateDate = registeredTemplate.getUpdateDate();
        this.createDate = registeredTemplate.getCreateDate();
        this.allowPreconditions = registeredTemplate.isAllowPreconditions();
        this.dataTransformation = registeredTemplate.isDataTransformation();
        this.icon = registeredTemplate.getIcon();
        this.iconColor = registeredTemplate.getIconColor();
        this.description = registeredTemplate.getDescription();
        //copy properties???
        if(registeredTemplate.getProperties() != null) {
            this.properties = new ArrayList<>(registeredTemplate.getProperties());
        }
        this.reusableTemplate = registeredTemplate.isReusableTemplate();
        if(registeredTemplate.getReusableTemplateConnections() != null) {
            this.reusableTemplateConnections = new ArrayList<>(registeredTemplate.getReusableTemplateConnections());
        }
        this.initializeProcessors();
    }

    public List<NifiProperty> getProperties() {
        return properties;
    }

    public void setProperties(List<NifiProperty> properties) {
        this.properties = properties;
    }

    public String getNifiTemplateId() {
        return nifiTemplateId;
    }

    public void setNifiTemplateId(String nifiTemplateId) {
        this.nifiTemplateId = nifiTemplateId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public boolean isDefineTable() {
        return defineTable;
    }

    public void setDefineTable(boolean defineTable) {
        this.defineTable = defineTable;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public boolean isAllowPreconditions() {
        return allowPreconditions;
    }

    public void setAllowPreconditions(boolean allowPreconditions) {
        this.allowPreconditions = allowPreconditions;
    }

    public boolean isDataTransformation() {
        return dataTransformation;
    }

    public void setDataTransformation(boolean dataTransformation) {
        this.dataTransformation = dataTransformation;
    }


    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getIconColor() {
        return iconColor;
    }

    public void setIconColor(String iconColor) {
        this.iconColor = iconColor;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String getId() {
        return id;
    }

    public void setId(String id){
        this.id = id;
    }


    public boolean isReusableTemplate() {
        return reusableTemplate;
    }

    public void setReusableTemplate(boolean reusableTemplate) {
        this.reusableTemplate = reusableTemplate;
    }

    public List<ReusableTemplateConnectionInfo> getReusableTemplateConnections() {
        return reusableTemplateConnections;
    }

    public void setReusableTemplateConnections(
        List<ReusableTemplateConnectionInfo> reusableTemplateConnections) {
        this.reusableTemplateConnections = reusableTemplateConnections;
    }

    public boolean usesReusableTemplate(){
        return getReusableTemplateConnections() != null && !getReusableTemplateConnections().isEmpty();
    }

    public List<Processor> getNonInputProcessors() {
        return nonInputProcessors;
    }

    public void setNonInputProcessors(List<Processor> nonInputProcessors) {
        this.nonInputProcessors = nonInputProcessors;
    }

    public List<Processor> getInputProcessors() {
        return inputProcessors;
    }

    public void setInputProcessors(List<Processor> inputProcessors) {
        this.inputProcessors = inputProcessors;
    }

    @JsonIgnore
    public static Predicate<NifiProperty> isValidInput(){
        return property -> property.isInputProperty() && !NifiProcessUtil.CLEANUP_TYPE.equalsIgnoreCase(property.getProcessorType());
    }

    @JsonIgnore
    public static Predicate<Processor> isValidInputProcessor(){
        return processor -> !NifiProcessUtil.CLEANUP_TYPE.equalsIgnoreCase(processor.getType());
    }

    @JsonIgnore
    public static Predicate<NifiProperty> isPropertyModifiedFromTemplateValue() {
        return property -> (property.isSelected() && ((property.getValue() == null && property.getTemplateValue() != null)
                                                      || (property.getValue() != null && !property.getValue().equalsIgnoreCase(property.getTemplateValue()))
        ));

    }

    public List<NifiProperty> findModifiedDefaultProperties() {
        if (properties != null) {
            return getProperties().stream().filter(isPropertyModifiedFromTemplateValue()).collect(Collectors.toList());
        } else {
            return null;
        }
    }

    @JsonIgnore
    public void initializeNonInputProcessors(){
        Map<String, Processor> processorMap = new HashMap<>();

        properties.stream().filter(property -> !property.isInputProperty()).forEach(property -> {
            processorMap.computeIfAbsent(property.getProcessorId(), processorId -> new Processor(processorId)).addProperty(property);
        });
        nonInputProcessors = Lists.newArrayList(processorMap.values());
    }

    @JsonIgnore
    public void initializeInputProcessors(){
        Map<String, Processor> processorMap = new HashMap<>();

        properties.stream().filter(isValidInput()).forEach(property -> {
            processorMap.computeIfAbsent(property.getProcessorId(), processorId -> new Processor(property.getProcessorId())).addProperty(property);
        });
        inputProcessors = Lists.newArrayList(processorMap.values());
    }

    public void initializeProcessors(){
        Map<String, Processor> processorMap = new HashMap<>();
        Map<String, Processor> inputProcessorMap = new HashMap<>();
        Map<String, Processor> nonInputProcessorMap = new HashMap<>();

        properties.stream().forEach(property -> {
            processorMap.computeIfAbsent(property.getProcessorId(), processorId -> new Processor(property.getProcessorId())).addProperty(property);
            if(property.isInputProperty()){
                //dont allow the cleanup processor as a valid input selection
                if(property.isInputProperty() && !NifiProcessUtil.CLEANUP_TYPE.equalsIgnoreCase(property.getProcessorType())) {
                    inputProcessorMap.computeIfAbsent(property.getProcessorId(), processorId -> processorMap.get(property.getProcessorId()));
                }
            }
            else{
                nonInputProcessorMap.computeIfAbsent(property.getProcessorId(), processorId -> processorMap.get(property.getProcessorId()));
            }

        });

        inputProcessors = Lists.newArrayList(inputProcessorMap.values());
        nonInputProcessors = Lists.newArrayList(nonInputProcessorMap.values());

    }




    public static class Processor {
        private String type;
        private String name;
        private String id;
        private String groupName;
        private String groupId;
        private boolean inputProcessor;
        private boolean userDefinedInputProcessor;

        List<NifiProperty> properties;



        public Processor(){

        }

        public Processor(String processorId) {
            this.id = processorId;
        }


        private void setProcessorData(NifiProperty property){
            if(StringUtils.isBlank(name)){
                name = property.getProcessorName();
            }
            if(StringUtils.isBlank(groupName)){
                groupName = property.getProcessGroupName();
            }
            if(StringUtils.isBlank(groupId)){
                groupId = property.getProcessGroupId();
            }
            if(StringUtils.isBlank(type)) {
                type = property.getProcessorType();
            }
            if(property.isInputProperty()){
                inputProcessor =true;
            }
        }


        public void addProperty(NifiProperty property){
            getProperties().add(property);
            setProcessorData(property);
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getGroupName() {
            return groupName;
        }

        public void setGroupName(String groupName) {
            this.groupName = groupName;
        }

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public boolean isInputProcessor() {
            return inputProcessor;
        }

        public void setInputProcessor(boolean inputProcessor) {
            this.inputProcessor = inputProcessor;
        }

        public boolean isUserDefinedInputProcessor() {
            return userDefinedInputProcessor;
        }

        public void setUserDefinedInputProcessor(boolean userDefinedInputProcessor) {
            this.userDefinedInputProcessor = userDefinedInputProcessor;
        }

        public List<NifiProperty> getProperties() {
            if(properties == null){
                properties = new ArrayList<>();
            }
            return properties;
        }

        public void setProperties(List<NifiProperty> properties) {
            this.properties = properties;
        }
    }


}
