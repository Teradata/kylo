package com.thinkbiganalytics.kylo.catalog.spark.sources.spark;

/*-
 * #%L
 * Kylo Catalog Core
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

import com.thinkbiganalytics.kylo.catalog.spark.DataSourceResourceLoader;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.spark.Accumulable;
import org.apache.spark.scheduler.JobSucceeded$;
import org.apache.spark.scheduler.SparkListenerJobEnd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import scala.Unit;
import scala.runtime.AbstractFunction1;

/**
 * Deletes Hadoop file system paths.
 */
public class DeletePathsListener extends AbstractFunction1<SparkListenerJobEnd, Unit> {

    private static final Logger log = LoggerFactory.getLogger(DeletePathsListener.class);

    /**
     * Indicates that an attempt to delete the paths has been made
     */
    private boolean closed;

    /**
     * Hadoop configuration
     */
    @Nonnull
    private final Configuration conf;

    /**
     * Indicates that the files have been read and may be deleted
     */
    @Nonnull
    private final Accumulable<Boolean, ?> flag;

    /**
     * Class loader for file systems
     */
    @Nullable
    private final DataSourceResourceLoader loader;

    /**
     * Paths to be deleted
     */
    @Nonnull
    private final List<String> paths;

    /**
     * Constructs a {@code DeletePathsListener} for the specified paths.
     *
     * @param paths paths to be deleted
     * @param flag  indicates when the files may be deleted
     * @param conf  Hadoop configuration
     */
    public DeletePathsListener(@Nonnull final List<String> paths, @Nonnull final Accumulable<Boolean, ?> flag, @Nonnull final Configuration conf) {
        this.paths = paths;
        this.flag = flag;
        this.conf = conf;
        loader = (conf.getClassLoader() instanceof DataSourceResourceLoader) ? (DataSourceResourceLoader) conf.getClassLoader() : null;
    }

    @Override
    public Unit apply(@Nonnull final SparkListenerJobEnd jobEnd) {
        if (!closed && jobEnd.jobResult() instanceof JobSucceeded$ && flag.value() == Boolean.TRUE) {
            closed = true;  // prevents files from being deleted multiple times
            if (loader != null) {
                loader.runWithThreadContext(new Runnable() {
                    @Override
                    public void run() {
                        deletePaths();
                    }
                });
            } else {
                deletePaths();
            }
        }
        return null;
    }

    /**
     * Deletes the paths.
     */
    private void deletePaths() {
        for (final String pathString : paths) {
            try {
                final Path path = new Path(pathString);
                if (!path.getFileSystem(conf).delete(path, false)) {
                    log.warn("Failed to delete path: {}", pathString);
                }
            } catch (final IOException e) {
                log.warn("Failed to delete path: {}", pathString);
            } catch (final Exception e) {
                log.error("Failed to delete path: {}: {}", pathString, e);
            }
        }
    }
}
