package com.thinkbiganalytics.install.inspector.inspection;

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
    public InspectionStatus execute(Configuration configuration) {
        throw new IllegalStateException("Not implemented yet");
    }
}
