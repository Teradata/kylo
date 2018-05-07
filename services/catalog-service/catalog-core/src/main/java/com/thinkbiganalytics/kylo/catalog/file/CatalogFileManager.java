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
import com.thinkbiganalytics.kylo.catalog.DataSetNotFound;
import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogConstants;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSet;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSetFile;
import com.thinkbiganalytics.kylo.util.HadoopClassLoader;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileAlreadyExistsException;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Manages browsing and uploading files for data sets.
 */
@Component
public class CatalogFileManager {

    private static final Logger log = LoggerFactory.getLogger(CatalogFileManager.class);

    /**
     * Hadoop configuration with default values
     */
    @Nonnull
    private final Configuration defaultConf;

    /**
     * Root path for Kylo data directory
     */
    @Nonnull
    private final Path dataSetsRoot;

    /**
     * Default group for uploaded files
     */
    @Nullable
    private String groupname;

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
    public CatalogFileManager(@Nonnull @Value("${config.kylo.datasets.root}") final String dataRoot) {
        this.dataSetsRoot = new Path(dataRoot);

        defaultConf = new Configuration();
        defaultConf.size();  // causes defaults to be loaded
        defaultConf.set(FileSystem.FS_DEFAULT_NAME_KEY, "file:///");  // Spark uses file:/// as default FileSystem
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

    /**
     * Creates a file in the specified dataset from an uploaded file.
     *
     * @param dataSetId the dataset id
     * @param fileName  the file name
     * @param in        the file input stream
     * @return the uploaded file
     * @throws DataSetNotFound            if the dataset does not exist
     * @throws FileAlreadyExistsException if a file with the same name already exists
     * @throws IllegalArgumentException   if the fileName is invalid
     * @throws IOException                if an I/O error occurs creating the file
     */
    @Nonnull
    public DataSetFile createUpload(@Nonnull final String dataSetId, @Nonnull final String fileName, @Nonnull final InputStream in) throws IOException {
        final DataSet dataSet = getDataSet(dataSetId).orElseThrow(DataSetNotFound::new);
        final Path path = getUploadPath(dataSet.getId(), fileName);
        final List<DataSetFile> files = isolatedFunction(dataSet, path, fs -> {
            log.debug("Creating file [{}] for dataset {}", fileName, dataSetId);
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

    /**
     * Deletes the uploaded file with the specified name.
     *
     * @param dataSetId the dataset id
     * @param fileName  the file name
     * @throws DataSetNotFound          if the dataset does not exist
     * @throws IllegalArgumentException if the fileName is invalid
     * @throws IOException              if an I/O error occurs when deleting the file
     */
    public void deleteUpload(@Nonnull final String dataSetId, @Nonnull final String fileName) throws IOException {
        final DataSet dataSet = getDataSet(dataSetId).orElseThrow(DataSetNotFound::new);
        final Path path = getUploadPath(dataSet.getId(), fileName);
        if (!isolatedFunction(dataSet, path, fs -> fs.delete(path, false))) {
            log.debug("Delete unsuccessful for path: {}", path);
            throw new IOException("Failed to delete: " + dataSet.getId() + Path.SEPARATOR + fileName);
        }
    }

    /**
     * Lists files that have been uploaded for the specified dataset.
     *
     * @param dataSetId the dataset id
     * @return the uploaded files
     * @throws DataSetNotFound          if the dataset does not exist
     * @throws IllegalArgumentException if the dataSetId is invalid
     * @throws IOException              if an I/O error occurs when accessing the files
     */
    @Nonnull
    public List<DataSetFile> listUploads(@Nonnull final String dataSetId) throws IOException {
        final DataSet dataSet = getDataSet(dataSetId).orElseThrow(DataSetNotFound::new);
        final Path path = getUploadPath(dataSet.getId());

        try {
            return isolatedFunction(dataSet, path, fs -> listFiles(fs, path));
        } catch (final FileNotFoundException e) {
            log.debug("Dataset directory does not exist: {}", path);
            return Collections.emptyList();
        }
    }

    /**
     * Gets the data set with the specified id.
     */
    @Nonnull
    private Optional<DataSet> getDataSet(@Nonnull final String dataSetId) {
        final DataSet dataSet = new DataSet();
        dataSet.setId(dataSetId);
        return Optional.of(dataSet);
    }

    /**
     * Gets the upload storage path for the specified data set.
     */
    @Nonnull
    private Path getUploadPath(@Nonnull final String dataSetId) {
        Preconditions.checkArgument(dataSetId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"), "Invalid dataset id");
        return new Path(dataSetsRoot, dataSetId);
    }

    /**
     * Gets the path for the specified uploaded file.
     */
    @Nonnull
    private Path getUploadPath(@Nonnull final String dataSetId, @Nonnull final String fileName) {
        Preconditions.checkArgument(!fileName.matches("|\\.|\\.\\.|.*[/:].*"), "Invalid filename");
        Preconditions.checkArgument(fileName.chars().noneMatch(Character::isIdentifierIgnorable), "Invalid filename");
        return new Path(getUploadPath(dataSetId), fileName);
    }

    /**
     * Executes the specified function in a separate class loader containing the jars of the specified data set.
     */
    @VisibleForTesting
    protected <R> R isolatedFunction(@Nonnull final DataSet dataSet, @Nonnull final Path path, @Nonnull final FileSystemFunction<R> function) throws IOException {
        // Create configuration with dataset options
        final Configuration conf = new Configuration(defaultConf);
        if (dataSet.getOptions() != null) {
            log.debug("Creating Hadoop configuration with options: {}", dataSet.getOptions());
            dataSet.getOptions().entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(KyloCatalogConstants.HADOOP_CONF_PREFIX))
                .forEach(entry -> conf.set(entry.getKey(), entry.getValue().substring(KyloCatalogConstants.HADOOP_CONF_PREFIX.length())));
        }

        // Run function in separate class loader
        try (final HadoopClassLoader classLoader = new HadoopClassLoader(conf)) {
            if (dataSet.getJars() != null) {
                log.debug("Adding jars to HadoopClassLoader: {}", dataSet.getJars());
                classLoader.addJars(dataSet.getJars());
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
