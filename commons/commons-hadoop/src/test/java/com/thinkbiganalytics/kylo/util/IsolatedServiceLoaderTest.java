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

import com.google.common.collect.Iterables;
import com.thinkbiganalytics.kylo.hadoop.MockFileSystem;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Test;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

public class IsolatedServiceLoaderTest {

    /**
     * Verify adding new services from the class loader.
     */
    @Test
    @SuppressWarnings({"squid:S2112", "unchecked"})
    public void testUpdate() {
        // Mock class loader
        final Set<URL> resources = new HashSet<>();
        final URLClassLoader classLoader = new URLClassLoader(new URL[0]) {

            @Override
            public Enumeration<URL> findResources(final String name) {
                return Collections.enumeration(resources);
            }
        };

        // Test with no services
        final IsolatedServiceLoader<FileSystem> loader = new IsolatedServiceLoader<>(FileSystem.class, classLoader);

        boolean result = loader.update();
        Assert.assertFalse(result);
        Assert.assertEquals(0, Iterables.size(loader));

        // Test with new services
        resources.add(getClass().getResource("file-system-services-1.txt"));

        result = loader.update();
        Assert.assertTrue(result);
        Assert.assertEquals(1, Iterables.size(loader));
        Assert.assertThat(loader, CoreMatchers.hasItem(CoreMatchers.<FileSystem>instanceOf(MockFileSystem.class)));

        // Test with existing services
        result = loader.update();
        Assert.assertFalse(result);
        Assert.assertEquals(1, Iterables.size(loader));
        Assert.assertThat(loader, CoreMatchers.hasItem(CoreMatchers.<FileSystem>instanceOf(MockFileSystem.class)));

        // Test with adding services
        resources.add(getClass().getResource("file-system-services-2.txt"));

        result = loader.update();
        Assert.assertTrue(result);
        Assert.assertEquals(2, Iterables.size(loader));
        Matcher[] matchers = new Matcher[] {CoreMatchers.instanceOf(MockFileSystem.class), CoreMatchers.instanceOf(LocalFileSystem.class)};
        Assert.assertThat(loader, CoreMatchers.hasItems(matchers));
    }
}
