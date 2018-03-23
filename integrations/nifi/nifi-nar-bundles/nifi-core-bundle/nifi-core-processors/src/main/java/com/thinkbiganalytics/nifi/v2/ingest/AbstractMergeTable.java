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

import com.thinkbiganalytics.ingest.TableMergeSyncSupport;
import com.thinkbiganalytics.nifi.processor.AbstractNiFiProcessor;
import com.thinkbiganalytics.nifi.v2.thrift.ThriftService;
import com.thinkbiganalytics.util.ColumnSpec;
import com.thinkbiganalytics.util.PartitionSpec;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.expression.AttributeExpression;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.flowfile.attributes.CoreAttributes;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.util.StopWatch;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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


public abstract class AbstractMergeTable extends AbstractNiFiProcessor {

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
    public static final PropertyDescriptor MERGE_STRATEGY = new PropertyDescriptor.Builder()
        .name("Merge Strategy")
        .description(
            "Specifies the algorithm used to merge. Valid values are SYNC,MERGE, PK_MERGE, DEDUPE_AND_MERGE, and ROLLING_SYNC.  Sync will completely overwrite the target table with the source data. "
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
    public static final PropertyDescriptor HIVE_CONFIGURATIONS = new PropertyDescriptor.Builder()
        .name("Hive Configurations")
        .description("Pipe separated list of Hive Configurations that you would like to set for Hive queries ")
        .required(false)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    public static final PropertyDescriptor RESET_HIVE = new PropertyDescriptor.Builder()
        .name("Reset hive on connection")
        .description("Upon getting a new connection it will call Hive 'reset' to reset hive settings back to the default.")
        .allowableValues("true", "false")
        .defaultValue("true")
        .required(true)
        .build();


    public static final PropertyDescriptor BLOCKING_KEY = new PropertyDescriptor.Builder()
        .name("blocking-id")
        .displayName("Blocking Id")
        .description("A value, or the results of an Attribute Expression Language statement, which will " +
                     "be evaluated against a FlowFile in order to determine the blocking key.  If more than 1 flowfile enter this processor with the same blocking key the flowfile will be penalized and routed to the 'blocked' relationship where it can retry if needed.")
        .required(false)
        .addValidator(StandardValidators.createAttributeExpressionLanguageValidator(AttributeExpression.ResultType.STRING, true))
        .expressionLanguageSupported(true)
        .build();

    public static final Relationship REL_BLOCKED = new Relationship.Builder()
        .description("A block detection will route the FlowFile here.").name("blocked").build();

    private Set<Relationship> relationships;

    /**
     * Relationships that will be used if the property BLOCKING_KEY is not specified
     */
    private final Set<Relationship> nonBlockingRelationships;

    /**
     * Relationships that will be used if the property BLOCKING_KEY is specified
     */
    private final Set<Relationship> blockingRelationships;

    private final List<PropertyDescriptor> propDescriptors;


    /**
     * Flag to determine if we are blocking or not based upon any value set in the BLOCKING_KEY property
     */
    private volatile boolean blocking = false;

    public AbstractMergeTable() {
        final Set<Relationship> r = new HashSet<>();
        r.add(REL_SUCCESS);
        r.add(REL_FAILURE);
        nonBlockingRelationships = Collections.unmodifiableSet(r);

        Set<Relationship> tempBlockingRelationships = new HashSet<>();
        tempBlockingRelationships.add(REL_SUCCESS);
        tempBlockingRelationships.add(REL_FAILURE);
        tempBlockingRelationships.add(REL_BLOCKED);
        blockingRelationships = Collections.unmodifiableSet(tempBlockingRelationships);

        relationships = nonBlockingRelationships;

        List<PropertyDescriptor> pds = new ArrayList<>();
        pds.add(THRIFT_SERVICE);
        pds.add(MERGE_STRATEGY);
        pds.add(SOURCE_SCHEMA);
        pds.add(SOURCE_TABLE);
        pds.add(TARGET_SCHEMA);
        pds.add(TARGET_TABLE);
        pds.add(FEED_PARTITION);
        pds.add(PARTITION_SPECIFICATION);
        pds.add(FIELD_SPECIFICATION);
        pds.add(HIVE_CONFIGURATIONS);
        pds.add(RESET_HIVE);
        pds.add(BLOCKING_KEY);
        addPropertyDescriptors(pds);
        propDescriptors = Collections.unmodifiableList(pds);
    }

    public void addPropertyDescriptors(final List<PropertyDescriptor> pds) {

    }

    @Override
    public Set<Relationship> getRelationships() {
        return relationships;
    }

    /**
     * Property that is added to the flow file if the file is blocked
     */
    private static String BLOCKED_START_TIME = "blocked.start.time";

    /**
     * Property added to the flow file if it was previously blocked to track total block time
     */
    private static String BLOCKED_TIME = "blocked.time";

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return propDescriptors;
    }

    public Connection getConnection(ProcessContext context) {
        ThriftService thriftService = context.getProperty(THRIFT_SERVICE).asControllerService(ThriftService.class);
        return thriftService.getConnection();
    }

    /**
     * Cache of keys that should be blocked.
     * The key into this map is the evaluation of the BLOCKING_KEY property.
     */
    private Map<String, String> blockingCache = new ConcurrentHashMap<>();

    /**
     * Detect when the BLOCKING_KEY value changes.
     * if it does and it has a value we need to update the relationships accordingly to allow for the 'blocked' relationship
     */
    @Override
    public void onPropertyModified(final PropertyDescriptor descriptor, final String oldValue, final String newValue) {
        super.onPropertyModified(descriptor, oldValue, newValue);

        if (descriptor.equals(BLOCKING_KEY)) {
            if (StringUtils.isNotBlank(newValue)) {
                blocking = true;
                relationships = blockingRelationships;
            } else {
                blocking = false;
                relationships = nonBlockingRelationships;
            }
        }
    }

