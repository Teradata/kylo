package com.thinkbiganalytics.servicemonitor.rest.client.ambari;

import com.thinkbiganalytics.rest.JerseyRestClient;
import com.thinkbiganalytics.servicemonitor.rest.client.RestCommand;
import com.thinkbiganalytics.servicemonitor.rest.model.ambari.AlertSummary;
import com.thinkbiganalytics.servicemonitor.rest.model.ambari.Cluster;
import com.thinkbiganalytics.servicemonitor.rest.model.ambari.ClusterItem;
import com.thinkbiganalytics.servicemonitor.rest.model.ambari.ClusterList;
import com.thinkbiganalytics.servicemonitor.rest.model.ambari.ServiceComponentInfoSummary;

import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.WebTarget;

/**
 * Created by sr186054 on 11/3/16.
 */
public class AmbariJerseyClient extends JerseyRestClient implements AmbariClient {

    private String apiPath = "/api/v1";

    protected WebTarget getBaseTarget() {
        WebTarget target = super.getBaseTarget();
        return target.path(apiPath);
    }

    protected WebTarget getTargetFromPath(String path) {
        WebTarget target = client.target(uri + apiPath + path);
        return target;

    }

    public AmbariJerseyClient(AmbariJerseyRestClientConfig config) {
        super(config);
        this.apiPath = config.getApiPath();
    }


    protected <T> T get(RestCommand<T> restCommand) {
        restCommand.beforeRestRequest();
        Map<String, Object> parameters = restCommand.getParameters();
        String url = restCommand.getUrl();
        if (!url.startsWith("/")) {
            url = "/" + url;
        }
        if (restCommand.getPathString() != null) {
            return getFromPathString(url + restCommand.getPathString(), restCommand.getResponseType());
        } else {
            return get(url, parameters, restCommand.getResponseType());
        }

    }


    public List<String> getAmbariClusterNames() {
        List<String> clusterNames = new ArrayList<>();
        ClusterList clusterList = get(new AmbariGetClustersCommand());
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


    public ServiceComponentInfoSummary getServiceComponentInfo(List<String> clusterNames, String services) {
        ServiceComponentInfoSummary summary = null;
        for (String clusterName : clusterNames) {
            AmbariServicesComponentInfoCommand
                servicesComponentInfoCommand =
                new AmbariServicesComponentInfoCommand(clusterName, services);
            ServiceComponentInfoSummary clusterSummary = get(servicesComponentInfoCommand);
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

    public AlertSummary getAlerts(List<String> clusterNames, String services) throws RestClientException {
        AlertSummary alerts = null;
        for (String clusterName : clusterNames) {
            AmbariAlertsCommand alertsCommand = new AmbariAlertsCommand(clusterName, services);
            AlertSummary alertSummary = get(alertsCommand);
            if (alerts == null) {
                alerts = alertSummary;
            } else {
                alerts.getItems().addAll(alertSummary.getItems());
            }
        }
        return alerts;
    }


}
