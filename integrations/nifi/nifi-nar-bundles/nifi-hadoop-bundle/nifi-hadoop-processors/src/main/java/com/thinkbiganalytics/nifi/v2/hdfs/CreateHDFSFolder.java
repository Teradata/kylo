/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.nifi.v2.hdfs;

import com.thinkbiganalytics.nifi.security.ApplySecurityPolicy;
import com.thinkbiganalytics.nifi.security.SecurityUtil;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.nifi.annotation.behavior.EventDriven;
import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.PropertyValue;
import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.components.ValidationResult;
import org.apache.nifi.components.Validator;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.util.StopWatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * This processor copies FlowFiles to HDFS.
 */
@EventDriven
@InputRequirement(InputRequirement.Requirement.INPUT_REQUIRED)
@Tags({"hadoop", "HDFS", "folder", "filesystem"})
@CapabilityDescription("Create a folder in Hadoop Distributed File System (HDFS)")
public class CreateHDFSFolder extends AbstractHadoopProcessor {

    //public static final String REPLACE_RESOLUTION = "replace";
    public static final String IGNORE_RESOLUTION = "ignore";
    public static final String FAIL_RESOLUTION = "fail";

    // relationships
    public static final Relationship REL_SUCCESS = new Relationship.Builder()
        .name("success")
        .description("Files that have been successfully written to HDFS are transferred to this relationship")
        .build();

    public static final Relationship REL_FAILURE = new Relationship.Builder()
        .name("failure")
        .description(
            "Files that could not be written to HDFS for some reason are transferred to this relationship")
        .build();

    // properties
    public static final PropertyDescriptor DIRECTORY = new PropertyDescriptor.Builder()
        .name(DIRECTORY_PROP_NAME)
        .description("The full HDFS directory(s) to create separated by newline")
        .required(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();
    /*
        public static final PropertyDescriptor CONFLICT_RESOLUTION = new PropertyDescriptor.Builder()
                .name("Conflict Resolution Strategy")
                .description("Indicates what should happen when a file with the same name already exists in the output directory")
                .required(true)
                .defaultValue(FAIL_RESOLUTION)
                .allowableValues(REPLACE_RESOLUTION, IGNORE_RESOLUTION, FAIL_RESOLUTION)
                .build();
    */
    public static final PropertyDescriptor UMASK = new PropertyDescriptor.Builder()
        .name("Permissions umask")
        .description(
            "A umask represented as an octal number which determines the permissions of files written to HDFS. This overrides the Hadoop Configuration dfs.umaskmode")
        .addValidator(createUmaskValidator())
        .build();

    public static final PropertyDescriptor REMOTE_OWNER = new PropertyDescriptor.Builder()
        .name("Remote Owner")
        .description(
            "Changes the owner of the HDFS file to this value after it is written. This only works if NiFi is running as a user that has HDFS super user privilege to change owner")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    public static final PropertyDescriptor REMOTE_GROUP = new PropertyDescriptor.Builder()
        .name("Remote Group")
        .description(
            "Changes the group of the HDFS file to this value after it is written. This only works if NiFi is running as a user that has HDFS super user privilege to change group")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    private static final Set<Relationship> relationships;
    private static final List<PropertyDescriptor> localProperties;

    static {
        final Set<Relationship> rels = new HashSet<>();
        rels.add(REL_SUCCESS);
        rels.add(REL_FAILURE);
        relationships = Collections.unmodifiableSet(rels);

        List<PropertyDescriptor> props = new ArrayList<>(properties);
        props.add(DIRECTORY);
        //props.add(CONFLICT_RESOLUTION);
        props.add(UMASK);
        props.add(REMOTE_OWNER);
        props.add(REMOTE_GROUP);
        localProperties = Collections.unmodifiableList(props);
    }

    @Override
    public Set<Relationship> getRelationships() {
        return relationships;
    }

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return localProperties;
    }

    @OnScheduled
    public void onScheduled(ProcessContext context) throws Exception {
        super.abstractOnScheduled(context);

        // Set umask once, to avoid thread safety issues doing it in onTrigger
        final PropertyValue umaskProp = context.getProperty(UMASK);
        final short dfsUmask = resolveUMask(umaskProp);

        final Configuration conf = getConfiguration();
        FsPermission.setUMask(conf, new FsPermission(dfsUmask));
    }

