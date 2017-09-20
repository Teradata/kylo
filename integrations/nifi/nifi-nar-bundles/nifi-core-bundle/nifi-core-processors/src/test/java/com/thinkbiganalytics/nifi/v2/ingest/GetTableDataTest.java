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
import com.thinkbiganalytics.nifi.core.api.metadata.KyloNiFiFlowProvider;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataProvider;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataProviderService;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataRecorder;
import com.thinkbiganalytics.nifi.v2.common.CommonProperties;
import com.thinkbiganalytics.util.ComponentAttributes;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.SeekableByteArrayInput;
import org.apache.avro.file.SeekableInput;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.util.Utf8;
import org.apache.nifi.components.ValidationResult;
import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.dbcp.DBCPService;
import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.MockProcessContext;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.joda.time.DateTimeUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.Nonnull;

public class GetTableDataTest {

    /**
     * Identifier for JDBC service
     */
    private static final String JDBC_SERVICE_IDENTIFIER = "MockDBCPService";

    /**
     * Identifier for Metadata service
     */
    private static final String METADATA_SERVICE_IDENTIFIER = "MockMetadataProviderService";

    /**
     * Test runner
     */
    private final TestRunner runner = TestRunners.newTestRunner(GetTableData.class);

    /**
     * Initialize instance variables
     */
    @Before
    public void setUp() throws Exception {
        // Setup services
        final DBCPService jdbcService = new MockDBCPService();
        final MetadataProviderService metadataService = new MockMetadataService();

        // Setup test runner
        runner.addControllerService(JDBC_SERVICE_IDENTIFIER, jdbcService);
        runner.addControllerService(METADATA_SERVICE_IDENTIFIER, metadataService);
        runner.enableControllerService(jdbcService);
        runner.enableControllerService(metadataService);
        runner.setProperty(GetTableData.JDBC_SERVICE, JDBC_SERVICE_IDENTIFIER);
        runner.setProperty(CommonProperties.METADATA_SERVICE, METADATA_SERVICE_IDENTIFIER);
        runner.setProperty(GetTableData.TABLE_NAME, "mytable");
        runner.setProperty(GetTableData.TABLE_SPECS, "id\nfirst_name\nlast_name\nemail  \n last_updated  \n\n");
    }

    /**
     * Reset current time
     */
    @After
    public void tearDown() {
        DateTimeUtils.setCurrentMillisSystem();
    }

    /**
     * Verify property validators
     */
    @Test
    public void testValidators() {
        // Test with no properties
        runner.removeProperty(GetTableData.JDBC_SERVICE);
        runner.removeProperty(CommonProperties.METADATA_SERVICE);
        List<ValidationResult> results = (List<ValidationResult>) ((MockProcessContext) runner.getProcessContext()).validate();
        if (results.size() != 2) {
            Assert.fail("Expected 2 validation errors but found: " + results);
        }
        Assert.assertEquals("'Source Database Connection' is invalid because Source Database Connection is required", results.get(0).toString());
        Assert.assertEquals("'Metadata Service' is invalid because Metadata Service is required", results.get(1).toString());

        // Test with valid properties
        runner.setProperty(GetTableData.JDBC_SERVICE, JDBC_SERVICE_IDENTIFIER);
        runner.setProperty(CommonProperties.METADATA_SERVICE, METADATA_SERVICE_IDENTIFIER);
        results = (List<ValidationResult>) ((MockProcessContext) runner.getProcessContext()).validate();
        Assert.assertEquals(0, results.size());
    }

