package com.thinkbiganalytics.nifi.v2.ingest;

/*-
 * #%L
 * thinkbig-nifi-core-processors
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


import com.thinkbiganalytics.ingest.TableRegisterSupport;
import com.thinkbiganalytics.nifi.processor.AbstractNiFiProcessor;
import com.thinkbiganalytics.nifi.v2.thrift.ThriftService;
import com.thinkbiganalytics.util.ColumnSpec;
import com.thinkbiganalytics.util.TableRegisterConfiguration;
import com.thinkbiganalytics.util.TableType;

import org.apache.commons.lang3.StringUtils;
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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.thinkbiganalytics.nifi.v2.ingest.IngestProperties.FEED_CATEGORY;
import static com.thinkbiganalytics.nifi.v2.ingest.IngestProperties.FEED_FIELD_SPECIFICATION;
import static com.thinkbiganalytics.nifi.v2.ingest.IngestProperties.FEED_FORMAT_SPECS;
import static com.thinkbiganalytics.nifi.v2.ingest.IngestProperties.FEED_NAME;
import static com.thinkbiganalytics.nifi.v2.ingest.IngestProperties.FIELD_SPECIFICATION;
import static com.thinkbiganalytics.nifi.v2.ingest.IngestProperties.PARTITION_SPECS;
import static com.thinkbiganalytics.nifi.v2.ingest.IngestProperties.REL_FAILURE;
import static com.thinkbiganalytics.nifi.v2.ingest.IngestProperties.REL_SUCCESS;
import static com.thinkbiganalytics.nifi.v2.ingest.IngestProperties.TARGET_FORMAT_SPECS;
import static com.thinkbiganalytics.nifi.v2.ingest.IngestProperties.TARGET_TBLPROPERTIES;
import static com.thinkbiganalytics.nifi.v2.ingest.IngestProperties.THRIFT_SERVICE;


@EventDriven
@InputRequirement(InputRequirement.Requirement.INPUT_REQUIRED)
@Tags({"hive", "ddl", "register", "thinkbig"})
@CapabilityDescription("Creates a set of standard feed tables managed by the Think Big platform. ")
public class RegisterFeedTables extends AbstractNiFiProcessor {

    public static final PropertyDescriptor FEED_ROOT = new PropertyDescriptor.Builder()
        .name("Feed Root Path")
        .description("Specify the full HDFS or S3 root path for the feed,valid,invalid tables.")
        .required(true)
        .defaultValue("${hive.ingest.root}")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();
    public static final PropertyDescriptor MASTER_ROOT = new PropertyDescriptor.Builder()
        .name("Master Root Path")
        .description("Specify the HDFS or S3 folder root path for creating the master table")
        .required(true)
        .defaultValue("${hive.master.root}")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();
    public static final PropertyDescriptor PROFILE_ROOT = new PropertyDescriptor.Builder()
        .name("Profile Root Path")
        .description("Specify the HDFS or S3 folder root path for creating the profile table")
        .required(true)
        .defaultValue("${hive.profile.root}")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();
    private final static String DEFAULT_STORAGE_FORMAT = "STORED AS ORC";
    private final static String DEFAULT_FEED_FORMAT_OPTIONS = "ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' LINES TERMINATED BY '\n' STORED AS TEXTFILE";
    /**
     * Specify creation of all tables
     */
    public static final String ALL_TABLES = "ALL";
    /**
     * Property indicating which tables to register
     */
    public static final PropertyDescriptor TABLE_TYPE = new PropertyDescriptor.Builder()
        .name("Table Type")
        .description("Specifies the standard table type to create or ALL for standard set.")
        .required(true)
        .allowableValues(TableType.FEED.toString(), TableType.VALID.toString(), TableType.INVALID.toString(), TableType.PROFILE.toString(), TableType.MASTER.toString(), ALL_TABLES)
        .defaultValue(ALL_TABLES)
        .build();

    // Relationships
    private final Set<Relationship> relationships;
    private final List<PropertyDescriptor> propDescriptors;

    public RegisterFeedTables() {
        final Set<Relationship> r = new HashSet<>();
        r.add(REL_SUCCESS);
        r.add(REL_FAILURE);
        relationships = Collections.unmodifiableSet(r);

        final List<PropertyDescriptor> pds = new ArrayList<>();
        pds.add(THRIFT_SERVICE);
        pds.add(FEED_CATEGORY);
        pds.add(FEED_NAME);
        pds.add(TABLE_TYPE);
        pds.add(FIELD_SPECIFICATION);
        pds.add(PARTITION_SPECS);
        pds.add(FEED_FIELD_SPECIFICATION);
        pds.add(FEED_FORMAT_SPECS);
        pds.add(TARGET_FORMAT_SPECS);
        pds.add(TARGET_TBLPROPERTIES);
        pds.add(FEED_ROOT);
        pds.add(PROFILE_ROOT);
        pds.add(MASTER_ROOT);

        propDescriptors = Collections.unmodifiableList(pds);
    }

    @Override
    public Set<Relationship> getRelationships() {
        return relationships;
    }

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return propDescriptors;
    }


    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
        // Verify flow file exists
        final FlowFile flowFile = session.get();
        if (flowFile == null) {
            return;
        }

        // Verify properties and attributes
        final String feedFormatOptions = Optional.ofNullable(context.getProperty(FEED_FORMAT_SPECS).evaluateAttributeExpressions(flowFile).getValue())
            .filter(StringUtils::isNotEmpty)
            .orElse(DEFAULT_FEED_FORMAT_OPTIONS);
        final String targetFormatOptions = Optional.ofNullable(context.getProperty(TARGET_FORMAT_SPECS).evaluateAttributeExpressions(flowFile).getValue())
            .filter(StringUtils::isNotEmpty)
            .orElse(DEFAULT_STORAGE_FORMAT);
        final String targetTableProperties = context.getProperty(TARGET_TBLPROPERTIES).evaluateAttributeExpressions(flowFile).getValue();
        final ColumnSpec[] partitions = Optional.ofNullable(context.getProperty(PARTITION_SPECS).evaluateAttributeExpressions(flowFile).getValue())
            .filter(StringUtils::isNotEmpty)
            .map(ColumnSpec::createFromString)
            .orElse(new ColumnSpec[0]);
        final String tableType = context.getProperty(TABLE_TYPE).getValue();

        final ColumnSpec[] columnSpecs = Optional.ofNullable(context.getProperty(FIELD_SPECIFICATION).evaluateAttributeExpressions(flowFile).getValue())
            .filter(StringUtils::isNotEmpty)
            .map(ColumnSpec::createFromString)
            .orElse(new ColumnSpec[0]);
        if (columnSpecs == null || columnSpecs.length == 0) {
            getLog().error("Missing field specification");
            session.transfer(flowFile, IngestProperties.REL_FAILURE);
            return;
        }

        ColumnSpec[] feedColumnSpecs = Optional.ofNullable(context.getProperty(FEED_FIELD_SPECIFICATION).evaluateAttributeExpressions(flowFile).getValue())
            .filter(StringUtils::isNotEmpty)
            .map(ColumnSpec::createFromString)
            .orElse(new ColumnSpec[0]);
        if (feedColumnSpecs == null || feedColumnSpecs.length == 0) {
            // Backwards compatibility with older templates we set the source and target to the same
            feedColumnSpecs = columnSpecs;
        }

        final String entity = context.getProperty(IngestProperties.FEED_NAME).evaluateAttributeExpressions(flowFile).getValue();
        if (entity == null || entity.isEmpty()) {
            getLog().error("Missing feed name");
            session.transfer(flowFile, IngestProperties.REL_FAILURE);
            return;
        }

        final String source = context.getProperty(IngestProperties.FEED_CATEGORY).evaluateAttributeExpressions(flowFile).getValue();
        if (source == null || source.isEmpty()) {
            getLog().error("Missing category name");
            session.transfer(flowFile, IngestProperties.REL_FAILURE);
            return;
        }

        final String feedRoot = context.getProperty(FEED_ROOT).evaluateAttributeExpressions(flowFile).getValue();
        final String profileRoot = context.getProperty(PROFILE_ROOT).evaluateAttributeExpressions(flowFile).getValue();
        final String masterRoot = context.getProperty(MASTER_ROOT).evaluateAttributeExpressions(flowFile).getValue();
        final TableRegisterConfiguration config = new TableRegisterConfiguration(feedRoot, profileRoot, masterRoot);

        // Register the tables
        final ThriftService thriftService = context.getProperty(THRIFT_SERVICE).asControllerService(ThriftService.class);

        try (final Connection conn = thriftService.getConnection()) {

            final TableRegisterSupport register = new TableRegisterSupport(conn, config);

            final boolean result;
            if (ALL_TABLES.equals(tableType)) {
                result = register.registerStandardTables(source, entity, feedColumnSpecs, feedFormatOptions, targetFormatOptions, partitions, columnSpecs, targetTableProperties);
            } else {
                result = register.registerTable(source, entity, feedColumnSpecs, feedFormatOptions, targetFormatOptions, partitions, columnSpecs, targetTableProperties, TableType.valueOf(tableType),
                                                true);
            }

            final Relationship relnResult = (result ? REL_SUCCESS : REL_FAILURE);
            session.transfer(flowFile, relnResult);
        } catch (final ProcessException | SQLException e) {
            getLog().error("Unable to obtain connection for {} due to {}; routing to failure", new Object[]{flowFile, e});
            session.transfer(flowFile, REL_FAILURE);
        }
    }
}
