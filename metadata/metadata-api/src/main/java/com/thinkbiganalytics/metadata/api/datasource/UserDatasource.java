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

import com.thinkbiganalytics.metadata.api.security.AccessControlled;

import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * Defines a data source managed through Kylo by a user.
 */
public interface UserDatasource extends Datasource, AccessControlled {

    /**
     * Sets the description of this data source.
     *
     * @param description the description
     */
    void setDescription(@Nonnull String description);

    /**
     * Gets the additional properties for this data source.
     *
     * @return the details
     */
    @Nonnull
    Optional<? extends DatasourceDetails> getDetails();

    /**
     * Sets the name of this data source.
     *
     * @param name the name
     */
    void setName(@Nonnull String name);

    /**
     * Gets the type of this data source.
     *
     * @return the type
     */
    @Nonnull
    String getType();

    /**
     * Sets the type of this data source.
     *
     * @param type the type
     */
    void setType(@Nonnull String type);
}
