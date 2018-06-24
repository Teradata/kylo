package com.thinkbiganalytics.kylo.util;

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

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Loads data source services using {@link ServiceLoader}.
 *
 * <p>Use {@link #update()} to reload services only if new services have been added.</p>
 *
 * @param <S> type of service to be loaded
 */
public class IsolatedServiceLoader<S> implements Iterable<S> {

    private static final XLogger log = XLoggerFactory.getXLogger(IsolatedServiceLoader.class);

    /**
     * Path prefix for provider configuration files
     */
    private static final String SERVICES_PREFIX = "META-INF/services/";

    /**
     * Class loader for finding provider configuration files
     */
    @Nonnull
    private final ClassLoader classLoader;

    /**
     * Paths to provider configuration files that have been loaded
     */
    @Nonnull
    private final Set<String> configLoaded = new HashSet<>();

    /**
     * Interface representing the service
     */
    @Nonnull
    private final Class<S> service;

    /**
     * Service loaded for provider classes
     */
    @Nonnull
    private final ServiceLoader<S> serviceLoader;

    /**
     * Constructs a {@code IsolatedServiceLoader} that loads services of the specified interface.
     *
     * @param service interface representing the service
     * @param loader  class loader for finding provider configuration files
     */
    public IsolatedServiceLoader(@Nonnull final Class<S> service, @Nonnull final URLClassLoader loader) {
        this.service = service;
        classLoader = new IsolatedClassLoader(loader);
        serviceLoader = ServiceLoader.load(service, classLoader);
    }

    @Nonnull
    @Override
    public Iterator<S> iterator() {
        return serviceLoader.iterator();
    }

    @Override
    public String toString() {
        return "IsolatedServiceLoader[" + service.getName() + "]";
    }

    /**
     * Checks if new provider configuration files are available from the class loader, and if so clears the provider cache so that all providers will be reloaded.
     *
     * <p>Subsequent invocations of {@link #iterator()} will lazily locate providers.</p>
     *
     * @return {@code true} if new provider configuration files were found
     */
    public boolean update() {
        log.entry();

        // Find all services
        final Enumeration<URL> urls;
        try {
            urls = classLoader.getResources(SERVICES_PREFIX + service.getName());
        } catch (final IOException e) {
            log.warn("Unable to find {} services", service.getSimpleName(), e);
            return log.exit(false);
        }

        // Determine if services were added
        final Set<String> resources = new HashSet<>();
        while (urls.hasMoreElements()) {
            resources.add(urls.nextElement().toString());
        }

        if (configLoaded.containsAll(resources)) {
            log.debug("No new {} services found", service.getSimpleName());
            return log.exit(false);
        }

        // Reload all services
        serviceLoader.reload();
        configLoaded.addAll(resources);
        return log.exit(true);
    }

    /**
     * A {@code ClassLoader} that limits loading of service files to the immediate parent.
     */
    private static class IsolatedClassLoader extends ClassLoader {

        /**
         * Parent class loader
         */
        @Nonnull
        private final URLClassLoader delegate;

        /**
         * Constructs an {@code IsolatedClassLoader} with the specified parent.
         */
        private IsolatedClassLoader(@Nonnull URLClassLoader delegate) {
            super(delegate);
            this.delegate = delegate;
        }

        @Nullable
        @Override
        public URL getResource(@Nonnull final String name) {
            if (name.startsWith(SERVICES_PREFIX)) {
                return delegate.findResource(name);
            } else {
                return super.getResource(name);
            }
        }

        @Nonnull
        @Override
        public Enumeration<URL> getResources(@Nonnull final String name) throws IOException {
            if (name.startsWith(SERVICES_PREFIX)) {
                return delegate.findResources(name);
            } else {
                return super.getResources(name);
            }
        }
    }
}
