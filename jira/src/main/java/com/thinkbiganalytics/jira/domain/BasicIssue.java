package com.thinkbiganalytics.jira.domain;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by sr186054 on 10/16/15.
 */
public class BasicIssue {
    private  URI self;
    private  String key;
    private  Long id;

    public BasicIssue(GetIssue getIssue) {
        try {
            this.self = new URI(getIssue.getSelf());
            this.key = getIssue.getKey();
            this.id = new Long(getIssue.getId());
        }catch( URISyntaxException e){
            e.printStackTrace();
        }
    }

    public BasicIssue(URI self, String key, Long id) {
        this.self = self;
        this.key = key;
        this.id = id;
    }

    public BasicIssue(){

    }


    public URI getSelf() {
        return self;
    }

    /**
     * @return issue key
     */
    public String getKey() {
        return key;
    }

    public Long getId() {
        return id;
    }


    public String toString() {
        return getToStringHelper().toString();
    }

    protected MoreObjects.ToStringHelper getToStringHelper() {
        return MoreObjects.toStringHelper(this).
                add("self", self).
                add("key", key).
                add("id", id);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BasicIssue) {
            BasicIssue that = (BasicIssue) obj;
            return Objects.equal(this.self, that.self)
                    && Objects.equal(this.key, that.key)
                    && Objects.equal(this.id, that.id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(self, key, id);
    }

}