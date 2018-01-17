package com.thinkbiganalytics.install.inspector.inspection;

public class Path {

    private String uri;
    private Boolean devMode;

    public String getUri() {
        return this.uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Boolean isDevMode() {
        return devMode;
    }

    public void setDevMode(Boolean devMode) {
        this.devMode = devMode;
    }
}
