package com.thinkbiganalytics.ui.api.template;

/*-
 * #%L
 * kylo-ui-api
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

import java.util.List;

import javax.annotation.Nullable;

/**
 * Define custom NiFi Processor Rendering when creating/editing feeds
 */
public interface ProcessorTemplate {


    /**
     * Match against the display name of the processor
     * @return
     */
    String getProcessorDisplayName();

    /**
     * Return an array of the NiFi processor class name (i.e. com.thinkbiganalytics.nifi.GetTableData)
     * @return Return an array of the NiFi processor class name (i.e. com.thinkbiganalytics.nifi.GetTableData)
     */
   List getProcessorTypes();

    /**
     *
     * @return the url for the template used when creating a new feed
     */
    @Nullable
    default String getStepperTemplateUrl()  {
        return null;
    };


    /**
     *
     * @return the url for the template used when editing a new feed
     */
    @Nullable
    default String getFeedDetailsTemplateUrl() {
        return null;
    }

}
