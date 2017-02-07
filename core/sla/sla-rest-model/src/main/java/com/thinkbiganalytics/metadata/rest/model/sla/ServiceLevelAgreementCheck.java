package com.thinkbiganalytics.metadata.rest.model.sla;

/*-
 * #%L
 * thinkbig-sla-rest-model
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

import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementActionConfiguration;

import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;

/**
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
