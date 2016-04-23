package com.thinkbiganalytics.nifi.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by sr186054 on 2/2/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NifiError {

    public enum SEVERITY {
        INFO(0),WARN(1),FATAL(2);
        SEVERITY(int level){
            this.level  = level;
        }
        private int level;
    }

    private String message;
    private String category;
    private SEVERITY severity;


    public NifiError(){

    }

    public NifiError(SEVERITY severity, String message) {
        this.severity = SEVERITY.WARN;
        this.message = message;
    }

    public NifiError(String message) {

        this.severity = SEVERITY.WARN;
        this.message = message;
    }

    public NifiError(SEVERITY severity,String message, String category) {
        this.severity = SEVERITY.WARN;
        this.message = message;
        this.category = category;
    }

    @JsonIgnore
    public boolean isFatal(){
        return SEVERITY.FATAL.equals(this.severity);
    }
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public SEVERITY getSeverity() {
        return severity;
    }

    public void setSeverity(SEVERITY severity) {
        this.severity = severity;
    }
}
