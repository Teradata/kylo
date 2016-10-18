package com.thinkbiganalytics.datalake.authorization.model;

/**
 * Created by Jeremy Merrifield on 9/12/16.
 */
public class SentryGroup implements HadoopAuthorizationGroup {

    private String id;
    //private DateTime createDate;
    //private DateTime updateDate;
    private String owner;
    private String name;
    private String description;
    private boolean isVisible;

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /*public DateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(DateTime createDate) {
        this.createDate = createDate;
    }

    public DateTime getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(DateTime updateDate) {
        this.updateDate = updateDate;
    }*/

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setIsVisible(boolean isVisible) {
        this.isVisible = isVisible;
    }
}
