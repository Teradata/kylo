/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.nifi.v2.ingest;

import com.thinkbiganalytics.ingest.GetTableDataSupport;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataProviderService;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataRecorder;
import com.thinkbiganalytics.nifi.thrift.api.AbstractRowVisitor;
import com.thinkbiganalytics.util.ComponentAttributes;
import com.thinkbiganalytics.util.JdbcCommon;

import org.apache.commons.lang3.Validate;
import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.behavior.InputRequirement.Requirement;
import org.apache.nifi.annotation.behavior.TriggerWhenEmpty;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.dbcp.DBCPService;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.logging.ProcessorLog;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.io.OutputStreamCallback;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.util.LongHolder;
import org.apache.nifi.util.StopWatch;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import static com.thinkbiganalytics.nifi.v2.ingest.ComponentProperties.FEED_CATEGORY;
import static com.thinkbiganalytics.nifi.v2.ingest.ComponentProperties.FEED_NAME;
import static com.thinkbiganalytics.nifi.v2.ingest.ComponentProperties.METADATA_SERVICE;
import static com.thinkbiganalytics.nifi.v2.ingest.ComponentProperties.REL_FAILURE;
import static com.thinkbiganalytics.nifi.v2.ingest.ComponentProperties.REL_SUCCESS;

@TriggerWhenEmpty
@InputRequirement(Requirement.INPUT_FORBIDDEN)
@Tags({"thinkbig", "table", "jdbc", "query", "database"})
@CapabilityDescription(
    "Extracts data from a JDBC source table and can optional extract incremental data if provided criteria. Query result will be converted to CSV format. Streaming is used so arbitrarily large result sets are supported. This processor can be scheduled to run on a timer, or cron expression, using the standard scheduling methods, or it can be triggered by an incoming FlowFile. If it is triggered by an incoming FlowFile, then attributes of that FlowFile will be available when evaluating the select query. FlowFile attribute \'source.row.count\' indicates how many rows were selected.")

// Implements strategies outlined by https://thebibackend.wordpress.com/2011/05/18/incremental-load-part-i-overview/
public class GetTableData extends AbstractProcessor {

    public static final String RESULT_ROW_COUNT = "source.row.count";

    public enum LoadStrategy {
        FULL_LOAD,
        INCREMENTAL;

        @Override
        public String toString() {
            switch (this) {
                case FULL_LOAD:
                    return "FULL_LOAD";
                case INCREMENTAL:
                    return "INCREMENTAL";
            }
            return null;
        }
    }

    private final Set<Relationship> relationships;

    // Relationships

    public static final Relationship REL_NODATA = new Relationship.Builder()
        .name("nodata")
        .description("Successful but no new data to process.")
        .build();

    public static final PropertyDescriptor JDBC_SERVICE = new PropertyDescriptor.Builder()
        .name("Source Database Connection")
        .description("The database where the source table resides")
        .required(true)
        .identifiesControllerService(DBCPService.class)
        .build();

    public static final PropertyDescriptor TABLE_NAME = new PropertyDescriptor.Builder()
        .name("Source Table")
        .description("Name of table including schema (if applicable)")
        .required(true)
        .defaultValue("${metadata.table.sourceTableSchema.name}")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    public static final PropertyDescriptor TABLE_SPECS = new PropertyDescriptor.Builder()
        .name("Source Fields")
        .description(
            "Field names (in order) to read from the source table. ie. the select fields. The format is separated by newline. Inconsistent order will cause corruption of the downstream Hive data.")
        .required(true)
        .defaultValue("${metadata.table.sourceFields}")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    public static final PropertyDescriptor
        LOAD_STRATEGY =
        new PropertyDescriptor.Builder().name("Load Strategy").description("Whether to load the entire table or perform an incremental extract")
            .required(true)
            .allowableValues(LoadStrategy.values()).defaultValue(LoadStrategy.FULL_LOAD.toString()).build();

    public static final PropertyDescriptor DATE_FIELD = new PropertyDescriptor.Builder()
        .name("Date Field")
        .description("Source field containing a modified date for tracking incremental load")
        .required(false)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    public static final PropertyDescriptor OVERLAP_TIME = new PropertyDescriptor.Builder()
        .name("Overlap Period")
        .description("Amount of time to overlap into the last load date to ensure long running transactions missed by previous load weren't missed. Recommended: >0s")
        .required(false)
        .addValidator(StandardValidators.TIME_PERIOD_VALIDATOR)
        .defaultValue("0 seconds")
        .expressionLanguageSupported(true)
        .build();

    public static final PropertyDescriptor QUERY_TIMEOUT = new PropertyDescriptor.Builder()
        .name("Max Wait Time")
        .description("The maximum amount of time allowed for a running SQL select query "
                     + " , zero means there is no limit. Max time less than 1 second will be equal to zero.")
        .defaultValue("0 seconds")
        .required(true)
        .addValidator(StandardValidators.TIME_PERIOD_VALIDATOR)
        .sensitive(false)
        .build();

