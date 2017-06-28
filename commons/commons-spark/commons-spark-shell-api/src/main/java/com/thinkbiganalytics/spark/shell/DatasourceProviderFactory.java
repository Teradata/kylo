package com.thinkbiganalytics.spark.shell;

/*-
 * #%L
 * Kylo Commons Spark Shell
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

import com.thinkbiganalytics.spark.rest.model.Datasource;

import java.util.Collection;

import javax.annotation.Nonnull;

/**
 * Creates data source providers.
 */
public interface DatasourceProviderFactory {

    /**
     * Creates a new data source provider for the specified data sources.
     *
     * @param datasources the data sources
     * @return the data source provider
     */
    @Nonnull
    DatasourceProvider getDatasourceProvider(@Nonnull Collection<Datasource> datasources);
}
