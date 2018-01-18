package com.thinkbiganalytics.install.inspector.inspection;

public class InspectionStatus {

    private boolean valid;

    InspectionStatus(boolean isValid) {
        this.valid = isValid;
    }

    public boolean isValid() {
        return valid;
    }
}
