package com.thinkbiganalytics.kylo.catalog.datasource;

/*-
 * #%L
 * kylo-catalog-core
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.thinkbiganalytics.json.ObjectMapperSerializer;
import com.thinkbiganalytics.kylo.catalog.rest.model.Connector;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSource;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

/**
 * Provides access to {@link Connector} objects.
 */
@Component
public class DataSourceProvider {

    /**
     * Maps id to connector
     */
    @Nonnull
    private final Map<String, DataSource> datasources;

    /**
     * Constructs a {@code ConnectorProvider}.
     */
    public DataSourceProvider() throws IOException {
        final String connectorsJson = IOUtils.toString(getClass().getResourceAsStream("/catalog-datasources.json"), StandardCharsets.UTF_8);
        final List<DataSource> connectorList = ObjectMapperSerializer.deserialize(connectorsJson, new TypeReference<List<DataSource>>() {
        });
        datasources = connectorList.stream()
            .collect(Collectors.toMap(DataSource::getId, Function.identity()));
    }

    /**
     * Gets the connector with the specified id.
     */
    @Nonnull
    public Optional<DataSource> getDataSource(@Nonnull final String id) {
        return Optional.ofNullable(datasources.get(id));
    }

    /**
     * Gets the list of available connectors.
     */
    @Nonnull
    public List<DataSource> getDataSources() {
        return new ArrayList<>(datasources.values());
    }
}