    /**
     * Verify Avro output.
     */
    @Test
    public void testAvro() throws IOException {
        // Trigger processor
        runner.setProperty(GetTableData.OUTPUT_TYPE, GetTableDataSupport.OutputType.AVRO.toString());
        runner.enqueue(new byte[0]);
        runner.run();

        List<MockFlowFile> flowFiles = runner.getFlowFilesForRelationship(CommonProperties.REL_SUCCESS);
        Assert.assertEquals(0, runner.getFlowFilesForRelationship(CommonProperties.REL_FAILURE).size());
        Assert.assertEquals(0, runner.getFlowFilesForRelationship(GetTableData.REL_NO_DATA).size());
        Assert.assertEquals(1, flowFiles.size());
        Assert.assertEquals("2", flowFiles.get(0).getAttribute(GetTableData.RESULT_ROW_COUNT));
        Assert.assertEquals("2", flowFiles.get(0).getAttribute(ComponentAttributes.NUM_SOURCE_RECORDS.key()));

        // Build Avro record reader
        final SeekableInput avroInput = new SeekableByteArrayInput(flowFiles.get(0).toByteArray());
        final Schema schema = SchemaBuilder
            .record("NiFi_ExecuteSQL_Record").namespace("any.data")
            .fields()
            .name("id").type().nullable().intType().noDefault()
            .name("first_name").type().nullable().stringType().noDefault()
            .name("last_name").type().nullable().stringType().noDefault()
            .name("email").type().nullable().stringType().noDefault()
            .name("last_updated").type().nullable().stringType().noDefault()
            .endRecord();
        final DatumReader<GenericRecord> datumReader = new GenericDatumReader<>(schema);
        final DataFileReader<GenericRecord> dataReader = new DataFileReader<>(avroInput, datumReader);

        // Verify Avro records
        List<GenericRecord> records = StreamSupport.stream(dataReader.spliterator(), false).collect(Collectors.toList());
        Assert.assertEquals(2, records.size());

        Assert.assertEquals(1, records.get(0).get(0));
        Assert.assertEquals(new Utf8("Mike"), records.get(0).get(1));
        Assert.assertEquals(new Utf8("Hillyer"), records.get(0).get(2));
        Assert.assertEquals(new Utf8("Mike.Hillyer@sakilastaff.com"), records.get(0).get(3));
        Assert.assertEquals(new Utf8("2006-02-15T03:57:16.000Z"), records.get(0).get(4));

        Assert.assertEquals(2, records.get(1).get(0));
        Assert.assertEquals(new Utf8("Jon"), records.get(1).get(1));
        Assert.assertEquals(new Utf8("Stephens"), records.get(1).get(2));
        Assert.assertEquals(new Utf8("Jon.Stephens@sakilastaff.com"), records.get(1).get(3));
        Assert.assertEquals(new Utf8("2006-02-15T03:57:16.000Z"), records.get(1).get(4));
    }

