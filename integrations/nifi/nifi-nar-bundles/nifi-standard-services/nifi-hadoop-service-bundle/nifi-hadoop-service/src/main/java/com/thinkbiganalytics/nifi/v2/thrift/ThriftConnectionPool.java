package com.thinkbiganalytics.nifi.v2.thrift;

/*-
 * #%L
 * thinkbig-nifi-hadoop-service
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

import com.thinkbiganalytics.nifi.security.ApplySecurityPolicy;
import com.thinkbiganalytics.nifi.security.KerberosProperties;
import com.thinkbiganalytics.nifi.security.SecurityUtil;
import com.thinkbiganalytics.nifi.security.SpringSecurityContextLoader;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnDisabled;
import org.apache.nifi.annotation.lifecycle.OnEnabled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.components.ValidationResult;
import org.apache.nifi.components.Validator;
import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.controller.ConfigurationContext;
import org.apache.nifi.controller.ControllerServiceInitializationContext;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.reporting.InitializationException;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

/**
 * Implementation of for Database Connection Pooling Service. Apache DBCP is used for connection pooling functionality.
 */
@Tags({"hive", "spark", "thrift", "jdbc", "database", "connection", "pooling", "store", "thinkbig"})
@CapabilityDescription("Provides a Thrift connection service.")
public class ThriftConnectionPool extends AbstractControllerService implements ThriftService {

    /**
     * A property to get the database connection URL used to connect to a database.
     */
    public static final PropertyDescriptor DATABASE_URL = new PropertyDescriptor.Builder()
        .name("Database Connection URL")
        .description("A database connection URL used to connect to a database. May contain database system name, host, port, database name and some parameters."
                     + " The exact syntax of a database connection URL is specified by your DBMS.")
        .defaultValue(null)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .required(true)
        .build();

