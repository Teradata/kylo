package com.thinkbiganalytics.feedmgr.nifi.controllerservice;

/*-
 * #%L
 * kylo-feed-manager-controller
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

import org.apache.nifi.web.api.dto.ControllerServiceDTO;

import javax.annotation.Nonnull;

/**
 * Executes a query on a data source managed by a controller service.
 */
public class ExecuteQueryControllerServiceRequest extends AbstractControllerServiceRequest {

    private String query;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public static class ExecuteQueryControllerServiceRequestBuilder extends AbstractControllerServiceRequest.AbstractControllerServiceRequestBuilder<ExecuteQueryControllerServiceRequestBuilder> {

        private String query;

        public ExecuteQueryControllerServiceRequestBuilder(@Nonnull final ControllerServiceDTO controllerServiceDTO) {
            super(controllerServiceDTO);
        }

        public ExecuteQueryControllerServiceRequestBuilder query(String query) {
            this.query = query;
            return this;
        }

        @Nonnull
        public ExecuteQueryControllerServiceRequest build() {
            final ExecuteQueryControllerServiceRequest request = super.build(new ExecuteQueryControllerServiceRequest());
            request.setQuery(query);
            return request;
        }
    }
}
