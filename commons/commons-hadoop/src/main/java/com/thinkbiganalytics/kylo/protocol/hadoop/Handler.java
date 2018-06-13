package com.thinkbiganalytics.kylo.protocol.hadoop;

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
import org.apache.hadoop.fs.FsUrlStreamHandlerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Creates streams for accessing hadoop:// URLs using {@link FileSystem}.
 */
public class Handler extends URLStreamHandler {

    /**
     * Thread-specific Hadoop URLStreamHandler factories
     */
    static final ThreadLocal<URLStreamHandlerFactory> FACTORY = new InheritableThreadLocal<>();

    /**
     * System property for registering custom URL handlers
     */
    static final String PROTOCOL_PATH_PROP = "java.protocol.handler.pkgs";

    /**
     * Registers this handler as a custom {@link URL} handler.
     */
    public static void register() {
        String protocols = System.getProperty(PROTOCOL_PATH_PROP);

        if (protocols == null) {
            protocols = "";
        } else if (!protocols.isEmpty()) {
            protocols += "|";
        }

        protocols += "com.thinkbiganalytics.kylo.protocol";
        System.setProperty(PROTOCOL_PATH_PROP, protocols);
    }

    /**
     * Sets the Hadoop configuration to use with this handler.
     */
    public static void setConfiguration(@Nonnull final Configuration conf) {
        FACTORY.set(new FsUrlStreamHandlerFactory(conf));
    }

    @Nullable
    @Override
    protected URLConnection openConnection(@Nullable final URL url) throws IOException {
        final URLStreamHandlerFactory handlerFactory = FACTORY.get();
        final String protocol = (url != null) ? url.getProtocol() : null;

        if (handlerFactory != null && protocol != null && protocol.equals("hadoop")) {
            final String file = url.getFile();
            final URLStreamHandler handler = handlerFactory.createURLStreamHandler(URI.create(file).getScheme());
            return (handler != null) ? new URL(null, file, handler).openConnection() : null;
        } else {
            return null;
        }
    }
}
