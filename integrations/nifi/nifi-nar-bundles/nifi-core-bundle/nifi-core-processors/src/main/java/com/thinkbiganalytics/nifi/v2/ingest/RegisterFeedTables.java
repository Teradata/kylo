/*
 * Copyright (c) 2015. Teradata Inc.
 */
package com.thinkbiganalytics.nifi.v2.ingest;


import com.thinkbiganalytics.ingest.TableRegisterSupport;
import com.thinkbiganalytics.nifi.v2.thrift.ThriftService;
import com.thinkbiganalytics.util.ColumnSpec;
import com.thinkbiganalytics.util.TableType;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.annotation.behavior.EventDriven;
import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.logging.ProcessorLog;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.thinkbiganalytics.nifi.v2.ingest.ComponentProperties.FEED_CATEGORY;
import static com.thinkbiganalytics.nifi.v2.ingest.ComponentProperties.FEED_FORMAT_SPECS;
import static com.thinkbiganalytics.nifi.v2.ingest.ComponentProperties.FEED_NAME;
import static com.thinkbiganalytics.nifi.v2.ingest.ComponentProperties.FIELD_SPECIFICATION;
import static com.thinkbiganalytics.nifi.v2.ingest.ComponentProperties.PARTITION_SPECS;
import static com.thinkbiganalytics.nifi.v2.ingest.ComponentProperties.REL_FAILURE;
import static com.thinkbiganalytics.nifi.v2.ingest.ComponentProperties.REL_SUCCESS;
import static com.thinkbiganalytics.nifi.v2.ingest.ComponentProperties.TARGET_FORMAT_SPECS;
import static com.thinkbiganalytics.nifi.v2.ingest.ComponentProperties.TARGET_TBLPROPERTIES;
import static com.thinkbiganalytics.nifi.v2.ingest.ComponentProperties.THRIFT_SERVICE;


@EventDriven
@InputRequirement(InputRequirement.Requirement.INPUT_REQUIRED)
@Tags({"hive", "ddl", "register", "thinkbig"})
@CapabilityDescription("Creates a set of standard feed tables managed by the Think Big platform. ")
public class RegisterFeedTables extends AbstractProcessor {

    private static String DEFAULT_STORAGE_FORMAT = "STORED AS ORC";

    private static String DEFAULT_FEED_FORMAT_OPTIONS = "ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' LINES TERMINATED BY '\n' STORED AS TEXTFILE";

    /**
     * Specify creation of all tables
     */
    public static String ALL_TABLES = "ALL";

    // Relationships
    private final Set<Relationship> relationships;

    public static final PropertyDescriptor TABLE_TYPE = new PropertyDescriptor.Builder()
        .name("Table Type")
        .description("Specifies the standard table type to create or ALL for standard set.")
        .required(true)
        .allowableValues(TableType.FEED.toString(), TableType.VALID.toString(), TableType.INVALID.toString(), TableType.PROFILE.toString(), TableType.MASTER.toString(), ALL_TABLES)
        .defaultValue("ALL")
        .build();

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
        pds.add(FEED_FORMAT_SPECS);
        pds.add(TARGET_FORMAT_SPECS);
        pds.add(TARGET_TBLPROPERTIES);

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
        final ProcessorLog logger = getLogger();
        FlowFile flowFile = session.get();
        if (flowFile == null) {
            return;
        }

        final ThriftService thriftService = context.getProperty(THRIFT_SERVICE).asControllerService(ThriftService.class);

        try (final Connection conn = thriftService.getConnection()) {

            String entity = context.getProperty(FEED_NAME).evaluateAttributeExpressions(flowFile).getValue();
            String source = context.getProperty(FEED_CATEGORY).evaluateAttributeExpressions(flowFile).getValue();
            String feedFormatOptions = context.getProperty(FEED_FORMAT_SPECS).evaluateAttributeExpressions(flowFile).getValue();
            String targetFormatOptions = context.getProperty(TARGET_FORMAT_SPECS).evaluateAttributeExpressions(flowFile).getValue();
            String partitionSpecs = context.getProperty(PARTITION_SPECS).evaluateAttributeExpressions(flowFile).getValue();
            String targetTableProperties = context.getProperty(TARGET_TBLPROPERTIES).evaluateAttributeExpressions(flowFile).getValue();
            ColumnSpec[] partitions = ColumnSpec.createFromString(partitionSpecs);
            String specString = context.getProperty(FIELD_SPECIFICATION).evaluateAttributeExpressions(flowFile).getValue();
            ColumnSpec[] columnSpecs = ColumnSpec.createFromString(specString);
            String tableType = context.getProperty(TABLE_TYPE).evaluateAttributeExpressions(flowFile).getValue();

            // Maintain backward compatibility
            if (StringUtils.isEmpty(targetFormatOptions)) {
                targetFormatOptions = DEFAULT_STORAGE_FORMAT;
            }

            if (StringUtils.isEmpty(feedFormatOptions)) {
                feedFormatOptions = DEFAULT_FEED_FORMAT_OPTIONS;
            }

            TableRegisterSupport register = new TableRegisterSupport(conn);

            Boolean result;
            if (ALL_TABLES.equals(tableType)) {
                result = register.registerStandardTables(source, entity, feedFormatOptions, targetFormatOptions, partitions, columnSpecs, targetTableProperties);
            } else {
                result = register.registerTable(source, entity, feedFormatOptions, targetFormatOptions, partitions, columnSpecs, targetTableProperties, TableType.valueOf(tableType));
            }
            Relationship relnResult = (result ? REL_SUCCESS : REL_FAILURE);

            session.transfer(flowFile, relnResult);

        } catch (final ProcessException | SQLException e) {
            logger.error("Unable to obtain connection for {} due to {}; routing to failure", new Object[]{flowFile, e});
            session.transfer(flowFile, REL_FAILURE);
        }
    }

}