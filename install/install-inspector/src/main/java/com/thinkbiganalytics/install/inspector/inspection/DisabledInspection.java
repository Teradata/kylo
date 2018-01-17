package com.thinkbiganalytics.install.inspector.inspection;

public abstract class DisabledInspection extends AbstractInspection {

    public DisabledInspection() {
        super(InspectionStatus.DISABLED);
    }

}
