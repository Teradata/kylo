package com.thinkbiganalytics.feedmgr.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.thinkbiganalytics.feedmgr.metadata.MetadataField;

import java.util.List;

/**
 * Created by sr186054 on 5/3/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeedDataTransformation {

    @MetadataField(description = "The Data Transformation Spark Script")
    private String dataTransformScript;

    private List<String> formulas;


    public String getDataTransformScript() {
        return dataTransformScript;
    }

    public void setDataTransformScript(String dataTransformScript) {
        this.dataTransformScript = dataTransformScript;
    }

    public List<String> getFormulas() {
        return formulas;
    }

    public void setFormulas(List<String> formulas) {
        this.formulas = formulas;
    }

}
