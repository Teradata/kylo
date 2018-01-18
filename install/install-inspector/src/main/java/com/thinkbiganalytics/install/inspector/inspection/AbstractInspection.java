package com.thinkbiganalytics.install.inspector.inspection;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class AbstractInspection<SP, UP> implements Inspection<SP, UP> {

    private int id;

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public InspectionStatus inspect(SP servicesProperties, UP uiProperties) {
        throw new IllegalStateException("Not implemented yet");
    }

    @Override
    @JsonIgnore
    public SP getServicesProperties() {
        return null;
    }

    @Override
    @JsonIgnore
    public UP getUiProperties() {
        return null;
    }
}
