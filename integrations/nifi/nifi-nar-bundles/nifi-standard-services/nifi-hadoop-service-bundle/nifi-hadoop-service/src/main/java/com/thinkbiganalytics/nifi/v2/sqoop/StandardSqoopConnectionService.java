package com.thinkbiganalytics.nifi.v2.sqoop;

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

import org.apache.nifi.annotation.lifecycle.OnEnabled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.controller.ConfigurationContext;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.reporting.InitializationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An implementation for {@link SqoopConnectionService} to provide connection details to a relational system for running a sqoop job
 */
public class StandardSqoopConnectionService
    extends AbstractControllerService
    implements SqoopConnectionService {

    /**
     * A property to provide the connection string for accessing the relational source system.
     */
    public static final PropertyDescriptor SOURCE_CONNECTION_STRING = new PropertyDescriptor.Builder()
        .name("Source Connection String")
        .description("The connection string for accessing the relational source system. "
                     + "Sqoop will attempt to determine the best connector and driver based upon this value. "
                     + "In most cases, this behavior should be OK and acceptable. "
                     + "[*** Note 1: If you need to manually specify a connector, provide it via the (optional) Source Connection Manager property.] "
                     + "[*** Note 2: Based upon the connector, Sqoop will automatically choose a best Source Driver. "
                     + "In the rare case that you need to manually specify a driver, provide it via the (optional) Source Driver property. "
                     + "Please be careful: If a driver is manually provided, the Generic JDBC Connector will always be used and any other optimized connectors will be ignored, even if available. "
                     + " *---> So, in most cases, you should not need to provide a Source Driver value <---*]")
        .required(true)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .build();

    /**
     * A property to get the user name for accessing the relational source system
     */
    public static final PropertyDescriptor SOURCE_USERNAME = new PropertyDescriptor.Builder()
        .name("Source User Name")
        .description("The user name for accessing the relational source system")
        .required(true)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .build();


    /**
     * A property to get the password mode, which indicates type of password and how it is provided.
     */
    public static final PropertyDescriptor PASSWORD_MODE = new PropertyDescriptor.Builder()
        .name("Password Mode")
        .description("Indicates type of password and how it is provided. "
                     + "(1) Entered as clear text (2) Entered as encrypted text (3) Location of file on HDFS containing encrypted password")
        .required(true)
        .expressionLanguageSupported(false)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .allowableValues(PasswordMode.values())
        .defaultValue(PasswordMode.ENCRYPTED_ON_HDFS_FILE.toString())
        .build();

    /**
     * A property to get the HDFS location containing encrypted password file for accessing the relational source system.
     */
    public static final PropertyDescriptor SOURCE_PASSWORD_HDFS_FILE = new PropertyDescriptor.Builder()
        .name("Source Password File")
        .description("The HDFS location containing encrypted password file for accessing the relational source system.")
        .required(false)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .sensitive(true)
        .build();

    /**
     * A property to get the passphrase to decrypt the password for connecting to source system.
     */
    public static final PropertyDescriptor SOURCE_PASSWORD_PASSPHRASE = new PropertyDescriptor.Builder()
        .name("Source Password Passphrase")
        .description("The passphrase to decrypt the password for connecting to source system.")
        .required(false)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .sensitive(true)
        .build();

    /**
     * A property to get the password (can be either encrypted or clear text).
     */
    public static final PropertyDescriptor SOURCE_ENTERED_PASSWORD = new PropertyDescriptor.Builder()
        .name("Source Password (Encrypted Base64/Clear Text)")
        .description("The password (can be either encrypted or clear text). "
                     + "For encrypted password, use the base64 encoded version which is output by the encryption utility. "
                     + "The Password Mode indicates how the password will be obtained and/or decrypted.")
        .required(false)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .sensitive(true)
        .build();

    /**
     * A property to get the connection manager (also called connector) to use for accessing the relational source system.
     */
    public static final PropertyDescriptor SOURCE_CONNECTION_MANAGER = new PropertyDescriptor.Builder()
        .name("Source Connection Manager")
        .description("The connection manager (also called connector) to use for accessing the relational source system. "
                     + "Note: Sqoop will try to detect the best connection manager automatically. So, providing this value is optional, and may even be not recommended in some cases. "
                     + "See description of Source Connection String property for full explanation.")
        .required(false)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .build();

    /**
     * A property to get the driver for accessing the relational source system.
     */
    public static final PropertyDescriptor SOURCE_DRIVER = new PropertyDescriptor.Builder()
        .name("Source Driver (Avoid providing value)")
        .description("The driver for accessing the relational source system. "
                     + "Note: This should be auto-detected. Try to avoid providing a value for this property. See description of Source Connection String property for full explanation.")
        .required(false)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .build();

    private static final List<PropertyDescriptor> sqoopConnectionProperties;

    static {
        final List<PropertyDescriptor> properties = new ArrayList<>();
        properties.add(SOURCE_CONNECTION_STRING);
        properties.add(SOURCE_USERNAME);
        properties.add(PASSWORD_MODE);
        properties.add(SOURCE_PASSWORD_HDFS_FILE);
        properties.add(SOURCE_PASSWORD_PASSPHRASE);
        properties.add(SOURCE_ENTERED_PASSWORD);
        properties.add(SOURCE_CONNECTION_MANAGER);
        properties.add(SOURCE_DRIVER);

        sqoopConnectionProperties = Collections.unmodifiableList(properties);
    }

    private String sourceConnectionString;
    private String sourceUserName;
    private PasswordMode passwordMode;
    private String sourcePasswordHdfsFile;
    private String sourcePasswordPassphrase;
    private String sourceEnteredPassword;
    private String sourceConnectionManager;
    private String sourceDriver;

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return sqoopConnectionProperties;
    }

    /**
     * Called when the controller service is initiated.  It will set up access to the various properties.
     *
     * @param context the configuration context gives access to properties
     * @throws InitializationException if there are any issues accessing the properties
     */
    @OnEnabled
    public void onConfigured(final ConfigurationContext context) throws InitializationException {
        sourceConnectionString = context.getProperty(SOURCE_CONNECTION_STRING).evaluateAttributeExpressions().getValue();
        sourceUserName = context.getProperty(SOURCE_USERNAME).evaluateAttributeExpressions().getValue();
        passwordMode = PasswordMode.valueOf(context.getProperty(PASSWORD_MODE).getValue());
        sourcePasswordHdfsFile = context.getProperty(SOURCE_PASSWORD_HDFS_FILE).evaluateAttributeExpressions().getValue();
        sourcePasswordPassphrase = context.getProperty(SOURCE_PASSWORD_PASSPHRASE).evaluateAttributeExpressions().getValue();
        sourceEnteredPassword = context.getProperty(SOURCE_ENTERED_PASSWORD).evaluateAttributeExpressions().getValue();
        sourceConnectionManager = context.getProperty(SOURCE_CONNECTION_MANAGER).evaluateAttributeExpressions().getValue();
        sourceDriver = context.getProperty(SOURCE_DRIVER).evaluateAttributeExpressions().getValue();
    }

    @Override
    public String getConnectionString() {
        return this.sourceConnectionString;
    }

    @Override
    public String getUserName() {
        return this.sourceUserName;
    }

    @Override
    public PasswordMode getPasswordMode() {
        return this.passwordMode;
    }

    @Override
    public String getPasswordHdfsFile() {
        return this.sourcePasswordHdfsFile;
    }

    @Override
    public String getPasswordPassphrase() {
        return this.sourcePasswordPassphrase;
    }

    @Override
    public String getEnteredPassword() {
        return this.sourceEnteredPassword;
    }

    @Override
    public String getConnectionManager() {
        return this.sourceConnectionManager;
    }

    @Override
    public String getDriver() {
        return this.sourceDriver;
    }
}
