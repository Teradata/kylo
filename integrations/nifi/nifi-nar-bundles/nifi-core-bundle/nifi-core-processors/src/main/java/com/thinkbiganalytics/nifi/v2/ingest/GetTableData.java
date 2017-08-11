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

import com.thinkbiganalytics.ingest.GetTableDataSupport;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataProviderService;
import com.thinkbiganalytics.nifi.processor.AbstractNiFiProcessor;
import com.thinkbiganalytics.nifi.thrift.api.AbstractRowVisitor;
import com.thinkbiganalytics.util.ComponentAttributes;
import com.thinkbiganalytics.util.JdbcCommon;

import org.apache.avro.Schema;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.nifi.annotation.behavior.EventDriven;
import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.behavior.InputRequirement.Requirement;
import org.apache.nifi.annotation.behavior.TriggerWhenEmpty;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.PropertyValue;
import org.apache.nifi.components.Validator;
import org.apache.nifi.dbcp.DBCPService;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.io.OutputStreamCallback;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.util.StopWatch;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static com.thinkbiganalytics.nifi.v2.common.CommonProperties.FEED_CATEGORY;
import static com.thinkbiganalytics.nifi.v2.common.CommonProperties.FEED_NAME;
import static com.thinkbiganalytics.nifi.v2.common.CommonProperties.METADATA_SERVICE;
import static com.thinkbiganalytics.nifi.v2.ingest.IngestProperties.REL_FAILURE;
import static com.thinkbiganalytics.nifi.v2.ingest.IngestProperties.REL_SUCCESS;

@EventDriven
@TriggerWhenEmpty
@InputRequirement(Requirement.INPUT_ALLOWED)
@Tags({"thinkbig", "table", "jdbc", "query", "database"})
@CapabilityDescription(
    "Extracts data from a JDBC source table and can optional extract incremental data if provided criteria. Query result will be converted to a delimited format, or to Avro if specified. Streaming is used so arbitrarily large result sets are supported. This processor can be scheduled to run on a timer, or cron expression, using the standard scheduling methods, or it can be triggered by an incoming FlowFile. If it is triggered by an incoming FlowFile, then attributes of that FlowFile will be available when evaluating the select query. FlowFile attribute \'source.row.count\' indicates how many rows were selected.")
@WritesAttributes({
        @WritesAttribute(attribute = "db.table.output.format", description = "Output format for database table ingested"),
        @WritesAttribute(attribute = "db.table.avro.schema", description = "Avro schema for the database table ingested")
    })

// Implements strategies outlined by https://thebibackend.wordpress.com/2011/05/18/incremental-load-part-i-overview/
public class GetTableData extends AbstractNiFiProcessor {
    private Schema avroSchema = null;

    public static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ISO_DATE_TIME;
    public static final String RESULT_ROW_COUNT = "source.row.count";
    public static final String EMPTY_STRING = "";

    public static final Relationship REL_NO_DATA = new Relationship.Builder()
        .name("nodata")
        .description("Successful but no new data to process.")
        .build();
    public static final PropertyDescriptor JDBC_SERVICE = new PropertyDescriptor.Builder()
        .name("Source Database Connection")
        .description("The database where the source table resides")
        .required(true)
        .identifiesControllerService(DBCPService.class)
        .build();

    // Relationships
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
    public static final PropertyDescriptor HIGH_WATER_MARK_PROP = new PropertyDescriptor.Builder()
        .name("High-Water Mark Property Name")
        .description("Name of the flow file attribute that should contain the current hig-water mark date, and which this processor will update with new values.  "
                     + "Required if the load strategy is set to INCREMENTAL.")
        .required(false)
        .defaultValue(ComponentAttributes.HIGH_WATER_DATE.key())
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();
    public static final PropertyDescriptor LOAD_STRATEGY = new PropertyDescriptor.Builder()
        .name("Load Strategy")
        .description("Whether to load the entire table or perform an incremental extract")
        .required(true)
        .allowableValues(LoadStrategy.values())
        .defaultValue(LoadStrategy.FULL_LOAD.toString())
        .build();
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
        .allowableValues(GetTableDataSupport.UnitSizes.values())
        .required(true)
        .defaultValue(GetTableDataSupport.UnitSizes.NONE.toString())
        .build();
    public static final PropertyDescriptor OUTPUT_TYPE = new PropertyDescriptor.Builder()
        .name("Output Type")
        .description("How should the results be returned.  Either a Delimited output such as CSV, or AVRO.  If delimited you must specify the delimiter.")
        .allowableValues(GetTableDataSupport.OutputType.values())
        .required(true)
        .defaultValue(GetTableDataSupport.OutputType.DELIMITED.toString())
        .build();
    public static final PropertyDescriptor OUTPUT_DELIMITER = new PropertyDescriptor.Builder()
        .name("Output Delimiter")
        .description(
            "Used only if the Output Type is 'Delimited'.  If this is empty and the Output Type is delimited it will default to a ','.  This property is not used if the Output Type is AVRO.")
        .required(false)
        .addValidator(Validator.VALID)
        .defaultValue(",")
        .expressionLanguageSupported(true)
        .build();
    private final Set<Relationship> relationships;
    private final List<PropertyDescriptor> propDescriptors;

