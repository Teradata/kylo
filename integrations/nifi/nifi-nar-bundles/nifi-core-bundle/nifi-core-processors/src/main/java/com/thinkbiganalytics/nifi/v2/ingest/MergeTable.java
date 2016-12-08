/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.nifi.v2.ingest;

import com.thinkbiganalytics.ingest.TableMergeSyncSupport;
import com.thinkbiganalytics.nifi.processor.AbstractNiFiProcessor;
import com.thinkbiganalytics.nifi.v2.thrift.ThriftService;
import com.thinkbiganalytics.util.ColumnSpec;
import com.thinkbiganalytics.util.PartitionSpec;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.annotation.behavior.EventDriven;
import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.util.StopWatch;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.thinkbiganalytics.nifi.v2.ingest.IngestProperties.FEED_PARTITION;
import static com.thinkbiganalytics.nifi.v2.ingest.IngestProperties.FIELD_SPECIFICATION;
import static com.thinkbiganalytics.nifi.v2.ingest.IngestProperties.PARTITION_SPECIFICATION;
import static com.thinkbiganalytics.nifi.v2.ingest.IngestProperties.REL_FAILURE;
import static com.thinkbiganalytics.nifi.v2.ingest.IngestProperties.REL_SUCCESS;
import static com.thinkbiganalytics.nifi.v2.ingest.IngestProperties.SOURCE_SCHEMA;
import static com.thinkbiganalytics.nifi.v2.ingest.IngestProperties.SOURCE_TABLE;
import static com.thinkbiganalytics.nifi.v2.ingest.IngestProperties.TARGET_SCHEMA;
import static com.thinkbiganalytics.nifi.v2.ingest.IngestProperties.TARGET_TABLE;
import static com.thinkbiganalytics.nifi.v2.ingest.IngestProperties.THRIFT_SERVICE;

@EventDriven
@InputRequirement(InputRequirement.Requirement.INPUT_ALLOWED)
@Tags({"hive", "ddl", "merge", "sync", "thinkbig"})
@CapabilityDescription("Fully synchronize or Merge values from a feed partition into the target table optionally supporting de-dupe and overwriting partitions. Sync will overwrite the entire table "
                       + "to match the source."
)
public class MergeTable extends AbstractNiFiProcessor {

    /**
     * Merge using primary key
     **/
    public static final String STRATEGY_PK_MERGE = "PK_MERGE";

    /**
     * Merge with dedupe
     **/
    public static final String STRATEGY_DEDUPE_MERGE = "DEDUPE_AND_MERGE";
    /**
     * Merge allowing duplicates
     **/
    public static final String STRATEGY_MERGE = "MERGE";

    /**
     * Sync replace everything in table
     **/
    public static final String STRATEGY_SYNC = "SYNC";

    /**
     * Rolling SYNC same as SYNC but at a partition level overwriting only partitions present in source.
     **/
    public static final String STRATEGY_ROLLING_SYNC = "ROLLING_SYNC";

    private final Set<Relationship> relationships;

    public static final PropertyDescriptor MERGE_STRATEGY = new PropertyDescriptor.Builder()
        .name("Merge Strategy")
        .description("Specifies the algorithm used to merge. Valid values are SYNC,MERGE, PK_MERGE, DEDUPE_AND_MERGE, and ROLLING_SYNC.  Sync will completely overwrite the target table with the source data. "
                     + "Rolling Sync will overwrite target partitions only when present in source. "
                     + "Merge will append "
                     + "the data into the target partitions. Dedupe will insert into the target partition but ensure no duplicate rows are remaining. PK Merge will insert or update existing rows "
                     + "matching the"
                     + " same primary key.")
        .required(true)
        .expressionLanguageSupported(true)
        .allowableValues(STRATEGY_MERGE, STRATEGY_DEDUPE_MERGE, STRATEGY_PK_MERGE, STRATEGY_SYNC, STRATEGY_ROLLING_SYNC, "${metadata.table.targetMergeStrategy}")
        .defaultValue("${metadata.table.targetMergeStrategy}")
        .build();


    private final List<PropertyDescriptor> propDescriptors;

