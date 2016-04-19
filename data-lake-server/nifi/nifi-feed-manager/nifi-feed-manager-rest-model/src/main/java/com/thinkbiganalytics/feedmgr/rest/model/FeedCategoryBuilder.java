package com.thinkbiganalytics.feedmgr.rest.model;

/**
 * Created by sr186054 on 2/10/16.
 */
public class FeedCategoryBuilder {

    private String name;
    private String description;
    private String icon;
    private String iconColor;
    private int relatedFeeds;


    public FeedCategoryBuilder description(String description) {
        this.description = description;
        return this;
    }

    public FeedCategoryBuilder icon(String icon) {
        this.icon = icon;
        return this;
    }

    public FeedCategoryBuilder iconColor(String iconColor) {
        this.iconColor = iconColor;
        return this;
    }

    public FeedCategoryBuilder relatedFeeds(int relatedFeeds){
        this.relatedFeeds = relatedFeeds;
        return this;
    }

    public FeedCategoryBuilder(String name){
        this.name = name;
        this.iconColor = "black";
    }

    public FeedCategory build(){
        FeedCategory category = new FeedCategory();
        category.setName(this.name);
        category.setDescription(this.description);
        category.setIconColor(this.iconColor);
        category.setIcon(this.icon);

        category.generateSystemName();
        return category;
    }


}

