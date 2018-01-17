package com.thinkbiganalytics.install.inspector.inspection;

public class InspectionStatus {

    static final InspectionStatus INITIAL = new InspectionStatus("Initial");
    static final InspectionStatus DISABLED = new InspectionStatus("Disabled");
    static final InspectionStatus VALID = new InspectionStatus("Valid");
    static final InspectionStatus INVALID = new InspectionStatus("Invalid");

    private final String state;

    private InspectionStatus(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }
}
