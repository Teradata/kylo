package com.thinkbiganalytics.ui.module;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.thinkbiganalytics.ui.api.module.AngularModule;
import com.thinkbiganalytics.ui.api.module.AngularStateMetadata;

import java.util.List;
import java.util.Map;

/**
 * Created by sr186054 on 8/16/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultAngularModule implements AngularModule {

    @JsonSerialize(contentAs = DefaultAngularStateMetadata.class)
    @JsonDeserialize(contentAs = DefaultAngularStateMetadata.class)
    private List<AngularStateMetadata> states;

    private String moduleJsUrl;

    public DefaultAngularModule(){

    }

    public List<AngularStateMetadata> getStates() {
        return states;
    }

    public void setStates(List<AngularStateMetadata> states) {
        this.states = states;
    }


    @Override
    public String getModuleJsUrl() {
        return moduleJsUrl;
    }

    public void setModuleJsUrl(String moduleJsUrl) {
        this.moduleJsUrl = moduleJsUrl;
    }
}
