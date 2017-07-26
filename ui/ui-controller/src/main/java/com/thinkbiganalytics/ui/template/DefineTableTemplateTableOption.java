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
 * Steps for defining the destination Hive table and data processing operations on a feed.
 */
@Component
public class DefineTableTemplateTableOption implements TemplateTableOption {

    @Nonnull
    @Override
    public String getDescription() {
        return "Allow users to define and customize the destination data table.";
    }

    @Nonnull
    @Override
    public String getDisplayName() {
        return "Customize destination table";
    }

    @Override
    public String getFeedDetailsTemplateUrl() {
        return "js/plugin/template-table-option/define-table/define-table-feed-details.html";
    }

    @Override
    public String getStepperTemplateUrl() {
        return "js/plugin/template-table-option/define-table/define-table-stepper.html";
    }

    @Override
    public int getTotalSteps() {
        return 2;
    }

    @Nonnull
    @Override
    public String getType() {
        return "DEFINE_TABLE";
    }
}