    /**
     * A property to get the database driver class name
     */
    public static final PropertyDescriptor DB_DRIVERNAME = new PropertyDescriptor.Builder()
        .name("Database Driver Class Name")
        .description("Database driver class name")
        .defaultValue(null)
        .required(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .build();

    /**
     * A property to get an optional database driver jar file path url. For example 'file:///var/tmp/mariadb-java-client-1.1.7.jar'
     */
    public static final PropertyDescriptor DB_DRIVER_JAR_URL = new PropertyDescriptor.Builder()
        .name("Database Driver Jar Url")
        .description("Optional database driver jar file path url. For example 'file:///var/tmp/mariadb-java-client-1.1.7.jar'")
        .defaultValue(null)
        .required(false)
        .addValidator(StandardValidators.URL_VALIDATOR)
        .build();

    /**
     * A property to get the database user name
     */
    public static final PropertyDescriptor DB_USER = new PropertyDescriptor.Builder()
        .name("Database User")
        .description("Database user name")
        .defaultValue(null)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .build();

    /**
     * A property to get the password for the database user
     */
    public static final PropertyDescriptor DB_PASSWORD = new PropertyDescriptor.Builder()
        .name("Password")
        .description("The password for the database user")
        .defaultValue(null)
        .required(false)
        .sensitive(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .build();

    /**
     * A property to get the query to be used when testing the Datasource.
     */
    public static final PropertyDescriptor DB_VALIDATION_QUERY = new PropertyDescriptor.Builder()
        .name("Validation Query")
        .description("Query to be used when testing the Datasource. ")
        .defaultValue("show tables 'test'")
        .required(true)
        .sensitive(false)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .build();

    /**
     * A property to get the query to be used when testing the Datasource.
     */
    public static final PropertyDescriptor DB_VALIDATION_QUERY_TIMEOUT = new PropertyDescriptor.Builder()
        .name("Validation Query Timeout")
        .description("The amount of time, in seconds, to wait for a validation query to succeed.")
        .defaultValue("10 s")
        .required(true)
        .sensitive(false)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .build();

    /**
     * A property to get the maximum amount of time that the pool will wait for an available connection
     */
    public static final PropertyDescriptor MAX_WAIT_TIME = new PropertyDescriptor.Builder()
        .name("Max Wait Time")
        .description("The maximum amount of time that the pool will wait (when there are no available connections) "
                     + " for a connection to be returned before failing, or -1 to wait indefinitely. ")
        .defaultValue("500 millis")
        .required(true)
        .addValidator(StandardValidators.TIME_PERIOD_VALIDATOR)
        .sensitive(false)
        .build();

    /**
     * A property to get the  maximum number of active connections that can be allocated from this pool at the same time
     */
    public static final PropertyDescriptor MAX_TOTAL_CONNECTIONS = new PropertyDescriptor.Builder()
        .name("Max Total Connections")
        .description("The maximum number of active connections that can be allocated from this pool at the same time, "
                     + " or negative for no limit.")
        .defaultValue("8")
        .required(true)
        .addValidator(StandardValidators.INTEGER_VALIDATOR)
        .sensitive(false)
        .build();

    /**
     * A property to get a file, or comma separated list of files, which contains the Hadoop file system configuration.
     */
    public static final PropertyDescriptor HADOOP_CONFIGURATION_RESOURCES = new PropertyDescriptor.Builder()
        .name("Hadoop Configuration Resources")
        .description("A file, or comma separated list of files, which contains the Hadoop file system configuration. Without this, Hadoop "
                     + "will search the classpath for a 'core-site.xml' and 'hdfs-site.xml' file or will revert to a default configuration.")
        .required(false).addValidator(createMultipleFilesExistValidator())
        .build();
    private String hadoopConfiguraiton;
    private String principal;
    private String keytab;

    /**
     * Property for Kerberos service keytab
     */
    private PropertyDescriptor kerberosKeytab;

    /**
     * Property for Kerberos service principal
     */
    private PropertyDescriptor kerberosPrincipal;

    /**
     * List of properties
     */
    private List<PropertyDescriptor> properties;

    private volatile BasicDataSource dataSource;

    /**
     * Validates that one or more files exist, as specified in a single property.
     *
     * @return a validator which describes the result of the validation
     */
    public static final Validator createMultipleFilesExistValidator() {
        return new Validator() {
            @Override
            public ValidationResult validate(String subject, String input, ValidationContext context) {
                final String[] files = input.split(",");
                for (String filename : files) {
                    try {
                        final File file = new File(filename.trim());
                        final boolean valid = file.exists() && file.isFile();
                        if (!valid) {
                            final String message = "File " + file + " does not exist or is not a file";
                            return new ValidationResult.Builder().subject(subject).input(input).valid(false).explanation(message).build();
                        }
                    } catch (SecurityException e) {
                        final String message = "Unable to access " + filename + " due to " + e.getMessage();
                        return new ValidationResult.Builder().subject(subject).input(input).valid(false).explanation(message).build();
                    }
                }
                return new ValidationResult.Builder().subject(subject).input(input).valid(true).build();
            }

        };
    }

    @Override
    protected void init(@Nonnull final ControllerServiceInitializationContext config) throws InitializationException {
        // Create Kerberos properties
        final SpringSecurityContextLoader securityContextLoader = SpringSecurityContextLoader.create(config);
        final KerberosProperties kerberosProperties = securityContextLoader.getKerberosProperties();
        kerberosKeytab = kerberosProperties.createKerberosKeytabProperty();
        kerberosPrincipal = kerberosProperties.createKerberosPrincipalProperty();

        // Create list of properties
        final List<PropertyDescriptor> props = new ArrayList<>();
        props.add(DATABASE_URL);
        props.add(DB_DRIVERNAME);
        props.add(DB_DRIVER_JAR_URL);
        props.add(DB_USER);
        props.add(DB_PASSWORD);
        props.add(DB_VALIDATION_QUERY);
        props.add(DB_VALIDATION_QUERY_TIMEOUT);
        props.add(MAX_WAIT_TIME);
        props.add(MAX_TOTAL_CONNECTIONS);
        props.add(HADOOP_CONFIGURATION_RESOURCES);
        props.add(kerberosPrincipal);
        props.add(kerberosKeytab);
        properties = Collections.unmodifiableList(props);
    }

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return properties;
    }

    /**
     * Configures connection pool by creating an instance of the
     * {@link BasicDataSource} based on configuration provided with
     * {@link ConfigurationContext}.
     * <p>
     * This operation makes no guarantees that the actual connection could be
     * made since the underlying system may still go off-line during normal
     * operation of the connection pool.
     *
     * @param context the configuration context
     * @throws InitializationException if unable to create a database connection
     */
    @OnEnabled
    public void onConfigured(final ConfigurationContext context) throws InitializationException {

        final String drv = context.getProperty(DB_DRIVERNAME).getValue();
        final String user = context.getProperty(DB_USER).getValue();
        final String passw = context.getProperty(DB_PASSWORD).getValue();
        final Long maxWaitMillis = context.getProperty(MAX_WAIT_TIME).asTimePeriod(TimeUnit.MILLISECONDS);
        final Integer maxTotal = context.getProperty(MAX_TOTAL_CONNECTIONS).asInteger();
        //Kerberos Property
        this.hadoopConfiguraiton = context.getProperty(HADOOP_CONFIGURATION_RESOURCES).getValue();
        this.principal = context.getProperty(kerberosPrincipal).getValue();
        this.keytab = context.getProperty(kerberosKeytab).getValue();
        final String validationQuery = context.getProperty(DB_VALIDATION_QUERY).getValue();
        final Long validationQueryTimeout = context.getProperty(DB_VALIDATION_QUERY_TIMEOUT).asTimePeriod(TimeUnit.SECONDS);

        // Optional driver URL used to get DriverClassLoader, when exist, this URL will be used to locate driver jar file location
        final String urlString = context.getProperty(DB_DRIVER_JAR_URL).getValue();
        final String dburl = context.getProperty(DATABASE_URL).getValue();

        dataSource =
            new RefreshableDataSource.Builder()
                .driverClassName(drv)
                .url(dburl)
                .username(user)
                .password(passw)
                .driverClassLoader(getDriverClassLoader(urlString, drv))
                .validationQuery(validationQuery)
                .validationQueryTimeout(validationQueryTimeout)
                .build();
        getLogger().info("Created new ThirftConnectionPool with Refreshable Datasource for " + urlString);
    }

    /**
     * using Thread.currentThread().getContextClassLoader() will ensure that you are using the ClassLoader for your NAR.
     *
     * @param urlString URL of the class
     * @param drvName   the driver string
     * @return the class loader
     * @throws InitializationException if there is a problem obtaining the ClassLoader
     */
    protected ClassLoader getDriverClassLoader(String urlString, String drvName) throws InitializationException {
        if (urlString != null && urlString.length() > 0) {
            try {
                final URL[] urls = new URL[]{new URL(urlString)};
                final URLClassLoader ucl = new URLClassLoader(urls);

                // Workaround which allows to use URLClassLoader for JDBC driver loading.
                // (Because the DriverManager will refuse to use a driver not loaded by the system ClassLoader.)
                final Class<?> clazz = Class.forName(drvName, true, ucl);
                if (clazz == null) {
                    throw new InitializationException("Can't load Database Driver " + drvName);
                }
                final Driver driver = (Driver) clazz.newInstance();
                DriverManager.registerDriver(new DriverShim(driver));

                return ucl;
            } catch (final MalformedURLException e) {
                throw new InitializationException("Invalid Database Driver Jar Url", e);
            } catch (final Exception e) {
                throw new InitializationException("Can't load Database Driver", e);
            }
        } else {
            // That will ensure that you are using the ClassLoader for you NAR.
            return Thread.currentThread().getContextClassLoader();
        }
    }

    /**
     * Shutdown pool, close all open connections.
     */
    @OnDisabled
    public void shutdown() {
        try {
            if( dataSource != null ) {
                dataSource.close();
            }
        } catch (final SQLException e) {
            throw new ProcessException(e);
        }
    }

    @Override
    public Connection getConnection() throws ProcessException {
        try {
            if (kerberosAuthentication()) {
                final Connection con = dataSource.getConnection();
                return con;
            }

            getLogger().error("Unable to get connection from pool , returning null");
            return null;
        } catch (final SQLException e) {
            throw new ProcessException(e);
        }
    }

    /**
     * Invoke kerberos aauthentication code and validate user with given keytab.
     *
     * @return false
     */
    @SuppressWarnings("static-access")
    protected boolean kerberosAuthentication() {
        ComponentLog loggerInstance;
        loggerInstance = getLogger();

        //Kerberos Security Validation

        String principal = this.principal;
        String keyTab = this.keytab;
        String hadoopConfigurationResources = hadoopConfiguraiton;

        // If all 3 fields are filled out then assume kerberos is enabled and we want to authenticate the user
        boolean loadConfigurationForKerberosAuthentication = false;
        if (!(StringUtils.isEmpty(principal) && StringUtils.isEmpty(keyTab) && StringUtils.isEmpty(hadoopConfigurationResources))) {
            loadConfigurationForKerberosAuthentication = true;
        }

        //Get Security class object reference
        Configuration configuration = null;
        ApplySecurityPolicy applySecurityObject = null;
        if (loadConfigurationForKerberosAuthentication) {
            applySecurityObject = new ApplySecurityPolicy();

            try {
                configuration = applySecurityObject.getConfigurationFromResources(hadoopConfigurationResources);
            } catch (Exception hadoopConfigException) {
                loggerInstance.error("Unable to get Hadoop Configuration resources . Loading default hadoop configuration." + hadoopConfigException.getMessage());

                //Load default configuration if unable to load from property descriptor.
                configuration = new Configuration();
            }
        }

        if (loadConfigurationForKerberosAuthentication && SecurityUtil.isSecurityEnabled(configuration))  // Check if kerberos security is enabled in cluster
        {
            if (principal.equals("") && keyTab.equals("")) {
                loggerInstance.error("Kerberos Principal and Kerberos KeyTab information missing in Kerboeros enabled cluster.");
                return false;
            }

            try {

                loggerInstance.info("User anuthentication initiated");

                boolean authenticationStatus = applySecurityObject.validateUserWithKerberos(loggerInstance, hadoopConfigurationResources, principal, keyTab);
                if (authenticationStatus) {
                    loggerInstance.info("User authenticated successfully.");
                    return true;
                } else {
                    loggerInstance.error("User authentication failed.");
                    return false;
                }

            } catch (Exception unknownException) {
                loggerInstance.error("Kerberos : Unable to validate user - " + unknownException.getMessage());
                return false;
            }

        }

        return true;
    }

    @Override
    public String toString() {
        return "ThriftConnectionPool[id=" + getIdentifier() + "]";
    }

}
