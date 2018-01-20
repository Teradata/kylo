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

import com.thinkbiganalytics.ui.api.template.TemplateTableOption;

import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * load the *-stepper-definition.json files and register their resource context paths with kylo
 */
public class TemplateTableOptionConfigurerAdapter extends WebMvcConfigurerAdapter {

    private StandardUiTemplateService uiTemplateService;

    private List<TemplateTableOption> options = null;
    private List<String> resourceLocations = null;

    public TemplateTableOptionConfigurerAdapter(StandardUiTemplateService uiTemplateService){
        super();
        this.uiTemplateService = uiTemplateService;
        initializeJsonTemplateTableOptions();
    }

    private void initializeJsonTemplateTableOptions(){

        List<TemplateTableOption> options = uiTemplateService.loadStepperTemplateDefinitionFiles();
        List<String> resources = new ArrayList<>();
        options.stream().forEach(o -> {
            if(o.getResourceContext() != null) {
                resources.add(o.getResourceContext());
            }
        });
       this.resourceLocations = resources;
       this.options = options;

    }

    public List<TemplateTableOption> getOptions() {
        return options;
    }

    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if(this.resourceLocations != null){
            this.resourceLocations.forEach(location -> registry.addResourceHandler(location+"/**").addResourceLocations("classpath:"+location+"/"));
        }
        }

}
