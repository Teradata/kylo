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
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementAction;
import com.thinkbiganalytics.policy.rest.model.BaseUiPolicyRule;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

/**
 * UI object representing the possible ServiceLevelAgreement actions one can perform.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceLevelAgreementActionUiConfigurationItem extends BaseUiPolicyRule {

    private boolean validConfiguration = false;

    private String validationMessage;

    private String velocityTemplateType;

    @ApiModelProperty(value = "actionClasses", name = "actionClasses", reference = "#")
    private List<Class<? extends ServiceLevelAgreementAction>> actionClasses;

    public List<Class<? extends ServiceLevelAgreementAction>> getActionClasses() {
        return actionClasses;
    }

    public void setActionClasses(List<Class<? extends ServiceLevelAgreementAction>> actionClasses) {
        this.actionClasses = actionClasses;
    }


    public boolean isValidConfiguration() {
        return validConfiguration;
    }

    public void setValidConfiguration(boolean validConfiguration) {
        this.validConfiguration = validConfiguration;
    }

    public String getValidationMessage() {
        return validationMessage;
    }

    public void setValidationMessage(String validationMessage) {
        this.validationMessage = validationMessage;
    }

    public String getVelocityTemplateType() {
        return velocityTemplateType;
    }

    public void setVelocityTemplateType(String velocityTemplateType) {
        this.velocityTemplateType = velocityTemplateType;
    }
}
