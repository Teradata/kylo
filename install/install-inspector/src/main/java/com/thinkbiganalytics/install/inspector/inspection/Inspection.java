package com.thinkbiganalytics.install.inspector.inspection;

public interface Inspection<SP, UP> {

    int getId();

    void setId(int id);

    String getName();

    String getDescription();

    InspectionStatus inspect(SP servicesProperties, UP uiProperties);

    SP getServicesProperties();

    UP getUiProperties();
}
