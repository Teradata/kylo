package com.thinkbiganalytics.ui.rest.controller;

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

import com.thinkbiganalytics.ui.api.module.AngularModule;
import com.thinkbiganalytics.ui.api.module.NavigationLink;
import com.thinkbiganalytics.ui.api.template.ProcessorTemplate;
import com.thinkbiganalytics.ui.api.template.TemplateTableOption;
import com.thinkbiganalytics.ui.service.UiTemplateService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Component
@Path("v1/ui")
@Produces(MediaType.APPLICATION_JSON)
public class UiRestController {

    private static Logger log = LoggerFactory.getLogger(UiRestController.class);

    @Autowired
    private UiTemplateService uiTemplateService;

    @Autowired(required = false)
    private List<TemplateTableOption> templateTableOptions;

    private List<ProcessorTemplate> processorTemplates;

    private List<AngularModule> angularExtensionModules;

    private Map<String, Object> sparkFunctions;

    private Map<String, Object> teradataFunctions;

    @PostConstruct
    private void init() {
        processorTemplates = uiTemplateService.loadProcessorTemplateDefinitionFiles();
        if (!processorTemplates.isEmpty()) {
            log.info("Loaded {} custom processor templates ", processorTemplates.size());
        }

        angularExtensionModules = uiTemplateService.loadAngularModuleDefinitionFiles();
        if(angularExtensionModules != null && !angularExtensionModules.isEmpty()){
            log.info("Loaded {} angular extension modules ",angularExtensionModules.size());
        }

        sparkFunctions = uiTemplateService.loadSparkFunctionsDefinitions();
        teradataFunctions = uiTemplateService.loadTeradataFunctionsDefinitions();
    }

    @GET
    @Path("template-table-options")
    public List<TemplateTableOption> getTemplateTableOptions() {
        return (templateTableOptions != null) ? templateTableOptions : Collections.emptyList();
    }

    @GET
    @Path("processor-templates")
    public List<ProcessorTemplate> getProcessorTemplate() {
        return (processorTemplates != null) ? processorTemplates : Collections.emptyList();
    }

    @GET
    @Path("spark-functions")
    public Map<String, Object> getSparkFunctions() {
        return sparkFunctions;
    }

    @GET
    @Path("extension-modules")
    public List<AngularModule> getAngularExtensionModules() {
        return angularExtensionModules;
    }

    @GET
    @Path("teradata-functions")
    public Map<String, Object> getTeradataFunctions() {
        return teradataFunctions;
    }
}
