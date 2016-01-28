/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.nifi;


import com.thinkbiganalytics.components.TableMergeSupport;
import com.thinkbiganalytics.controller.ThriftService;
import com.thinkbiganalytics.util.PartitionSpec;
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
import java.util.concurrent.TimeUnit;


@EventDriven
@InputRequirement(InputRequirement.Requirement.INPUT_ALLOWED)
@Tags({"hive", "ddl", "merge", "thinkbig"})
@CapabilityDescription("Merge values from a feed partition into the target table optionally supporting de-dupe and overwriting partitions."
)
public class TableMerge extends AbstractProcessor {

    // Relationships
    public static final Relationship REL_SUCCESS = new Relationship.Builder()
            .name("success")
            .description("Successfully created FlowFile from .")
            .build();
    public static final Relationship REL_FAILURE = new Relationship.Builder()
            .name("failure")
            .description("SQL query execution failed. Incoming FlowFile will be penalized and routed to this relationship")
            .build();
    private final Set<Relationship> relationships;

    public static final PropertyDescriptor THRIFT_SERVICE = new PropertyDescriptor.Builder()
            .name("Database Connection Pooling Service")
            .description("The Controller Service that is used to obtain connection to database")
            .required(true)
            .identifiesControllerService(ThriftService.class)
            .build();

    public static final PropertyDescriptor SOURCE_TABLE = new PropertyDescriptor.Builder()
            .name("Source table")
            .description("Fully qualified name of the source table")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .expressionLanguageSupported(true)
            .build();
    public static final PropertyDescriptor TARGET_TABLE = new PropertyDescriptor.Builder()
            .name("Target table")
            .description("Fully qualified name of the target table")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .expressionLanguageSupported(true)
            .build();

    public static final PropertyDescriptor FEED_PARTITION = new PropertyDescriptor.Builder()
            .name("Feed partition value")
            .description("Feed timestamp that identifies the current feed partition")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .expressionLanguageSupported(true)
            .build();

    public static final PropertyDescriptor PARTITION_SPECIFICATION = new PropertyDescriptor.Builder()
            .name("Partition Specification")
            .description("Partition specification in format: field|type|formula\nfield|type|formula")
            .required(false)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .expressionLanguageSupported(true)
            .build();

    private final List<PropertyDescriptor> propDescriptors;

    public TableMerge() {
        final Set<Relationship> r = new HashSet<>();
        r.add(REL_SUCCESS);
        r.add(REL_FAILURE);
        relationships = Collections.unmodifiableSet(r);

        final List<PropertyDescriptor> pds = new ArrayList<>();
        pds.add(THRIFT_SERVICE);
        pds.add(SOURCE_TABLE);
        pds.add(TARGET_TABLE);
        pds.add(FEED_PARTITION);
        pds.add(PARTITION_SPECIFICATION);
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
        FlowFile outgoing = (incoming == null ? session.create() : incoming);

        ThriftService thriftService = context.getProperty(THRIFT_SERVICE).asControllerService(ThriftService.class);
        String partitionSpecString = context.getProperty(PARTITION_SPECIFICATION).evaluateAttributeExpressions(outgoing).getValue();
        String sourceTable = context.getProperty(SOURCE_TABLE).evaluateAttributeExpressions(outgoing).getValue();
        String targetTable = context.getProperty(TARGET_TABLE).evaluateAttributeExpressions(outgoing).getValue();
        String feedPartitionValue = context.getProperty(FEED_PARTITION).evaluateAttributeExpressions(outgoing).getValue();

        logger.info("Using Source: " + sourceTable + " Target: " + targetTable + " feed partition:" + feedPartitionValue + " partSpec: " + partitionSpecString);

        final StopWatch stopWatch = new StopWatch(true);

        try (final Connection conn = thriftService.getConnection()) {

            TableMergeSupport mergeSupport = new TableMergeSupport(conn);
            PartitionSpec partitionSpec = new PartitionSpec(partitionSpecString);
            mergeSupport.doDedupe(sourceTable, targetTable, partitionSpec, feedPartitionValue);

            stopWatch.stop();
            session.getProvenanceReporter().modifyContent(outgoing, "Execution completed", stopWatch.getElapsed(TimeUnit.MILLISECONDS));
            session.transfer(outgoing, REL_SUCCESS);

        } catch (final Exception e) {
            logger.error("Unable to execute merge dedupe for {} due to {}; routing to failure", new Object[]{incoming, e});
            session.transfer(incoming, REL_FAILURE);
        }
    }
}