package com.thinkbiganalytics.metadata.api.datasource;

/*-
 * #%L
 * kylo-metadata-api
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

import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * Defines a connection to a JDBC data source.
 */
public interface JdbcDatasourceDetails extends DatasourceDetails {

    /**
     * Gets the id of the NiFi DBCPConnectionPool controller service.
     *
     * @return the controller service id
     */
    @Nonnull
    Optional<String> getControllerServiceId();

    /**
     * Sets the id of the NiFi DBCPConnectionPool controller service.
     *
     * @param controllerServiceId the controller service id
     */
    void setControllerServiceId(@Nonnull String controllerServiceId);

    /**
     * Gets the password to use when connecting to this data source.
     *
     * @return the JDBC password
     */
    @Nonnull
    String getPassword();

    /**
     * Sets the password to use when connecting to this data source.
     *
     * @param password the JDBC password
     */
    void setPassword(@Nonnull String password);
}
