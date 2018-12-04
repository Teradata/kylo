package com.thinkbiganalytics.kylo.catalog.file;

/*-
 * #%L
 * kylo-catalog-core
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics
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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.thinkbiganalytics.kylo.catalog.dataset.DataSetUtil;
import com.thinkbiganalytics.kylo.catalog.datasource.DataSourceUtil;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSet;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSetFile;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSetTemplate;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSource;
import com.thinkbiganalytics.kylo.catalog.spi.FileSystemProvider;
import com.thinkbiganalytics.kylo.util.HadoopClassLoader;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AccessDeniedException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Manages browsing and uploading files for data sets.
 */
@Component
public class DefaultCatalogFileManager implements CatalogFileManager {

    private static final Logger log = LoggerFactory.getLogger(DefaultCatalogFileManager.class);

    /**
     * Hadoop configuration with default values
     */
    @Nonnull
    private final Configuration defaultConf;

    /**
     * File system providers for listing buckets (or hosts) of Hadoop-compatible file systems
     */
    @Nullable
    private List<FileSystemProvider> fileSystemProviders;

    /**
     * Default group for uploaded files
     */
    @Nullable
    private String groupname;

    /**
     * Validates data set paths
     */
    @Nonnull
    private PathValidator pathValidator;

    /**
     * Default permissions for uploaded files
     */
    @Nullable
    private FsPermission permission;

    /**
     * Default owner for uploaded files
     */
    @Nullable
    private String username;

    /**
     * Constructs a {@code CatalogFileManager} using the specified Kylo data directory.
     */
    @Autowired
    public DefaultCatalogFileManager(@Nonnull final PathValidator pathValidator) {
        this.pathValidator = pathValidator;

        defaultConf = new Configuration();
        defaultConf.size();  // causes defaults to be loaded
        defaultConf.set(FileSystem.FS_DEFAULT_NAME_KEY, "file:///");  // Spark uses file:/// as default FileSystem
    }

    /**
     * Sets the file system providers to use for listing files.
     */
    @Autowired(required = false)
    public void setFileSystemProviders(@Nullable final List<FileSystemProvider> fileSystemProviders) {
        this.fileSystemProviders = fileSystemProviders;
    }

    /**
     * Sets the default group for uploaded files.
     */
    @Value("${catalog.uploads.group:#{null}}")
    public void setGroupname(@Nullable final String groupname) {
        this.groupname = groupname;
    }

    /**
     * Sets the default permissions for uploaded files.
     */
    @Value("${catalog.uploads.permission:#{null}}")
    public void setPermission(@Nullable final String permission) {
        this.permission = (permission != null) ? new FsPermission(permission) : null;
    }

    /**
     * Sets the default owner for upload files.
     */
    @Value("${catalog.uploads.owner:#{null}}")
    public void setUsername(@Nullable final String username) {
        this.username = username;
    }

    @Override
    public <R> R readDataSetInputStream(@Nonnull final DataSet dataSet, @Nonnull final FileSystemReadFunction<R> readFunction) throws IOException {
        final Path path = new Path(dataSet.getPaths().get(0));
        return readDataSet(dataSet, fs -> {
            InputStream in = null;
            try {
                in = fs.open(path);
                return readFunction.apply(in);
            } finally {
                IOUtils.closeQuietly(in);
            }
        });
    }

    @Override
    public <R> R readDataSet(@Nonnull final DataSet dataSet, @Nonnull final FileSystemFunction<R> function) throws IOException {
        final Path path = new Path(dataSet.getPaths().get(0));

        return isolatedFunction(dataSet, path, fs -> {
            return function.apply(fs);
        });

    }

    @Nonnull
    @Override
    public DataSetFile createUpload(@Nonnull final DataSet dataSet, @Nonnull final String fileName, @Nonnull final InputStream in) throws IOException {
        final Path path = getUploadPath(dataSet, fileName);
        final List<DataSetFile> files = isolatedFunction(dataSet, path, fs -> {
            log.debug("Creating file [{}] for dataset {}", fileName, dataSet.getId());
            try (final FSDataOutputStream out = fs.create(path, false)) {
                IOUtils.copyLarge(in, out);
            }

            if (username != null || groupname != null) {
                log.debug("Changing owner of [{}] to {}:{}", path, username, groupname);
                fs.setOwner(path, username, groupname);
            }
            if (permission != null) {
                log.debug("Setting permissions of [{}] to {}", path, permission);
                fs.setPermission(path, permission);
            }

            return listFiles(fs, path);
        });

        if (files.size() == 1) {
            return files.get(0);
        } else {
            log.error("Failed to upload file for dataset {} at path: {}. Expected 1 file but found {} files.", dataSet.getId(), path, files.size());
            throw new IOException("Uploaded file not found");
        }
    }

