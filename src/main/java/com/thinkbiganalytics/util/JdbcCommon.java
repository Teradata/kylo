/*
 * Copyright (c) 2015. Teradata Inc.
 */

package com.thinkbiganalytics.util;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import static java.sql.Types.ARRAY;
import static java.sql.Types.BIGINT;
import static java.sql.Types.BINARY;
import static java.sql.Types.BIT;
import static java.sql.Types.BLOB;
import static java.sql.Types.BOOLEAN;
import static java.sql.Types.CHAR;
import static java.sql.Types.CLOB;
import static java.sql.Types.DATE;
import static java.sql.Types.DECIMAL;
import static java.sql.Types.DOUBLE;
import static java.sql.Types.FLOAT;
import static java.sql.Types.INTEGER;
import static java.sql.Types.LONGNVARCHAR;
import static java.sql.Types.LONGVARBINARY;
import static java.sql.Types.LONGVARCHAR;
import static java.sql.Types.NCHAR;
import static java.sql.Types.NUMERIC;
import static java.sql.Types.NVARCHAR;
import static java.sql.Types.REAL;
import static java.sql.Types.ROWID;
import static java.sql.Types.SMALLINT;
import static java.sql.Types.TIME;
import static java.sql.Types.TIMESTAMP;
import static java.sql.Types.TINYINT;
import static java.sql.Types.VARBINARY;
import static java.sql.Types.VARCHAR;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.sql.*;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.SchemaBuilder.FieldAssembler;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JDBC / SQL common functions.
 */
public class JdbcCommon {

    public static Logger logger = LoggerFactory.getLogger(JdbcCommon.class);

    public static long convertToCSVStream(final ResultSet rs, final OutputStream outStream, final RowVisitor visitor) throws SQLException, IOException {

        if (rs == null || rs.getMetaData() == null) {
            logger.warn("Received empty resultset or no metadata.");
            return 0;
        }
        OutputStreamWriter writer = new OutputStreamWriter(outStream);
        final ResultSetMetaData meta = rs.getMetaData();

        // Write header
        final int nrOfColumns = meta.getColumnCount();
        StringBuffer sb = new StringBuffer();
        for (int i = 1; i <= nrOfColumns; i++) {
            String columnName = meta.getColumnName(i);
            sb.append(columnName);
            if (i != nrOfColumns) {
                sb.append(",");
            } else {
                sb.append("\n");
            }
        }
        writer.append(sb.toString());
        long nrOfRows = 0;
        while (rs.next()) {
            if (visitor != null) visitor.visitRow(rs);
            sb = new StringBuffer();
            nrOfRows++;
            for (int i = 1; i <= nrOfColumns; i++) {
                String val = null;
                // TODO: Support escaping commas, XML, CLOB, JSON, ARRAYS, etc.

                int colType = meta.getColumnType(i);
                if (colType == Types.DATE || colType == Types.TIMESTAMP) {
                    Timestamp sqlDate = rs.getTimestamp(i);
                    if (visitor != null) visitor.visitColumn(rs.getMetaData().getColumnName(i), colType, sqlDate);
                    if (sqlDate != null) {
                        DateTimeFormatter formatter = ISODateTimeFormat.basicDateTime().withZoneUTC();
                        val = formatter.print(new DateTime(sqlDate.getTime()));
                    }
                } else if (colType == Types.TIME) {
                    Time time = rs.getTime(i);
                    if (visitor != null) visitor.visitColumn(rs.getMetaData().getColumnName(i), colType, time);
                    DateTimeFormatter formatter = ISODateTimeFormat.time().withZoneUTC();
                    val = formatter.print(new DateTime(time.getTime()));
                } else {
                    val = rs.getString(i);
                    if (visitor != null) visitor.visitColumn(rs.getMetaData().getColumnName(i), colType, val);
                }
                sb.append((val == null ? "" : val));
                if (i != nrOfColumns) {
                    sb.append(",");
                } else {
                    sb.append("\n");
                }
            }
            writer.append(sb.toString());
        }
        writer.flush();
        return nrOfRows;
    }


    public static long convertToAvroStream(final ResultSet rs, final OutputStream outStream) throws SQLException, IOException {
        final Schema schema = createSchema(rs);
        final GenericRecord rec = new GenericData.Record(schema);

        final DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<GenericRecord>(schema);
        try (final DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<GenericRecord>(datumWriter)) {
            dataFileWriter.create(schema, outStream);

            final ResultSetMetaData meta = rs.getMetaData();
            final int nrOfColumns = meta.getColumnCount();
            long nrOfRows = 0;
            while (rs.next()) {
                for (int i = 1; i <= nrOfColumns; i++) {
                    final int javaSqlType = meta.getColumnType(i);
                    final Object value = rs.getObject(i);

                    if (value == null) {
                        rec.put(i - 1, null);

                    } else if (javaSqlType == BINARY || javaSqlType == VARBINARY || javaSqlType == LONGVARBINARY || javaSqlType == ARRAY || javaSqlType == BLOB || javaSqlType == CLOB) {
                        // bytes requires little bit different handling
                        byte[] bytes = rs.getBytes(i);
                        ByteBuffer bb = ByteBuffer.wrap(bytes);
                        rec.put(i - 1, bb);

                    } else if (value instanceof Byte) {
                        // tinyint(1) type is returned by JDBC driver as java.sql.Types.TINYINT
                        // But value is returned by JDBC as java.lang.Byte
                        // (at least H2 JDBC works this way)
                        // direct put to avro record results:
                        // org.apache.avro.AvroRuntimeException: Unknown datum type java.lang.Byte
                        rec.put(i - 1, ((Byte) value).intValue());

                    } else if (value instanceof BigDecimal || value instanceof BigInteger) {
                        // Avro can't handle BigDecimal and BigInteger as numbers - it will throw an AvroRuntimeException such as: "Unknown datum type: java.math.BigDecimal: 38"
                        rec.put(i - 1, value.toString());

                    } else if (value instanceof Number || value instanceof Boolean) {
                        rec.put(i - 1, value);

                    } else {
                        // The different types that we support are numbers (int, long, double, float),
                        // as well as boolean values and Strings. Since Avro doesn't provide
                        // timestamp types, we want to convert those to Strings. So we will cast anything other
                        // than numbers or booleans to strings by using the toString() method.
                        rec.put(i - 1, value.toString());
                    }
                }
                dataFileWriter.append(rec);
                nrOfRows += 1;
            }

            return nrOfRows;
        }
    }