    public GetTableData() {
        HashSet<Relationship> r = new HashSet<>();
        r.add(REL_SUCCESS);
        r.add(REL_FAILURE);
        r.add(REL_NO_DATA);
        this.relationships = Collections.unmodifiableSet(r);

        ArrayList<PropertyDescriptor> pds = new ArrayList<>();
        pds.add(JDBC_SERVICE);
        pds.add(METADATA_SERVICE);
        pds.add(FEED_CATEGORY);
        pds.add(FEED_NAME);
        pds.add(TABLE_NAME);
        pds.add(TABLE_SPECS);
        pds.add(LOAD_STRATEGY);
        pds.add(HIGH_WATER_MARK_PROP);
        pds.add(DATE_FIELD);
        pds.add(OVERLAP_TIME);
        pds.add(QUERY_TIMEOUT);
        pds.add(BACKOFF_PERIOD);
        pds.add(UNIT_SIZE);
        pds.add(OUTPUT_TYPE);
        pds.add(OUTPUT_DELIMITER);
        this.propDescriptors = Collections.unmodifiableList(pds);
    }

    private static LocalDateTime toDateTime(Date date) {
        return date == null ? LocalDateTime.MIN : LocalDateTime.ofInstant(date.toInstant(), ZoneOffset.UTC.normalized());
    }

    public static Date toDate(LocalDateTime dateTime) {
        return dateTime == null ? new Date(0L) : Date.from(dateTime.toInstant(ZoneOffset.UTC));
    }

