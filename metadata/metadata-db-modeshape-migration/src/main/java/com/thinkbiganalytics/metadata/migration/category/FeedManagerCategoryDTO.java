package com.thinkbiganalytics.metadata.migration.category;

import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.extension.UserFieldDescriptor;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategory;
import com.thinkbiganalytics.metadata.api.security.HadoopSecurityGroup;
import com.thinkbiganalytics.security.action.AllowedActions;

import org.joda.time.DateTime;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * A POJO for migrating categories to a ModeShape repository.
 */
public class FeedManagerCategoryDTO implements FeedManagerCategory {

    private Category.ID id;

    private String icon;

    private String iconColor;

    private String displayName;

    private String name;

    private String description;

    private Integer version;

    private DateTime createdTime;

    private DateTime modifiedTime;

    public ID getId() {
        return id;
    }

    public void setId(ID id) {
        this.id = id;
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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
    public List<? extends Feed> getFeeds() {
        return null;
    }

    @Nonnull
    @Override
    public Map<String, String> getUserProperties() {
        return Collections.emptyMap();
    }

    @Override
    public void setUserProperties(@Nonnull Map<String, String> userProperties, @Nonnull Set<UserFieldDescriptor> userFields) {}

    @Override
    public List<? extends HadoopSecurityGroup> getSecurityGroups() {
        return null;
    }

    @Override
    public void setSecurityGroups(List<? extends HadoopSecurityGroup> securityGroups) {

    }

    @Override
    public AllowedActions getAllowedActions() {
        // TODO Auto-generated method stub
        return null;
    }
}
