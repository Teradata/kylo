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
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.tools.DistCp;
import org.apache.hadoop.tools.DistCpOptions;
import org.apache.nifi.annotation.behavior.EventDriven;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Processor for performing a distributed copy between two HDFS clusters
 *
 * @see <a href="http://hadoop.apache.org/docs/stable/hadoop-distcp/DistCp.html">http://hadoop.apache.org/docs/stable/hadoop-distcp/DistCp.html</a>
 */
@CapabilityDescription("Copies files from one HDFS location into another using DistCp MR job")
@EventDriven
@Tags({"hadoop", "HDFS", "filesystem", "thinkbig", "copy", "distributed copy", "distcp"})
public class DistCopyHDFS extends AbstractHadoopProcessor {

    /**
     * Relationship for failure
     */
    public static final Relationship REL_FAILURE = new Relationship.Builder()
        .name("failure")
        .description("At least one of the provided files not found")
        .build();

    /**
     * Relationship for success
     */
    public static final Relationship REL_SUCCESS = new Relationship.Builder()
        .name("success")
        .description("Files copied to new location")
        .build();

    /**
     * Property to define the source path, which is used as a base for all {@link FILES}
     */
    public static final PropertyDescriptor SOURCE = new PropertyDescriptor.Builder()
        .name("source.path")
        .description("Absolute source path, if provided along with 'files' parameter will be treated as absolute " +
                     "path for relative paths provided in that parameter")
        .required(false)
        .addValidator(StandardValidators.ATTRIBUTE_EXPRESSION_LANGUAGE_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    /**
     * Property that defines the destination path as an absolute path
     */
    public static final PropertyDescriptor DESTINATION = new PropertyDescriptor.Builder()
        .name("destination.path")
        .description("Absolute destination path")
        .required(true)
        .addValidator(StandardValidators.ATTRIBUTE_EXPRESSION_LANGUAGE_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    /**
     * property which is a JSON encoded list of files of the form:
     * [{ "name" : "example" }]
     */
    public static final PropertyDescriptor FILES = new PropertyDescriptor.Builder()
        .name("files")
        .description("JSON-encoded list of files, given like: " +
                     "[{\n" +
                     "   \"name\": \"example\",\n" +
                     " }\n" +
                     "]")
        .required(false)
        .addValidator(StandardValidators.ATTRIBUTE_EXPRESSION_LANGUAGE_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    /**
     * Output paths to other NiFi processors
     */
    private static final Set<Relationship> relationships = ImmutableSet.of(REL_FAILURE, REL_SUCCESS);

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return ImmutableList.<PropertyDescriptor>builder().addAll(super.getSupportedPropertyDescriptors()).
            add(DESTINATION).add(SOURCE).add(FILES).build();
    }

    /**
     * get the relationships required for the Processor
     *
     * @return a set of relationships
     */
    @Override
    public Set<Relationship> getRelationships() {
        return relationships;
    }

    /**
     * onTrigger is called when the flow file proceeds through the processor
     *
     * @param context passed in by the framework and provides access to the data configured in the processor
     * @param session passed in by the framework and provides access to the flow file
     * @throws ProcessException if any framework actions fail
     */
    @Override
    public void onTrigger(@Nonnull final ProcessContext context, @Nonnull final ProcessSession session) throws ProcessException {
        FlowFile flowFile = session.get();
        if (flowFile == null) {
            return;
        }
        final FileSystem fs = getFileSystem(context);
        if (fs == null) {
            getLog().error("Couldn't initialize HDFS");
            session.transfer(flowFile, REL_FAILURE);
            return;
        }
        String filesJSON = context.getProperty(FILES).evaluateAttributeExpressions(flowFile).getValue();
        String source = context.getProperty(SOURCE).evaluateAttributeExpressions(flowFile).getValue();
        String destination = context.getProperty(DESTINATION).evaluateAttributeExpressions(flowFile).getValue();
        Gson jsonParser = new Gson();
        File[] filesList;
        ArrayList<Path> pathsList = new ArrayList<>();
        try {
            if (!(filesJSON == null) && !filesJSON.isEmpty()) {
                filesList = jsonParser.fromJson(filesJSON, File[].class);
                if (filesList == null) {
                    filesList = new File[0];
                }
                if (source != null && !source.isEmpty()) {
                    for (File f : filesList) {
                        pathsList.add(new Path(source, f.getName()));
                    }
                } else {
                    for (File f : filesList) {
                        pathsList.add(new Path(f.getName()));
                    }
                }
            } else {
                if (source == null || source.isEmpty()) {
                    getLog().error(String.format("At least one of attributes: %s or %s needs to be set",
                                                 SOURCE.getName(), FILES.getName()));

                    session.transfer(flowFile, REL_FAILURE);
                    return;
                }
                pathsList.add(new Path(source));
            }
            DistCp distCp = getDistCp(pathsList, new Path(destination));
            Job job = distCp.execute();
            job.waitForCompletion(false);
        } catch (JsonSyntaxException e) {
            getLog().error("Files list attribute does not contain a proper JSON array");
            session.transfer(flowFile, REL_FAILURE);
            return;
        } catch (Exception e) {
            getLog().error("Exception during processor execution: " + e.getMessage());
            session.transfer(flowFile, REL_FAILURE);
            return;
        }
        session.transfer(flowFile, REL_SUCCESS);
    }

    /**
     * method to construct a new DistCp object to perform the distcp
     *
     * @param pathsList   A list of paths to be recursively copied from one cluster to another
     * @param destination The root location on the target cluster
     * @return a DistCp object
     * @throws Exception if the construction of the {@link DistCp} object fails for any reason
     */
    protected DistCp getDistCp(List<Path> pathsList, Path destination) throws Exception {
        final Configuration conf = getConfiguration();
        DistCpOptions opts = new DistCpOptions(pathsList, destination);
        return new DistCp(conf, opts);
    }

    class File {

        private String name;

        public File(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

}
