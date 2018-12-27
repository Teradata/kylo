package com.thinkbiganalytics.feedmgr.service.feed.importing.model;
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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.thinkbiganalytics.nifi.rest.model.NiFiAllowableValue;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class NifiControllerServiceAllowableValue extends NiFiAllowableValue {

    NifiProperty templatePropertyValue;
    String templateServiceName;

    boolean templateServiceAlreadyExists = false;

    public NifiControllerServiceAllowableValue() {
    }

    public NifiControllerServiceAllowableValue(NiFiAllowableValue v){
        this(v.getDisplayName(),v.getValue());
        this.setDescription(v.getDescription());
    }

    public NifiControllerServiceAllowableValue(String displayName, String value) {
        super(displayName, value);
    }

    public NifiProperty getTemplatePropertyValue() {
        return templatePropertyValue;
    }

    public void setTemplatePropertyValue(NifiProperty templatePropertyValue) {
        this.templatePropertyValue = templatePropertyValue;
    }

    public boolean isTemplateServiceAlreadyExists() {
        return templateServiceAlreadyExists;
    }

    public void setTemplateServiceAlreadyExists(boolean templateServiceAlreadyExists) {
        this.templateServiceAlreadyExists = templateServiceAlreadyExists;
    }

    public String getTemplateServiceName() {
        return templateServiceName;
    }

    public void setTemplateServiceName(String templateServiceName) {
        this.templateServiceName = templateServiceName;
    }
}
