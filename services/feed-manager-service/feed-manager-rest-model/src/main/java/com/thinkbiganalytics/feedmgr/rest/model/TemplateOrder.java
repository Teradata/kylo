package com.thinkbiganalytics.feedmgr.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sr186054 on 11/17/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TemplateOrder {

    List<String> templateIds;

    public TemplateOrder() {

    }

    public List<String> getTemplateIds() {
        if (templateIds == null) {
            templateIds = new ArrayList<>();
        }
        return templateIds;
    }

    public void setTemplateIds(List<String> templateIds) {
        this.templateIds = templateIds;
    }
}
