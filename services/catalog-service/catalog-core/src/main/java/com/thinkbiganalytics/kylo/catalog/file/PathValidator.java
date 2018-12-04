package com.thinkbiganalytics.kylo.catalog.file;

import com.thinkbiganalytics.kylo.catalog.ConnectorPluginManager;

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

import com.thinkbiganalytics.kylo.catalog.connector.ConnectorUtil;
import com.thinkbiganalytics.kylo.catalog.datasource.DataSourceUtil;
import com.thinkbiganalytics.kylo.catalog.rest.model.Connector;
import com.thinkbiganalytics.kylo.catalog.rest.model.ConnectorPluginDescriptor;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSet;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSource;
import com.thinkbiganalytics.kylo.catalog.spi.ConnectorPlugin;

import org.apache.hadoop.fs.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Validates data set paths.
 */
@Component
public class PathValidator {

    private static final Pattern FILENAME_REGEX = Pattern.compile("^([^\\./:]{1,2}|[^/:]{3,})$");
    
    @Inject
    private ConnectorPluginManager pluginManager;

    /**
     * Expected connector tab srefs for file systems
     */
    @Nonnull
    private List<String> fileSystemSrefs = Collections.emptyList();

    /**
     * Expected connector tab srefs for uploading files
     */
    @Nonnull
    private List<String> uploadSrefs = Collections.emptyList();

    /**
     * Sets the connector tab srefs for file systems.
     */
    @Value("${catalog.collection.filesystem.sref:#{null}}")
    public void setFileSystemSrefs(@Nullable final List<String> sref) {
        fileSystemSrefs = (sref != null) ? sref : Collections.singletonList(".browse");
    }

    /**
     * Sets the connector tab srefs for uploading files.
     */
    @Value("${catalog.collection.upload.sref:#{null}}")
    public void setUploadSrefs(@Nullable final List<String> sref) {
        uploadSrefs = (sref != null) ? sref : Collections.singletonList(".upload");
    }

    /**
     * Determines if the specified path is allowed for the specified data set.
     */
    public boolean isPathAllowed(@Nonnull final Path path, @Nonnull final DataSet dataSet) {
        return isPathAllowed(path, dataSet.getId(), dataSet.getDataSource());
    }

    /**
     * Determines if the specified path is allowed for the specified data source.
     */
    public boolean isPathAllowed(@Nonnull final Path path, @Nonnull final DataSource dataSource) {
        return isPathAllowed(path, null, dataSource);
    }

    /**
     * Indicates if the specified file name is valid.
     */
    public boolean isValidFileName(@Nonnull final String fileName) {
        return FILENAME_REGEX.matcher(fileName).matches() && fileName.chars().noneMatch(Character::isIdentifierIgnorable);
    }

    /**
     * Determines if the specified path is allowed for the specified data set and data source.
     */
    private boolean isPathAllowed(@Nonnull final Path path, @Nullable final String dataSetId, @Nonnull final DataSource dataSource) {
        final Optional<List<String>> dataSourcePaths = DataSourceUtil.getPaths(dataSource);
        if (dataSourcePaths.isPresent()) {
            final Stream<String> allowedPaths = dataSourcePaths.get().stream();
            final String pluginId = dataSource.getConnector().getPluginId();
            final Optional<ConnectorPlugin> plugin = this.pluginManager.getPlugin(pluginId);

            if (plugin.isPresent()) {
                if (ConnectorUtil.hasAnyTabSref(plugin.get().getDescriptor(), fileSystemSrefs)) {
                    return isPathAllowed(path.toUri(), toURIs(allowedPaths));
                }
                if (dataSetId != null && ConnectorUtil.hasAnyTabSref(plugin.get().getDescriptor(), uploadSrefs)) {
                    final Stream<String> uploadPaths = allowedPaths
                                    .map(allowedPath -> allowedPath.endsWith(Path.SEPARATOR) ? allowedPath : allowedPath + Path.SEPARATOR)
                                    .map(allowedPath -> allowedPath + dataSetId + Path.SEPARATOR);
                    return isPathAllowed(path.toUri(), toURIs(uploadPaths));
                }
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * Determines if the specified path matches one of the allowed URIs.
     */
    private boolean isPathAllowed(@Nonnull final URI path, @Nonnull final List<URI> allowedUris) {
        final String scheme = (path.getScheme() != null) ? path.getScheme() : "file";
        final String normalPath = path.normalize().getPath();

        for (final URI allowedUri : allowedUris) {
            final String allowedPath = allowedUri.getPath();
            final String allowedScheme = allowedUri.getScheme() != null ? allowedUri.getScheme() : "file";
            if (scheme.equals(allowedScheme) && (allowedPath == null || allowedPath.equals(normalPath) || normalPath.startsWith(allowedPath))) {
                return true;
            }
        }

        return false;
    }

    /**
     * Converts the specified paths to URIs.
     */
    @Nonnull
    private List<URI> toURIs(@Nonnull final Stream<String> paths) {
        return paths.map(Path::new).map(Path::toUri).collect(Collectors.toList());
    }
}
