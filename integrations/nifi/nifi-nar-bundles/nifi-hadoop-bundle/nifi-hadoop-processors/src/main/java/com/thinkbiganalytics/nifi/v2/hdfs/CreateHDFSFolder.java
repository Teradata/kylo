package com.thinkbiganalytics.nifi.v2.hdfs;

/*-
 * #%L
 * thinkbig-nifi-hadoop-processors
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * This processor creates an HDFS folder
 */
@EventDriven
@InputRequirement(InputRequirement.Requirement.INPUT_REQUIRED)
@Tags({"hadoop", "HDFS", "folder"})
@CapabilityDescription("Create a folder in Hadoop Distributed File System (HDFS)")
public class CreateHDFSFolder extends AbstractHadoopProcessor {

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

    static {
        final Set<Relationship> rels = new HashSet<>();
        rels.add(REL_SUCCESS);
        rels.add(REL_FAILURE);
        relationships = Collections.unmodifiableSet(rels);
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

    @Override
    public Set<Relationship> getRelationships() {
        return relationships;
    }

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        List<PropertyDescriptor> props = new ArrayList<>(super.getSupportedPropertyDescriptors());
        props.add(DIRECTORY);
        props.add(UMASK);
        props.add(REMOTE_OWNER);
        props.add(REMOTE_GROUP);
        return Collections.unmodifiableList(props);
    }

    /**
     * @param context The context provides configuration properties from the processor
     * @throws IOException in the event
     * @see OnScheduled
     */
    @OnScheduled
    public void onScheduled(ProcessContext context) throws IOException {
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
        if (flowFile == null) {
            return;
        }

        final StopWatch stopWatch = new StopWatch(true);
        try {
            final FileSystem hdfs = getFileSystem(context);
            if (hdfs == null) {
                getLog().error("HDFS not configured properly");
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
                getLog().info("Creating folder " + path);
                final Path folderPath = new Path(path.trim());
                hdfsSupport.createFolder(folderPath, owner, group);
            }

            stopWatch.stop();
            final long millis = stopWatch.getDuration(TimeUnit.MILLISECONDS);

            getLog().info("created folders {} in {} milliseconds",
                          new Object[]{pathString, millis});

            session.transfer(flowFile, REL_SUCCESS);

        } catch (Exception e) {
            getLog().error("failed folder creation {}",
                           new Object[]{e});
            session.transfer(flowFile, REL_FAILURE);
        }
    }


}
