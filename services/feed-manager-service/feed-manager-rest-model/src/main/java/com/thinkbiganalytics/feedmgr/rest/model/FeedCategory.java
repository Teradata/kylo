package com.thinkbiganalytics.feedmgr.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.CaseFormat;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.thinkbiganalytics.feedmgr.metadata.MetadataField;
import com.thinkbiganalytics.feedmgr.rest.support.SystemNamingService;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sr186054 on 2/7/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeedCategory {

    private String id;
    @MetadataField
    private String name;
    @MetadataField
    private String systemName;
    private String icon;
    private String iconColor;
    private String description;
    @JsonIgnore
    private List<FeedMetadata> feeds;

    private int relatedFeeds;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSystemName() {
        if(systemName == null) {
            generateSystemName();
        }
        return systemName;
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

    public List<FeedMetadata> getFeeds() {
        if(feeds == null){
            feeds = new ArrayList<>();
        }
        return feeds;
    }

    //TODO remove after POC.
    public void setRelatedFeeds(int relatedFeeds) {
        this.relatedFeeds = relatedFeeds;
    }

    public void setFeeds(List<FeedMetadata> feeds) {
        this.feeds = feeds;
    }
    @JsonIgnore
    public void removeRelatedFeed(final FeedMetadata feed){
        FeedMetadata match = Iterables.tryFind(feeds, new Predicate<FeedMetadata>() {
            @Override
            public boolean apply(FeedMetadata metadata) {
                return feed.getFeedName().equalsIgnoreCase(metadata.getFeedName());
            }
        }).orNull();
        if(match != null) {
            getFeeds().remove(match);
        }
    }
    @JsonIgnore
    public void addRelatedFeed(final FeedMetadata feed){
        if(feeds != null) {
            List<FeedMetadata> arr = Lists.newArrayList(feeds);
            FeedMetadata match = Iterables.tryFind(arr, new Predicate<FeedMetadata>() {
                @Override
                public boolean apply(FeedMetadata metadata) {
                    return feed.getFeedName().equalsIgnoreCase(metadata.getFeedName());
                }
            }).orNull();
            if(match != null){
                feeds.remove(match);
            }
        }
        getFeeds().add(feed);
        relatedFeeds = getFeeds().size();

    }

    public int getRelatedFeeds() {
        return relatedFeeds;
    }

    @JsonIgnore
    public void generateSystemName(){

        this.systemName = SystemNamingService.generateSystemName(name);
    }



}
