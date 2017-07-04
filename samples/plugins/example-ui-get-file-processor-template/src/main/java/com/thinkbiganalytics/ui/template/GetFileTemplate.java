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
 * #L
 */

import com.google.common.collect.Lists;
import com.thinkbiganalytics.ui.api.template.ProcessorTemplate;

import org.springframework.stereotype.Component;

import java.util.List;

import javax.annotation.Nullable;


@Component
public class GetFileTemplate implements ProcessorTemplate {

    @Override
    public List getProcessorTypes() {
        return Lists.newArrayList("org.apache.nifi.processors.standard.GetFile");
    }

    @Nullable
    @Override
    public String getStepperTemplateUrl() {
        return "js/plugin/processor-templates/GetFile/get-file-processor.html";
    }

    @Nullable
    @Override
    public String getFeedDetailsTemplateUrl() {
        return "js/plugin/processor-templates/GetFile/get-file-processor.html";
    }
}
