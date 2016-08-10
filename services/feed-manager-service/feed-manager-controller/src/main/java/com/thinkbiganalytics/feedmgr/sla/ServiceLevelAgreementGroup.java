package com.thinkbiganalytics.feedmgr.sla;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sr186054 on 7/19/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceLevelAgreementGroup {


    private String id;
    private String name;
    private String description;
    private List<ServiceLevelAgreementRule> rules;

    private List<ServiceLevelAgreementActionUiConfigurationItem> actionConfigurations;

    private List<String> actionErrors;

    private List<String> ruleErrors;


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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getActionErrors() {
        return actionErrors;
    }

    public void setActionErrors(List<String> actionErrors) {
        this.actionErrors = actionErrors;
    }

    public List<String> getRuleErrors() {
        return ruleErrors;
    }

    public void setRuleErrors(List<String> ruleErrors) {
        this.ruleErrors = ruleErrors;
    }
}