    public static Schema createSchema(final ResultSet rs) throws SQLException {
        final ResultSetMetaData meta = rs.getMetaData();
        final int nrOfColumns = meta.getColumnCount();
        String tableName = "";
        try {
            tableName = meta.getTableName(1);
        } catch (SQLException e) {

        }
        if (StringUtils.isBlank(tableName)) {
            tableName = "NiFi_ExecuteSQL_Record";
        }

        final FieldAssembler<Schema> builder = SchemaBuilder.record(tableName).namespace("any.data").fields();

        /**
         * Some missing Avro types - Decimal, Date types. May need some additional work.
         */
        for (int i = 1; i <= nrOfColumns; i++) {
            switch (meta.getColumnType(i)) {
                case CHAR:
                case LONGNVARCHAR:
                case LONGVARCHAR:
                case NCHAR:
                case NVARCHAR:
                case VARCHAR:
                    builder.name(meta.getColumnName(i)).type().unionOf().nullBuilder().endNull().and().stringType().endUnion().noDefault();
                    break;
                case BIT:
                case BOOLEAN:
                    builder.name(meta.getColumnName(i)).type().unionOf().nullBuilder().endNull().and().booleanType().endUnion().noDefault();
                    break;

                case INTEGER:
                    if (meta.isSigned(i)) {
                        builder.name(meta.getColumnName(i)).type().unionOf().nullBuilder().endNull().and().intType().endUnion().noDefault();
                    } else {
                        builder.name(meta.getColumnName(i)).type().unionOf().nullBuilder().endNull().and().longType().endUnion().noDefault();
                    }
                    break;

                case SMALLINT:
                case TINYINT:
                    builder.name(meta.getColumnName(i)).type().unionOf().nullBuilder().endNull().and().intType().endUnion().noDefault();
                    break;

                case BIGINT:
                    builder.name(meta.getColumnName(i)).type().unionOf().nullBuilder().endNull().and().longType().endUnion().noDefault();
                    break;

                // java.sql.RowId is interface, is seems to be database
                // implementation specific, let's convert to String
                case ROWID:
                    builder.name(meta.getColumnName(i)).type().unionOf().nullBuilder().endNull().and().stringType().endUnion().noDefault();
                    break;

                case FLOAT:
                case REAL:
                    builder.name(meta.getColumnName(i)).type().unionOf().nullBuilder().endNull().and().floatType().endUnion().noDefault();
                    break;

                case DOUBLE:
                    builder.name(meta.getColumnName(i)).type().unionOf().nullBuilder().endNull().and().doubleType().endUnion().noDefault();
                    break;

                // Did not find direct suitable type, need to be clarified!!!!
                case DECIMAL:
                case NUMERIC:
                    builder.name(meta.getColumnName(i)).type().unionOf().nullBuilder().endNull().and().stringType().endUnion().noDefault();
                    break;

                // Did not find direct suitable type, need to be clarified!!!!
                case DATE:
                case TIME:
                case TIMESTAMP:
                    builder.name(meta.getColumnName(i)).type().unionOf().nullBuilder().endNull().and().stringType().endUnion().noDefault();
                    break;

                case BINARY:
                case VARBINARY:
                case LONGVARBINARY:
                case ARRAY:
                case BLOB:
                case CLOB:
                    builder.name(meta.getColumnName(i)).type().unionOf().nullBuilder().endNull().and().bytesType().endUnion().noDefault();
                    break;


                default:
                    throw new IllegalArgumentException("createSchema: Unknown SQL type " + meta.getColumnType(i) + " cannot be converted to Avro type");
            }
        }

        return builder.endRecord();
    }

    public static void main(String[] args) throws SQLException, IOException {

        //CREATE USER 'thinkbig'@'localhost' IDENTIFIED BY 'thinkbig';
        //GRANT ALL PRIVILEGES ON *.* TO 'thinkbig'@'localhost' IDENTIFIED BY 'thinkbig' WITH GRANT OPTION;
        //FLUSH PRIVILEGES;

        com.mysql.jdbc.jdbc2.optional.MysqlDataSource ds
                = new com.mysql.jdbc.jdbc2.optional.MysqlDataSource();
        ds.setServerName("localhost");
        ds.setPortNumber(3306);
        ds.setDatabaseName("pipeline_db");
        ds.setUser("thinkbig");
        ds.setPassword("thinkbig");

        Connection conn = ds.getConnection();
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM BATCH_STEP_EXECUTION");
        OutputStream os = new ByteArrayOutputStream();
        JdbcCommon.convertToCSVStream(rs, os, null);
        System.out.println(os.toString());
    }
}
