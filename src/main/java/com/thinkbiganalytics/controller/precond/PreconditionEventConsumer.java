package com.thinkbiganalytics.controller.precond;

public interface PreconditionEventConsumer {

    void addListener(String datasourceName, PreconditionListener listener);
    
    void removeListener(PreconditionListener listener);
}
