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
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.apache.hadoop.fs.FileChecksum;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnull;

@CapabilityDescription("Computes HDFS checksums of list of files")
@EventDriven
@Tags({"hadoop", "HDFS", "filesystem", "thinkbig", "checksum", "hash", "md5"})
public class ComputeHDFSChecksums extends AbstractHadoopProcessor {

    /**
     * Relationship for failure
     */
    public static final Relationship REL_FAILURE = new Relationship.Builder()
        .name("failure")
        .description("At least one of the provided checksums don't match computed one")
        .build();

    /**
     * Relationship for success
     */
    public static final Relationship REL_SUCCESS = new Relationship.Builder()
        .name("success")
        .description("Flow files goes to success relationship")
        .build();

    /**
     * the absolute base directory for the files given by {@link FILES}
     */
    public static final PropertyDescriptor DIRECTORY = new PropertyDescriptor.Builder()
        .name("absolute.path")
        .description("The absolute path to HDFS directory containing files to check. If not provided file names " +
                     "will be treated as absolute paths")
        .required(false)
        .addValidator(StandardValidators.ATTRIBUTE_EXPRESSION_LANGUAGE_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    /**
     * directs the processor to fail if any of the files given have a provided checksum not matching the one computed by this processor
     */
    public static final PropertyDescriptor FAIL_IF_INCORRECT_CHECKSUM = new PropertyDescriptor.Builder()
        .name("failIfWrongChecksum")
        .description("Decides whether flow should be failed if provided checksum doesn't match computed one")
        .required(true)
        .defaultValue("True")
        .addValidator(StandardValidators.BOOLEAN_VALIDATOR)
        .allowableValues(Sets.newHashSet("True", "False"))
        .build();

    /**
     * A JSON encoded list of files and checksums.  file names will be relative to {@link DIRECTORY} or absolute paths.
     */
    public static final PropertyDescriptor FILES = new PropertyDescriptor.Builder()
        .name("files")
        .description("JSON-encoded list of files with their checksums, given like: " +
                     "[{\n" +
                     "   \"name\": \"example\",\n" +
                     "   \"size\": 123456,\n" +
                     "   \"checksum\": {\n" +
                     "      \"length\": 28,\n" +
                     "      \"value\": \"AAAAAAAAAAAAAAAAcLyPS3KoaSFGi/joRB3OUQAAAAA=\",\n" +
                     "      \"algorithm\": \"MD5-of-0MD5-of-0CRC32\"\n" +
                     "   }\n" +
                     "}]")
        .required(true)
        .addValidator(StandardValidators.ATTRIBUTE_EXPRESSION_LANGUAGE_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    /**
     * Output paths to other NiFi processors
     */
    private static final Set<Relationship> relationships = ImmutableSet.of(REL_FAILURE, REL_SUCCESS);

    /**
     * methods to get the properties list
     *
     * @return the list of properties supported by this processor
     */
    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return ImmutableList.<PropertyDescriptor>builder().addAll(super.getSupportedPropertyDescriptors()).
            add(DIRECTORY).add(FAIL_IF_INCORRECT_CHECKSUM).add(FILES).build();
    }

    /**
     * get the relationships for this processor
     *
     * @return the set of relationships
     */
    @Override
    public Set<Relationship> getRelationships() {
        return relationships;
    }

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
        String absolutePath = context.getProperty(DIRECTORY).evaluateAttributeExpressions(flowFile).getValue();
        Boolean failIfWrongChecksum = context.getProperty(FAIL_IF_INCORRECT_CHECKSUM).
            evaluateAttributeExpressions(flowFile).asBoolean();
        Gson jsonParser = new Gson();
        File[] filesList;
        try {
            filesList = jsonParser.fromJson(filesJSON, File[].class);
            if (filesList == null) {
                filesList = new File[0];
            }

            for (File f : filesList) {
                String name = f.getName();
                Path filePath;
                if (absolutePath == null || absolutePath.isEmpty()) {
                    filePath = new Path(name);
                } else {
                    filePath = new Path(absolutePath, name);
                }
                FileChecksum computed_checksum = fs.getFileChecksum(filePath);
                String b64_checksum = Base64.getEncoder().encodeToString(computed_checksum.getBytes());
                f.setComputedChecksum(new Checksum(b64_checksum.length(), b64_checksum,
                                                   computed_checksum.getAlgorithmName()));
                if (failIfWrongChecksum && !Objects.equals(b64_checksum, f.getChecksum().getValue())) {
                    getLog().error("Checksums don't match! File: " + filePath.toString() + " checksum provided: " +
                                   f.getChecksum().getValue() + " checksum computed: " + b64_checksum);
                    session.transfer(flowFile, REL_FAILURE);
                    return;
                }
            }
        } catch (JsonSyntaxException e) {
            getLog().error("Files list attribute does not contain a proper JSON array");
            session.transfer(flowFile, REL_FAILURE);
            return;
        } catch (FileNotFoundException e) {
            getLog().error("One of the provided files not found.\n" + e.getMessage());
            session.transfer(flowFile, REL_FAILURE);
            return;
        } catch (IOException e) {
            throw new ProcessException(e);
        }
        flowFile = session.putAttribute(flowFile, FILES.getName(), jsonParser.toJson(filesList));
        session.transfer(flowFile, REL_SUCCESS);
    }

    class File {

        private String name;
        private Integer size;
        private Checksum checksum;
        private Checksum computedChecksum;

        public Checksum getComputedChecksum() {
            return computedChecksum;
        }

        public void setComputedChecksum(Checksum computedChecksum) {
            this.computedChecksum = computedChecksum;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getSize() {
            return size;
        }

        public void setSize(Integer size) {
            this.size = size;
        }

        public Checksum getChecksum() {
            return checksum;
        }

        public void setChecksum(Checksum checksum) {
            this.checksum = checksum;
        }
    }

    class Checksum {

        private Integer length;
        private String value;
        private String algorithm;

        public Checksum(Integer length, String value, String algorithm) {
            this.length = length;
            this.value = value;
            this.algorithm = algorithm;
        }

        public Integer getLength() {
            return length;
        }

        public void setLength(Integer length) {
            this.length = length;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getAlgorithm() {
            return algorithm;
        }

        public void setAlgorithm(String algorithm) {
            this.algorithm = algorithm;
        }
    }
}
