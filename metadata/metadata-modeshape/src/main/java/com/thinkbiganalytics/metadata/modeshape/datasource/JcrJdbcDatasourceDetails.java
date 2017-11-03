package com.thinkbiganalytics.metadata.modeshape.datasource;

/*-
 * #%L
 * kylo-metadata-modeshape
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

import com.thinkbiganalytics.metadata.api.datasource.JdbcDatasourceDetails;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.jcr.Node;

/**
 * A {@code JdbcDatasource} that is stored in JRC.
 */
public class JcrJdbcDatasourceDetails extends JcrDatasourceDetails implements JdbcDatasourceDetails {

    /**
     * JCR node type
     */
    public static final String NODE_TYPE = "tba:jdbcDatasourceDetails";

    /**
     * Name of the controllerServiceId attribute
     */
    private static final String CONTROLLER_SERVICE_ID = "tba:controllerServiceId";

    /**
     * Name of the password attribute
     */
    private static final String PASSWORD = "tba:password";

    /**
     * Constructs a {@code JcrJdbcDatasourceDetails} with the specified JCR node.
     *
     * @param node the JCR node
     */
    public JcrJdbcDatasourceDetails(@Nonnull final Node node) {
        super(node);
    }

    @Nonnull
    @Override
    public Optional<String> getControllerServiceId() {
        return Optional.ofNullable(getProperty(CONTROLLER_SERVICE_ID, String.class));
    }

    @Override
    public void setControllerServiceId(@Nonnull final String controllerServiceId) {
        setProperty(CONTROLLER_SERVICE_ID, controllerServiceId);
    }

    @Nonnull
    @Override
    public String getPassword() {
        return Optional.ofNullable(getProperty(PASSWORD, String.class)).orElse("");
    }

    @Override
    public void setPassword(@Nonnull final String password) {
        setProperty(PASSWORD, password);
    }
}
