package com.thinkbiganalytics.servicemonitor.rest.client;

/*-
 * #%L
 * thinkbig-service-monitor-ambari
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

import org.springframework.core.GenericTypeResolver;

import java.util.Map;

/**
 * Superclass for REST commands
 */
public abstract class RestCommand<T> {

    private Class<T> responseType;

    public RestCommand() {
        this.responseType = (Class<T>) GenericTypeResolver.resolveTypeArgument(getClass(), RestCommand.class);
    }

    public String getPathString() {
        return null;
    }

    /**
     * called just before making the request to allow for class to setup data
     */
    public void beforeRestRequest() {

    }

    public abstract String payload();

    public Class<T> getResponseType() {
        return this.responseType;
    }


    public String getUrl() {
        return null;
    }

    public Map<String, Object> getParameters() {
        return null;
    }
}
