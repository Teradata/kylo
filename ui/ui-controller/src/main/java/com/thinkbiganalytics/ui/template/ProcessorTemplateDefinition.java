package com.thinkbiganalytics.ui.template;

/*-
 * #%L
 * kylo-ui-controller
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

import com.thinkbiganalytics.ui.api.template.ProcessorTemplate;

import java.util.List;


public class ProcessorTemplateDefinition implements ProcessorTemplate {

    private String processorDisplayName;

    /**
     * An array of the NiFi processor class name (i.e. com.thinkbiganalytics.nifi.GetTableData)
     */
    List processorTypes;

    /**
     * The url for the template used when creating a new feed
     */
    private String stepperTemplateUrl;


    /**
     * The url for the template used when editing a new feed
     */
    private String feedDetailsTemplateUrl;

    public List getProcessorTypes() {
        return processorTypes;
    }

    public void setProcessorTypes(List processorTypes) {
        this.processorTypes = processorTypes;
    }

    public String getStepperTemplateUrl() {
        return stepperTemplateUrl;
    }

    public void setStepperTemplateUrl(String stepperTemplateUrl) {
        this.stepperTemplateUrl = stepperTemplateUrl;
    }

    public String getFeedDetailsTemplateUrl() {
        return feedDetailsTemplateUrl;
    }

    public void setFeedDetailsTemplateUrl(String feedDetailsTemplateUrl) {
        this.feedDetailsTemplateUrl = feedDetailsTemplateUrl;
    }

    @Override
    public String getProcessorDisplayName() {
        return processorDisplayName;
    }

    public void setProcessorDisplayName(String processorDisplayName) {
        this.processorDisplayName = processorDisplayName;
    }
}