    /**
     * Verify an incremental load from the database.
     */
    @Test
    public void testIncremental() {
        // Test first load
        DateTimeUtils.setCurrentMillisFixed(1139979600000L);
        runner.setProperty(GetTableData.LOAD_STRATEGY, GetTableData.LoadStrategy.INCREMENTAL.toString());
        runner.setProperty(GetTableData.DATE_FIELD, "last_updated");
        runner.setProperty(GetTableData.OVERLAP_TIME, "5 minutes");
        runner.enqueue(new byte[0], Collections.singletonMap(ComponentAttributes.HIGH_WATER_DATE.key(), null));
        runner.run();

        List<MockFlowFile> flowFiles = runner.getFlowFilesForRelationship(CommonProperties.REL_SUCCESS);
        Assert.assertEquals(0, runner.getFlowFilesForRelationship(CommonProperties.REL_FAILURE).size());
        Assert.assertEquals(0, runner.getFlowFilesForRelationship(GetTableData.REL_NO_DATA).size());
        Assert.assertEquals(1, flowFiles.size());
        Assert.assertEquals("3", flowFiles.get(0).getAttribute(GetTableData.RESULT_ROW_COUNT));
        Assert.assertEquals("3", flowFiles.get(0).getAttribute(ComponentAttributes.NUM_SOURCE_RECORDS.key()));
        Assert.assertEquals("2006-02-15T04:47:30", flowFiles.get(0).getAttribute(ComponentAttributes.HIGH_WATER_DATE.key()));
        flowFiles.get(0).assertContentEquals("id,first_name,last_name,email,last_updated\n"
                                             + "1,MARY,SMITH,MARY.SMITH@sakilacustomer.org,2006-02-15T04:32:21.000Z\n"
                                             + "2,PATRICIA,JOHNSON,PATRICIA.JOHNSON@sakilacustomer.org,2006-02-15T04:25:50.000Z\n"
                                             + "3,LINDA,WILLIAMS,LINDA.WILLIAMS@sakilacustomer.org,2006-02-15T04:47:30.000Z\n");

        // Test second load
        DateTimeUtils.setCurrentMillisFixed(1139983200000L);
        runner.clearTransferState();
        runner.enqueue(new byte[0], Collections.singletonMap(ComponentAttributes.HIGH_WATER_DATE.key(), "2006-02-15T04:47:30.000Z"));
        runner.run();

        flowFiles = runner.getFlowFilesForRelationship(CommonProperties.REL_SUCCESS);
        Assert.assertEquals(0, runner.getFlowFilesForRelationship(CommonProperties.REL_FAILURE).size());
        Assert.assertEquals(0, runner.getFlowFilesForRelationship(GetTableData.REL_NO_DATA).size());
        Assert.assertEquals(1, flowFiles.size());
        Assert.assertEquals("3", flowFiles.get(0).getAttribute(GetTableData.RESULT_ROW_COUNT));
        Assert.assertEquals("3", flowFiles.get(0).getAttribute(ComponentAttributes.NUM_SOURCE_RECORDS.key()));
        Assert.assertEquals("2006-02-15T05:15:33", flowFiles.get(0).getAttribute(ComponentAttributes.HIGH_WATER_DATE.key()));
        flowFiles.get(0).assertContentEquals("id,first_name,last_name,email,last_updated\n"
                                             + "3,LINDA,WILLIAMS,LINDA.WILLIAMS@sakilacustomer.org,2006-02-15T04:47:30.000Z\n"
                                             + "4,BARBARA,JONES,BARBARA.JONES@sakilacustomer.org,2006-02-15T04:57:14.000Z\n"
                                             + "5,ELIZABETH,BROWN,ELIZABETH.BROWN@sakilacustomer.org,2006-02-15T05:15:33.000Z\n");
    }

    /**
     * Verify a full load from the database.
     */
    @Test
    public void testFullLoad() {
        runner.enqueue(new byte[0]);
        runner.run();

        List<MockFlowFile> flowFiles = runner.getFlowFilesForRelationship(CommonProperties.REL_SUCCESS);
        Assert.assertEquals(0, runner.getFlowFilesForRelationship(CommonProperties.REL_FAILURE).size());
        Assert.assertEquals(0, runner.getFlowFilesForRelationship(GetTableData.REL_NO_DATA).size());
        Assert.assertEquals(1, flowFiles.size());
        Assert.assertEquals("2", flowFiles.get(0).getAttribute(GetTableData.RESULT_ROW_COUNT));
        Assert.assertEquals("2", flowFiles.get(0).getAttribute(ComponentAttributes.NUM_SOURCE_RECORDS.key()));
        flowFiles.get(0).assertContentEquals("id,first_name,last_name,email,last_updated\n"
                                             + "1,Mike,Hillyer,Mike.Hillyer@sakilastaff.com,2006-02-15T03:57:16.000Z\n"
                                             + "2,Jon,Stephens,Jon.Stephens@sakilastaff.com,2006-02-15T03:57:16.000Z\n");
    }

