package com.thinkbiganalytics.kylo.utils;

/*-
 * #%L
 * kylo-spark-livy-server
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtils {

    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    FileUtils() {
    }  // static method access only

    public static String getCwd() {
        return Paths.get(".").toAbsolutePath().normalize().toString();
    }

    public static boolean canExecute(String file) {
        File f = new File(file);
        if (f.exists() && !f.isDirectory() && f.canExecute()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean canRead(String file) {
        File f = new File(file);
        if (f.exists() && !f.isDirectory() && f.canRead()) {
            return true;
        } else {
            return false;
        }
    }

    public static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    public static String readFile(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, StandardCharsets.UTF_8);
    }

    public static String getFilePathFromResource(Class<?> clazz, String resourceLocation) {
        URI resourceUri;
        String resourcePath = "";
        try {
            URL resourceUrl = clazz.getResource(resourceLocation);
            if (resourceUrl == null) {
                return "";
            }
            resourceUri = resourceUrl.toURI();

            resourcePath = new File(resourceUri).getCanonicalPath();
        } catch (URISyntaxException e) {
            logger.error("malformed path from resourceUrl");
            throw new IllegalStateException(e);
        } catch (IOException e) {
            logger.error("cannot find '{}' for unknown reason", resourceLocation);
            throw new IllegalStateException("Error attempting to find resource.", e);
        }

        return resourcePath;
    }

    public static String getPathAdjacentToSourceRoot(Class<?> clazz, String resourceLocation) {
        String resourcePath = "";
        try {
            URL url = clazz.getProtectionDomain().getCodeSource().getLocation();
            Path path = Paths.get(url.toURI()).resolve(resourceLocation)
                    .toAbsolutePath();

            resourcePath = path.toFile().getCanonicalPath();
        } catch (IOException e) {
            logger.error("cannot find '{}' for unknown reason", resourceLocation);
            throw new IllegalStateException("Error attempting to find resource.", e);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return resourcePath;
    }

    public static String getResourceFromJar(String resourceLocation) {
        InputStream in = FileUtils.class.getClassLoader().getResourceAsStream(resourceLocation);
        StringWriter writer = new StringWriter();
        try {
            org.apache.commons.io.IOUtils.copy(in, writer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("cannot find '{}' for unknown reason", resourceLocation);
            throw new IllegalStateException("Error attempting to find resource.", e);
        }
        return writer.toString();
    }

}
