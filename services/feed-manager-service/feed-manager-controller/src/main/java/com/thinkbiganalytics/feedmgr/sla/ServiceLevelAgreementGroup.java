package com.thinkbiganalytics.feedmgr.sla;

/*-
 * #%L
 * thinkbig-feed-manager-controller
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.ApiModel;

/**
 * User interface object used to hold information about a given SLA
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(value = "Service Level Agreement UI Form Object", description = "A Service Level Agreement that is in the correct for for the UI to parse and display on a form with inputs.")
public class ServiceLevelAgreementGroup {


    private String id;
    private String name;
    private String description;

    private boolean canEdit;

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


    public boolean isCanEdit() {
        return canEdit;
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }
}
