package com.thinkbiganalytics.kylo.catalog.file;

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
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSet;

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

/**
 * Validates data set paths.
 */
@Component
public class PathValidator {

    private static final Pattern FILENAME_REGEX = Pattern.compile("^([^\\./:]{1,2}|[^/:]{3,})$");

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
        final Optional<List<String>> dataSourcePaths = DataSourceUtil.getPaths(dataSet.getDataSource());
        if (dataSourcePaths.isPresent()) {
            final Stream<String> allowedPaths = dataSourcePaths.get().stream();
            final Connector connector = dataSet.getDataSource().getConnector();

            if (ConnectorUtil.hasAnyTabSref(connector, fileSystemSrefs)) {
                return isPathAllowed(path.toUri(), toURIs(allowedPaths));
            }
            if (ConnectorUtil.hasAnyTabSref(connector, uploadSrefs)) {
                final Stream<String> uploadPaths = allowedPaths
                    .map(allowedPath -> allowedPath.endsWith(Path.SEPARATOR) ? allowedPath : allowedPath + Path.SEPARATOR)
                    .map(allowedPath -> allowedPath + dataSet.getId() + Path.SEPARATOR);
                return isPathAllowed(path.toUri(), toURIs(uploadPaths));
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
            if (scheme.equals(allowedUri.getScheme()) && (allowedPath == null || allowedPath.equals(normalPath) || normalPath.startsWith(allowedPath))) {
                return true;
            }
        }

        return false;
    }

    /**
     * Indicates if the specified file name is valid.
     */
    public boolean isValidFileName(@Nonnull final String fileName) {
        return FILENAME_REGEX.matcher(fileName).matches() && fileName.chars().noneMatch(Character::isIdentifierIgnorable);
    }

    /**
     * Converts the specified paths to URIs.
     */
    @Nonnull
    private List<URI> toURIs(@Nonnull final Stream<String> paths) {
        return paths.map(Path::new).map(Path::toUri).collect(Collectors.toList());
    }
}
