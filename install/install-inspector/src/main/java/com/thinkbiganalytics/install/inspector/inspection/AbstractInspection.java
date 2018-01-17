package com.thinkbiganalytics.install.inspector.inspection;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class AbstractInspection implements Inspection {

    private final InspectionStatus status;
    private int id;

    AbstractInspection() {
        this(InspectionStatus.INITIAL);
    }

    AbstractInspection(InspectionStatus status) {
        this.status = status;
    }

    @Override
    public InspectionStatus getStatus() {
        return status;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public InspectionStatus inspect(Object properties) {
        throw new IllegalStateException("Not implemented yet");
    }

    @Override
    @JsonIgnore
    public Object getProperties() {
        return null;
    }
}