    /**
     * Release the blocking value from the map
     * This should be called when processing is done for the flow file.
     */
    private void release(String blockingValue) {
        if(StringUtils.isNotBlank(blockingValue)) {
            blockingCache.remove(blockingValue);
        }
    }


    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
        final ComponentLog logger = getLog();
        FlowFile flowFile = session.get();
        if (flowFile == null) {
            return;
        }
        final String blockingValue = context.getProperty(BLOCKING_KEY).evaluateAttributeExpressions(flowFile).getValue();

        String flowFileId = flowFile.getAttribute(CoreAttributes.UUID.key());
        boolean block = false;

        //determine if this flow file should be blocked

        if (blocking && blockingCache.putIfAbsent(blockingValue, flowFileId) != null) {
            if (StringUtils.isBlank(flowFile.getAttribute(BLOCKED_START_TIME))) {
                flowFile = session.putAttribute(flowFile, BLOCKED_START_TIME, String.valueOf(System.currentTimeMillis()));
                getLogger().info("Transferring Flow file {} to blocked relationship", new Object[]{flowFile});
            }
            //penalize the flow file and transfer to BLOCKED
            flowFile = session.penalize(flowFile);
            session.transfer(flowFile, REL_BLOCKED);
            return;
        }

        //Add Blocking time to flow file if this was a blocked flowfile.
        if (blocking && StringUtils.isNotBlank(flowFile.getAttribute(BLOCKED_START_TIME))) {
            String blockedStartTime = flowFile.getAttribute(BLOCKED_START_TIME);
            try {
                Long l = Long.parseLong(blockedStartTime);
                Long blockTime = System.currentTimeMillis() - l;
                getLogger().info("Processing Blocked flow file {}.  This was blocked for {} ms", new Object[]{flowFile, blockTime});
                flowFile = session.putAttribute(flowFile, BLOCKED_TIME, String.valueOf(blockTime) + " ms");
            } catch (NumberFormatException e) {

            }
        }

        String PROVENANCE_EXECUTION_STATUS_KEY = context.getName() + " Execution Status";

        String partitionSpecString = context.getProperty(PARTITION_SPECIFICATION).evaluateAttributeExpressions(flowFile).getValue();
        String sourceSchema = context.getProperty(SOURCE_SCHEMA).evaluateAttributeExpressions(flowFile).getValue();
        String sourceTable = context.getProperty(SOURCE_TABLE).evaluateAttributeExpressions(flowFile).getValue();
        String targetSchema = context.getProperty(TARGET_SCHEMA).evaluateAttributeExpressions(flowFile).getValue();
        String targetTable = context.getProperty(TARGET_TABLE).evaluateAttributeExpressions(flowFile).getValue();
        String feedPartitionValue = context.getProperty(FEED_PARTITION).evaluateAttributeExpressions(flowFile).getValue();
        String mergeStrategyValue = context.getProperty(MERGE_STRATEGY).evaluateAttributeExpressions(flowFile).getValue();
        String hiveConfigurations = context.getProperty(HIVE_CONFIGURATIONS).evaluateAttributeExpressions(flowFile).getValue();
        boolean resetHive = context.getProperty(RESET_HIVE).asBoolean();
        final ColumnSpec[] columnSpecs = Optional.ofNullable(context.getProperty(FIELD_SPECIFICATION).evaluateAttributeExpressions(flowFile).getValue())
            .filter(StringUtils::isNotEmpty)
            .map(ColumnSpec::createFromString)
            .orElse(new ColumnSpec[0]);

        if (STRATEGY_PK_MERGE.equals(mergeStrategyValue) && (columnSpecs == null || columnSpecs.length == 0)) {
            getLog().error("Missing required field specification for PK merge feature");
            flowFile = session.putAttribute(flowFile, PROVENANCE_EXECUTION_STATUS_KEY, "Failed: Missing required field specification for PK merge feature");
            release(blockingValue);
            session.transfer(flowFile, IngestProperties.REL_FAILURE);
            return;
        }

        // Maintain default for backward compatibility
        if (StringUtils.isEmpty(mergeStrategyValue)) {
            mergeStrategyValue = STRATEGY_DEDUPE_MERGE;
        }

        logger.info(
            "Merge strategy: " + mergeStrategyValue + " Using Source: " + sourceTable + " Target: " + targetTable + " feed partition:" + feedPartitionValue + " partSpec: " + partitionSpecString);

        final StopWatch stopWatch = new StopWatch(true);

        try (final Connection conn = getConnection(context)) {

            TableMergeSyncSupport mergeSupport = new TableMergeSyncSupport(conn);
            if (resetHive) {
                mergeSupport.resetHiveConf();
            }
            mergeSupport.enableDynamicPartitions();

            if (StringUtils.isNotEmpty(hiveConfigurations)) {
                mergeSupport.setHiveConf(hiveConfigurations.split("\\|"));
            }

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
            flowFile = session.putAttribute(flowFile, PROVENANCE_EXECUTION_STATUS_KEY, "Successful");
            release(blockingValue);
            session.transfer(flowFile, REL_SUCCESS);

        } catch (final Exception e) {
            logger.error("Unable to execute merge doMerge for {} due to {}; routing to failure", new Object[]{flowFile, e}, e);
            flowFile = session.putAttribute(flowFile, PROVENANCE_EXECUTION_STATUS_KEY, "Failed: " + e.getMessage());
            release(blockingValue);
            session.transfer(flowFile, REL_FAILURE);
        }
    }
}
