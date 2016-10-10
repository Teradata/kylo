/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.discovery.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.thinkbiganalytics.policy.rest.model.BaseUiPolicyRule;

@JsonIgnoreProperties(ignoreUnknown = true)
/**
 * Model used to pass the parser properties
 */
public class SchemaParserDescriptor extends BaseUiPolicyRule {

    private boolean supportsBinary;
    private boolean generatesHiveSerde;
    private String[] tags;
    private String clientHelper;

    public boolean isSupportsBinary() {
        return supportsBinary;
    }

    public boolean isGeneratesHiveSerde() {
        return generatesHiveSerde;
    }

    public String[] getTags() {
        return tags;
    }

    public String getClientHelper() {
        return clientHelper;
    }

    public void setSupportsBinary(boolean supportsBinary) {
        this.supportsBinary = supportsBinary;
    }

    public void setGeneratesHiveSerde(boolean generatesHiveSerde) {
        this.generatesHiveSerde = generatesHiveSerde;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public void setClientHelper(String clientHelper) {
        this.clientHelper = clientHelper;
    }
}
