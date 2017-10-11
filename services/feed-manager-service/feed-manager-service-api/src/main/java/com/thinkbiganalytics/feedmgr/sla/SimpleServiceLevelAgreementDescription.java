package com.thinkbiganalytics.feedmgr.sla;
/*-
 * #%L
 * thinkbig-feed-manager-service-api
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

/**
 * Created by sr186054 on 10/9/17.
 */
public class SimpleServiceLevelAgreementDescription {
    String slaId;
    String name;
    String description;

    public SimpleServiceLevelAgreementDescription(){

    }

    public SimpleServiceLevelAgreementDescription(String slaId, String name, String description) {
        this.slaId = slaId;
        this.name = name;
        this.description = description;
    }

    public String getSlaId() {
        return slaId;
    }

    public void setSlaId(String slaId) {
        this.slaId = slaId;
    }

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
}
