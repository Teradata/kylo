package com.thinkbiganalytics.db;

/*-
 * #%L
 * kylo-commons-jdbc
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


import com.google.common.base.Function;
import com.google.common.collect.Lists;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import javax.annotation.Nonnull;

/**
 * Information for creating and identifying a JDBC data source.
 */
public class DataSourceProperties {

    private String user;
    private String password;
    private String url;
    private String driverClassName;
    private boolean testOnBorrow;
    private String validationQuery;
    private String driverLocation;
    private ClassLoader driverClassLoader;
    private Properties properties;

    /**
     * Constructs a {@code DataSourceProperties} with the specified properties.
     */
    public DataSourceProperties(String user, String password, String url) {
        this.user = user;
        this.password = password;
        this.url = url;
    }

    /**
     * Constructs a {@code DataSourceProperties} with the specified properties.
     */
    public DataSourceProperties(String user, String password, String url, String driverClassName, boolean testOnBorrow, String validationQuery) {
        this.user = user;
        this.password = password;
        this.url = url;
        this.driverClassName = driverClassName;
        this.testOnBorrow = testOnBorrow;
        this.validationQuery = validationQuery;
    }

    /**
     * Constructs a {@code DataSourceProperties} by copying the properties of another instance.
     */
    public DataSourceProperties(@Nonnull final DataSourceProperties other) {
        this.user = other.user;
        this.password = other.password;
        this.url = other.url;
        this.driverClassName = other.driverClassName;
        this.testOnBorrow = other.testOnBorrow;
        this.validationQuery = other.validationQuery;
        this.driverLocation = other.driverLocation;
        this.driverClassLoader = other.driverClassLoader;
        this.properties = new Properties(other.properties);
    }

    public String getUser() {
        return user;
    }

    /**
     * Sets the database user for making connections.
     */
    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    /**
     * Sets the database user's password.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    /**
     * Sets the URL of the database.
     */
    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isTestOnBorrow() {
        return testOnBorrow;
    }

    /**
     * Indicates that connections should be validated before being used.
     */
    public void setTestOnBorrow(boolean testOnBorrow) {
        this.testOnBorrow = testOnBorrow;
    }

    public String getValidationQuery() {
        return validationQuery;
    }

    /**
     * Sets the SQL query used to validate connections.
     */
    public void setValidationQuery(String validationQuery) {
        this.validationQuery = validationQuery;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    /**
     * Sets the JDBC driver class name.
     */
    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getDriverLocation() {
        return driverLocation;
    }

    /**
     * Sets the comma-separated list of URLs containing jar files required by the driver.
     */
    public void setDriverLocation(String driverLocation) {
        this.driverLocation = driverLocation;
    }

    public ClassLoader getDriverClassLoader() {
        if (driverClassLoader == null && driverLocation != null) {
            driverClassLoader = createClassLoader();
        }
        return driverClassLoader;
    }

    /**
     * Sets the class loader to be used to load the JDBC driver.
     */
    @SuppressWarnings("squid:HiddenFieldCheck")
    public void setDriverClassLoader(ClassLoader driverClassLoader) {
        this.driverClassLoader = driverClassLoader;

        if (driverClassLoader instanceof URLClassLoader) {
            final StringBuilder driverLocation = new StringBuilder();
            for (final URL url : ((URLClassLoader) driverClassLoader).getURLs()) {
                if (driverLocation.length() != 0) {
                    driverLocation.append(',');
                }
                driverLocation.append(url.toString());
            }
            this.driverLocation = driverLocation.toString();
        }
    }

    public Properties getProperties() {
        return properties;
    }

    /**
     * Sets the connection properties passed to driver.connect(...).
     */
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DataSourceProperties that = (DataSourceProperties) o;
        return Objects.equals(user, that.user) &&
               Objects.equals(password, that.password) &&
               Objects.equals(url, that.url) &&
               Objects.equals(driverClassName, that.driverClassName) &&
               // ClassLoaders don't support equals() so compare driverLocation instead
               ((driverLocation == null && that.driverLocation == null) ? driverClassLoader == that.driverClassLoader : Objects.equals(driverLocation, that.driverLocation)) &&
               Objects.equals(properties, that.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, password, url, driverClassName, driverLocation, driverClassLoader, properties);
    }

    /**
     * Creates a new class loader that loads from the specified list of locations.
     */
    @Nonnull
    private ClassLoader createClassLoader() {
        if (StringUtils.isNotBlank(driverLocation)) {
            final List<URL> urls = new ArrayList<>();

            for (final String location : driverLocation.split(",")) {
                if (StringUtils.isNotBlank(location)) {
                    urls.addAll(getURLs(location.trim()));
                }
            }

            return new URLClassLoader(urls.toArray(new URL[0]), Thread.currentThread().getContextClassLoader());
        } else {
            return Thread.currentThread().getContextClassLoader();
        }
    }

    /**
     * Converts the specified location to a list of URLs.
     */
    @Nonnull
    private List<URL> getURLs(@Nonnull final String location) {
        // Check if location is URL
        try {
            return Collections.singletonList(new URL(location));
        } catch (final MalformedURLException e) {
            // ignored
        }

        // Check if file or directory
        final File locationFile = new File(location);
        if (locationFile.exists()) {
            final List<File> files;
            if (locationFile.isFile()) {
                files = Collections.singletonList(locationFile);
            } else {
                final File[] fileList = locationFile.listFiles();
                files = (fileList != null) ? Arrays.asList(fileList) : Collections.<File>emptyList();
            }

            return Lists.transform(files, new Function<File, URL>() {
                @Override
                public URL apply(final File file) {
                    try {
                        return file.toURI().toURL();
                    } catch (final MalformedURLException e) {
                        throw new IllegalArgumentException("Not a valid path: " + location);
                    }
                }
            });
        }

        // Not a valid file or directory
        throw new IllegalArgumentException("Not a valid URL or file: " + location);
    }
}
