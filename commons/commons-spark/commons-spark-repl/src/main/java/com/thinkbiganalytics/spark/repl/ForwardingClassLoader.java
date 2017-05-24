package com.thinkbiganalytics.spark.repl;

/*-
 * #%L
 * kylo-commons-spark-repl
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
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

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;

import java.util.Arrays;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Forwards loading of classes to delegate class loaders.
 */
public class ForwardingClassLoader extends ClassLoader {

    /**
     * List of delegate class loaders
     */
    @Nonnull
    private final ParentClassLoader[] delegates;

    /**
     * Constructs a {@code ForwardingClassLoader} that uses the specified delegate class loaders.
     *
     * @param delegates the delegate class loaders
     */
    @SuppressWarnings({"squid:S2637", "WeakerAccess"})
    public ForwardingClassLoader(@Nonnull final ClassLoader... delegates) {
        this(Arrays.asList(delegates));
    }

    /**
     * Constructs a {@code ForwardingClassLoader} that uses the specified delegate class loaders.
     *
     * @param delegates the delegate class loaders
     */
    @SuppressWarnings("WeakerAccess")
    public ForwardingClassLoader(@Nonnull final Iterable<ClassLoader> delegates) {
        this.delegates = FluentIterable.from(delegates)
            .transform(new Function<ClassLoader, ParentClassLoader>() {
                @Nullable
                @Override
                public ParentClassLoader apply(@Nullable final ClassLoader classLoader) {
                    return (classLoader != null) ? new ParentClassLoader(classLoader) : null;
                }
            })
            .toArray(ParentClassLoader.class);
    }

    @Override
    @SuppressWarnings("squid:S1166")
    protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
        for (final ParentClassLoader classLoader : delegates) {
            try {
                return classLoader.loadClass(name, resolve);
            } catch (final ClassNotFoundException e) {
                // ignored
            }
        }
        throw new ClassNotFoundException(name);
    }

    /**
     * A wrapper around {@link ClassLoader} to expose protected methods.
     */
    private static class ParentClassLoader extends ClassLoader {

        /**
         * Constructs a {@code ParentClassLoader} that exposes the specified class loader.
         *
         * @param parent the class loader to expose
         */
        ParentClassLoader(ClassLoader parent) {
            super(parent);
        }

        @Override
        public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            return super.loadClass(name, resolve);
        }
    }
}
