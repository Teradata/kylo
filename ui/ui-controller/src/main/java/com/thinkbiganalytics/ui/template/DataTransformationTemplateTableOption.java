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

import com.thinkbiganalytics.ui.api.template.TemplateTableOption;

import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

/**
 * Steps for using the Data Wrangler to transform a SQL query and store it in a Hive table.
 */
@Component
public class DataTransformationTemplateTableOption implements TemplateTableOption {

    @Nonnull
    @Override
    public String getDescription() {
        return "Users pick and choose different data tables and columns and apply functions to transform the data to their desired destination table.";
    }

    @Nonnull
    @Override
    public String getDisplayName() {
        return "Data Transformation";
    }

    @Override
    public String getFeedDetailsTemplateUrl() {
        return "js/plugin/template-table-option/data-transformation/data-transform-feed-details.html";
    }

    @Override
    public String getStepperTemplateUrl() {
        return "js/plugin/template-table-option/data-transformation/data-transform-stepper.html";
    }

    @Override
    public int getTotalSteps() {
        return 4;
    }

    @Nonnull
    @Override
    public String getType() {
        return "DATA_TRANSFORMATION";
    }
}