    public static final PropertyDescriptor BACKOFF_PERIOD = new PropertyDescriptor.Builder()
        .name("Backoff Period")
        .description("Only records older than the backoff period will be eligible for pickup. This can be used in the ILM use case to define a retention period. Recommended: >5m")
        .defaultValue("300 seconds")
        .required(true)
        .addValidator(StandardValidators.TIME_PERIOD_VALIDATOR)
        .sensitive(false)
        .build();

    public static final PropertyDescriptor UNIT_SIZE = new PropertyDescriptor.Builder()
        .name("Minimum Time Unit")
        .description("The minimum unit of data eligible to load. For the ILM case, this would be DAY, WEEK, MONTH, YEAR"
                     + " , zero means there is no limit. Max time less than 1 second will be equal to zero.")
        .allowableValues(GetTableDataSupport.UnitSizes.values()).required(true).defaultValue(GetTableDataSupport.UnitSizes.NONE.toString()).build();

    private final List<PropertyDescriptor> propDescriptors;

    public GetTableData() {
        HashSet r = new HashSet();

        r.add(REL_SUCCESS);
        r.add(REL_FAILURE);
        r.add(REL_NODATA);
        this.relationships = Collections.unmodifiableSet(r);
        ArrayList pds = new ArrayList();
        pds.add(JDBC_SERVICE);
        pds.add(METADATA_SERVICE);
        pds.add(FEED_CATEGORY);
        pds.add(FEED_NAME);
        pds.add(TABLE_NAME);
        pds.add(TABLE_SPECS);
        pds.add(LOAD_STRATEGY);
        pds.add(DATE_FIELD);
        pds.add(OVERLAP_TIME);
        pds.add(QUERY_TIMEOUT);
        pds.add(BACKOFF_PERIOD);
        pds.add(UNIT_SIZE);
        this.propDescriptors = Collections.unmodifiableList(pds);
    }

