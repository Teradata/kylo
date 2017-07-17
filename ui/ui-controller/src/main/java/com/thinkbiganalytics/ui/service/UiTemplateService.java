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
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

    /**
     * Loads and merges the '*spark-functions.json' files.
     *
     * @return the Spark function definitions
     */
    public Map<String, Object> loadSparkFunctionsDefinitions() {
        // Attempt to load resources
        final Resource[] resources;
        try {
            resources = fileResourceService.loadResources("classpath*:**/*spark-functions.json");
        } catch (final IOException e) {
            log.error("Unable to load Spark function definitions", e);
            return Collections.emptyMap();
        }

        // Merge resources into map
        final Map<String, Object> merged = new LinkedHashMap<>();
        merged.put("!name", "spark-functions");

        for (final Resource resource : resources) {
            // Deserialize resource
            Map<String, Object> functions = null;
            try {
                final String json = fileResourceService.resourceAsString(resource);
                functions = (json != null) ? ObjectMapperSerializer.deserialize(json, Map.class) : null;
            } catch (final RuntimeException e) {
                log.error("Failed to parse Spark functions: {}", resource.getFilename(), e);
            }

            // Merge definitions
            if (functions != null) {
                try {
                    merge(merged, functions, null);
                } catch (final Exception e) {
                    log.error("Failed to merge Spark functions: {}", resource.getFilename(), e);
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
