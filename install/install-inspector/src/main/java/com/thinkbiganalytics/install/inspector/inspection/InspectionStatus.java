package com.thinkbiganalytics.install.inspector.inspection;

public class InspectionStatus {

    private boolean valid;
    private String description;
    private String error;

    InspectionStatus(boolean isValid) {
        this.valid = isValid;
    }

    public boolean isValid() {
        return valid;
    }

    void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }
}
