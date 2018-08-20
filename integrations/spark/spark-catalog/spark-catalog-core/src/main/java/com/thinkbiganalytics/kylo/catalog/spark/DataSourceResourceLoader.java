package com.thinkbiganalytics.kylo.catalog.spark;

/*-
 * #%L
 * Kylo Catalog Core
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

import com.google.common.base.Optional;
import com.thinkbiganalytics.kylo.hadoop.FileSystemUtil;
import com.thinkbiganalytics.kylo.protocol.hadoop.Handler;
import com.thinkbiganalytics.kylo.util.HadoopClassLoader;
import com.thinkbiganalytics.kylo.util.IsolatedServiceLoader;

import org.apache.commons.io.FilenameUtils;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.sources.DataSourceRegister;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Manages file and jar resources required by data sources.
 */
public class DataSourceResourceLoader extends HadoopClassLoader {

    private static final XLogger log = XLoggerFactory.getXLogger(DataSourceResourceLoader.class);

    /**
     * Creates a new {@code DataSourceResourceLoader} using the specified URL handler and Spark context.
     *
     * @param sparkContext Spark context
     */
    static DataSourceResourceLoader create(@Nonnull final SparkContext sparkContext) {
        Handler.setConfiguration(sparkContext.hadoopConfiguration());
        Handler.register();

        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        final ClassLoader parentClassLoader = (contextClassLoader != null) ? contextClassLoader : DataSourceResourceLoader.class.getClassLoader();
        return new DataSourceResourceLoader(sparkContext, parentClassLoader);
    }

    /**
     * Service loader for {@link DataSourceRegister} providers
     */
    private final IsolatedServiceLoader<DataSourceRegister> dataSourceLoader = new IsolatedServiceLoader<>(DataSourceRegister.class, this);

    /**
     * File paths that have been added
     */
    @Nonnull
    private final Set<String> files = new HashSet<>();

    /**
     * Spark context
     */
    @Nonnull
    private final SparkContext sparkContext;

    /**
     * Constructs a {@code DataSourceResourceLoader}.
     *
     * @param sparkContext Spark context
     * @param parent       parent class loader
     */
    public DataSourceResourceLoader(@Nonnull final SparkContext sparkContext, @Nonnull final ClassLoader parent) {
        super(sparkContext.hadoopConfiguration(), parent);
        this.sparkContext = sparkContext;
    }

    /**
     * Add a file to be included in all Spark jobs.
     *
     * @param path a local file, a Hadoop file, or a web URL
     */
    public void addFile(@Nullable final String path) {
        // Ignore null file
        if (path == null) {
            log.debug("Ignoring null file");
            return;
        }

        // Ignore duplicate file
        final String name = FilenameUtils.getName(path);
        if (files.contains(name)) {
            log.debug("Ignoring existing file: {}", path);
            return;
        }

        // Ignore invalid file
        URI uri;
        try {
            uri = URI.create(path);
            if (uri.getScheme() == null) {
                uri = URI.create("file:///").resolve(new File("").getAbsolutePath() + "/").resolve(path);
            }
        } catch (final IllegalArgumentException e) {
            log.debug("Ignoring invalid path: {}", path, e);
            return;
        }

        // Ignore missing file
        try {
            if (!FileSystemUtil.fileExists(uri, sparkContext.hadoopConfiguration())) {
                log.debug("Ignoring missing file: {}", path);
                return;
            }
        } catch (final IOException e) {
            log.debug("Failed to determine if file [{}] exists: {}", path, e, e);
        }

        // Add file to Spark context
        log.debug("Updating Spark files with: {}", path);
        sparkContext.addFile(path);
        files.add(name);
    }

    /**
     * Gets the data source with the specified name.
     *
     * <p>The algorithm mirrors Spark's {@code DataSource} class to ensure that Spark data sources can be loaded in the same way.</p>
     *
     * @param shortName data source name, case insensitive
     * @return the data source, if found
     * @throws IllegalArgumentException if multiple data sources match the name
     */
    @Nonnull
    public Optional<DataSourceRegister> getDataSource(@Nonnull final String shortName) {
        // Find matching data sources
        final List<DataSourceRegister> matches = new ArrayList<>(1);
        for (final DataSourceRegister dataSource : dataSourceLoader) {
            if (dataSource.shortName().equalsIgnoreCase(shortName)) {
                matches.add(dataSource);
            }
        }

        // Return matches
        if (matches.size() == 1) {
            return Optional.of(matches.get(0));
        } else if (matches.isEmpty()) {
            return Optional.absent();
        } else {
            if (log.isDebugEnabled()) {
                final List<String> matchClasses = new ArrayList<>(matches.size());
                for (final DataSourceRegister dataSource : matches) {
                    matchClasses.add(dataSource.getClass().getName());
                }
                log.debug("Multiple sources found for data source [{}]: {}", shortName, matchClasses);
            }

            throw new IllegalArgumentException("Multiple sources found for " + shortName + ", please specify the fully qualified class name.");
        }
    }

    /**
     * Executes the specified callable with the Thread context class loader set to this class.
     */
    public <T> T runWithThreadContext(@Nonnull final Callable<T> callable) throws Exception {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this);

        try {
            return callable.call();
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    /**
     * Executes the specified runnable with the Thread context class loader set to this class.
     */
    public void runWithThreadContext(@Nonnull final Runnable runnable) {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this);

        try {
            runnable.run();
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    @Override
    protected boolean addURL(@Nullable final String path, final boolean reload) {
        if (path != null) {
            final URL url = SparkUtil.parseUrl(path);
            return addURL(url, reload);
        } else {
            log.debug("Ignoring null jar url");
            return false;
        }
    }

    @Override
    @SuppressWarnings("squid:S2259")  // use IntelliJ check for null values instead of FindBugs
    protected boolean addURL(@Nullable URL url, boolean reload) {
        if (super.addURL(url, reload)) {
            try {
                final URI uri = url.toURI();
                sparkContext.addJar("hadoop".equals(uri.getScheme()) ? uri.getSchemeSpecificPart() : uri.toString());
            } catch (final URISyntaxException e) {
                log.debug("Unable to convert URI to URL: {}", url, e);
                sparkContext.addJar(url.toString());
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void reload() {
        super.reload();

        log.debug("Reloading data sources");
        if (dataSourceLoader.update()) {
            log.debug("New data sources found");
        }
    }
}
