package com.thinkbiganalytics.nifi.core.api.spring;

/*-
 * #%L
 * thinkbig-nifi-core-service-api
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

import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.controller.ControllerService;
import org.springframework.beans.BeansException;

/**
 */
@Tags({"thinkbig", "spring", "bean", "context"})
@CapabilityDescription("Provides access to spring beans loaded from a spring configuration")
public interface SpringContextService extends ControllerService {

    /**
     * used to fetch a bean from the spring context of the given type, expects there is only one bean matching the type given
     *
     * @param <T>          the type of the bean to fetch
     * @param requiredType the class instance representing the required type
     * @return the bean
     * @throws BeansException if more than one bean found of the given type, or bean not found
     */
    <T> T getBean(Class<T> requiredType) throws BeansException;

    /**
     * used to fetch a bean from the spring context of the given name and type, expects there is only one bean matching the name and type given
     *
     * @param name         the name of the bean
     * @param <T>          the type of the bean to fetch
     * @param requiredType the class instance representing the required type
     * @return the bean
     * @throws BeansException if more than one bean found of the given type, or bean not found
     */
    <T> T getBean(String name, Class<T> requiredType) throws BeansException;

}
