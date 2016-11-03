package com.thinkbiganalytics.servicemonitor.rest.client.ambari;

import com.thinkbiganalytics.servicemonitor.rest.model.ambari.AlertSummary;
import com.thinkbiganalytics.servicemonitor.rest.model.ambari.ServiceComponentInfoSummary;

import org.springframework.web.client.RestClientException;

import java.util.List;

/**
 * Created by sr186054 on 11/3/16.
 */
public interface AmbariClient {

    List<String> getAmbariClusterNames();

    ServiceComponentInfoSummary getServiceComponentInfo(List<String> clusterNames, String services)
        throws RestClientException;

    AlertSummary getAlerts(List<String> clusterNames, String services) throws RestClientException;
}
