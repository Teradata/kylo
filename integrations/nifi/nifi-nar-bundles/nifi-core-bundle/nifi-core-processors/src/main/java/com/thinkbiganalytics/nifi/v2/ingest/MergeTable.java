/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.nifi.v2.ingest;


import com.thinkbiganalytics.ingest.TableMergeSupport;
import com.thinkbiganalytics.nifi.v2.thrift.ThriftService;
import com.thinkbiganalytics.util.ComponentAttributes;
import com.thinkbiganalytics.util.PartitionBatch;
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
import java.util.*;
import java.util.concurrent.TimeUnit;


@EventDriven
@InputRequirement(InputRequirement.Requirement.INPUT_ALLOWED)
@Tags({"hive", "ddl", "merge", "thinkbig"})
@CapabilityDescription("Merge values from a feed partition into the target table optionally supporting de-dupe and overwriting partitions."
)
public class MergeTable extends AbstractProcessor {

    /** Merge with dedupe **/
    public static final String STRATEGY_MERGE_DEDUPE = "DEDUPE";
    /** Merge allowing duplicates **/
    public static final String STRATEGY_MERGE_ALL = "ALL";

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

    public static final PropertyDescriptor MERGE_STRATEGY = new PropertyDescriptor.Builder()
        .name("Merge Strategy")
        .description("Specifies the algorithm used to merge. The 'Dedupe' strategy ensures duplicate rows will not appear in the target.")
        .required(true)
        .allowableValues(STRATEGY_MERGE_ALL, STRATEGY_MERGE_DEDUPE)
        .defaultValue(STRATEGY_MERGE_DEDUPE)
        .build();

    private final List<PropertyDescriptor> propDescriptors;

    public MergeTable() {
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
        pds.add(MERGE_STRATEGY);
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
        if (flowFile == null) return;

        ThriftService thriftService = context.getProperty(THRIFT_SERVICE).asControllerService(ThriftService.class);
        String partitionSpecString = context.getProperty(PARTITION_SPECIFICATION).evaluateAttributeExpressions(flowFile).getValue();
        String sourceTable = context.getProperty(SOURCE_TABLE).evaluateAttributeExpressions(flowFile).getValue();
        String targetTable = context.getProperty(TARGET_TABLE).evaluateAttributeExpressions(flowFile).getValue();
        String feedPartitionValue = context.getProperty(FEED_PARTITION).evaluateAttributeExpressions(flowFile).getValue();
        Boolean shouldDedupe = (STRATEGY_MERGE_DEDUPE.equals(context.getProperty(MERGE_STRATEGY).evaluateAttributeExpressions(flowFile).getValue()));

        logger.info("Using Source: " + sourceTable + " Target: " + targetTable + " feed partition:" + feedPartitionValue + " partSpec: " + partitionSpecString);

        final StopWatch stopWatch = new StopWatch(true);

        try (final Connection conn = thriftService.getConnection()) {

            TableMergeSupport mergeSupport = new TableMergeSupport(conn);
            PartitionSpec partitionSpec = new PartitionSpec(partitionSpecString);
            List<PartitionBatch> batches = mergeSupport.doMerge(sourceTable, targetTable, partitionSpec, feedPartitionValue, shouldDedupe);

            // Record detail of each batch
            if (batches != null) {
                flowFile = session.putAttribute(flowFile, ComponentAttributes.NUM_MERGED_PARTITIONS.key(), String.valueOf(batches.size()));
                int i = 1;
                for (PartitionBatch batch : batches) {
                    flowFile = session.putAttribute(flowFile, ComponentAttributes.MERGED_PARTITION.key() + "." + i, batch.getBatchDescription());
                    flowFile = session.putAttribute(flowFile, ComponentAttributes.MERGED_PARTITION_ROWCOUNT.key() + "." + i, String.valueOf(batch.getRecordCount()));
                }
            }

            stopWatch.stop();
            session.getProvenanceReporter().modifyContent(flowFile, "Execution completed", stopWatch.getElapsed(TimeUnit.MILLISECONDS));
            session.transfer(flowFile, REL_SUCCESS);

        } catch (final Exception e) {
            logger.error("Unable to execute merge doMerge for {} due to {}; routing to failure", new Object[]{flowFile, e});
            session.transfer(flowFile, REL_FAILURE);
        }
    }
}