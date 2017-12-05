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
import com.thinkbiganalytics.spring.FileResourceService;
import com.thinkbiganalytics.ui.api.module.AngularModule;
import com.thinkbiganalytics.ui.api.service.UiTemplateService;
import com.thinkbiganalytics.ui.api.template.DefaultTemplateTableOption;
import com.thinkbiganalytics.ui.api.template.ProcessorTemplate;
import com.thinkbiganalytics.ui.api.template.TemplateTableOption;
import com.thinkbiganalytics.ui.module.DefaultAngularModule;
import com.thinkbiganalytics.ui.template.ProcessorTemplateDefinition;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StandardUiTemplateService implements UiTemplateService {

    private static final Logger log = LoggerFactory.getLogger(StandardUiTemplateService.class);

    @Autowired
    private FileResourceService fileResourceService;


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


    /**
     * Load the '*-stepper-definition.json files
     */
    public List<TemplateTableOption> loadStepperTemplateDefinitionFiles() {

        List<String> resources = fileResourceService.loadResourcesAsString("classpath*:**/*-stepper-definition.json");
        if (resources != null) {
           return  resources.stream().map(json -> ObjectMapperSerializer.deserialize(json, DefaultTemplateTableOption.class)).map(o -> { o.updateTotalSteps(); return o;}).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }



    public List<AngularModule> loadAngularModuleDefinitionFiles() {
        List<AngularModule> modules = new ArrayList<>();
        fileResourceService.loadResources("classpath*:**/*module-definition.json", (resource) -> {
            try {
                final String json = fileResourceService.resourceAsString(resource);
                DefaultAngularModule module = (json != null) ? ObjectMapperSerializer.deserialize(json, DefaultAngularModule.class) : null;
                String moduleJsUrl = "";
                if (module != null) {
                    if (StringUtils.isBlank(module.getModuleJsUrl())) {
                        //attempt to derive it
                        try {
                            String url = resource.getURL().getPath().toString();
                            String modulePath = "";
                            int idx = url.indexOf("/static/js/");
                            if (idx >= 0) {
                                modulePath = url.substring(idx + ("/static/js/".length()));
                            }
                            if (StringUtils.isNotBlank(modulePath)) {
                                moduleJsUrl = StringUtils.substringBefore(modulePath, resource.getFilename()) + "module";
                                modulePath = moduleJsUrl;
                                module.setModuleJsUrl(modulePath);
                            } else {
                                log.error("Unable to load Angular Extension Module {}.  Please ensure you have a module.js file located in the same directory as the {} definition file",
                                          resource.getFilename(), resource.getFilename());
                            }


                        } catch (Exception e) {
                            log.error("Unable to load Angular Extension Module ", resource.getFilename(), e);
                        }

                    }
                    if (StringUtils.isNotBlank(module.getModuleJsUrl())) {
                        modules.add(module);
                    }
                }

            } catch (final RuntimeException e) {
                log.error("Failed to parse Angular Extension Module: {}", resource.getFilename(), e);
            }

        });
        return modules;
    }

    /**
     * Loads and merges the '*spark-functions.json' files.
     *
     * @return the Spark function definitions
     */
    public Map<String, Object> loadSparkFunctionsDefinitions() {
        return loadFunctionsDefinitions("spark-functions", "classpath*:**/*spark-functions.json");
    }

    /**
     * Loads and merges function definition files.
     *
     * @return the function definitions
     */
    public Map<String, Object> loadFunctionsDefinitions(@Nonnull final String name, @Nonnull final String pattern) {
        // Attempt to load resources
        final Resource[] resources;
        try {
            resources = fileResourceService.loadResources(pattern);
        } catch (final IOException e) {
            log.error("Unable to load Spark function definitions", e);
            return Collections.emptyMap();
        }

        // Merge resources into map
        final Map<String, Object> merged = new LinkedHashMap<>();
        merged.put("!name", name);

        for (final Resource resource : resources) {
            // Deserialize resource
            Map<String, Object> functions = null;
            try {
                final String json = fileResourceService.resourceAsString(resource);
                functions = (json != null) ? ObjectMapperSerializer.deserialize(json, Map.class) : null;
            } catch (final RuntimeException e) {
                log.error("Failed to parse {} functions: {}", name, resource.getFilename(), e);
            }

            // Merge definitions
            if (functions != null) {
                try {
                    merge(merged, functions, null);
                } catch (final Exception e) {
                    log.error("Failed to merge {} functions: {}", name, resource.getFilename(), e);
                }
            }
        }

        return merged;
    }

    /**
     * Merges members of the source map into the destination map.
     *
     * @param destination the destination map
     * @param source      the source map
     * @param prefix      a key prefix for reporting errors
     */
    private void merge(@Nonnull final Map<String, Object> destination, @Nonnull final Map<String, Object> source, @Nullable final String prefix) {
        for (final Map.Entry<String, Object> entry : source.entrySet()) {
            final String key = entry.getKey();
            final Object value = entry.getValue();

            if (destination.containsKey(key)) {
                final String newPrefix = (prefix != null) ? prefix + "." + key : key;
                if (destination.get(key) instanceof Map && value instanceof Map) {
                    //noinspection unchecked
                    merge((Map<String, Object>) destination.get(key), (Map<String, Object>) value, newPrefix);
                } else if (prefix != null || "!name".equals(key)) {
                    log.info("Ignoring duplicate Spark function key: {}", newPrefix);
                }
            } else {
                destination.put(key, value);
            }
        }
    }
}
