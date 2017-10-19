package com.thinkbiganalytics.feedmgr.rest.model;

/*-
 * #%L
 * thinkbig-feed-manager-rest-model
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.nifi.rest.support.NifiProcessUtil;
import com.thinkbiganalytics.security.rest.model.EntityAccessControl;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.TemplateDTO;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisteredTemplate extends EntityAccessControl {

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
    private String state;

    private boolean defineTable;
    @JsonProperty("allowPreconditions")
    private boolean allowPreconditions;
    @JsonProperty("dataTransformation")
    private boolean dataTransformation;

    private boolean reusableTemplate;

    private List<ReusableTemplateConnectionInfo> reusableTemplateConnections;

    private List<TemplateProcessorDatasourceDefinition> registeredDatasourceDefinitions;

    private Long order;

    private List<String> templateOrder;

    @JsonProperty("isStream")
    private boolean isStream;

    @JsonIgnore
    private Set<String> feedNames;

    /**
     * The number of feeds that use this template
     */
    private Integer feedsCount;

    @JsonIgnore
    private TemplateDTO nifiTemplate;

    /**
     * flag to indicate the template was updated
     */
    @JsonIgnore
    private boolean updated;

    /**
     * Type of TemplateTableOption used when creating and editing feeds.
     */
    private String templateTableOption;

    /**
     * For Batch Feeds that may start many flowfiles/jobs at once in a short amount of time
     * we don't necessarily want to show all of those as individual jobs in ops manager as they may merge and join into a single ending flow.
     * For a flood of starting jobs if ops manager receives more than 1 starting event within this given interval it will supress the creation of the next Job
     * Set this to -1L or 0L to bypass and always create a job instance per starting flow file.
     */
    private Long timeBetweenStartingBatchJobs = 1000L;

    public RegisteredTemplate() {

    }

    public RegisteredTemplate(RegisteredTemplate registeredTemplate) {
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
        this.state = registeredTemplate.getState();
        //copy properties???
        if (registeredTemplate.getProperties() != null) {
            this.properties = new ArrayList<>(registeredTemplate.getProperties());
        }
        this.reusableTemplate = registeredTemplate.isReusableTemplate();
        if (registeredTemplate.getReusableTemplateConnections() != null) {
            this.reusableTemplateConnections = new ArrayList<>(registeredTemplate.getReusableTemplateConnections());
        }
        this.feedsCount = registeredTemplate.getFeedsCount();
        this.registeredDatasourceDefinitions = registeredTemplate.getRegisteredDatasourceDefinitions();
        this.order = registeredTemplate.getOrder();
        this.isStream = registeredTemplate.isStream();
        this.setOwner(registeredTemplate.getOwner());
        this.setRoleMemberships(registeredTemplate.getRoleMemberships());
        this.setAllowedActions(registeredTemplate.getAllowedActions());
        this.setTemplateTableOption(registeredTemplate.getTemplateTableOption());
        this.setTimeBetweenStartingBatchJobs(registeredTemplate.getTimeBetweenStartingBatchJobs());
        this.initializeProcessors();
    }

    @JsonIgnore
    public static Predicate<NifiProperty> isValidInput() {
        return property -> property.isInputProperty() && !NifiProcessUtil.CLEANUP_TYPE.equalsIgnoreCase(property.getProcessorType());
    }

    @JsonIgnore
    public static Predicate<Processor> isValidInputProcessor() {
        return processor -> !NifiProcessUtil.CLEANUP_TYPE.equalsIgnoreCase(processor.getType());
    }

    @JsonIgnore
    public static Predicate<NifiProperty> isPropertyModifiedFromTemplateValue() {
        return property -> (property.isSelected() && ((property.getValue() == null && property.getTemplateValue() != null)
                                                      || (property.getValue() != null && !property.getValue().equalsIgnoreCase(property.getTemplateValue()))
        ));

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

    public void setId(String id) {
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

    public boolean usesReusableTemplate() {
        return getReusableTemplateConnections() != null && !getReusableTemplateConnections().isEmpty();
    }

    public Integer getFeedsCount() {
        return feedsCount;
    }

    public void setFeedsCount(Integer feedsCount) {
        this.feedsCount = feedsCount;
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

    public List<NifiProperty> findModifiedDefaultProperties() {
        if (properties != null) {
            return getProperties().stream().filter(isPropertyModifiedFromTemplateValue()).collect(Collectors.toList());
        } else {
            return null;
        }
    }

    @JsonIgnore
    public void initializeNonInputProcessors() {
        Map<String, Processor> processorMap = new HashMap<>();

        properties.stream().filter(property -> !property.isInputProperty()).forEach(property -> {
            processorMap.computeIfAbsent(property.getProcessorId(), processorId -> new Processor(processorId)).addProperty(property);
        });
        nonInputProcessors = Lists.newArrayList(processorMap.values());
    }

    @JsonIgnore
    public void initializeInputProcessors() {
        Map<String, Processor> processorMap = new HashMap<>();

        properties.stream().filter(isValidInput()).forEach(property -> {
            processorMap.computeIfAbsent(property.getProcessorId(), processorId -> new Processor(property.getProcessorId())).addProperty(property);
        });
        inputProcessors = Lists.newArrayList(processorMap.values());
    }

    public void initializeProcessors() {
        Map<String, Processor> processorMap = new HashMap<>();
        Map<String, Processor> inputProcessorMap = new HashMap<>();
        Map<String, Processor> nonInputProcessorMap = new HashMap<>();

        properties.stream().forEach(property -> {
            processorMap.computeIfAbsent(property.getProcessorId(), processorId -> new Processor(property.getProcessorId())).addProperty(property);
            if (property.isInputProperty()) {
                //dont allow the cleanup processor as a valid input selection
                if (property.isInputProperty() && !NifiProcessUtil.CLEANUP_TYPE.equalsIgnoreCase(property.getProcessorType())) {
                    inputProcessorMap.computeIfAbsent(property.getProcessorId(), processorId -> processorMap.get(property.getProcessorId()));
                }
                //mark the template as allowing preconditions if it has an input of TriggerFeed
                if(NifiProcessUtil.TRIGGER_FEED_TYPE.equalsIgnoreCase(property.getProcessorType()) && !this.isAllowPreconditions()) {
                    this.setAllowPreconditions(true);
                }
            } else {
                nonInputProcessorMap.computeIfAbsent(property.getProcessorId(), processorId -> processorMap.get(property.getProcessorId()));
            }

        });

        inputProcessors = Lists.newArrayList(inputProcessorMap.values());
        nonInputProcessors = Lists.newArrayList(nonInputProcessorMap.values());

    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public List<String> getTemplateOrder() {
        return templateOrder;
    }

    public void setTemplateOrder(List<String> templateOrder) {
        this.templateOrder = templateOrder;
    }

    public Long getOrder() {
        return order;
    }

    public void setOrder(Long order) {
        this.order = order;
    }

    public List<TemplateProcessorDatasourceDefinition> getRegisteredDatasourceDefinitions() {
        if (registeredDatasourceDefinitions == null) {
            registeredDatasourceDefinitions = new ArrayList<>();
        }
        return registeredDatasourceDefinitions;
    }

    public void setRegisteredDatasourceDefinitions(List<TemplateProcessorDatasourceDefinition> registeredDatasources) {
        this.registeredDatasourceDefinitions = registeredDatasources;
    }

    public TemplateDTO getNifiTemplate() {
        return nifiTemplate;
    }

    public void setNifiTemplate(TemplateDTO nifiTemplate) {
        this.nifiTemplate = nifiTemplate;
    }

    @JsonIgnore
    public Set<String> getFeedNames() {
        return feedNames;
    }

    @JsonIgnore
    public void setFeedNames(Set<String> feedNames) {
        this.feedNames = feedNames;
    }

    public boolean isStream() {
        return isStream;
    }

    public void setStream(boolean isStream) {
        this.isStream = isStream;
    }


    @JsonIgnore
    public List<NifiProperty> getConfigurationProperties(){
        return getProperties().stream().filter(nifiProperty -> nifiProperty.isContainsConfigurationVariables()).collect(Collectors.toList());
    }
    @JsonIgnore
    public List<NifiProperty> getSensitiveProperties(){
        return getProperties().stream().filter(nifiProperty -> nifiProperty.isSensitive()).collect(Collectors.toList());
    }

    public String getTemplateTableOption() {
        return templateTableOption;
    }

    public void setTemplateTableOption(String templateTableOption) {
        this.templateTableOption = templateTableOption;
    }

    public static class FlowProcessor extends RegisteredTemplate.Processor {

        private String flowId;
        private boolean isLeaf;

        public FlowProcessor() {
            super();
        }

        public FlowProcessor(String processorId) {
            super(processorId);
        }

        public String getFlowId() {
            return flowId;
        }

        public void setFlowId(String flowId) {
            this.flowId = flowId;
        }

        public boolean isLeaf() {
            return isLeaf;
        }

        public void setIsLeaf(boolean isLeaf) {
            this.isLeaf = isLeaf;
        }

    }

    public static class Processor {

        List<NifiProperty> properties;
        private String type;
        private String name;
        private String id;
        private String groupName;
        private String groupId;
        private boolean inputProcessor;
        private boolean userDefinedInputProcessor;

        public Processor() {

        }

        public Processor(String processorId) {
            this.id = processorId;
        }


        private void setProcessorData(NifiProperty property) {
            if (StringUtils.isBlank(name)) {
                name = property.getProcessorName();
            }
            if (StringUtils.isBlank(groupName)) {
                groupName = property.getProcessGroupName();
            }
            if (StringUtils.isBlank(groupId)) {
                groupId = property.getProcessGroupId();
            }
            if (StringUtils.isBlank(type)) {
                type = property.getProcessorType();
            }
            if (property.isInputProperty()) {
                inputProcessor = true;
            }
        }


        public void addProperty(NifiProperty property) {
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
            if (properties == null) {
                properties = new ArrayList<>();
            }
            return properties;
        }

        public void setProperties(List<NifiProperty> properties) {
            this.properties = properties;
        }



    }

    @JsonIgnore
    public boolean isUpdated() {
        return updated;
    }
    @JsonIgnore
    public void setUpdated(boolean updated) {
        this.updated = updated;
    }

    public Long getTimeBetweenStartingBatchJobs() {
        return timeBetweenStartingBatchJobs;
    }

    public void setTimeBetweenStartingBatchJobs(Long timeBetweenStartingBatchJobs) {
        this.timeBetweenStartingBatchJobs = timeBetweenStartingBatchJobs;
    }
}
