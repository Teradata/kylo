package com.thinkbiganalytics.metadata.migration.template;

import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeed;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplate;

import org.joda.time.DateTime;

import java.util.List;

/**
 * Created by sr186054 on 6/15/16.
 */
public class FeedManagerTemplateDTO implements FeedManagerTemplate {

    private ID id;

    private String name;

    private String nifiTemplateId;

    private String description;

    private boolean defineTable;

    private boolean dataTransformation;

    private boolean allowPreconditions;

    private String icon;

    private String iconColor;

    private String state;

    private String json;

    private Integer version;

    private DateTime createdTime;

    private DateTime modifiedTime;

    @Override
    public ID getId() {
        return id;
    }

    public void setId(ID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNifiTemplateId() {
        return nifiTemplateId;
    }

    public void setNifiTemplateId(String nifiTemplateId) {
        this.nifiTemplateId = nifiTemplateId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isDefineTable() {
        return defineTable;
    }

    public void setDefineTable(boolean defineTable) {
        this.defineTable = defineTable;
    }

    public boolean isDataTransformation() {
        return dataTransformation;
    }

    public void setDataTransformation(boolean dataTransformation) {
        this.dataTransformation = dataTransformation;
    }

    public boolean isAllowPreconditions() {
        return allowPreconditions;
    }

    public void setAllowPreconditions(boolean allowPreconditions) {
        this.allowPreconditions = allowPreconditions;
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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public DateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(DateTime createdTime) {
        this.createdTime = createdTime;
    }

    public DateTime getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(DateTime modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    @Override
    public List<FeedManagerFeed> getFeeds() {
        return null;
    }
}
