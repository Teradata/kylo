package com.thinkbiganalytics.feedmgr.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by sr186054 on 1/26/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisteredTemplate {
    private List<NifiProperty> properties;

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
}
