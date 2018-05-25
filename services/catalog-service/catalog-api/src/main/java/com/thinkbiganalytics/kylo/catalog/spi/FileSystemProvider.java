package com.thinkbiganalytics.kylo.catalog.spi;

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

import com.thinkbiganalytics.kylo.catalog.CatalogException;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSetFile;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Service provider class for Hadoop file systems. Provides additional methods beyond what it typically provided by {@link FileSystem} implementations.
 */
public interface FileSystemProvider {

    /**
     * List the files/directories in the given path if the path is a directory.
     *
     * <p>Does not guarantee to return the list of files/directories in sorted order.</p>
     *
     * @param path directory path
     * @param conf Hadoop configuration
     * @return files and directories
     * @throws CatalogException if an error occurs that can be corrected by the user
     * @throws RuntimeException if an I/O error occurs
     */
    @Nonnull
    List<DataSetFile> listFiles(@Nonnull final Path path, @Nonnull final Configuration conf);

    /**
     * Indicates if this provider can be used for the specified path.
     *
     * <p>Typically this only checks that the scheme is supported.</p>
     *
     * @param path Hadoop file system path
     * @return {@code true} if this provider recognizes the path, or {@code false} otherwise
     */
    boolean supportsPath(@Nonnull final Path path);
}
