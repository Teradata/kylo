package com.thinkbiganalytics.spring;

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

import com.google.common.collect.Lists;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class FileResourceService {

    private static final Logger log = LoggerFactory.getLogger(FileResourceService.class);

    @Autowired
    private ResourceLoader resourceLoader;

    public String resourceAsString(Resource resource) {
        try {
            if (resource != null) {
                InputStream is = resource.getInputStream();
                return IOUtils.toString(is, Charset.defaultCharset());
            }
        } catch (IOException e) {
            log.error("Unable to load file {} ", resource.getFilename(), e);
        }
        return null;
    }

    public String getResourceAsString(String resourceLocation) {
        return loadResourceAsString(resourceLocation);
    }

    public Resource getResource(String resource) {
        return resourceLoader.getResource(resource);
    }

    /**
     * Return a resourcs/file as a string
     *
     * @param resourceLocation a file location string
     * @return the contents of the file/resource as a string
     */
    public String loadResourceAsString(String resourceLocation) {
        Resource resource = resourceLoader.getResource(resourceLocation);
        return resourceAsString(resource);
    }

    public Resource[] loadResources(String pattern) throws IOException {
        return ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(pattern);
    }

    public List<String> loadResourcesAsString(String pattern) {
        try {
            Resource[] resources = loadResources(pattern);
            if (resources != null) {
                return Lists.newArrayList(resources).stream().map(resource -> resourceAsString(resource)).filter(Objects::nonNull).collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("unable to load resources matching the pattern {} ", pattern, e);
        }
        return Collections.emptyList();
    }


    public void loadResources(String pattern,LoadResourceCallback callback ) {
        try {
            Resource[] resources = loadResources(pattern);
            if (resources != null) {
                Lists.newArrayList(resources).stream().forEach(resource -> {
                    try {
                        callback.execute(resource);
                    }
                    catch (Exception e){
                        log.error("Unable to load resource ",resource.getFilename(),e);
                    }
                });
            }
        } catch (Exception e) {
            log.error("unable to load resources matching the pattern {} ", pattern, e);
        }

    }

}
