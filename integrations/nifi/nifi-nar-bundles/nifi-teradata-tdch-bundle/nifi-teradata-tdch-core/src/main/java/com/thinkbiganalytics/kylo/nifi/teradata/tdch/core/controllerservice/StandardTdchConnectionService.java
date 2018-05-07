package com.thinkbiganalytics.kylo.nifi.teradata.tdch.core.controllerservice;

/*-
 * #%L
 * kylo-nifi-hadoop-service
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

import com.thinkbiganalytics.kylo.nifi.teradata.tdch.api.TdchConnectionService;
import com.thinkbiganalytics.kylo.nifi.teradata.tdch.core.common.TdchValidations;
import com.thinkbiganalytics.kylo.nifi.teradata.tdch.core.processor.export.utils.TdchUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.annotation.lifecycle.OnEnabled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.controller.ConfigurationContext;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.reporting.InitializationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * The default implementation for {@link TdchConnectionService} to provide connection details to Teradata system for running a TDCH job
 */
public class StandardTdchConnectionService
    extends AbstractControllerService
    implements TdchConnectionService {

    protected final static String DEFAULT_JDBC_DRIVER_CLASS = "com.teradata.jdbc.TeraDriver";
    protected final static String DEFAULT_JDBC_CONNECTION_URL = "jdbc:teradata://localhost";
    protected final static String DEFAULT_USERNAME = "dbc";
    private final static String DEFAULT_TDCH_JAR_PATH = "/usr/lib/tdch/1.5/lib/teradata-connector-1.5.4.jar"; //same for CDH and HDP
    private final static String DEFAULT_HIVE_LIB = "/usr/hdp/current/hive-client/lib";  //CDH 5.14 default: /usr/lib/hive/lib
    private final static String DEFAULT_HIVE_CONF = "/usr/hdp/current/hive-client/conf"; //CDH 5.14 default: /usr/lib/hive/conf

    private final static Set<String> hiveDependencies = new LinkedHashSet<>();
    protected final static String HIVE_DEPENDENCY_AVRO_IDENTIFIER = "avro";
    protected final static String HIVE_DEPENDENCY_ANTLR_IDENTIFIER = "antlr-";
    protected final static String HIVE_DEPENDENCY_ANTLR_RUNTIME_IDENTIFIER = "antlr-runtime-";
    protected final static String HIVE_DEPENDENCY_COMMONS_DBCP_IDENTIFIER = "commons-dbcp-";
    protected final static String HIVE_DEPENDENCY_COMMONS_POOL_IDENTIFIER = "commons-pool-";
    protected final static String HIVE_DEPENDENCY_DATANUCLEUS_API_JDO_IDENTIFIER = "datanucleus-api-jdo-";
    protected final static String HIVE_DEPENDENCY_DATANUCLEUS_CORE_IDENTIFIER = "datanucleus-core-";
    protected final static String HIVE_DEPENDENCY_DATANUCLEUS_RDBMS_IDENTIFIER = "datanucleus-rdbms-";
    protected final static String HIVE_DEPENDENCY_HIVE_CLI_IDENTIFIER = "hive-cli-";
    protected final static String HIVE_DEPENDENCY_HIVE_EXEC_IDENTIFIER = "hive-exec-";
    protected final static String HIVE_DEPENDENCY_STANDALONE_IDENTIFIER = "standalone";
    protected final static String HIVE_DEPENDENCY_HIVE_METASTORE_IDENTIFIER = "hive-metastore-";
    protected final static String HIVE_DEPENDENCY_JDO_API_IDENTIFIER = "jdo-api-";
    protected final static String HIVE_DEPENDENCY_LIBFB303_IDENTIFIER = "libfb303-";
    protected final static String HIVE_DEPENDENCY_LIBTHRIFT_IDENTIFIER = "libthrift-";

    static {
        //HDP 2.5 default: /usr/hdp/current/hive-client/lib/avro-1.7.5.jar
        //CDH 5.14 default: /usr/lib/hive/lib/avro.jar
        hiveDependencies.add(HIVE_DEPENDENCY_AVRO_IDENTIFIER);

        //HDP 2.5 default: /usr/hdp/current/hive-client/lib/antlr-2.7.7.jar
        //HDP 2.5 default: /usr/hdp/current/hive-client/lib/antlr-runtime-3.4.jar
        //CDH 5.14 default: /usr/lib/hive/lib/antlr-2.7.7.jar
        //CDH 5.14 default: /usr/lib/hive/lib/antlr-runtime-3.4.jar
        hiveDependencies.add(HIVE_DEPENDENCY_ANTLR_IDENTIFIER);
        hiveDependencies.add(HIVE_DEPENDENCY_ANTLR_RUNTIME_IDENTIFIER);

        //HDP 2.5 default: /usr/hdp/current/hive-client/lib/commons-dbcp-1.4.jar
        //CDH 5.14 default: /usr/lib/hive/lib/commons-dbcp-1.4.jar
        hiveDependencies.add(HIVE_DEPENDENCY_COMMONS_DBCP_IDENTIFIER);

        //HDP 2.5 default: /usr/hdp/current/hive-client/lib/commons-pool-1.5.4.jar
        //CDH 5.14 default: /usr/lib/hive/lib/commons-pool-1.5.4.jar
        hiveDependencies.add(HIVE_DEPENDENCY_COMMONS_POOL_IDENTIFIER);

        //HDP 2.5 default: /usr/hdp/current/hive-client/lib/datanucleus-api-jdo-4.2.1.jar
        //CDH 5.14 default: /usr/lib/hive/lib/datanucleus-api-jdo-3.2.6.jar
        hiveDependencies.add(HIVE_DEPENDENCY_DATANUCLEUS_API_JDO_IDENTIFIER);

        //HDP 2.5 default: /usr/hdp/current/hive-client/lib/datanucleus-core-4.1.6.jar
        //CDH 5.14 default: /usr/lib/hive/lib/datanucleus-core-3.2.10.jar
        hiveDependencies.add(HIVE_DEPENDENCY_DATANUCLEUS_CORE_IDENTIFIER);

        //HDP 2.5 default: /usr/hdp/current/hive-client/lib/datanucleus-rdbms-4.1.7.jar
        //CDH 5.14 default: /usr/lib/hive/lib/datanucleus-rdbms-3.2.9.jar
        hiveDependencies.add(HIVE_DEPENDENCY_DATANUCLEUS_RDBMS_IDENTIFIER);

        //HDP 2.5 default: /usr/hdp/current/hive-client/lib/hive-cli-1.2.1000.2.5.3.0-37.jar
        //CDH 5.14 default: /usr/lib/hive/lib/hive-cli-1.1.0-cdh5.14.0.jar
        hiveDependencies.add(HIVE_DEPENDENCY_HIVE_CLI_IDENTIFIER);

        //HDP 2.5 default: /usr/hdp/current/hive-client/lib/hive-exec-1.2.1000.2.5.3.0-37.jar
        //CDH 5.14 default: /usr/lib/hive/lib/hive-exec-1.1.0-cdh5.14.0.jar
        hiveDependencies.add(HIVE_DEPENDENCY_HIVE_EXEC_IDENTIFIER);

        //HDP 2.5 default: /usr/hdp/current/hive-client/lib/hive-jdbc-1.2.1000.2.5.3.0-37-standalone.jar
        //CDH 5.14 default: /usr/lib/hive/lib/hive-jdbc-1.1.0-cdh5.14.0-standalone.jar
        hiveDependencies.add(HIVE_DEPENDENCY_STANDALONE_IDENTIFIER);

        //HDP 2.5 default: /usr/hdp/current/hive-client/lib/hive-metastore-1.2.1000.2.5.3.0-37.jar
        //CDH 5.14 default: /usr/lib/hive/lib/hive-metastore-1.1.0-cdh5.14.0.jar
        hiveDependencies.add(HIVE_DEPENDENCY_HIVE_METASTORE_IDENTIFIER);

        //HDP 2.5 default: /usr/hdp/current/hive-client/lib/jdo-api-3.0.1.jar
        //CDH 5.14 default: /usr/lib/hive/lib/jdo-api-3.0.1.jar
        hiveDependencies.add(HIVE_DEPENDENCY_JDO_API_IDENTIFIER);

        //HDP 2.5 default: /usr/hdp/current/hive-client/lib/libfb303-0.9.3.jar
        //CDH 5.14 default: /usr/lib/hive/lib/libfb303-0.9.3.jar
        hiveDependencies.add(HIVE_DEPENDENCY_LIBFB303_IDENTIFIER);

        //HDP 2.5 default: /usr/hdp/current/hive-client/lib/libthrift-0.9.3.jar
        //CDH 5.14 default: /usr/lib/hive/lib/libthrift-0.9.3.jar
        hiveDependencies.add(HIVE_DEPENDENCY_LIBTHRIFT_IDENTIFIER);
    }

    private final static String COMMA = ",";
    private final static String COLON = ":";

    public static final PropertyDescriptor JDBC_DRIVER_CLASS_NAME = new PropertyDescriptor.Builder()
        .name("JDBC Driver Class")
        .description("The JDBC driver class used by the source and target Teradata plugins when connecting to the Teradata system.")
        .required(true)
        .sensitive(false)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .defaultValue(DEFAULT_JDBC_DRIVER_CLASS)
        .build();

    public static final PropertyDescriptor JDBC_CONNECTION_URL = new PropertyDescriptor.Builder()
        .name("JDBC Connection URL")
        .description("The JDBC url used by the source and target Teradata plugins to connect to the Teradata system.")
        .required(true)
        .sensitive(false)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .addValidator(new TdchValidations.JdbcConnectionUrlDoesNotContainDatabaseSpecValidator())
        .defaultValue(DEFAULT_JDBC_CONNECTION_URL)
        .build();

    public static final PropertyDescriptor USERNAME = new PropertyDescriptor.Builder()
        .name("Username")
        .description("The authentication username used by the source and target Teradata plugins to connect to the Teradata system. "
                     + "Note that the value can include Teradata Wallet references in order to use user name information from the current user's wallet.")
        .required(true)
        .sensitive(false)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .defaultValue(DEFAULT_USERNAME)
        .build();

    public static final PropertyDescriptor PASSWORD = new PropertyDescriptor.Builder()
        .name("Password")
        .description("The authentication password used by the source and target Teradata plugins to connect to the Teradata system. "
                     + "Note that the value can include Teradata Wallet references in order to use user name information from the current user's wallet.")
        .required(true)
        .sensitive(true)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .build();

    public static final PropertyDescriptor TDCH_JAR_PATH = new PropertyDescriptor.Builder()
        .name("TDCH jar path")
        .description("The full path to teradata-connector-<version>.jar. "
                     + "This is referred via $USERLIBTDCH variable in TDCH documentation. ")
        .required(true)
        .sensitive(false)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .addValidator(new StandardValidators.FileExistsValidator(true))
        .addValidator(new TdchValidations.TdchJarExistsValidator())
        .defaultValue(DEFAULT_TDCH_JAR_PATH)
        .build();

    public static final PropertyDescriptor HIVE_LIB_PATH = new PropertyDescriptor.Builder()
        .name("Hive lib path")
        .description("Path to Hive's lib directory. Defaults to HDP location. For CDH, try with '/usr/lib/hive/lib'. Refer Hadoop distribution's docs for specifics.")
        .required(true)
        .sensitive(false)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .addValidator(new StandardValidators.DirectoryExistsValidator(true, false))
        .addValidator(new TdchValidations.TdchRequiredHiveDependenciesValidator(hiveDependencies))
        .defaultValue(DEFAULT_HIVE_LIB)
        .build();

    public static final PropertyDescriptor HIVE_CONF_PATH = new PropertyDescriptor.Builder()
        .name("Hive conf path")
        .description("Path to Hive's conf directory. Defaults to HDP location. For CDH, try with '/usr/lib/hive/conf'. Refer Hadoop distribution's docs for specifics.")
        .required(true)
        .sensitive(false)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .addValidator(new StandardValidators.DirectoryExistsValidator(true, false))
        .defaultValue(DEFAULT_HIVE_CONF)
        .build();

    private static final List<PropertyDescriptor> tdchConnectionProperties;

    static {
        final List<PropertyDescriptor> properties = new ArrayList<>();
        properties.add(JDBC_DRIVER_CLASS_NAME);
        properties.add(JDBC_CONNECTION_URL);
        properties.add(USERNAME);
        properties.add(PASSWORD);
        properties.add(TDCH_JAR_PATH);
        properties.add(HIVE_CONF_PATH);
        properties.add(HIVE_LIB_PATH);
        tdchConnectionProperties = Collections.unmodifiableList(properties);
    }

    private String jdbcDriverClassName;
    private String jdbcConnectionUrl;
    private String userName;
    private String password;
    private String tdchJarPath;
    private String tdchLibraryJarsPath;
    private String tdchHadoopClassPath;

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return tdchConnectionProperties;
    }

    /**
     * Called when the controller service is initiated.  It will set up access to the various properties.
     *
     * @param context the configuration context gives access to properties
     * @throws InitializationException if there are any issues accessing the properties
     */
    @OnEnabled
    public void onConfigured(final ConfigurationContext context) throws InitializationException {
        jdbcDriverClassName = context.getProperty(JDBC_DRIVER_CLASS_NAME).evaluateAttributeExpressions().getValue();
        getLogger().info("JDBC Driver Class set to: " + jdbcDriverClassName);

        jdbcConnectionUrl = context.getProperty(JDBC_CONNECTION_URL).evaluateAttributeExpressions().getValue();
        getLogger().info("JDBC Connection URL set to: " + jdbcConnectionUrl);

        userName = context.getProperty(USERNAME).evaluateAttributeExpressions().getValue();
        getLogger().info("User name set to: " + userName);

        password = context.getProperty(PASSWORD).evaluateAttributeExpressions().getValue();
        getLogger().info("Password set to: " + TdchUtils.MASK);

        tdchJarPath = context.getProperty(TDCH_JAR_PATH).evaluateAttributeExpressions().getValue();
        getLogger().info("TDCH Jar Path set to: " + tdchJarPath);

        String hiveLibPath = context.getProperty(HIVE_LIB_PATH).evaluateAttributeExpressions().getValue();
        getLogger().info("Hive Lib Path set to: " + hiveLibPath);

        String hiveConfPath = context.getProperty(HIVE_CONF_PATH).evaluateAttributeExpressions().getValue();
        getLogger().info("Hive Conf Path set to: " + hiveLibPath);

        tdchLibraryJarsPath = generatePath(hiveLibPath, hiveConfPath, hiveDependencies, COMMA);
        getLogger().info("TDCH Library Jars Path set to: " + tdchLibraryJarsPath);

        tdchHadoopClassPath = generatePath(hiveLibPath, hiveConfPath, hiveDependencies, COLON);
        getLogger().info("TDCH Hadoop Classpath set to: " + tdchHadoopClassPath);
    }

    //helper method to generate classpath
    private String generatePath(String hiveLibPath, String hiveConfPath, Set<String> hiveDependencies, String delimiter) {
        List<String> classpath = new ArrayList<>();
        int depth = 1;

        for (String hiveDependency : hiveDependencies) {
            try (Stream<Path> paths = Files.find(Paths.get(hiveLibPath), depth,
                                                 (path, attributes) -> TdchValidations.checkMatch(path.toString(), attributes, hiveDependency))) {

                int classPathSizeBefore = classpath.size();
                paths.forEach(path -> {
                    getLogger().debug("Classpath generation: Checking for: " + hiveDependency + " gave path: " + path.toString());
                    classpath.add(path.toString());
                });
                int classPathSizeAfter = classpath.size();
                if (classPathSizeBefore == classPathSizeAfter) {
                    getLogger().warn("Unable to find Hive dependency with name having: " + hiveDependency);
                }

            } catch (IOException e) {
                getLogger().warn("Unable to generate classpath required for TDCH. The jobs will likely fail.");
                e.printStackTrace();
            }
        }
        classpath.add(hiveConfPath);
        return StringUtils.join(classpath, delimiter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getJdbcDriverClassName() {
        return this.jdbcDriverClassName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getJdbcConnectionUrl() {
        return this.jdbcConnectionUrl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUserName() {
        return this.userName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPassword() {
        return this.password;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTdchJarPath() {
        return this.tdchJarPath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTdchLibraryJarsPath() {
        return this.tdchLibraryJarsPath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTdchHadoopClassPath() {
        return this.tdchHadoopClassPath;
    }
}