package com.thinkbiganalytics.spring;

/*-
 * #%L
 * thinkbig-commons-util
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

import org.apache.commons.io.IOUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

/**
 * Spring bean that gets access to File resources
 */
public class FileResourceService implements ApplicationContextAware {

    ApplicationContext context;

    public ApplicationContext getContext() {
        return context;
    }

    @Override
    public void setApplicationContext(ApplicationContext context)
        throws BeansException {
        this.context = context;
    }

    /**
     * Return a resource for a string location
     *
     * @return the resource found at the supplied location/name
     */
    public Resource getResource(String resource) {
        return context.getResource(resource);
    }

    /**
     * Return a resourcs/file as a string
     *
     * @param resourceLocation a file location string
     * @return the contents of the file/resource as a string
     */
    public String getResourceAsString(String resourceLocation) {
        try {
            Resource resource = getResource(resourceLocation);
            if (resource != null) {
                InputStream is = resource.getInputStream();
                return IOUtils.toString(is);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return null;
    }


}
