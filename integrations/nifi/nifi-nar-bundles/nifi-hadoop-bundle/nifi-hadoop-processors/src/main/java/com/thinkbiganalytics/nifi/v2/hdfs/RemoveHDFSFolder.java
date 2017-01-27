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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.nifi.annotation.behavior.EventDriven;
import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * A NiFi processor that permanently deletes files and folder in HDFS.
 */
@CapabilityDescription("Removes a folder in Hadoop Distributed File System (HDFS)")
@EventDriven
@InputRequirement(InputRequirement.Requirement.INPUT_REQUIRED)
@Tags({"hadoop", "HDFS", "folder", "filesystem", "thinkbig"})
public class RemoveHDFSFolder extends AbstractHadoopProcessor {

    /**
     * Property for the directory to be removed
     */
    public static final PropertyDescriptor DIRECTORY = new PropertyDescriptor.Builder()
        .name(DIRECTORY_PROP_NAME)
        .description("The absolute path to the HDFS directory to be permanently deleted. One directory per line.")
        .required(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    /**
     * Relationship for failure
     */
    public static final Relationship REL_FAILURE = new Relationship.Builder()
        .name("failure")
        .description("FlowFiles that failed to be processed")
        .build();

    /**
     * Relationship for success
     */
    public static final Relationship REL_SUCCESS = new Relationship.Builder()
        .name("success")
        .description("FlowFiles that removed a directory")
        .build();

    /**
     * Output paths to other NiFi processors
     */
    private static final Set<Relationship> relationships = ImmutableSet.of(REL_FAILURE, REL_SUCCESS);

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return ImmutableList.<PropertyDescriptor>builder().addAll(super.getSupportedPropertyDescriptors()).add(DIRECTORY).build();
    }

    @Override
    public Set<Relationship> getRelationships() {
        return relationships;
    }

    @Override
    public void onTrigger(@Nonnull final ProcessContext context, @Nonnull final ProcessSession session) throws ProcessException {
        // Get file to process
        FlowFile flowFile = session.get();
        if (flowFile == null) {
            return;
        }

        // Get file system
        FileSystem fileSystem = getFileSystem(context);
        if (fileSystem == null) {
            session.transfer(flowFile, REL_FAILURE);
            return;
        }

        // Delete the specified paths
        String[] directories = context.getProperty(DIRECTORY).evaluateAttributeExpressions(flowFile).getValue().split("\\r?\\n");

        for (String string : directories) {
            // Check for possible missing properties - accidentally deleting parent directory instead of child
            String pathString = string.trim();
            if (!pathString.endsWith("/")) {
                getLog().error("Path must end with a slash /: " + pathString);
                session.transfer(flowFile, REL_FAILURE);
                return;
            }
            if (pathString.contains("//")) {
                getLog().error("Path cannot contain double slashes //: " + pathString);
                session.transfer(flowFile, REL_FAILURE);
                return;
            }

            // Check for relative directories - accidentally deleting folder in home directory
            Path path = new Path(pathString);
            if (!path.isAbsolute()) {
                getLog().error("Path is not absolute: " + path);
                session.transfer(flowFile, REL_FAILURE);
                return;
            }

            // Delete path
            getLog().debug("Deleting path: " + path);
            try {
                if (!fileSystem.delete(path, true) && fileSystem.exists(path)) {
                    getLog().error("Failed to remove path: " + path);
                    session.transfer(flowFile, REL_FAILURE);
                    return;
                }
            } catch (IOException e) {
                getLog().error("Failed to remove path: " + path, e);
                session.transfer(flowFile, REL_FAILURE);
                return;
            }
        }

        // Return success
        session.transfer(flowFile, REL_SUCCESS);
    }
}
