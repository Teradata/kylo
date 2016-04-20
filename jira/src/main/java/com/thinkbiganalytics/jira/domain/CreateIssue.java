package com.thinkbiganalytics.jira.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import javax.annotation.Generated;

/**
 * Created by sr186054 on 10/16/15.
 */@JsonInclude(JsonInclude.Include.NON_NULL)
   @Generated("org.jsonschema2pojo")
   @JsonPropertyOrder({
           "fields"
   })
   @JsonIgnoreProperties(ignoreUnknown = true)
public class CreateIssue {
    @JsonProperty("fields")
    private Fields fields;


    public CreateIssue(Issue issue){
        this.fields = new Fields();
        fields.setSummary(issue.getSummary());
        fields.setIssueType(issue.getIssueType());
        fields.setProject(issue.getProject());
        fields.setDescription(issue.getDescription());
        fields.setAssignee(issue.getAssignee());
    }

    /**
     *
     * @return
     *     The fields
     */
    @JsonProperty("fields")
    public Fields getFields() {
        return fields;
    }

    /**
     *
     * @param fields
     *     The fields
     */
    @JsonProperty("fields")
    public void setFields(Fields fields) {
        this.fields = fields;
    }

}