    /**
     * Verify output for no data.
     */
    @Test
    public void testNoData() {
        runner.setProperty(GetTableData.TABLE_NAME, "empty");
        runner.setProperty(GetTableData.TABLE_SPECS, "id\nemail");
        runner.enqueue(new byte[0]);
        runner.run();

        List<MockFlowFile> flowFiles = runner.getFlowFilesForRelationship(GetTableData.REL_NO_DATA);
        Assert.assertEquals(0, runner.getFlowFilesForRelationship(CommonProperties.REL_FAILURE).size());
        Assert.assertEquals(0, runner.getFlowFilesForRelationship(CommonProperties.REL_SUCCESS).size());
        Assert.assertEquals(1, flowFiles.size());
        Assert.assertEquals("0", flowFiles.get(0).getAttribute(GetTableData.RESULT_ROW_COUNT));
        Assert.assertEquals("0", flowFiles.get(0).getAttribute(ComponentAttributes.NUM_SOURCE_RECORDS.key()));
        flowFiles.get(0).assertContentEquals("id,email\n");
    }

    /**
     * Verify using a custom output delimiter.
     */
    @Test
    public void testOutputDelimiter() {
        runner.setProperty(GetTableData.OUTPUT_DELIMITER, "|");
        runner.enqueue(new byte[0]);
        runner.run();

        List<MockFlowFile> flowFiles = runner.getFlowFilesForRelationship(CommonProperties.REL_SUCCESS);
        Assert.assertEquals(0, runner.getFlowFilesForRelationship(CommonProperties.REL_FAILURE).size());
        Assert.assertEquals(0, runner.getFlowFilesForRelationship(GetTableData.REL_NO_DATA).size());
        Assert.assertEquals(1, flowFiles.size());
        Assert.assertEquals("2", flowFiles.get(0).getAttribute(GetTableData.RESULT_ROW_COUNT));
        Assert.assertEquals("2", flowFiles.get(0).getAttribute(ComponentAttributes.NUM_SOURCE_RECORDS.key()));
        flowFiles.get(0).assertContentEquals("id|first_name|last_name|email|last_updated\n"
                                             + "1|Mike|Hillyer|Mike.Hillyer@sakilastaff.com|2006-02-15T03:57:16.000Z\n"
                                             + "2|Jon|Stephens|Jon.Stephens@sakilastaff.com|2006-02-15T03:57:16.000Z\n");
    }

    /**
     * A mock implementation of {@link DBCPService} for unit testing.
     */
    private class MockDBCPService extends AbstractControllerService implements DBCPService {

        /**
         * A SQL connection
         */
        final Connection connection = Mockito.mock(Connection.class);

        /**
         * A SQL statement
         */
        final Statement statement = Mockito.mock(Statement.class);

        /**
         * Database metadata
         */
        final DatabaseMetaData databaseMetaData = Mockito.mock(DatabaseMetaData.class);

        /**
         * Constructs a {@code MockDBCPService}.
         */
        MockDBCPService() throws Exception {
            Mockito.when(connection.createStatement()).thenReturn(statement);
            Mockito.when(connection.prepareStatement("select tbl.id,tbl.first_name,tbl.last_name,tbl.email,tbl.last_updated from mytable tbl WHERE tbl.last_updated > ? and tbl.last_updated < ?"))
                .then(invocation -> getIncrementalResults());

            Mockito.when(statement.executeQuery("SELECT tbl.id,tbl.email FROM empty tbl")).then(invocation -> getEmptyResults());
            Mockito.when(statement.executeQuery("SELECT tbl.id,tbl.first_name,tbl.last_name,tbl.email,tbl.last_updated FROM mytable tbl")).then(invocation -> getSimpleResults());

            Mockito.when(connection.getMetaData()).thenReturn(databaseMetaData);
            Mockito.when(databaseMetaData.getIdentifierQuoteString()).thenReturn("");
        }

        @Override
        public Connection getConnection() {
            return connection;
        }

