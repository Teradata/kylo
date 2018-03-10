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

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.spark.SparkContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Manages file and jar resources required by data sources.
 */
class DataSourceResourceLoader extends URLClassLoader {

    private static final Logger log = LoggerFactory.getLogger(DataSourceResourceLoader.class);

    /**
     * Path to the services file for {@link FileSystem} classes.
     */
    private static final String FILE_SYSTEM_SERVICES = "META-INF/services/" + FileSystem.class.getName();

    /**
     * Creates a new {@code DataSourceResourceLoader} using the specified URL handler and Spark context.
     *
     * @param urlHandler   provides access to JAR files in Hadoop
     * @param sparkContext Spark context
     */
    static DataSourceResourceLoader create(@Nullable final URLStreamHandlerFactory urlHandler, @Nonnull final SparkContext sparkContext) {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        final ClassLoader parentClassLoader = (contextClassLoader != null) ? contextClassLoader : DataSourceResourceLoader.class.getClassLoader();
        return new DataSourceResourceLoader(urlHandler, sparkContext, parentClassLoader);
    }

    /**
     * File paths that have been added
     */
    @Nonnull
    private final Set<String> files = new HashSet<>();

    /**
     * Service loaded for {@link FileSystem} classes
     */
    @Nonnull
    private final ServiceLoader<FileSystem> fileSystemLoader = ServiceLoader.load(FileSystem.class, this);

    /**
     * Paths to {@link FileSystem} services files that have been loaded
     */
    @Nonnull
    private final Set<String> fileSystemServices = new HashSet<>();

    /**
     * Jar paths that have been added
     */
    @Nonnull
    private final Set<String> jars = new HashSet<>();

    /**
     * Spark context
     */
    @Nonnull
    private final SparkContext sparkContext;

    /**
     * Provides access to JAR files in Hadoop
     */
    @Nullable
    private final URLStreamHandlerFactory urlHandler;

    /**
     * Constructs a {@code DataSourceResourceLoader}.
     *
     * @param urlHandler   provides access to JAR files in Hadoop
     * @param sparkContext Spark context
     * @param parent       parent class loader
     */
    private DataSourceResourceLoader(@Nullable final URLStreamHandlerFactory urlHandler, @Nonnull final SparkContext sparkContext, @Nonnull final ClassLoader parent) {
        super(new URL[0], parent);
        this.sparkContext = sparkContext;
        this.urlHandler = urlHandler;
    }

    /**
     * Add a file to be included in all Spark jobs.
     *
     * @param path a local file, a Hadoop file, or a web URL
     */
    public void addFile(@Nullable final String path) {
        // Ignore null paths
        if (path == null) {
            return;
        }

        // Add path to Spark
        if (!files.contains(path)) {
            sparkContext.addFile(path);
            files.add(path);
        } else {
            log.debug("Skipping existing addFile path: {}", path);
        }
    }

    /**
     * Add a JAR dependency containing data source classes or their dependencies.
     *
     * @param path a local file, a Hadoop file, a web URL, or local:/path (for a file on every worker node)
     * @throws IllegalArgumentException if the {@code path} is not a valid URL
     */
    public void addJar(@Nullable final String path) {
        if (path != null) {
            addJars(Collections.singletonList(path));
        }
    }

    /**
     * Adds JAR dependencies containing data source classes and their dependencies.
     *
     * @param paths local files, Hadoop files, web URLs, or local:/path (for files on every worker node)
     * @throws IllegalArgumentException if a path is not a valid URL
     */
    public void addJars(@Nullable final List<String> paths) {
        // Ignore null paths
        if (paths == null) {
            return;
        }

        // Add paths to Spark and class loader
        for (final String path : paths) {
            if (!jars.contains(path)) {
                addURL(path, false);
                sparkContext.addJar(path);
                jars.add(path);
            } else {
                log.debug("Skipping existing addJar path: {}", path);
            }
        }

        // Reload
        reloadFileSystems();
    }

    @Nullable
    @Override
    public URL getResource(@Nonnull final String name) {
        if (FILE_SYSTEM_SERVICES.equals(name)) {
            return findResource(name);
        } else {
            return super.getResource(name);
        }
    }

    @Nonnull
    @Override
    public Enumeration<URL> getResources(@Nonnull final String name) throws IOException {
        if (FILE_SYSTEM_SERVICES.equals(name)) {
            return findResources(name);
        } else {
            return super.getResources(name);
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
    protected void addURL(@Nullable final URL url) {
        addURL(url, true);
    }

    /**
     * Adds the specified class or resource URL to this class loader.
     *
     * @throws IllegalArgumentException if the {@code path} is not a valid URL
     */
    private void addURL(@Nullable final String path, final boolean reload) {
        // Ignore null paths
        if (path == null) {
            return;
        }

        // Parse as a URI
        final URI uri = URI.create(path);

        // Determine the absolute URL
        final URLStreamHandler handler;
        final String protocol = uri.getScheme();
        final String spec;

        if (protocol == null) {
            final URI fullUri = FileSystem.getDefaultUri(sparkContext.hadoopConfiguration()).resolve(uri);
            handler = (urlHandler != null) ? urlHandler.createURLStreamHandler(fullUri.getScheme()) : null;
            spec = fullUri.toString();
        } else if ("file".equals(protocol) || "local".equals(protocol)) {
            handler = null;
            spec = "file:" + uri.getSchemeSpecificPart();
        } else if ("http".equals(protocol) || "https".equals(protocol)) {
            handler = null;
            spec = path;
        } else {
            handler = (urlHandler != null) ? urlHandler.createURLStreamHandler(protocol) : null;
            spec = path;
        }

        // Add the URL
        try {
            final URL url = new URL(null, spec, handler);
            addURL(url, reload);
        } catch (final MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Appends the specified URL to the list of URLs to search for classes and resources.
     */
    private void addURL(@Nullable final URL url, final boolean reload) {
        if (url != null) {
            super.addURL(url);
            if (reload) {
                reloadFileSystems();
            }
        }
    }

    /**
     * Adds Hadoop {@link FileSystem} classes to the Hadoop configuration.
     */
    private void reloadFileSystems() {
        // Find all services
        final Enumeration<URL> urls;
        try {
            urls = getResources(FILE_SYSTEM_SERVICES);
        } catch (final IOException e) {
            log.warn("Unable to find FileSystem services", e);
            return;
        }

        // Determine if new services were added
        final List<String> resources = new ArrayList<>();
        while (urls.hasMoreElements()) {
            resources.add(urls.nextElement().toString());
        }

        if (fileSystemServices.containsAll(resources)) {
            return;
        }

        // Reload all services
        fileSystemLoader.reload();

        final Configuration conf = sparkContext.hadoopConfiguration();
        conf.setClassLoader(this);

        for (final FileSystem fs : fileSystemLoader) {
            conf.setClass("fs." + fs.getScheme() + ".impl", fs.getClass(), FileSystem.class);
        }

        // Add new resources
        fileSystemServices.addAll(resources);
    }
}