    @Override
    public void deleteUpload(@Nonnull final DataSet dataSet, @Nonnull final String fileName) throws IOException {
        final Path path = getUploadPath(dataSet, fileName);
        if (!isolatedFunction(dataSet, path, fs -> fs.delete(path, false))) {
            log.info("Delete unsuccessful for path: {}", path);
            throw new IOException("Failed to delete: " + dataSet.getId() + Path.SEPARATOR + fileName);
        }
    }

    @Nonnull
    @Override
    public List<DataSetFile> listFiles(@Nonnull final String pathString, @Nonnull final DataSource dataSource) throws IOException {
        final Path path = new Path(pathString);
        if (pathValidator.isPathAllowed(path, dataSource)) {
            if (fileSystemProviders != null) {
                for (final FileSystemProvider fileSystemProvider : fileSystemProviders) {
                    if (fileSystemProvider.supportsPath(path)) {
                        final Configuration conf = DataSetUtil.getConfiguration(DataSourceUtil.mergeTemplates(dataSource), defaultConf);
                        return fileSystemProvider.listFiles(path, conf);
                    }
                }
            }
            return isolatedFunction(dataSource, path, fs -> listFiles(fs, path));
        } else {
            log.info("Datasource {} does not allow access to path: {}", dataSource.getId(), path);
            throw new AccessDeniedException("Access to path [{}] is restricted: " + path);
        }
    }

    @Nonnull
    @Override
    public List<DataSetFile> listUploads(@Nonnull final DataSet dataSet) throws IOException {
        final Path path = getUploadPath(dataSet);
        try {
            return isolatedFunction(dataSet, path, fs -> listFiles(fs, path));
        } catch (final FileNotFoundException e) {
            log.debug("Dataset directory does not exist: {}", path);
            return Collections.emptyList();
        }
    }

    /**
     * Gets the upload storage path for the specified data set.
     *
     * @throws IllegalArgumentException if the upload path cannot be determined
     */
    @Nonnull
    private Path getUploadPath(@Nonnull final DataSet dataSet) {
        final List<String> paths = DataSourceUtil.getPaths(dataSet.getDataSource()).orElseGet(Collections::emptyList);
        if (paths.size() == 1) {
            return new Path(paths.get(0), dataSet.getId());
        } else {
            log.error("Unable to determine upload path for dataset: {}", dataSet.getId());
            throw new IllegalArgumentException("Connector or data source must specify the upload path");
        }
    }

    /**
     * Gets the path for the specified uploaded file.
     *
     * @throws IllegalArgumentException if the filename is invalid
     */
    @Nonnull
    private Path getUploadPath(@Nonnull final DataSet dataSet, @Nonnull final String fileName) {
        Preconditions.checkArgument(pathValidator.isValidFileName(fileName), "Invalid filename");
        return new Path(getUploadPath(dataSet), fileName);
    }

    /**
     * Executes the specified function in a separate class loader containing the jars of the specified data set.
     */
    private <R> R isolatedFunction(@Nonnull final DataSet dataSet, @Nonnull final Path path, @Nonnull final FileSystemFunction<R> function) throws IOException {
        return isolatedFunction(DataSetUtil.mergeTemplates(dataSet), path, function);
    }

    /**
     * Executes the specified function in a separate class loader containing the jars of the specified data source.
     */
    private <R> R isolatedFunction(@Nonnull final DataSource dataSource, @Nonnull final Path path, @Nonnull final FileSystemFunction<R> function) throws IOException {
        return isolatedFunction(DataSourceUtil.mergeTemplates(dataSource), path, function);
    }

    /**
     * Executes the specified function in a separate class loader containing the jars of the specified template.
     */
    @VisibleForTesting
    protected <R> R isolatedFunction(@Nonnull final DataSetTemplate template, @Nonnull final Path path, @Nonnull final FileSystemFunction<R> function) throws IOException {
        final Configuration conf = DataSetUtil.getConfiguration(template, defaultConf);
        try (final HadoopClassLoader classLoader = new HadoopClassLoader(conf)) {
            if (template.getJars() != null) {
                log.debug("Adding jars to HadoopClassLoader: {}", template.getJars());
                classLoader.addJars(template.getJars());
            }

            log.debug("Creating FileSystem from path: {}", path);
            try (final FileSystem fs = FileSystem.newInstance(path.toUri(), conf)) {
                return function.apply(fs);
            }
        }
    }

    /**
     * Lists the files at the specified path.
     */
    @Nonnull
    private List<DataSetFile> listFiles(@Nonnull final FileSystem fs, @Nonnull final Path path) throws IOException {
        return Arrays.stream(fs.listStatus(path))
            .map(status -> {
                final DataSetFile file = new DataSetFile();
                file.setDirectory(status.isDirectory());
                file.setLength(status.getLen());
                file.setModificationTime(status.getModificationTime());
                file.setName(status.getPath().getName());
                file.setPath(status.getPath().toString());
                return file;
            })
            .collect(Collectors.toList());
    }
}