    private static String format(Date date) {
        return (date == null ? "" : DATE_TIME_FORMAT.format(toDateTime(date)));
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
        final ComponentLog logger = getLog();

        final DBCPService dbcpService = context.getProperty(JDBC_SERVICE).asControllerService(DBCPService.class);
        final MetadataProviderService metadataService = context.getProperty(METADATA_SERVICE).asControllerService(MetadataProviderService.class);
        final String loadStrategy = context.getProperty(LOAD_STRATEGY).getValue();
        final String categoryName = context.getProperty(FEED_CATEGORY).evaluateAttributeExpressions(incoming).getValue();
        final String feedName = context.getProperty(FEED_NAME).evaluateAttributeExpressions(incoming).getValue();
        final String tableName = context.getProperty(TABLE_NAME).evaluateAttributeExpressions(incoming).getValue();
        final String fieldSpecs = context.getProperty(TABLE_SPECS).evaluateAttributeExpressions(incoming).getValue();
        final String dateField = context.getProperty(DATE_FIELD).evaluateAttributeExpressions(incoming).getValue();
        final Integer queryTimeout = context.getProperty(QUERY_TIMEOUT).asTimePeriod(TimeUnit.SECONDS).intValue();
        final Integer overlapTime = context.getProperty(OVERLAP_TIME).evaluateAttributeExpressions(incoming).asTimePeriod(TimeUnit.SECONDS).intValue();
        final Integer backoffTime = context.getProperty(BACKOFF_PERIOD).asTimePeriod(TimeUnit.SECONDS).intValue();
        final String unitSize = context.getProperty(UNIT_SIZE).getValue();
        final String outputType = context.getProperty(OUTPUT_TYPE).getValue();
        String outputDelimiter = context.getProperty(OUTPUT_DELIMITER).evaluateAttributeExpressions(incoming).getValue();
        final String delimiter = StringUtils.isBlank(outputDelimiter) ? "," : outputDelimiter;

        final PropertyValue waterMarkPropName = context.getProperty(HIGH_WATER_MARK_PROP).evaluateAttributeExpressions(incoming);

        final String[] selectFields = parseFields(fieldSpecs);

        final LoadStrategy strategy = LoadStrategy.valueOf(loadStrategy);
        final StopWatch stopWatch = new StopWatch(true);

        try (final Connection conn = dbcpService.getConnection()) {

            FlowFile outgoing = (incoming == null ? session.create() : incoming);
            final AtomicLong nrOfRows = new AtomicLong(0L);
            final LastFieldVisitor visitor = new LastFieldVisitor(dateField, null);
            final FlowFile current = outgoing;

            outgoing = session.write(outgoing, new OutputStreamCallback() {
                @Override
                public void process(final OutputStream out) throws IOException {
                    ResultSet rs = null;
                    try {
                        GetTableDataSupport support = new GetTableDataSupport(conn, queryTimeout);
                        if (strategy == LoadStrategy.FULL_LOAD) {
                            rs = support.selectFullLoad(tableName, selectFields);
                        } else if (strategy == LoadStrategy.INCREMENTAL) {
                            String waterMarkValue = getIncrementalWaterMarkValue(current, waterMarkPropName);
                            LocalDateTime waterMarkTime = LocalDateTime.parse(waterMarkValue, DATE_TIME_FORMAT);
                            Date lastLoadDate = toDate(waterMarkTime);
                            visitor.setLastModifyDate(lastLoadDate);
                            rs = support.selectIncremental(tableName, selectFields, dateField, overlapTime, lastLoadDate, backoffTime, GetTableDataSupport.UnitSizes.valueOf(unitSize));
                        } else {
                            throw new RuntimeException("Unsupported loadStrategy [" + loadStrategy + "]");
                        }

                        if (GetTableDataSupport.OutputType.DELIMITED.equals(GetTableDataSupport.OutputType.valueOf(outputType))) {
                            nrOfRows.set(JdbcCommon.convertToDelimitedStream(rs, out, (strategy == LoadStrategy.INCREMENTAL ? visitor : null), delimiter));
                        } else if (GetTableDataSupport.OutputType.AVRO.equals(GetTableDataSupport.OutputType.valueOf(outputType))){
                            avroSchema = JdbcCommon.createSchema(rs);
                            nrOfRows.set(JdbcCommon.convertToAvroStream(rs, out, (strategy == LoadStrategy.INCREMENTAL ? visitor : null), avroSchema));
                        } else {
                            throw new RuntimeException("Unsupported output format type [" + outputType + "]");
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
                                getLog().error("Error closing sql statement and resultset");
                            }
                        }
                    }
                }
            });

            // set attribute how many rows were selected
            outgoing = session.putAttribute(outgoing, RESULT_ROW_COUNT, Long.toString(nrOfRows.get()));

            //set output format type and avro schema for feed setup, if available
            outgoing = session.putAttribute(outgoing, "db.table.output.format", outputType);
            String avroSchemaForFeedSetup = (avroSchema != null) ? JdbcCommon.getAvroSchemaForFeedSetup(avroSchema) : EMPTY_STRING;
            outgoing = session.putAttribute(outgoing, "db.table.avro.schema", avroSchemaForFeedSetup);

            session.getProvenanceReporter().modifyContent(outgoing, "Retrieved " + nrOfRows.get() + " rows", stopWatch.getElapsed(TimeUnit.MILLISECONDS));

            // Terminate flow file if no work
            Long rowcount = nrOfRows.get();
            outgoing = session.putAttribute(outgoing, ComponentAttributes.NUM_SOURCE_RECORDS.key(), String.valueOf(rowcount));

            if (nrOfRows.get() == 0L) {
                logger.info("{} contains no data; transferring to 'nodata'", new Object[]{outgoing});
                session.transfer(outgoing, REL_NO_DATA);
            } else {

                logger.info("{} contains {} records; transferring to 'success'", new Object[]{outgoing, nrOfRows.get()});

                if (strategy == LoadStrategy.INCREMENTAL) {
                    String newWaterMarkStr = format(visitor.getLastModifyDate());
                    outgoing = setIncrementalWaterMarkValue(session, outgoing, waterMarkPropName, newWaterMarkStr);

                    logger.info("Recorded load status feed {} date {}", new Object[]{feedName, newWaterMarkStr});
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

    private String getIncrementalWaterMarkValue(FlowFile ff, PropertyValue waterMarkPropName) {
        if (!waterMarkPropName.isSet()) {
            // TODO validate when scheduled?
            throw new IllegalArgumentException("The processor is configured for incremental load but the "
                                               + "property 'High-Water Mark Value Property Name' has not been set");
        } else {
            String propName = waterMarkPropName.getValue();

            if (StringUtils.isEmpty(propName)) {
                throw new IllegalArgumentException("The processor is configured for incremental load but the "
                                                   + "property 'High-Water Mark Value Property Name' does not have a value");
            } else {
                String value = ff.getAttribute(propName);

                // This can happen if the feed does not have an initial water mark, and the water mark
                // load processor was not configure with a default value.  In this case default to the epoch.
                if (StringUtils.isEmpty(value)) {
                    value = "1970-01-01T00:00:00";
                }

                return value;
            }
        }
    }

    private FlowFile setIncrementalWaterMarkValue(ProcessSession session, FlowFile ff, PropertyValue waterMarkPropName, String newValue) {
        String propName = waterMarkPropName.getValue();

        if (StringUtils.isEmpty(propName)) {
            throw new IllegalArgumentException("The processor is configured for incremental load but the "
                                               + "property 'High-Water Mark Value Property Name' does not have a value");
        } else {
            return session.putAttribute(ff, propName, newValue);
        }
    }

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

    /**
     * Track the max date we read
     */
    static class LastFieldVisitor extends AbstractRowVisitor {

        private String colName;
        private Date lastModifyDate = new Date(0L);

        public LastFieldVisitor(String lastModifyColumnName, Date lastModifyDate) {
            this.colName = lastModifyColumnName;
            // Validate.notEmpty(colName);
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

        public void setLastModifyDate(Date date) {
            lastModifyDate = date;
        }
    }
}