    @Override
    public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {
        FlowFile flowFile = session.get();
        if (flowFile == null) return;

        final StopWatch stopWatch = new StopWatch(true);
        try {
            final Configuration configuration = getConfiguration();

            String principal = context.getProperty(KERBEROS_PRINCIPAL).getValue();
            String keyTab = context.getProperty(KERBEROS_KEYTAB).getValue();
            String hadoopConfigurationResources = context.getProperty(HADOOP_CONFIGURATION_RESOURCES).getValue();

            if(SecurityUtil.isSecurityEnabled(configuration))
            {
                if(principal.equals("") && keyTab.equals("") )
                {
                    getLogger().error("Kerberos Principal and Kerberos KeyTab information missing in Kerboeros enabled cluster.");
                    session.transfer(flowFile, REL_FAILURE);
                    return;
                }

                try {
                    getLogger().info("User anuthentication initiated");
                    ApplySecurityPolicy applySecurityObject = new ApplySecurityPolicy();
                    boolean authenticationStatus = applySecurityObject.validateUserWithKerberos(getLogger(),hadoopConfigurationResources,principal,keyTab);
                    if (authenticationStatus)
                    {
                        getLogger().info("User authenticated successfully.");
                    }
                    else
                    {
                        getLogger().info("User authentication failed.");
                        session.transfer(flowFile, REL_FAILURE);
                        return;
                    }

                } catch (Exception unknownException) {
                    getLogger().error("Unknown exception occured while validating user :" + unknownException.getMessage());
                    unknownException.printStackTrace();
                    session.transfer(flowFile, REL_FAILURE);
                    return;
                }

            }

            final FileSystem hdfs = getFileSystem();
            if (configuration == null || hdfs == null) {
                getLogger().error("HDFS not configured properly");
                session.transfer(flowFile, REL_FAILURE);
                context.yield();
                return;
            }

            String owner = context.getProperty(REMOTE_OWNER).evaluateAttributeExpressions(flowFile).getValue();
            String group = context.getProperty(REMOTE_GROUP).evaluateAttributeExpressions(flowFile).getValue();

            HDFSSupport hdfsSupport = new HDFSSupport(hdfs);
            String pathString = context.getProperty(DIRECTORY).evaluateAttributeExpressions(flowFile).getValue();
            String[] paths = pathString.split("\\r?\\n");

            // Create for each path defined
            for (String path : paths) {
                getLogger().info("Creating folder " + path);
                final Path folderPath = new Path(path.trim());
                hdfsSupport.createFolder(folderPath, owner, group);
            }

            stopWatch.stop();
            final long millis = stopWatch.getDuration(TimeUnit.MILLISECONDS);

            getLogger().info("created folders {} in {} milliseconds",
                             new Object[]{pathString, millis});

            session.transfer(flowFile, REL_SUCCESS);

        } catch (Exception e) {
            getLogger().error("failed folder creation {}",
                              new Object[]{e});
            session.transfer(flowFile, REL_FAILURE);
        }
    }

    /*
* Validates that a property is a valid umask, i.e. a short octal number that is not negative.
*/
    static Validator createUmaskValidator() {
        return new Validator() {
            @Override
            public ValidationResult validate(final String subject, final String value, final ValidationContext context) {
                String reason = null;
                try {
                    final short shortVal = Short.parseShort(value, 8);
                    if (shortVal < 0) {
                        reason = "octal umask [" + value + "] cannot be negative";
                    } else if (shortVal > 511) {
                        // HDFS umask has 9 bits: rwxrwxrwx ; the sticky bit cannot be umasked
                        reason = "octal umask [" + value + "] is not a valid umask";
                    }
                } catch (final NumberFormatException e) {
                    reason = "[" + value + "] is not a valid short octal number";
                }
                return new ValidationResult.Builder().subject(subject).input(value).explanation(reason).valid(reason == null)
                    .build();
            }
        };

    }

    static short resolveUMask(final PropertyValue umaskProp) {
        final short dfsUmask;
        if (umaskProp.isSet()) {
            dfsUmask = Short.parseShort(umaskProp.getValue(), 8);
        } else {
            dfsUmask = FsPermission.DEFAULT_UMASK;
        }
        return dfsUmask;
    }


}