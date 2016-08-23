package com.thinkbiganalytics.nifi.provenance.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by sr186054 on 8/22/16.
 */
public class IdReferenceFlowFile implements Serializable {

    private boolean rootFlowFile;
    private String feedName;
    private String feedProcessGroupId;

    private String rootFlowFileId;
    private boolean isComplete;

    private String id;

    private Set<String> parentIds;

    private Set<String> childIds;

    public IdReferenceFlowFile(String id) {
        this.id = id;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<String> getParentIds() {
        if (parentIds == null) {
            parentIds = new HashSet<>();
        }
        return parentIds;
    }

    public void setParentIds(Set<String> parentIds) {
        this.parentIds = parentIds;
    }

    public Set<String> getChildIds() {
        if (childIds == null) {
            childIds = new HashSet<>();
        }
        return childIds;
    }

    public void setChildIds(Set<String> childIds) {
        this.childIds = childIds;
    }


    public void addParentId(String id) {
        getParentIds().add(id);
    }

    public void addChildId(String id) {
        getChildIds().add(id);
    }

    public String getFeedName() {
        return feedName;
    }

    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }

    public String getFeedProcessGroupId() {
        return feedProcessGroupId;
    }

    public void setFeedProcessGroupId(String feedProcessGroupId) {
        this.feedProcessGroupId = feedProcessGroupId;
    }

    public boolean isRootFlowFile() {
        return rootFlowFile;
    }

    public void setRootFlowFile(boolean rootFlowFile) {
        this.rootFlowFile = rootFlowFile;
    }

    public String getRootFlowFileId() {
        return rootFlowFileId;
    }

    public void setRootFlowFileId(String rootFlowFileId) {
        this.rootFlowFileId = rootFlowFileId;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setIsComplete(boolean isComplete) {
        this.isComplete = isComplete;
    }
}
