package com.thinkbiganalytics.servicemonitor.rest.client;

import org.springframework.core.GenericTypeResolver;

import java.util.Map;

/**
 * Created by sr186054 on 10/1/15.
 */
public abstract class RestCommand<T> {

    private String url;
    private Map<String,Object> parameters;

    public abstract String payload();

    private Class<T> responseType;

    public RestCommand(){
        this.responseType = (Class<T>) GenericTypeResolver.resolveTypeArgument(getClass(), RestCommand.class);
    }
    public Class<T> getResponseType() {
        return this.responseType;
    }




    public String getUrl() {
        return url;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }
}
