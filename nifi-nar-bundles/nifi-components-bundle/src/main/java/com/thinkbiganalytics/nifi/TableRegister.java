/*
 * Copyright (c) 2015. Teradata Inc.
 */
package com.thinkbiganalytics.nifi;


import com.thinkbiganalytics.components.TableRegisterSupport;
import com.thinkbiganalytics.controller.ThriftService;
import com.thinkbiganalytics.util.ColumnSpec;
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
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.util.StopWatch;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

@EventDriven
@InputRequirement(InputRequirement.Requirement.INPUT_ALLOWED)
@Tags({"hive", "ddl", "register", "thinkbig"})
@CapabilityDescription("Creates a set of tables managed by the Think Big platform. ")
public class TableRegister extends AbstractProcessor {

    // Relationships
    public static final Relationship REL_SUCCESS = new Relationship.Builder()
            .name("success")
            .description("Successfully created tables.")
            .build();
    public static final Relationship REL_FAILURE = new Relationship.Builder()
            .name("failure")
            .description("Table execution failed. Incoming FlowFile will be penalized and routed to this relationship")
            .build();
    private final Set<Relationship> relationships;

    public static final PropertyDescriptor THRIFT_SERVICE = new PropertyDescriptor.Builder()
            .name("Database Connection Pooling Service")
            .description("The Controller Service that is used to obtain connection to database")
            .required(true)
            .identifiesControllerService(ThriftService.class)
            .build();

    public static final PropertyDescriptor SOURCE = new PropertyDescriptor.Builder()
            .name("Source")
            .description("Name representing the source category")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .expressionLanguageSupported(true)
            .build();

    public static final PropertyDescriptor TABLE_ENTITY = new PropertyDescriptor.Builder()
            .name("Table Entity")
            .description("Name of the master table")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .expressionLanguageSupported(true)
            .build();

    public static final PropertyDescriptor COLUMN_SPECS = new PropertyDescriptor.Builder()
            .name("ColumnSpecs")
            .description("Pipe-delim format with the specifications for the columns (column name|data type|comment")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .expressionLanguageSupported(true)
            .build();

    public static final PropertyDescriptor FORMAT_SPECS = new PropertyDescriptor.Builder()
            .name("Format specification")
            .description("Provide format and delimiter specification. This is the full clause starting with the INPUTFORMAT such as: INPUTFORMAT 'org.apache.hadoop.mapred.TextInputFormat' OUTPUTFORMAT 'org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat' ")
            .required(true)
            .defaultValue("ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde' ")
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .expressionLanguageSupported(true)
            .build();

    public static final PropertyDescriptor PARTITION_SPECS = new PropertyDescriptor.Builder()
            .name("Partition specification")
            .description("Provide list of partition columns column-delimited")
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .expressionLanguageSupported(true)
            .build();


    private final List<PropertyDescriptor> propDescriptors;

    public TableRegister() {
        final Set<Relationship> r = new HashSet<>();
        r.add(REL_SUCCESS);
        r.add(REL_FAILURE);
        relationships = Collections.unmodifiableSet(r);

        final List<PropertyDescriptor> pds = new ArrayList<>();
        pds.add(THRIFT_SERVICE);
        pds.add(COLUMN_SPECS);
        pds.add(FORMAT_SPECS);
        pds.add(SOURCE);
        pds.add(PARTITION_SPECS);
        pds.add(TABLE_ENTITY);

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
        FlowFile incoming = session.get();

        final ThriftService thriftService = context.getProperty(THRIFT_SERVICE).asControllerService(ThriftService.class);

        try (final Connection conn = thriftService.getConnection()) {

            FlowFile outgoing = (incoming == null ? session.create() : incoming);

            String entity = context.getProperty(TABLE_ENTITY).evaluateAttributeExpressions(outgoing).getValue();
            String source = context.getProperty(SOURCE).evaluateAttributeExpressions(outgoing).getValue();
            String formatOptions = context.getProperty(FORMAT_SPECS).evaluateAttributeExpressions(outgoing).getValue();
            String partitionSpecs = context.getProperty(PARTITION_SPECS).evaluateAttributeExpressions(outgoing).getValue();
            ColumnSpec[] partitions = ColumnSpec.createFromString(partitionSpecs);
            String specString = context.getProperty(COLUMN_SPECS).evaluateAttributeExpressions(outgoing).getValue();
            ColumnSpec[] columnSpecs = ColumnSpec.createFromString(specString);

            TableRegisterSupport register = new TableRegisterSupport(conn);
            boolean result = register.registerStandardTables(source, entity, formatOptions, partitions, columnSpecs);
            Relationship relnResult = (result ? REL_SUCCESS : REL_FAILURE);

            session.transfer(outgoing, relnResult);

        } catch (final ProcessException | SQLException e) {
            logger.error("Unable to obtain connection for {} due to {}; routing to failure", new Object[]{incoming, e});
            session.transfer(incoming, REL_FAILURE);
        }
    }

}