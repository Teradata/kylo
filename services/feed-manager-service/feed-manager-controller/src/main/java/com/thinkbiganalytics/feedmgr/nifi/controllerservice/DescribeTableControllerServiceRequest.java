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

import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * Manages the properties for describing tables from a data source managed by a controller service.
 */
public class DescribeTableControllerServiceRequest extends AbstractControllerServiceRequest {

    private String tableName;
    private String schemaName;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        DescribeTableControllerServiceRequest that = (DescribeTableControllerServiceRequest) o;
        return Objects.equals(schemaName, that.schemaName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), schemaName);
    }

    public static class DescribeTableControllerServiceRequestBuilder extends AbstractControllerServiceRequest.AbstractControllerServiceRequestBuilder<DescribeTableControllerServiceRequestBuilder> {

        private String tableName;
        private String schemaName;

        public DescribeTableControllerServiceRequestBuilder(@Nonnull final ControllerServiceDTO controllerServiceDTO) {
            super(controllerServiceDTO);
        }

        public DescribeTableControllerServiceRequestBuilder tableName(String tableName) {
            this.tableName = tableName;
            return this;
        }

        public DescribeTableControllerServiceRequestBuilder schemaName(String schemaName) {
            this.schemaName = schemaName;
            return this;
        }

        public DescribeTableControllerServiceRequest build() {
            final DescribeTableControllerServiceRequest serviceProperties = super.build(new DescribeTableControllerServiceRequest());
            serviceProperties.setSchemaName(schemaName);
            serviceProperties.setTableName(tableName);
            return serviceProperties;
        }
    }
}