        /**
         * Creates a {@link ResultSet} for the specified metadata and rows.
         *
         * @param metadata the result set metadata
         * @param rows     the rows in the result set
         * @return a new result set
         * @throws SQLException never
         */
        ResultSet getResultSet(@Nonnull final ResultSetMetaData metadata, @Nonnull final Object[][] rows) throws SQLException {
            final LinkedList<Object[]> queue = new LinkedList<>(Arrays.asList(rows));
            queue.push(new Object[0]);

            final ResultSet results = Mockito.mock(ResultSet.class);
            Mockito.when(results.getMetaData()).thenReturn(metadata);
            Mockito.when(results.next()).then(invocation -> {
                queue.poll();
                return !queue.isEmpty();
            });
            Mockito.when(results.getDate(Mockito.anyInt())).then(invocation -> {
                final int index = invocation.getArgumentAt(0, Integer.class) - 1;
                final Object object = queue.peek()[index];
                if (object instanceof Date) {
                    return object;
                } else {
                    throw new SQLException("Not a Date: " + object);
                }
            });
            Mockito.when(results.getObject(Mockito.anyInt())).then(invocation -> {
                final int index = invocation.getArgumentAt(0, Integer.class) - 1;
                return queue.peek()[index];
            });
            Mockito.when(results.getString(Mockito.anyInt())).then(invocation -> {
                final int index = invocation.getArgumentAt(0, Integer.class) - 1;
                return queue.peek()[index].toString();
            });
            Mockito.when(results.getTime(Mockito.anyInt())).then(invocation -> {
                final int index = invocation.getArgumentAt(0, Integer.class) - 1;
                final Object object = queue.peek()[index];
                if (object instanceof Time) {
                    return object;
                } else {
                    throw new SQLException("Not a Time: " + object);
                }
            });
            Mockito.when(results.getTimestamp(Mockito.anyInt())).then(invocation -> {
                final int index = invocation.getArgumentAt(0, Integer.class) - 1;
                final Object object = queue.peek()[index];
                if (object instanceof Timestamp) {
                    return object;
                } else {
                    throw new SQLException("Not a Timestamp: " + object);
                }
            });

            return results;
        }

        /**
         * Creates an empty result set.
         *
         * @return a new result set
         * @throws SQLException never
         */
        ResultSet getEmptyResults() throws SQLException {
            final ResultSetMetaData metadata = Mockito.mock(ResultSetMetaData.class);
            Mockito.when(metadata.getColumnCount()).thenReturn(2);
            Mockito.when(metadata.getColumnName(1)).thenReturn("id");
            Mockito.when(metadata.getColumnName(2)).thenReturn("email");
            Mockito.when(metadata.getColumnType(1)).thenReturn(Types.TINYINT);
            Mockito.when(metadata.getColumnType(2)).thenReturn(Types.VARCHAR);

            return getResultSet(metadata, new Object[0][]);
        }

