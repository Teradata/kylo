package com.thinkbiganalytics.install.inspector.inspection;

public interface Inspection {

    int getId();

    void setId(int id);

    String getName();

    String getDescription();

    InspectionStatus inspect(Object properties);

    InspectionStatus getStatus();

    Object getProperties();
}
