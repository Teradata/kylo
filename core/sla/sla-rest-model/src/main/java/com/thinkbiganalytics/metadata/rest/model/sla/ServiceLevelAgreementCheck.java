package com.thinkbiganalytics.metadata.rest.model.sla;

import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementActionConfiguration;

import java.util.List;

/**
 * Created by sr186054 on 7/22/16.
 */
public class ServiceLevelAgreementCheck {

    private String id;
    private List<? extends ServiceLevelAgreementActionConfiguration> actionConfigurations;
    private String cronSchedule;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<? extends ServiceLevelAgreementActionConfiguration> getActionConfigurations() {
        return actionConfigurations;
    }

    public void setActionConfigurations(List<? extends ServiceLevelAgreementActionConfiguration> actionConfigurations) {
        this.actionConfigurations = actionConfigurations;
    }

    public String getCronSchedule() {
        return cronSchedule;
    }

    public void setCronSchedule(String cronSchedule) {
        this.cronSchedule = cronSchedule;
    }
}
