package com.thinkbiganalytics.kylo.util;

/*-
 * #%L
 * kylo-commons-hadoop
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

import com.thinkbiganalytics.kylo.hadoop.FileSystemUtil;
import com.thinkbiganalytics.kylo.protocol.hadoop.Handler;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.jetbrains.annotations.Contract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A {@code ClassLoader} that supports JAR files on Hadoop file systems.
 *
 * <p>NOTE: Requires the Hadoop protocol {@link Handler} to be registered first.</p>
 */
public class HadoopClassLoader extends URLClassLoader {

    private static final Logger log = LoggerFactory.getLogger(HadoopClassLoader.class);

    /**
     * Hadoop configuration
     */
    @Nonnull
    private final Configuration conf;

    /**
     * Service loaded for {@link FileSystem} providers
     */
    @Nonnull
    private final IsolatedServiceLoader<FileSystem> fileSystemLoader = new IsolatedServiceLoader<>(FileSystem.class, this);

    /**
     * Jar filenames that have been added
     */
    @Nonnull
    private final Set<String> jars = new HashSet<>();

    /**
     * Constructs a {@code HadoopClassLoader} using the default delegation parent {@code ClassLoader}.
     */
    public HadoopClassLoader(@Nonnull final Configuration conf) {
        super(new URL[0]);
        this.conf = conf;
    }

    /**
     * Constructs a {@code HadoopClassLoader} using the specified parent {@code ClassLoader}.
     *
     * @param conf   Hadoop configuration
     * @param parent parent class loader
     */
    public HadoopClassLoader(@Nonnull final Configuration conf, @Nonnull final ClassLoader parent) {
        super(new URL[0], parent);
        this.conf = conf;
    }

    /**
     * Add a JAR dependency containing data source classes or their dependencies.
     *
     * @param path a local file, a Hadoop file, or a web URL
     * @return {@code true} if the jar was added, or {@code false} if the jar was ignored
     * @throws IllegalArgumentException if the {@code path} is not a valid URL
     */
    public boolean addJar(@Nullable final String path) {
        return addURL(path, true);
    }

    /**
     * Adds JAR dependencies containing data source classes and their dependencies.
     *
     * @param paths local files, Hadoop files, or web URLs
     * @return {@code true} if at least one jar was added, or {@code false} if they were all ignored
     * @throws IllegalArgumentException if a path is not a valid URL
     */
    public boolean addJars(@Nullable final List<String> paths) {
        boolean shouldReload = false;

        if (paths != null) {
            for (final String path : paths) {
                shouldReload |= addURL(path, false);
            }
        }

        if (shouldReload) {
            reload();
        }

        return shouldReload;
    }

    @Override
    protected final void addURL(@Nullable final URL url) {
        addURL(url, true);
    }

    /**
     * Adds the specified class or resource URL to this class loader.
     *
     * <p>Ignores paths that are {@code null} or do not exist.</p>
     *
     * <p>Subclasses may override this method to provide custom conversions of strings to URLs.</p>
     *
     * @param path   a local file, a Hadoop file, or a web URL
     * @param reload {@code true} to reload service loaders
     * @return {@code true} if the URL was added, or {@code false} if the URL was ignored
     * @throws IllegalArgumentException if the {@code path} is not a valid URL
     */
    @Contract("null, _ -> false")
    protected boolean addURL(@Nullable final String path, final boolean reload) {
        if (StringUtils.isNotEmpty(path)) {
            final URL url = FileSystemUtil.parseUrl(path, conf);
            return addURL(url, reload);
        } else {
            log.debug("Ignoring null jar url");
            return false;
        }
    }

    /**
     * Appends the specified URL to the list of URLs to search for classes and resources.
     *
     * <p>Ignores paths that are {@code null} or do not exist.</p>
     *
     * @param url    a Java URL with a registered protocol handler
     * @param reload {@code true} to reload service loaders
     * @return {@code true} if the URL was added, or {@code false} if the URL was ignored
     */
    @Contract("null, _ -> false")
    protected boolean addURL(@Nullable final URL url, final boolean reload) {
        // Ignore null urls
        if (url == null) {
            log.debug("Ignoring null jar url");
            return false;
        }

        // Verify that the jar file exists
        try {
            if (!FileSystemUtil.fileExists(url.toURI(), conf)) {
                log.debug("Ignoring jar url that doesn't exist: {}", url);
                return false;
            }
        } catch (final Exception e) {
            log.debug("Unable to determine if jar url [{}} exists: {}", url, e, e);
        }

        // Add jar url to the class path
        final String name = FilenameUtils.getName(FilenameUtils.normalizeNoEndSeparator(url.getPath()));

        if (!jars.contains(name)) {
            log.debug("Adding jar url to class path: {}", url);
            super.addURL(url);
            jars.add(name);

            if (reload) {
                reload();
            }

            return true;
        } else {
            log.debug("Ignoring existing jar url: {}", url);
            return false;
        }
    }

    /**
     * Detects new service classes and reloads service loaders if necessary.
     */
    protected void reload() {
        log.debug("Reloading services");

        if (fileSystemLoader.update()) {
            log.debug("New file systems found");
            conf.setClassLoader(this);
            FileSystemUtil.registerFileSystems(fileSystemLoader, conf);
        }
    }
}