    public MergeTable() {
        final Set<Relationship> r = new HashSet<>();
        r.add(REL_SUCCESS);
        r.add(REL_FAILURE);
        relationships = Collections.unmodifiableSet(r);

        final List<PropertyDescriptor> pds = new ArrayList<>();
        pds.add(THRIFT_SERVICE);
        pds.add(MERGE_STRATEGY);
        pds.add(SOURCE_SCHEMA);
        pds.add(SOURCE_TABLE);
        pds.add(TARGET_SCHEMA);
        pds.add(TARGET_TABLE);
        pds.add(FEED_PARTITION);
        pds.add(PARTITION_SPECIFICATION);
        pds.add(FIELD_SPECIFICATION);

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
        final ComponentLog logger = getLog();
        FlowFile flowFile = session.get();
        if (flowFile == null) {
            return;
        }

        ThriftService thriftService = context.getProperty(THRIFT_SERVICE).asControllerService(ThriftService.class);
        String partitionSpecString = context.getProperty(PARTITION_SPECIFICATION).evaluateAttributeExpressions(flowFile).getValue();
        String sourceSchema = context.getProperty(SOURCE_SCHEMA).evaluateAttributeExpressions(flowFile).getValue();
        String sourceTable = context.getProperty(SOURCE_TABLE).evaluateAttributeExpressions(flowFile).getValue();
        String targetSchema = context.getProperty(TARGET_SCHEMA).evaluateAttributeExpressions(flowFile).getValue();
        String targetTable = context.getProperty(TARGET_TABLE).evaluateAttributeExpressions(flowFile).getValue();
        String feedPartitionValue = context.getProperty(FEED_PARTITION).evaluateAttributeExpressions(flowFile).getValue();
        String mergeStrategyValue = context.getProperty(MERGE_STRATEGY).evaluateAttributeExpressions(flowFile).getValue();
        String strategyOverride = flowFile.getAttribute("MERGE_STRATEGY_OVERRIDE");

        mergeStrategyValue = StringUtils.isEmpty(strategyOverride) ? mergeStrategyValue : strategyOverride;

        final ColumnSpec[] columnSpecs = Optional.ofNullable(context.getProperty(FIELD_SPECIFICATION).evaluateAttributeExpressions(flowFile).getValue())
            .filter(StringUtils::isNotEmpty)
            .map(ColumnSpec::createFromString)
            .orElse(new ColumnSpec[0]);

        if (STRATEGY_PK_MERGE.equals(mergeStrategyValue) && (columnSpecs == null || columnSpecs.length == 0)) {
            getLog().error("Missing required field specification for PK merge feature");
            session.transfer(flowFile, IngestProperties.REL_FAILURE);
            return;
        }

        // Maintain default for backward compatibility
        if (StringUtils.isEmpty(mergeStrategyValue)) {
            mergeStrategyValue = STRATEGY_DEDUPE_MERGE;
        }

        logger.info("Merge strategy: " + mergeStrategyValue + " Using Source: " + sourceTable + " Target: " + targetTable + " feed partition:" + feedPartitionValue + " partSpec: " + partitionSpecString);

        final StopWatch stopWatch = new StopWatch(true);

        try (final Connection conn = thriftService.getConnection()) {

            TableMergeSyncSupport mergeSupport = new TableMergeSyncSupport(conn);
            mergeSupport.enableDynamicPartitions();

            PartitionSpec partitionSpec = new PartitionSpec(partitionSpecString);

            if (STRATEGY_DEDUPE_MERGE.equals(mergeStrategyValue)) {
                mergeSupport.doMerge(sourceSchema, sourceTable, targetSchema, targetTable, partitionSpec, feedPartitionValue, true);
            } else if (STRATEGY_MERGE.equals(mergeStrategyValue)) {
                mergeSupport.doMerge(sourceSchema, sourceTable, targetSchema, targetTable, partitionSpec, feedPartitionValue, false);
            } else if (STRATEGY_SYNC.equals(mergeStrategyValue)) {
                mergeSupport.doSync(sourceSchema, sourceTable, targetSchema, targetTable, partitionSpec, feedPartitionValue);
            } else if (STRATEGY_ROLLING_SYNC.equals(mergeStrategyValue)) {
                mergeSupport.doRollingSync(sourceSchema, sourceTable, targetSchema, targetTable, partitionSpec, feedPartitionValue);
            } else if (STRATEGY_PK_MERGE.equals(mergeStrategyValue)) {
                mergeSupport.doPKMerge(sourceSchema, sourceTable, targetSchema, targetTable, partitionSpec, feedPartitionValue, columnSpecs);
            } else {
                throw new UnsupportedOperationException("Failed to resolve the merge strategy");
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
