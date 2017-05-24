package com.thinkbiganalytics.util;

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

import com.thinkbiganalytics.nifi.thrift.api.RowVisitor;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.SeekableByteArrayInput;
import org.apache.avro.file.SeekableInput;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.util.Utf8;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;

import static org.junit.Assert.assertEquals;

public class JdbcCommonTest {

    /**
     * Verify converting results to avro.
     */
    @Test
    public void convertToAvroStream() throws Exception {
        // Mock result set metadata
        final ResultSetMetaData metadata = Mockito.mock(ResultSetMetaData.class);
        Mockito.when(metadata.getColumnCount()).thenReturn(14);
        Mockito.when(metadata.getColumnName(1)).thenReturn("event");
        Mockito.when(metadata.getColumnName(2)).thenReturn("empty");
        Mockito.when(metadata.getColumnName(3)).thenReturn("binary");
        Mockito.when(metadata.getColumnName(4)).thenReturn("byte");
        Mockito.when(metadata.getColumnName(5)).thenReturn("decimal");
        Mockito.when(metadata.getColumnName(6)).thenReturn("maxlong");
        Mockito.when(metadata.getColumnName(7)).thenReturn("date");
        Mockito.when(metadata.getColumnName(8)).thenReturn("time");
        Mockito.when(metadata.getColumnName(9)).thenReturn("timestamp");
        Mockito.when(metadata.getColumnName(10)).thenReturn("bool");
        Mockito.when(metadata.getColumnName(11)).thenReturn("int");
        Mockito.when(metadata.getColumnName(12)).thenReturn("id");
        Mockito.when(metadata.getColumnName(13)).thenReturn("float");
        Mockito.when(metadata.getColumnName(14)).thenReturn("double");
        Mockito.when(metadata.getColumnType(1)).thenReturn(Types.VARCHAR);
        Mockito.when(metadata.getColumnType(2)).thenReturn(Types.VARCHAR);
        Mockito.when(metadata.getColumnType(3)).thenReturn(Types.VARBINARY);
        Mockito.when(metadata.getColumnType(4)).thenReturn(Types.TINYINT);
        Mockito.when(metadata.getColumnType(5)).thenReturn(Types.DECIMAL);
        Mockito.when(metadata.getColumnType(6)).thenReturn(Types.BIGINT);
        Mockito.when(metadata.getColumnType(7)).thenReturn(Types.DATE);
        Mockito.when(metadata.getColumnType(8)).thenReturn(Types.TIME);
        Mockito.when(metadata.getColumnType(9)).thenReturn(Types.TIMESTAMP);
        Mockito.when(metadata.getColumnType(10)).thenReturn(Types.BOOLEAN);
        Mockito.when(metadata.getColumnType(11)).thenReturn(Types.INTEGER);
        Mockito.when(metadata.getColumnType(12)).thenReturn(Types.ROWID);
        Mockito.when(metadata.getColumnType(13)).thenReturn(Types.FLOAT);
        Mockito.when(metadata.getColumnType(14)).thenReturn(Types.DOUBLE);
        Mockito.when(metadata.getTableName(Mockito.anyInt())).thenReturn("mockito");
        Mockito.when(metadata.isSigned(11)).thenReturn(true);

        // Mock result set
        final ResultSet results = Mockito.mock(ResultSet.class);
        Mockito.when(results.getByte(4)).thenReturn((byte) 42);
        Mockito.when(results.getBytes(3)).thenReturn(new byte[]{72, 73});
        Mockito.when(results.getDate(7)).thenReturn(new Date(1483660800000L));
        Mockito.when(results.getMetaData()).thenReturn(metadata);
        Mockito.when(results.getObject(1)).thenReturn("Fun Friday");
        Mockito.when(results.getObject(2)).thenReturn(null);
        Mockito.when(results.getObject(3)).thenReturn(new byte[]{72, 73});
        Mockito.when(results.getObject(4)).thenReturn((byte) 42);
        Mockito.when(results.getObject(5)).thenReturn(new BigDecimal("3.14159265359"));
        Mockito.when(results.getObject(6)).thenReturn(Long.MAX_VALUE);
        Mockito.when(results.getObject(7)).thenReturn(new Date(1483660800000L));
        Mockito.when(results.getObject(8)).thenReturn(new Time(42600000L));
        Mockito.when(results.getObject(9)).thenReturn(new Timestamp(1483703400000L));
        Mockito.when(results.getObject(10)).thenReturn(Boolean.TRUE);
        Mockito.when(results.getObject(11)).thenReturn(12);
        Mockito.when(results.getObject(12)).thenReturn((RowId) () -> new byte[]{1});
        Mockito.when(results.getObject(13)).thenReturn(2.5f);
        Mockito.when(results.getObject(14)).thenReturn(1.61803);
        Mockito.when(results.getTime(8)).thenReturn(new Time(42600000L));
        Mockito.when(results.getTimestamp(7)).thenThrow(SQLException.class);
        Mockito.when(results.getTimestamp(9)).thenReturn(new Timestamp(1483703400000L));
        Mockito.when(results.next()).thenReturn(true).thenReturn(false);

        // Test converting to avro
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final RowVisitor visitor = Mockito.mock(RowVisitor.class);
        Schema avroSchema = JdbcCommon.createSchema(results);
        JdbcCommon.convertToAvroStream(results, out, visitor, avroSchema);

        final InOrder inOrder = Mockito.inOrder(visitor);
        inOrder.verify(visitor).visitRow(results);
        inOrder.verify(visitor).visitColumn("event", Types.VARCHAR, "Fun Friday");
        inOrder.verify(visitor).visitColumn("empty", Types.VARCHAR, (String) null);
        inOrder.verify(visitor).visitColumn(Mockito.eq("binary"), Mockito.eq(Types.VARBINARY), Mockito.anyString());
        inOrder.verify(visitor).visitColumn("byte", Types.TINYINT, "42");
        inOrder.verify(visitor).visitColumn("decimal", Types.DECIMAL, "3.14159265359");
        inOrder.verify(visitor).visitColumn("maxlong", Types.BIGINT, Long.toString(Long.MAX_VALUE));
        inOrder.verify(visitor).visitColumn("date", Types.DATE, new Date(1483660800000L));
        inOrder.verify(visitor).visitColumn("time", Types.TIME, new Time(42600000L));
        inOrder.verify(visitor).visitColumn("timestamp", Types.TIMESTAMP, new Timestamp(1483703400000L));
        inOrder.verify(visitor).visitColumn("bool", Types.BOOLEAN, "true");
        inOrder.verify(visitor).visitColumn("int", Types.INTEGER, "12");
        inOrder.verify(visitor).visitColumn(Mockito.eq("id"), Mockito.eq(Types.ROWID), Mockito.anyString());
        inOrder.verify(visitor).visitColumn("float", Types.FLOAT, "2.5");
        inOrder.verify(visitor).visitColumn("double", Types.DOUBLE, "1.61803");
        inOrder.verifyNoMoreInteractions();

        final Schema schema = SchemaBuilder
            .record("mockito")
            .namespace("any.data")
            .fields()
            .name("event").type().nullable().stringType().noDefault()
            .name("empty").type().nullable().stringType().noDefault()
            .name("binary").type().nullable().bytesType().noDefault()
            .name("byte").type().nullable().intType().noDefault()
            .name("decimal").type().nullable().stringType().noDefault()
            .name("maxlong").type().nullable().longType().noDefault()
            .name("date").type().nullable().stringType().noDefault()
            .name("time").type().nullable().stringType().noDefault()
            .name("timestamp").type().nullable().stringType().noDefault()
            .name("bool").type().nullable().booleanType().noDefault()
            .name("int").type().nullable().intType().noDefault()
            .name("id").type().nullable().stringType().noDefault()
            .name("float").type().nullable().floatType().noDefault()
            .name("double").type().nullable().doubleType().noDefault()
            .endRecord();

        final SeekableInput input = new SeekableByteArrayInput(out.toByteArray());
        final GenericDatumReader<GenericRecord> datumReader = new GenericDatumReader<>(schema);
        final DataFileReader<GenericRecord> dataReader = new DataFileReader<>(input, datumReader);
        final GenericRecord record = dataReader.next();
        assertEquals(new Utf8("Fun Friday"), record.get(0));
        assertEquals(null, record.get(1));
        assertEquals(ByteBuffer.wrap(new byte[]{72, 73}), record.get(2));
        assertEquals(42, record.get(3));
        assertEquals(new Utf8("3.14159265359"), record.get(4));
        assertEquals(Long.MAX_VALUE, record.get(5));
        assertEquals(new Utf8("2017-01-06T00:00:00.000Z"), record.get(6));
        assertEquals(new Utf8("11:50:00.000Z"), record.get(7));
        assertEquals(new Utf8("2017-01-06T11:50:00.000Z"), record.get(8));
        assertEquals(Boolean.TRUE, record.get(9));
        assertEquals(12, record.get(10));
        Assert.assertNotNull(record.get(11));
        assertEquals(2.5f, record.get(12));
        assertEquals(1.61803, record.get(13));
        Assert.assertFalse(dataReader.hasNext());
    }

