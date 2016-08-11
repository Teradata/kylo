package com.thinkbiganalytics.metadata.rest.model.sla;

import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementActionConfiguration;

import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;

/**
 * Created by sr186054 on 7/22/16.
 */
public class ServiceLevelAgreementCheck {

    private String id;
    @ApiModelProperty(reference = "#")
    private List<? extends ServiceLevelAgreementActionConfiguration> actionConfigurations;
    private String cronSchedule;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<? extends ServiceLevelAgreementActionConfiguration> getActionConfigurations() {
        if (actionConfigurations == null) {
            return new ArrayList<>();
        }
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
