package com.thinkbiganalytics.servicemonitor;

import com.thinkbiganalytics.servicemonitor.model.ServiceStatusResponse;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Named;
import java.util.List;


@Named
public class DefaultServiceMonitorRepository implements ServiceMonitorRepository {

    @Autowired
    private ServiceMonitorManager serviceStatus;

    public List<ServiceStatusResponse> listServices() {
        return serviceStatus.doServiceCheck();
    }
}