    /**
     * Verify converting results to delimited text.
     */
    @Test
    public void convertToDelimitedStream() throws Exception {
        // Mock result set metadata
        final ResultSetMetaData metadata = Mockito.mock(ResultSetMetaData.class);
        Mockito.when(metadata.getColumnCount()).thenReturn(6);
        Mockito.when(metadata.getColumnName(1)).thenReturn("event");
        Mockito.when(metadata.getColumnName(2)).thenReturn("empty");
        Mockito.when(metadata.getColumnName(3)).thenReturn("date");
        Mockito.when(metadata.getColumnName(4)).thenReturn("time");
        Mockito.when(metadata.getColumnName(5)).thenReturn("timestamp");
        Mockito.when(metadata.getColumnName(6)).thenReturn("custom");
        Mockito.when(metadata.getColumnType(1)).thenReturn(Types.VARCHAR);
        Mockito.when(metadata.getColumnType(2)).thenReturn(Types.VARCHAR);
        Mockito.when(metadata.getColumnType(3)).thenReturn(Types.DATE);
        Mockito.when(metadata.getColumnType(4)).thenReturn(Types.TIME);
        Mockito.when(metadata.getColumnType(5)).thenReturn(Types.TIMESTAMP);
        Mockito.when(metadata.getColumnType(6)).thenReturn(Types.TIMESTAMP);

        // Mock result set
        final ResultSet results = Mockito.mock(ResultSet.class);
        Mockito.when(results.getDate(3)).thenReturn(new Date(1483660800000L));
        Mockito.when(results.getDate(6)).thenThrow(SQLException.class);
        Mockito.when(results.getMetaData()).thenReturn(metadata);
        Mockito.when(results.getString(1)).thenReturn("Fun Friday");
        Mockito.when(results.getTime(4)).thenReturn(new Time(42600000L));
        Mockito.when(results.getTimestamp(3)).thenThrow(SQLException.class);
        Mockito.when(results.getTimestamp(5)).thenReturn(new Timestamp(1483703400000L));
        Mockito.when(results.getTimestamp(6)).thenThrow(SQLException.class);
        Mockito.when(results.next()).thenReturn(true).thenReturn(false);

        // Test converting to delimited text
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final RowVisitor visitor = Mockito.mock(RowVisitor.class);
        JdbcCommon.convertToDelimitedStream(results, out, visitor, " ");

        final InOrder inOrder = Mockito.inOrder(visitor);
        inOrder.verify(visitor).visitRow(results);
        inOrder.verify(visitor).visitColumn("event", Types.VARCHAR, "Fun Friday");
        inOrder.verify(visitor).visitColumn("empty", Types.VARCHAR, (String) null);
        inOrder.verify(visitor).visitColumn("date", Types.DATE, new Timestamp(1483660800000L));
        inOrder.verify(visitor).visitColumn("time", Types.TIME, new Time(42600000L));
        inOrder.verify(visitor).visitColumn("timestamp", Types.TIMESTAMP, new Timestamp(1483703400000L));
        inOrder.verify(visitor).visitColumn("custom", Types.TIMESTAMP, (Timestamp) null);
        inOrder.verifyNoMoreInteractions();

        assertEquals("event empty date time timestamp custom\n\"Fun Friday\"  2017-01-06T00:00:00.000Z 11:50:00.000Z 2017-01-06T11:50:00.000Z \n", new String(out.toByteArray(), "UTF-8"));
    }

