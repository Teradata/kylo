package com.thinkbiganalytics.rest.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sr186054 on 9/22/15.
 */
public class RestResponseStatus {
    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_ERROR = "error";

    public String status;
    public String message;
    public boolean handledException; // mark true if you dont want the AngularHttpInterceptor to take care of this automatically
    private String url;
    private Map<String,String> properties = new HashMap<>();

    public RestResponseStatus() {


    }

    public RestResponseStatus(ResponseStatusBuilder builder){
        this.status = builder.status;
        this.message = builder.message;
        this.url = builder.url;
        this.properties = builder.properties;
        this.handledException = builder.handledException;
    }

    public static class ResponseStatusBuilder{
        private String status;
        private String message;
        private String url;
        public boolean handledException;
        private Map<String,String> properties = new HashMap<>();

        public ResponseStatusBuilder(){

        }
        public ResponseStatusBuilder message(String message) {
            this.message = message;
            return this;
        }

        public ResponseStatusBuilder properties(Map<String,String> properties){
            this.properties.putAll(properties);
            return this;
        }

        public ResponseStatusBuilder property(String key, String value) {
            this.properties.put(key,value);
            return this;
        }

        public ResponseStatusBuilder url(String url) {
            this.url = url;
            return this;
        }

        public ResponseStatusBuilder messageFromException(Throwable e) {
            this.message = e.getMessage();
            return this;
        }

        public ResponseStatusBuilder handledException(boolean handledException){
            this.handledException = handledException;
            return this;
        }
        public RestResponseStatus buildSuccess(){
            this.status = RestResponseStatus.STATUS_SUCCESS;
            return new RestResponseStatus(this);
        }
        public RestResponseStatus buildError(){
            this.status = RestResponseStatus.STATUS_ERROR;
            return new RestResponseStatus(this);
        }


    }

    public RestResponseStatus(String status) {
        this.status = status;
    }

    public static RestResponseStatus SUCCESS = new RestResponseStatus.ResponseStatusBuilder().buildSuccess();

    public static RestResponseStatus ERROR = new RestResponseStatus.ResponseStatusBuilder().buildError();

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public boolean isHandledException() {
        return handledException;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, String> getProperties() {
        return properties;
    }
}
