package com.thinkbiganalytics.kylo.catalog.file;

/*-
 * #%L
 * kylo-catalog-api
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

import com.thinkbiganalytics.kylo.catalog.rest.model.DataSet;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSetFile;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSource;

import org.apache.hadoop.fs.FileAlreadyExistsException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AccessDeniedException;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Manages browsing and uploading files for data sets.
 */
public interface CatalogFileManager {

    /**
     * Supply a function to act upon the input stream
     *
     * @param dataSet      the dataset to read
     * @param readFunction the function to apply
     * @param <R>          the result
     * @return the result
     */
    <R> R readDataSetInputStream(@Nonnull final DataSet dataSet, @Nonnull final FileSystemReadFunction<R> readFunction) throws IOException;

    /**
     * Read a dataset
     *
     * @param dataSet  the dataset
     * @param function the function to apply
     * @param <R>      the result
     */
    <R> R readDataSet(@Nonnull final DataSet dataSet, @Nonnull final FileSystemFunction<R> function) throws IOException;

    /**
     * Creates a file in the specified dataset from an uploaded file.
     *
     * @param dataSet  the data set
     * @param fileName the file name
     * @param in       the file input stream
     * @return the uploaded file
     * @throws FileAlreadyExistsException if a file with the same name already exists
     * @throws IllegalArgumentException   if the fileName is invalid
     * @throws IOException                if an I/O error occurs creating the file
     */
    @Nonnull
    DataSetFile createUpload(@Nonnull final DataSet dataSet, @Nonnull final String fileName, @Nonnull final InputStream in) throws IOException;

    /**
     * Deletes the uploaded file with the specified name.
     *
     * @param dataSet  the data set
     * @param fileName the file name
     * @throws IllegalArgumentException if the fileName is invalid
     * @throws IOException              if an I/O error occurs when deleting the file
     */
    void deleteUpload(@Nonnull final DataSet dataSet, @Nonnull final String fileName) throws IOException;

    /**
     * Lists the files at the specified URI for the specified data set.
     *
     * @param path       directory for listing files
     * @param dataSource data source
     * @return files and directories at the URI
     * @throws AccessDeniedException if the URI is not allowed for the data set
     * @throws IOException           if an I/O error occurs when listing files
     */
    @Nonnull
    List<DataSetFile> listFiles(@Nonnull final String path, @Nonnull final DataSource dataSource) throws IOException;

    /**
     * Lists files that have been uploaded for the specified dataset.
     *
     * @param dataSet the data set
     * @return the uploaded files
     * @throws IllegalArgumentException if the dataSetId is invalid
     * @throws IOException              if an I/O error occurs when accessing the files
     */
    @Nonnull
    List<DataSetFile> listUploads(@Nonnull final DataSet dataSet) throws IOException;
}
