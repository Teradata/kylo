package com.thinkbiganalytics.metadata.modeshape.service;

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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.api.datasource.DerivedDatasource;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.EntityUtil;
import com.thinkbiganalytics.search.api.Search;
import com.thinkbiganalytics.search.api.SearchIndex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;

/**
 * Listens for changes to the JCR metastore and updates indexes appropriately.
 */
public class JcrIndexService implements EventListener {

    private static final Logger log = LoggerFactory.getLogger(JcrIndexService.class);

    /**
     * Type name of Hive datasources.
     */
    private static final String HIVE_DATASOURCE = "HiveDatasource";

    /**
     * Provides access to datasources.
     */
    @Nonnull
    private final DatasourceProvider datasourceProvider;

    /**
     * Pool of threads for executing search index updates.
     */
    @Nonnull
    private final ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setDaemon(true).setNameFormat("kylo-index-pool-%d").build());

    /**
     * Provides access to the JCR metastore.
     */
    @Nonnull
    private final MetadataAccess metadataAccess;

    /**
     * Provides access to the search engine.
     */
    @Nonnull
    private final Search search;

    /**
     * Constructs a {@code JcrIndexService}.
     */
    public JcrIndexService(@Nonnull final Search search, @Nonnull final DatasourceProvider datasourceProvider, @Nonnull final MetadataAccess metadataAccess) {
        this.search = search;
        this.datasourceProvider = datasourceProvider;
        this.metadataAccess = metadataAccess;
    }

    /**
     * Re-indexes the metastore to ensure it's in a consistent state.
     */
    @PostConstruct
    public void initialize() {
        executor.execute(() -> metadataAccess.read(() -> datasourceProvider.getDatasources().stream()
                                                       .filter(DerivedDatasource.class::isInstance)
                                                       .map(DerivedDatasource.class::cast)
                                                       .filter(ds -> HIVE_DATASOURCE.equals(ds.getDatasourceType()))
                                                       .forEach(this::indexDerivedDatasource),
                                                   MetadataAccess.SERVICE)
        );
        search.commit(SearchIndex.DATASOURCES);
    }

    /**
     * Updates the index when a metastore object is modified.
     */
    @Override
    public void onEvent(final EventIterator events) {
        executor.execute(() -> {
            while (events.hasNext()) {
                final Event event = events.nextEvent();
                try {
                    indexEvent(event);
                } catch (final RepositoryException e) {
                    throw new MetadataRepositoryException("Unable to process event: " + event, e);
                }
            }
        });
    }

    /**
     * Initiates an orderly shutdown by waiting for currently queued events to be processed.
     */
    @PreDestroy
    public void shutdown() {
        executor.shutdown();
    }

    /**
     * Indexes derived datasource objects.
     *
     * @param datasource the derived datasource to index
     * @return {@code true} if the index was updated, or {@code false} otherwise
     */
    private boolean indexDerivedDatasource(@Nonnull final DerivedDatasource datasource) {
        if (HIVE_DATASOURCE.equals(datasource.getDatasourceType())) {
            final Map<String, Object> fields = new HashMap<>();

            // Determine database and table names
            final Map<String, Object> properties = datasource.getProperties();
            fields.put("databaseName", properties.get("Target schema"));
            fields.put("tableName", properties.get("Target table"));

            // Generate list of column metadata
            final Map<String, Object> genericProperties = datasource.getGenericProperties();
            final Object columns = genericProperties.get("columns");
            if (columns != null && columns instanceof List) {
                final List<Map<String, Object>> hiveColumns = ((List<?>) columns).stream()
                    .map(Map.class::cast)
                    .map(map -> {
                        final Map<String, Object> column = new HashMap<>();
                        column.put("columnComment", map.get("description"));
                        column.put("columnName", map.get("name"));
                        @SuppressWarnings("unchecked") final List<Map<String, String>> tags = (List<Map<String, String>>) map.get("tags");
                        if (tags != null && !tags.isEmpty()) {
                            column.put("columnTags", tags.stream().map(tag -> tag.get("name")).collect(Collectors.toList()));
                        }
                        column.put("columnType", map.get("derivedDataType"));
                        return column;
                    })
                    .collect(Collectors.toList());
                fields.put("hiveColumns", hiveColumns);
            }

            // Index the Hive schema
            if (fields.get("databaseName") != null && fields.get("tableName") != null) {
                search.index(SearchIndex.DATASOURCES, datasource.getDatasourceType(), datasource.getId().toString(), fields);
                return true;
            }
        }
        return false;
    }

    /**
     * Updates the index with derived datasource events.
     */
    private void indexDerivedDatasourceEvent(@Nonnull final Event event) throws RepositoryException {
        boolean commit = false;

        if (event.getType() == Event.NODE_REMOVED) {
            Pattern nodePattern = Pattern.compile("^/metadata/datasources/derived/HiveDatasource-([^/.]+)\\.([^/.]+)$");
            Matcher nodeMatcher = nodePattern.matcher(event.getPath());

            if (nodeMatcher.matches()) {
                if (nodeMatcher.groupCount() != 2) {
                    log.warn("Schema and table information not received for deletion event (id = {}). Deletion should be handled separately.", event.getIdentifier());
                }
                else {
                    search.delete(SearchIndex.DATASOURCES, HIVE_DATASOURCE, event.getIdentifier(), nodeMatcher.group(1), nodeMatcher.group(2));
                    commit = true;
                }
            }
        } else {
            commit = metadataAccess.read(() -> {
                final Datasource datasource = datasourceProvider.getDatasource(datasourceProvider.resolve(event.getIdentifier()));
                if (datasource == null) {
                    log.warn("Cannot update search index for missing datasource: {}", event.getIdentifier());
                } else if (datasource instanceof DerivedDatasource) {
                    return indexDerivedDatasource((DerivedDatasource) datasource);
                }
                return false;
            }, MetadataAccess.SERVICE);
        }

        if (commit) {
            search.commit(SearchIndex.DATASOURCES);
        }
    }

    /**
     * Updates the index with metastore events.
     */
    private void indexEvent(@Nonnull final Event event) throws RepositoryException {
        if (event.getPath().startsWith(EntityUtil.pathForDerivedDatasource())) {
            indexDerivedDatasourceEvent(event);
        }
    }
}
