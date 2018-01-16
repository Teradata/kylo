package com.thinkbiganalytics.install.inspector.check;

public class ConfigStatus {

    static final ConfigStatus INITIAL = new ConfigStatus("Initial");
    static final ConfigStatus DISABLED = new ConfigStatus("Disabled");

    private final String state;

    private ConfigStatus(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }
}
