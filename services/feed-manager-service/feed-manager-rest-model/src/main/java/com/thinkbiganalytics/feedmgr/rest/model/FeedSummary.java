package com.thinkbiganalytics.feedmgr.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;

/**
 * Created by sr186054 on 2/19/16.
 * Lightweight view of Feed Data with just the essential feed information
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeedSummary implements UIFeed {

    private String categoryName;
    private String systemCategoryName;
    private String categoryId;
    private String categoryIcon;
    private String categoryIconColor;
    private String id;
    private String feedId;
    private String feedName;
    private String systemFeedName;
    private boolean active;
    private String state;
    private Date updateDate;
    private String templateName;
    private String templateId;

    public FeedSummary() {

    }
    public FeedSummary(FeedMetadata feedMetadata){
        this.id = feedMetadata.getId();
        this.feedName = feedMetadata.getFeedName();
        this.categoryId = feedMetadata.getCategory().getId();
        this.categoryName = feedMetadata.getCategory().getName();
        this.systemCategoryName = feedMetadata.getCategory().getSystemName();
        this.systemFeedName = feedMetadata.getSystemFeedName();
        this.updateDate = feedMetadata.getUpdateDate();
        this.feedId = feedMetadata.getFeedId();
        this.categoryIcon = feedMetadata.getCategoryIcon();
        this.categoryIconColor = feedMetadata.getCategoryIconColor();
        this.active = feedMetadata.isActive();
        this.state = feedMetadata.getState();
        this.templateId = feedMetadata.getTemplateId();
        this.templateName = feedMetadata.getTemplateName();
    }

    @Override
    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    @Override
    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getFeedName() {
        return feedName;
    }

    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    @Override
    public String getCategoryAndFeedDisplayName(){
        return this.categoryName+"."+this.feedName;
    }

    public String getCategoryAndFeedSystemName(){
        return this.systemCategoryName+"."+this.systemFeedName;
    }


    @Override
    public String getSystemCategoryName() {
        return systemCategoryName;
    }

    public void setSystemCategoryName(String systemCategoryName) {
        this.systemCategoryName = systemCategoryName;
    }

    @Override
    public String getSystemFeedName() {
        return systemFeedName;
    }

    public void setSystemFeedName(String systemFeedName) {
        this.systemFeedName = systemFeedName;
    }

    @Override
    public String getFeedId() {
        return feedId;
    }

    public void setFeedId(String feedId) {
        this.feedId = feedId;
    }

    @Override
    public String getCategoryIcon() {
        return categoryIcon;
    }

    public void setCategoryIcon(String categoryIcon) {
        this.categoryIcon = categoryIcon;
    }

    @Override
    public String getCategoryIconColor() {
        return categoryIconColor;
    }

    public void setCategoryIconColor(String categoryIconColor) {
        this.categoryIconColor = categoryIconColor;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }
}
