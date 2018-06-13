package com.thinkbiganalytics.kylo.hadoop;

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

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Utility methods for Hadoop file systems.
 */
public class FileSystemUtil {

    private static final Logger log = LoggerFactory.getLogger(FileSystemUtil.class);

    /**
     * File systems that should be accessed through Java's URLStreamHandler rather than through Hadoop
     */
    private static final Set<String> IGNORED_PROTOCOLS = new HashSet<>(Arrays.asList("file", "ftp", "http", "https", "jar", "mailto", "netdoc"));

    /**
     * Detect if the specified file exists, for supported file systems.
     *
     * @param uri  path to the file
     * @param conf Hadoop configuration
     * @return {@code true} if the file exists, or {@code false} if the file does not exist
     * @throws IOException if an I/O error occurs or the scheme is not supported
     */
    public static boolean fileExists(@Nonnull final URI uri, @Nonnull final Configuration conf) throws IOException {
        // Remove hadoop: scheme
        final URI hadoopUri;

        if ("hadoop".equals(uri.getScheme())) {
            try {
                hadoopUri = new URI(uri.getSchemeSpecificPart());
            } catch (final URISyntaxException e) {
                throw new IOException("Not a valid hadoop URI: " + uri);
            }
        } else {
            hadoopUri = uri;
        }

        // Determine if file exists
        switch (hadoopUri.getScheme()) {
            case "file":
                log.debug("Checking local path exists: {}", hadoopUri);
                return new File(hadoopUri).exists();

            case "hdfs":
                log.debug("Checking HDFS path exists: {}", hadoopUri);
                try {
                    return FileSystem.get(hadoopUri, conf).exists(new Path(hadoopUri));
                } catch (final IOException e) {
                    log.debug("Unable to read HDFS path: {}", hadoopUri, e);
                    return true;
                }

            default:
                throw new IOException("Not a supported scheme: " + hadoopUri.getScheme());
        }
    }

    /**
     * Parses and modifies the specified string to produce a {@link URL} that can be used with {@link URLClassLoader}.
     *
     * @param s    string to parse as a URL
     * @param conf Hadoop configuration
     * @return parsed URL
     */
    @Nonnull
    public static URL parseUrl(@Nonnull final String s, @Nullable final Configuration conf) {
        // Parse as a URI
        URI uri = URI.create(s);

        if (uri.getScheme() == null) {
            uri = ((conf != null) ? FileSystem.getDefaultUri(conf) : URI.create("file:///").resolve(new File("").getAbsolutePath() + "/")).resolve(uri);
        }
        if (!IGNORED_PROTOCOLS.contains(uri.getScheme())) {
            uri = URI.create("hadoop:" + uri);
        }

        // Create the URL
        try {
            return uri.toURL();
        } catch (final MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Adds Hadoop {@link FileSystem} classes to the Hadoop configuration.
     */
    public static void registerFileSystems(@Nonnull final Iterable<FileSystem> fileSystems, @Nonnull final Configuration conf) {
        for (final FileSystem fs : fileSystems) {
            try {
                final String scheme = fs.getScheme();
                final Class clazz = fs.getClass();
                log.debug("Found {} FileSystem using class: {}", scheme, clazz);
                conf.setClass("fs." + scheme + ".impl", clazz, FileSystem.class);
            } catch (final Exception e) {
                log.warn("Cannot load FileSystem using class: {}: {}", fs.getClass().getName(), e, e);
            }
        }
    }

    /**
     * Instances of {@code FileSystemUtil} should not be constructed.
     */
    private FileSystemUtil() {
        throw new UnsupportedOperationException();
    }
}
