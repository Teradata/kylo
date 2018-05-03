package com.thinkbiganalytics.kylo.util;

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
        classLoader.addJar(null);
        classLoader.addJars(null);
        Assert.assertArrayEquals(new URL[0], classLoader.getURLs());
        Assert.assertEquals(0, conf.size());

        // Test invalid path
        classLoader.addJar("file:/tmp/" + UUID.randomUUID());
        Assert.assertArrayEquals(new URL[0], classLoader.getURLs());
        Assert.assertEquals(0, conf.size());

        // Test Hadoop path
        classLoader.addJar("mock:/tmp/file.ext");
        Assert.assertThat(Arrays.asList(classLoader.getURLs()), CoreMatchers.hasItems(withToString(CoreMatchers.equalTo("hadoop:mock:/tmp/file.ext"))));

        // Test path without FileSystem services
        final String classFileUrl = getClass().getResource("HadoopClassLoaderTest.class").toString();
        final String classDirUrl = classFileUrl.substring(0, classFileUrl.indexOf("HadoopClassLoaderTest"));
        classLoader.addJar(classDirUrl);
        Assert.assertThat(Arrays.asList(classLoader.getURLs()), CoreMatchers.hasItems(withToString(CoreMatchers.equalTo(classDirUrl))));
        Assert.assertEquals(0, conf.size());

        // Test path with FileSystem services
        final String resourceFileUrl = getClass().getResource("/META-INF/services/org.apache.hadoop.fs.FileSystem").toString();
        final String resourceDirUrl = resourceFileUrl.substring(0, resourceFileUrl.indexOf("META-INF"));
        classLoader.addJar(resourceDirUrl);
        Assert.assertThat(Arrays.asList(classLoader.getURLs()), CoreMatchers.hasItems(withToString(CoreMatchers.equalTo(resourceDirUrl))));
        Assert.assertEquals(MockFileSystem.class, conf.getClass("fs.mock.impl", null));

        // Test existing jar
        final int existingSize = classLoader.getURLs().length;
        classLoader.addJar(resourceDirUrl);
        Assert.assertEquals(existingSize, classLoader.getURLs().length);
    }
}
