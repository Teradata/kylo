package com.thinkbiganalytics.ui.service;

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

import com.thinkbiganalytics.json.ObjectMapperSerializer;
import com.thinkbiganalytics.ui.api.template.ProcessorTemplate;
import com.thinkbiganalytics.ui.template.ProcessorTemplateDefinition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class UiTemplateService {

    private static Logger log = LoggerFactory.getLogger(UiTemplateService.class);

    @Autowired
    private FileResourceLoaderService fileResourceService;


    /**
     * Load the '*processor-template-definition.json files
     */
    public List<ProcessorTemplate> loadProcessorTemplateDefinitionFiles() {

        List<String> resources = fileResourceService.loadResourcesAsString("classpath*:**/*processor-template-definition.json");
        if (resources != null) {
            return resources.stream().map(json -> ObjectMapperSerializer.deserialize(json, ProcessorTemplateDefinition.class)).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }


}