    /**
     * Verify row count for a {@code null} result set.
     */
    @Test
    public void convertToDelimitedStreamWithNull() throws Exception {
        final long count = JdbcCommon.convertToDelimitedStream(null, null, null, ",");
        assertEquals(0L, count);
    }

    /**
     * Verify schema format for setting up feed table
     */
    @Test
    public void getAvroSchemaForFeedSetupTest() {
        final Schema schema = SchemaBuilder
            .record("test_record")
            .namespace("my_schema")
            .fields()
                .name("po_id").type().nullable().intType().intDefault(0)
                .name("customer_name").type().nullable().stringType().stringDefault("null")
                .name("total_amount").type().nullable().stringType().stringDefault("null")
                .name("is_urgent").type().nullable().booleanType().booleanDefault(false)
                .name("last_update_time").type().nullable().stringType().stringDefault("null")
                .name("photo").type().nullable().bytesType().bytesDefault("null")
                .name("identifier").type().nullable().longType().longDefault(0)
                .name("state").type().stringType().stringDefault("null")
            .endRecord();

        String obtained = JdbcCommon.getAvroSchemaForFeedSetup(schema);
        String expected = "po_id|int||0|0|0\n"
                        + "customer_name|string||0|0|0\n"
                        + "total_amount|string||0|0|0\n"
                        + "is_urgent|boolean||0|0|0\n"
                        + "last_update_time|string||0|0|0\n"
                        + "photo|binary||0|0|0\n"
                        + "identifier|bigint||0|0|0\n"
                        + "state|string||0|0|0";

        assertEquals("Avro schema not mapped correctly for feed table setup", expected, obtained);
    }
}
