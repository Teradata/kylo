package com.thinkbiganalytics.nifi.v2.sqoop;

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
 * @author jagrut sharma
 */

/**
 * Implementation for SqoopConnectionService to provide connection details for a sqoop job
 */
public class StandardSqoopConnectionService
    extends AbstractControllerService
    implements SqoopConnectionService {

    public static final PropertyDescriptor SOURCE_DRIVER = new PropertyDescriptor.Builder()
        .name("Source Driver")
        .description("The driver for accessing the relational source system")
        .required(true)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .build();

    public static final PropertyDescriptor SOURCE_CONNECTION_STRING = new PropertyDescriptor.Builder()
        .name("Source Connection String")
        .description("The connection string for accessing the relational source system")
        .required(true)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .build();

    public static final PropertyDescriptor SOURCE_USERNAME = new PropertyDescriptor.Builder()
        .name("Source User Name")
        .description("The user name for accessing the relational source system")
        .required(true)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .build();

    public static final PropertyDescriptor SOURCE_PASSWORD_HDFS_FILE = new PropertyDescriptor.Builder()
        .name("Source Password File")
        .description("The HDFS location containing encrypted password file for accessing the relational source system.")
        .required(true)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .sensitive(true)
        .build();

    public static final PropertyDescriptor SOURCE_PASSWORD_PASSPHRASE = new PropertyDescriptor.Builder()
        .name("Source Password Passphrase")
        .description("The passphrase to decrypt the password for connecting to source system.")
        .required(true)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .sensitive(true)
        .build();

    private static final List<PropertyDescriptor> sqoopConnectionProperties;

    static {
        final List<PropertyDescriptor> properties = new ArrayList<>();
        properties.add(SOURCE_DRIVER);
        properties.add(SOURCE_CONNECTION_STRING);
        properties.add(SOURCE_USERNAME);
        properties.add(SOURCE_PASSWORD_HDFS_FILE);
        properties.add(SOURCE_PASSWORD_PASSPHRASE);

        sqoopConnectionProperties = Collections.unmodifiableList(properties);
    }

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return sqoopConnectionProperties;
    }

    String sourceDriver;
    String sourceConnectionString;
    String sourceUserName;
    String sourcePasswordHdfsFile;
    String sourcePasswordPassphrase;

    @OnEnabled
    public void onConfigured(final ConfigurationContext context) throws InitializationException {

        sourceDriver = context.getProperty(SOURCE_DRIVER).evaluateAttributeExpressions().getValue();
        sourceConnectionString = context.getProperty(SOURCE_CONNECTION_STRING).evaluateAttributeExpressions().getValue();
        sourceUserName = context.getProperty(SOURCE_USERNAME).evaluateAttributeExpressions().getValue();
        sourcePasswordHdfsFile = context.getProperty(SOURCE_PASSWORD_HDFS_FILE).evaluateAttributeExpressions().getValue();
        sourcePasswordPassphrase = context.getProperty(SOURCE_PASSWORD_PASSPHRASE).evaluateAttributeExpressions().getValue();
    }

    @Override
    public String getDriver() {
        return this.sourceDriver;
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
    public String getPasswordHdfsFile() {
        return this.sourcePasswordHdfsFile;
    }

    @Override
    public String getPasswordPassphrase() {
        return this.sourcePasswordPassphrase;
    }
}
