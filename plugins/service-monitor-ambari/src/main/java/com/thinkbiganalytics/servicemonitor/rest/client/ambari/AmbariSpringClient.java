package com.thinkbiganalytics.servicemonitor.rest.client.ambari;

import com.thinkbiganalytics.servicemonitor.rest.client.RestClient;
import com.thinkbiganalytics.servicemonitor.rest.client.RestClientConfig;
import com.thinkbiganalytics.servicemonitor.rest.client.TextPlainJackson2HttpMessageConverter;
import com.thinkbiganalytics.servicemonitor.rest.model.ambari.AlertSummary;
import com.thinkbiganalytics.servicemonitor.rest.model.ambari.Cluster;
import com.thinkbiganalytics.servicemonitor.rest.model.ambari.ClusterItem;
import com.thinkbiganalytics.servicemonitor.rest.model.ambari.ClusterList;
import com.thinkbiganalytics.servicemonitor.rest.model.ambari.ServiceComponentInfoSummary;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Please use the AmbariJerseyClient
 */
@Deprecated
public class AmbariSpringClient extends RestClient implements AmbariClient {


    @Autowired
    @Qualifier("ambariRestClientConfig")
    private RestClientConfig clientConfig;


    public AmbariSpringClient() {
        HttpMessageConverter c = new TextPlainJackson2HttpMessageConverter();
        setAdditionalMessageConverters(Arrays.asList(c));
    }

    public RestClientConfig getConfig() {
        return clientConfig;
    }

    @Override
    public List<String> getAmbariClusterNames() {
        List<String> clusterNames = new ArrayList<>();
        ClusterList clusterList = super.doGet(new AmbariGetClustersCommand());
        if (clusterList != null) {
            List<ClusterItem> items = clusterList.getItems();
            if (items != null) {
                for (ClusterItem item : items) {
                    Cluster cluster = item.getCluster();
                    if (cluster != null) {
                        String clusterName = cluster.getClusterName();
                        clusterNames.add(clusterName);
                    }
                }
            }
        }
        return clusterNames;

    }


    @Override
    public ServiceComponentInfoSummary getServiceComponentInfo(List<String> clusterNames, String services)
        throws RestClientException {
        ServiceComponentInfoSummary summary = null;
        for (String clusterName : clusterNames) {
            AmbariServicesComponentInfoCommand
                servicesComponentInfoCommand =
                new AmbariServicesComponentInfoCommand(clusterName, services);
            ServiceComponentInfoSummary clusterSummary = super.doGet(servicesComponentInfoCommand);
            if (clusterSummary != null) {
                if (summary == null) {
                    summary = clusterSummary;
                } else {
                    summary.getItems().addAll(clusterSummary.getItems());
                }
            }
        }
        return summary;

    }

    @Override
    public AlertSummary getAlerts(List<String> clusterNames, String services) throws RestClientException {
        AlertSummary alerts = null;
        for (String clusterName : clusterNames) {
            AmbariAlertsCommand alertsCommand = new AmbariAlertsCommand(clusterName, services);
            AlertSummary alertSummary = super.doGet(alertsCommand);
            if (alerts == null) {
                alerts = alertSummary;
            } else {
                alerts.getItems().addAll(alertSummary.getItems());
            }
        }
        return alerts;
    }

    public void setClientConfig(RestClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }
}
