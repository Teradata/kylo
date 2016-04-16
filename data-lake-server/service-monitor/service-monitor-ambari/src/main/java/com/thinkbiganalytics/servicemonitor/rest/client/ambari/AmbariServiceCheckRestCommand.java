package com.thinkbiganalytics.servicemonitor.rest.client.ambari;


import com.thinkbiganalytics.servicemonitor.rest.client.RestCommand;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by sr186054 on 10/12/15.
 */
public class AmbariServiceCheckRestCommand<T> extends RestCommand<T> {
    private String clusterName;
private String services;

    public AmbariServiceCheckRestCommand(String clusterName, String services) {
        super();
        this.clusterName = clusterName;
        this.services = services;
    }


    @Override
    public String payload() {
        return null;
    }

    public String getServices() {
        return services;
    }

    public void setServices(String services) {
        this.services = services;
    }



    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public boolean isActive() {
        return StringUtils.isNotBlank(getServices());
    }
}
