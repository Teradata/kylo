package com.thinkbiganalytics.nifi.rest.model;

/*-
 * #%L
 * thinkbig-nifi-rest-model
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
 * Information about property value changes between NiFi template and Kylo template
 */
public class NiFiPropertyRegistrationChangeInfo {

    private String key;
    private String valueFromOlderNiFiTemplate;
    private String valueFromNewerNiFiTemplate;
    private String valueRegisteredInKyloTemplateFromOlderNiFiTemplate;

    public NiFiPropertyRegistrationChangeInfo(String key, String valueFromOlderNiFiTemplate, String valueFromNewerNiFiTemplate, String valueRegisteredInKyloTemplateFromOlderNiFiTemplate) {
        this.key = key;
        this.valueFromOlderNiFiTemplate = valueFromOlderNiFiTemplate;
        this.valueFromNewerNiFiTemplate = valueFromNewerNiFiTemplate;
        this.valueRegisteredInKyloTemplateFromOlderNiFiTemplate = valueRegisteredInKyloTemplateFromOlderNiFiTemplate;
    }

    public NiFiPropertyRegistrationChangeInfo() {

    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValueFromOlderNiFiTemplate() {
        return valueFromOlderNiFiTemplate;
    }

    public void setValueFromOlderNiFiTemplate(String valueFromOlderNiFiTemplate) {
        this.valueFromOlderNiFiTemplate = valueFromOlderNiFiTemplate;
    }

    public String getValueFromNewerNiFiTemplate() {
        return valueFromNewerNiFiTemplate;
    }

    public void setValueFromNewerNiFiTemplate(String valueFromNewerNiFiTemplate) {
        this.valueFromNewerNiFiTemplate = valueFromNewerNiFiTemplate;
    }

    public String getValueRegisteredInKyloTemplateFromOlderNiFiTemplate() {
        return valueRegisteredInKyloTemplateFromOlderNiFiTemplate;
    }

    public void setValueRegisteredInKyloTemplateFromOlderNiFiTemplate(String valueRegisteredInKyloTemplateFromOlderNiFiTemplate) {
        this.valueRegisteredInKyloTemplateFromOlderNiFiTemplate = valueRegisteredInKyloTemplateFromOlderNiFiTemplate;
    }
}