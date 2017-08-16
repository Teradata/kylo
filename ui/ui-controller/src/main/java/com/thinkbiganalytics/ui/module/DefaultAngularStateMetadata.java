package com.thinkbiganalytics.ui.module;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.thinkbiganalytics.ui.api.module.AngularStateMetadata;

import java.util.Map;

/**
 * Created by sr186054 on 8/16/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultAngularStateMetadata implements AngularStateMetadata {
    private String state;
    private String url;
    private Map<String,Object> params;

    public DefaultAngularStateMetadata(){

    }

    @Override
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
}
