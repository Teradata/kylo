package com.thinkbiganalytics.servicemonitor;


import com.thinkbiganalytics.servicemonitor.model.ServiceStatusResponse;

import java.util.List;


public interface ServiceMonitorRepository {
    /**
     * Returns the status of services
     *
     * @return The status of ingestion services
     */
    List<ServiceStatusResponse> listServices();
}
