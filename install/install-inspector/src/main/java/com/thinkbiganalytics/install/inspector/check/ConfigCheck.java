package com.thinkbiganalytics.install.inspector.check;

public interface ConfigCheck {

    int getId();

    void setId(int id);

    String getName();

    String getDescription();

    ConfigStatus execute(Configuration configuration);

    ConfigStatus getStatus();

}