        /**
         * Creates a prepared statement for incremental results.
         *
         * @return a new prepared statement
         * @throws SQLException never
         */
        PreparedStatement getIncrementalResults() throws SQLException {
            final ResultSetMetaData metadata = Mockito.mock(ResultSetMetaData.class);
            Mockito.when(metadata.getColumnCount()).thenReturn(5);
            Mockito.when(metadata.getColumnName(1)).thenReturn("id");
            Mockito.when(metadata.getColumnName(2)).thenReturn("first_name");
            Mockito.when(metadata.getColumnName(3)).thenReturn("last_name");
            Mockito.when(metadata.getColumnName(4)).thenReturn("email");
            Mockito.when(metadata.getColumnName(5)).thenReturn("last_updated");
            Mockito.when(metadata.getColumnType(1)).thenReturn(Types.TINYINT);
            Mockito.when(metadata.getColumnType(2)).thenReturn(Types.VARCHAR);
            Mockito.when(metadata.getColumnType(3)).thenReturn(Types.VARCHAR);
            Mockito.when(metadata.getColumnType(4)).thenReturn(Types.VARCHAR);
            Mockito.when(metadata.getColumnType(5)).thenReturn(Types.TIMESTAMP);

            final Object[][] rows = new Object[][]{
                new Object[]{1, "MARY", "SMITH", "MARY.SMITH@sakilacustomer.org", new Timestamp(1139977941000L)},
                new Object[]{2, "PATRICIA", "JOHNSON", "PATRICIA.JOHNSON@sakilacustomer.org", new Timestamp(1139977550000L)},
                new Object[]{3, "LINDA", "WILLIAMS", "LINDA.WILLIAMS@sakilacustomer.org", new Timestamp(1139978850000L)},
                new Object[]{4, "BARBARA", "JONES", "BARBARA.JONES@sakilacustomer.org", new Timestamp(1139979434000L)},
                new Object[]{5, "ELIZABETH", "BROWN", "ELIZABETH.BROWN@sakilacustomer.org", new Timestamp(1139980533000L)},
                };

            final AtomicReference<Timestamp> before = new AtomicReference<>();
            final AtomicReference<Timestamp> after = new AtomicReference<>();

            final PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
            Mockito.when(preparedStatement.executeQuery()).then(invocation -> {
                final Object[][] selectedRows = Stream.of(rows).filter(row -> ((Timestamp) row[4]).after(after.get()) && ((Timestamp) row[4]).before(before.get())).toArray(Object[][]::new);
                return getResultSet(metadata, selectedRows);
            });
            Mockito.doAnswer(invocation -> {
                after.set(invocation.getArgumentAt(1, Timestamp.class));
                return after.get();
            }).when(preparedStatement).setTimestamp(Mockito.eq(1), Mockito.any());
            Mockito.doAnswer(invocation -> {
                before.set(invocation.getArgumentAt(1, Timestamp.class));
                return before.get();
            }).when(preparedStatement).setTimestamp(Mockito.eq(2), Mockito.any());
            return preparedStatement;
        }

        /**
         * Creates a simple result set.
         *
         * @return a new result set
         * @throws SQLException never
         */
        ResultSet getSimpleResults() throws SQLException {
            final ResultSetMetaData metadata = Mockito.mock(ResultSetMetaData.class);
            Mockito.when(metadata.getColumnCount()).thenReturn(5);
            Mockito.when(metadata.getColumnName(1)).thenReturn("id");
            Mockito.when(metadata.getColumnName(2)).thenReturn("first_name");
            Mockito.when(metadata.getColumnName(3)).thenReturn("last_name");
            Mockito.when(metadata.getColumnName(4)).thenReturn("email");
            Mockito.when(metadata.getColumnName(5)).thenReturn("last_updated");
            Mockito.when(metadata.getColumnType(1)).thenReturn(Types.TINYINT);
            Mockito.when(metadata.getColumnType(2)).thenReturn(Types.VARCHAR);
            Mockito.when(metadata.getColumnType(3)).thenReturn(Types.VARCHAR);
            Mockito.when(metadata.getColumnType(4)).thenReturn(Types.VARCHAR);
            Mockito.when(metadata.getColumnType(5)).thenReturn(Types.TIMESTAMP);
            Mockito.when(metadata.getTableName(Mockito.anyInt())).thenReturn("mytable");

            final Object[][] rows = new Object[][]{
                new Object[]{1, "Mike", "Hillyer", "Mike.Hillyer@sakilastaff.com", new Timestamp(1139975836000L)},
                new Object[]{2, "Jon", "Stephens", "Jon.Stephens@sakilastaff.com", new Timestamp(1139975836000L)}
            };

            return getResultSet(metadata, rows);
        }
    }

    /**
     * A mock implementation of {@link MetadataProviderService} for unit testing.
     */
    private class MockMetadataService extends AbstractControllerService implements MetadataProviderService {

        @Override
        public KyloNiFiFlowProvider getKyloNiFiFlowProvider() {
            throw new UnsupportedOperationException();
        }

        @Override
        public MetadataProvider getProvider() {
            throw new UnsupportedOperationException();
        }

        @Override
        public MetadataRecorder getRecorder() {
            throw new UnsupportedOperationException();
        }
    }
}
