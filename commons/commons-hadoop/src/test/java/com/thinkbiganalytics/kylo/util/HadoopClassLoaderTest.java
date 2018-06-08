package com.thinkbiganalytics.kylo.util;

/*-
 * #%L
 * Kylo Common Hadoop Utilities
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

import com.thinkbiganalytics.kylo.hadoop.MockFileSystem;
import com.thinkbiganalytics.kylo.protocol.hadoop.Handler;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.hamcrest.BaseMatcher;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.util.Arrays;
import java.util.UUID;

import javax.annotation.Nonnull;

public class HadoopClassLoaderTest {

    /**
     * Matches objects where {@code toString()} matches the specified matcher.
     */
    @Nonnull
    private static <T> Matcher<T> withToString(@Nonnull final Matcher<String> toStringMatcher) {
        return new BaseMatcher<T>() {
            @Override
            public boolean matches(final Object item) {
                return toStringMatcher.matches(item.toString());
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("toString() ");
                description.appendDescriptionOf(toStringMatcher);
            }
        };
    }

    @Before
    public void setUp() {
        final Configuration conf = new Configuration(false);
        conf.setClass("fs.mock.impl", MockFileSystem.class, FileSystem.class);

        Handler.register();
        Handler.setConfiguration(conf);
    }

    /**
     * Test Hadoop class loader.
     */
    @Test
    @SuppressWarnings({"squid:S2095", "unchecked"})
    public void test() {
        final Configuration conf = new Configuration(false);
        final HadoopClassLoader classLoader = new HadoopClassLoader(conf);

        // Test null paths
        Assert.assertFalse("Expected null jar to be ignored", classLoader.addJar(null));
        Assert.assertFalse("Expected null jars to be ignored", classLoader.addJars(null));
        Assert.assertArrayEquals(new URL[0], classLoader.getURLs());
        Assert.assertEquals(0, conf.size());

        // Test invalid path
        Assert.assertFalse("Expected invalid jar to be ignored", classLoader.addJar("file:/tmp/" + UUID.randomUUID()));
        Assert.assertArrayEquals(new URL[0], classLoader.getURLs());
        Assert.assertEquals(0, conf.size());

        // Test Hadoop path
        Assert.assertTrue("Expected Hadoop jar to be added", classLoader.addJar("mock:/tmp/file.ext"));
        Matcher matcher1 = withToString(CoreMatchers.equalTo("hadoop:mock:/tmp/file.ext"));
        Assert.assertThat(Arrays.asList(classLoader.getURLs()), CoreMatchers.hasItems(matcher1));

        // Test path without FileSystem services
        final String classUrl = getClass().getResource("./").toString();
        Assert.assertTrue("Expected class directory to be added", classLoader.addJar(classUrl));
        Matcher matcher2 = withToString(CoreMatchers.equalTo(classUrl));
        Assert.assertThat(Arrays.asList(classLoader.getURLs()), CoreMatchers.hasItems(matcher2));
        Assert.assertEquals(0, conf.size());

        // Test path with FileSystem services
        final String resourceUrl = getClass().getResource("/").toString();
        Assert.assertTrue("Expected resource directory to be added", classLoader.addJar(resourceUrl));
        Matcher matcher3 = withToString(CoreMatchers.equalTo(resourceUrl));
        Assert.assertThat(Arrays.asList(classLoader.getURLs()), CoreMatchers.hasItems(matcher3));
        Assert.assertEquals(MockFileSystem.class, conf.getClass("fs.mock.impl", null));

        // Test existing jar
        final int existingSize = classLoader.getURLs().length;
        Assert.assertFalse("Expected existing jar to be ignored", classLoader.addJar(resourceUrl));
        Assert.assertEquals(existingSize, classLoader.getURLs().length);
    }
}
