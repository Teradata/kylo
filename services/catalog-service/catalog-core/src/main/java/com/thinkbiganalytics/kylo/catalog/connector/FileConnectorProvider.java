package com.thinkbiganalytics.kylo.catalog.connector;

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
import com.thinkbiganalytics.kylo.catalog.ConnectorProvider;
import com.thinkbiganalytics.kylo.catalog.rest.model.Connector;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
public class FileConnectorProvider implements ConnectorProvider {

    /**
     * Maps id to connector
     */
    @Nonnull
    private final Map<String, Connector> connectors;

    /**
     * Constructs a {@code FileConnectorProvider}.
     */
    public FileConnectorProvider() throws IOException {
        final String connectorsJson = IOUtils.toString(getClass().getResourceAsStream("/catalog-connectors.json"), StandardCharsets.UTF_8);
        final List<Connector> connectorList = ObjectMapperSerializer.deserialize(connectorsJson, new TypeReference<List<Connector>>() {
        });
        connectors = connectorList.stream()
            .peek(connector -> {
                if (connector.getId() == null) {
                    connector.setId(connector.getTitle().toLowerCase().replaceAll("\\W", "-").replaceAll("-{2,}", "-"));
                }
            })
            .collect(Collectors.toMap(Connector::getId, Function.identity()));
    }

    /**
     * Gets the connector with the specified id.
     */
    @Override
    @Nonnull
    public Optional<Connector> findConnector(@Nonnull final String id) {
        return Optional.ofNullable(connectors.get(id)).map(Connector::new);
    }

    /**
     * Gets the list of available connectors.
     */
    @Override
    @Nonnull
    public List<Connector> findAllConnectors() {
        return connectors.values().stream().map(Connector::new).collect(Collectors.toList());
    }
}