    private String[] parseFields(String selectFields) {

        Validate.notEmpty(selectFields);
        Vector<String> fields = new Vector<>();
        String[] values = selectFields.split("\n");
        for (int i = 0; i < values.length; i++) {
            fields.add(values[i].trim());
        }
        return fields.toArray(new String[0]);
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
        FlowFile flowFile = null;
        if (context.hasIncomingConnection()) {
            flowFile = session.get();

            // If we have no FlowFile, and all incoming connections are self-loops then we can continue on.
            // However, if we have no FlowFile and we have connections coming from other Processors, then
            // we know that we should run only if we have a FlowFile.
            if (flowFile == null && context.hasNonLoopConnection()) {
                return;
            }
        }

        final FlowFile incoming = flowFile;
        final ProcessorLog logger = getLogger();

        final DBCPService dbcpService = context.getProperty(JDBC_SERVICE).asControllerService(DBCPService.class);
        final MetadataProviderService metadataService = context.getProperty(METADATA_SERVICE).asControllerService(MetadataProviderService.class);
        final String loadStrategy = context.getProperty(LOAD_STRATEGY).evaluateAttributeExpressions(incoming).getValue();
        final String categoryName = context.getProperty(FEED_CATEGORY).evaluateAttributeExpressions(incoming).getValue();
        final String feedName = context.getProperty(FEED_NAME).evaluateAttributeExpressions(incoming).getValue();
        final String tableName = context.getProperty(TABLE_NAME).evaluateAttributeExpressions(incoming).getValue();
        final String fieldSpecs = context.getProperty(TABLE_SPECS).evaluateAttributeExpressions(incoming).getValue();
        final String dateField = context.getProperty(DATE_FIELD).evaluateAttributeExpressions(incoming).getValue();
        final Integer queryTimeout = context.getProperty(QUERY_TIMEOUT).asTimePeriod(TimeUnit.SECONDS).intValue();
        final Integer overlapTime = context.getProperty(OVERLAP_TIME).asTimePeriod(TimeUnit.SECONDS).intValue();
        final Integer backoffTime = context.getProperty(BACKOFF_PERIOD).asTimePeriod(TimeUnit.SECONDS).intValue();
        final String unitSize = context.getProperty(UNIT_SIZE).evaluateAttributeExpressions(incoming).getValue();

        final String[] selectFields = parseFields(fieldSpecs);
        final MetadataRecorder recorder = metadataService.getRecorder();

        final LoadStrategy strategy = LoadStrategy.valueOf(loadStrategy);
        final StopWatch stopWatch = new StopWatch(true);
        final Map<String, Object> metadata = new HashMap<>();

        try (final Connection conn = dbcpService.getConnection()) {

            final LongHolder nrOfRows = new LongHolder(0L);
            FlowFile outgoing = (incoming == null ? session.create() : incoming);

            outgoing = session.write(outgoing, new OutputStreamCallback() {
                @Override
                public void process(final OutputStream out) throws IOException {
                    ResultSet rs = null;
                    try {
                        Date lastLoadDate = null;
                        LastFieldVisitor visitor = null;
                        GetTableDataSupport support = new GetTableDataSupport(conn, queryTimeout);
                        if (strategy == LoadStrategy.FULL_LOAD) {
                            rs = support.selectFullLoad(tableName, selectFields);
                        } else if (strategy == LoadStrategy.INCREMENTAL) {

                            // TODO: restore when working
                            //lastLoadDate = recorder.getLastLoadTime(session, incoming, categoryName).toDate();
                            lastLoadDate = recorder.getLastLoadTime(categoryName, feedName).toDate();
                            visitor = new LastFieldVisitor(dateField, lastLoadDate);
                            rs = support.selectIncremental(tableName, selectFields, dateField, overlapTime, lastLoadDate, backoffTime, GetTableDataSupport.UnitSizes.valueOf(unitSize));
                        } else {
                            throw new RuntimeException("Unsupported loadStrategy [" + loadStrategy + "]");
                        }
                        nrOfRows.set(JdbcCommon.convertToCSVStream(rs, out, visitor));
                        if (strategy == LoadStrategy.INCREMENTAL) {
                            metadata.put(ComponentAttributes.PREVIOUS_HIGHWATER_DATE.key(), lastLoadDate);
                            metadata.put(ComponentAttributes.NEW_HIGHWATER_DATE.key(), visitor.getLastModifyDate());
                        }

                    } catch (final SQLException e) {
                        throw new IOException("SQL execution failure", e);
                    } finally {
                        if (rs != null) {
                            try {
                                if (rs.getStatement() != null) {
                                    rs.getStatement().close();
                                }
                                rs.close();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });

            // set attribute how many rows were selected
            outgoing = session.putAttribute(outgoing, RESULT_ROW_COUNT, nrOfRows.get().toString());

            session.getProvenanceReporter().modifyContent(outgoing, "Retrieved " + nrOfRows.get() + " rows", stopWatch.getElapsed(TimeUnit.MILLISECONDS));

            // Terminate flow file if no work
            Long rowcount = nrOfRows.get();
            outgoing = session.putAttribute(outgoing, ComponentAttributes.NUM_SOURCE_RECORDS.key(), String.valueOf(rowcount));

            if (nrOfRows.get() == 0L) {
                logger.info("{} contains no data; transferring to 'nodata'", new Object[]{outgoing});
                session.transfer(outgoing, REL_NODATA);
            } else {

                logger.info("{} contains {} records; transferring to 'success'", new Object[]{outgoing, nrOfRows.get()});

                if (strategy == LoadStrategy.INCREMENTAL) {
                    Date previousHighwater = (Date) metadata.get(ComponentAttributes.PREVIOUS_HIGHWATER_DATE.key());
                    Date newHighwater = (Date) metadata.get(ComponentAttributes.NEW_HIGHWATER_DATE.key());
                    outgoing = session.putAttribute(outgoing, ComponentAttributes.PREVIOUS_HIGHWATER_DATE.key(), prettyDate(previousHighwater));
                    outgoing = session.putAttribute(outgoing, ComponentAttributes.NEW_HIGHWATER_DATE.key(), prettyDate(newHighwater));

                    //recorder.recordLastLoadTime(session, incoming, categoryName, new DateTime(newHighwater));
                    recorder.recordLastLoadTime(categoryName, feedName, new DateTime(newHighwater));

                    logger.info("Recorded load status feed {} date {}", new Object[]{feedName, newHighwater});
                }
                session.transfer(outgoing, REL_SUCCESS);
            }
        } catch (final Exception e) {
            if (incoming == null) {
                logger.error("Unable to execute SQL select from table due to {}. No incoming flow file to route to failure", new Object[]{e});
            } else {
                logger.error("Unable to execute SQL select from table due to {}; routing to failure", new Object[]{incoming, e});
                session.transfer(incoming, REL_FAILURE);
            }
        }
    }

    private static String prettyDate(Date date) {
        return (date == null ? "" : ISODateTimeFormat.dateTime().print(date.getTime()));
    }

    /**
     * Track the max date we read
     */
    static class LastFieldVisitor extends AbstractRowVisitor {

        private String colName;
        private Date lastModifyDate = new Date(0L);

        public LastFieldVisitor(String lastModifyColumnName, Date lastModifyDate) {
            this.colName = lastModifyColumnName;
            Validate.notEmpty(colName);
            this.lastModifyDate = (lastModifyDate == null ? new Date(0L) : lastModifyDate);
        }

        @Override
        public void visitColumn(String columnName, int colType, Date value) {
            if (colName.equals(columnName)) {
                if (value != null) {
                    if (value.after(lastModifyDate)) {
                        lastModifyDate = new Date(value.getTime());
                    }
                }
            }
        }

        public Date getLastModifyDate() {
            return lastModifyDate;
        }
    }
}