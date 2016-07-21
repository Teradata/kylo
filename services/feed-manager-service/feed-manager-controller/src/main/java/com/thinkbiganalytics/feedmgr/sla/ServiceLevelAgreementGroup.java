package com.thinkbiganalytics.feedmgr.sla;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sr186054 on 7/19/16.
 */
public class ServiceLevelAgreementGroup {


    private String name;
    private String description;
    private List<ServiceLevelAgreementRule> rules;

    private List<ServiceLevelAgreementActionUiConfigurationItem> actionConfigurations;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ServiceLevelAgreementRule> getRules() {

        if (rules == null) {
            rules = new ArrayList<>();
        }
        return rules;
    }

    public void setRules(List<ServiceLevelAgreementRule> rules) {
        this.rules = rules;
    }

    public void addServiceLevelAgreementRule(ServiceLevelAgreementRule rule) {
        getRules().add(rule);
    }

    public List<ServiceLevelAgreementActionUiConfigurationItem> getActionConfigurations() {
        return actionConfigurations;
    }

    public void setActionConfigurations(List<ServiceLevelAgreementActionUiConfigurationItem> actionConfigurations) {
        this.actionConfigurations = actionConfigurations;
    }
}
