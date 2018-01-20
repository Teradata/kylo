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


import com.google.common.base.Preconditions;
import com.thinkbiganalytics.nifi.thrift.api.RowVisitor;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.SchemaBuilder.FieldAssembler;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.translate.CharSequenceTranslator;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;

import javax.annotation.Nonnull;

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

/**
 * JDBC / SQL common functions.
 */
public class JdbcCommon {

    private static final Logger log = LoggerFactory.getLogger(JdbcCommon.class);

    /**
     * Converts the specified SQL result set to a delimited text file written to the specified output stream.
     *
     * @param rs        the SQL result set
     * @param outStream the output stream for the delimited text file
     * @param visitor   records position of the result set
     * @param delimiter the column delimiter for the delimited text file
     * @return the number of rows written
     * @throws SQLException if a SQL error occurs while reading the result set
     * @throws IOException  if an I/O error occurs while writing to the output stream
     */
    public static long convertToDelimitedStream(final ResultSet rs, final OutputStream outStream, final RowVisitor visitor, String delimiter) throws SQLException, IOException {
        // avoid overflowing log with redundant messages
        int dateConversionWarning = 0;

        if (rs == null || rs.getMetaData() == null) {
            log.warn("Received empty resultset or no metadata.");
            return 0;
        }
        OutputStreamWriter writer = new OutputStreamWriter(outStream);
        final ResultSetMetaData meta = rs.getMetaData();
        final DelimiterEscaper escaper = new DelimiterEscaper(delimiter);

        // Write header
        final int nrOfColumns = meta.getColumnCount();
        StringBuffer sb = new StringBuffer();
        for (int i = 1; i <= nrOfColumns; i++) {
            String columnName = meta.getColumnName(i);
            sb.append(escaper.translate(columnName));
            if (i != nrOfColumns) {
                sb.append(delimiter);
            } else {
                sb.append("\n");
            }
        }
        writer.append(sb.toString());
        long nrOfRows = 0;
        while (rs.next()) {
            if (visitor != null) {
                visitor.visitRow(rs);
            }
            sb = new StringBuffer();
            nrOfRows++;
            for (int i = 1; i <= nrOfColumns; i++) {
                String val = null;

                int colType = meta.getColumnType(i);
                if (colType == Types.DATE || colType == Types.TIMESTAMP) {
                    Timestamp sqlDate = null;
                    try {
                        // Extract timestamp
                        sqlDate = extractSqlDate(rs, i);
                    } catch (Exception e) {
                        // Still failed, maybe exotic date type
                        if (dateConversionWarning++ < 10) {
                            log.warn("{} is not convertible to timestamp or date", rs.getMetaData().getColumnName(i));
                        }
                    }

                    if (visitor != null) {
                        visitor.visitColumn(rs.getMetaData().getColumnName(i), colType, sqlDate);
                    }
                    if (sqlDate != null) {
                        DateTimeFormatter formatter = ISODateTimeFormat.dateTime().withZoneUTC();
                        val = formatter.print(new DateTime(sqlDate.getTime()));
                    }
                } else if (colType == Types.TIME) {
                    Time time = rs.getTime(i);
                    if (visitor != null) {
                        visitor.visitColumn(rs.getMetaData().getColumnName(i), colType, time);
                    }
                    DateTimeFormatter formatter = ISODateTimeFormat.time().withZoneUTC();
                    val = formatter.print(new DateTime(time.getTime()));
                } else if (colType == Types.BLOB) {
                    byte[] bytes = rs.getBytes(i);

                    if (bytes != null)
                        val = rs.getBytes(i).toString();

                    if (visitor != null) {
                        visitor.visitColumn(rs.getMetaData().getColumnName(i), colType, val);
                    }
                } else {
                    val = rs.getString(i);
                    if (visitor != null) {
                        visitor.visitColumn(rs.getMetaData().getColumnName(i), colType, val);
                    }
                }
                sb.append((val == null ? "" : escaper.translate(val)));
                if (i != nrOfColumns) {
                    sb.append(delimiter);
                } else {
                    sb.append("\n");
                }
            }
            writer.append(sb.toString());
        }
        writer.flush();
        return nrOfRows;
    }


    /**
     * Extracts a resultset col to a SQL timestamp
     */
    private static Timestamp extractSqlDate(ResultSet rs, int col) throws SQLException {
        Timestamp sqlDate = null;
        try {
            // Extract timestamp
            sqlDate = rs.getTimestamp(col);
        } catch (SQLException e) {
            // Attempt to extract date
            Date sqlDateDate = rs.getDate(col);
            if (sqlDateDate != null) {
                Long timeInMillis = sqlDateDate.getTime();
                sqlDate = new Timestamp(timeInMillis);
            }
        }
        return sqlDate;
    }


    public static long convertToAvroStream(final ResultSet rs, final OutputStream outStream, final RowVisitor visitor, final Schema schema) throws SQLException, IOException {
        int dateConversionWarning = 0;
        final GenericRecord rec = new GenericData.Record(schema);

        final DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<>(schema);
        try (final DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<>(datumWriter)) {
            dataFileWriter.create(schema, outStream);

            final ResultSetMetaData meta = rs.getMetaData();
            final int nrOfColumns = meta.getColumnCount();
            long nrOfRows = 0;
            while (rs.next()) {
                if (visitor != null) {
                    visitor.visitRow(rs);
                }
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

                    } else if (value instanceof Date) {
                        final DateTimeFormatter formatter = ISODateTimeFormat.dateTime().withZoneUTC();
                        rec.put(i - 1, formatter.print(new DateTime(((Date) value).getTime())));

                    } else if (value instanceof Time) {
                        final DateTimeFormatter formatter = ISODateTimeFormat.time().withZoneUTC();
                        rec.put(i - 1, formatter.print(new DateTime(((Time) value).getTime())));

                    } else if (value instanceof Timestamp) {
                        final DateTimeFormatter formatter = ISODateTimeFormat.dateTime().withZoneUTC();
                        rec.put(i - 1, formatter.print(new DateTime(((Timestamp) value).getTime())));

                    } else {
                        // The different types that we support are numbers (int, long, double, float),
                        // as well as boolean values and Strings. Since Avro doesn't provide
                        // timestamp types, we want to convert those to Strings. So we will cast anything other
                        // than numbers or booleans to strings by using the toString() method.
                        rec.put(i - 1, value.toString());
                    }

                    //notify the visitor
                    if (javaSqlType == Types.DATE || javaSqlType == Types.TIMESTAMP) {
                        Timestamp sqlDate = null;
                        try {
                            // Extract timestamp
                            sqlDate = extractSqlDate(rs, i);

                        } catch (Exception e) {
                            if (dateConversionWarning++ < 10) {
                                log.warn("{} is not convertible to timestamp or date", rs.getMetaData().getColumnName(i));
                            }
                        }

                        if (visitor != null) {
                            visitor.visitColumn(rs.getMetaData().getColumnName(i), javaSqlType, sqlDate);
                        }
                    } else if (javaSqlType == Types.TIME) {
                        Time time = rs.getTime(i);
                        if (visitor != null) {
                            visitor.visitColumn(rs.getMetaData().getColumnName(i), javaSqlType, time);
                        }
                    } else {
                        if (visitor != null) {
                            visitor.visitColumn(rs.getMetaData().getColumnName(i), javaSqlType, (value != null) ? value.toString() : null);
                        }
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
            // ignored
        }
        if (StringUtils.isBlank(tableName)) {
            tableName = "NiFi_ExecuteSQL_Record";
        }

        final FieldAssembler<Schema> builder = SchemaBuilder.record(tableName).namespace("any.data").fields();

        // Some missing Avro types - Decimal, Date types. May need some additional work.
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

    /**
     * Escapes values in delimited text files.
     */
    static class DelimiterEscaper extends CharSequenceTranslator {

        /**
         * Character for quoting values
         */
        private static final char QUOTE = '"';

        /**
         * String for quoting values
         */
        private static final String QUOTE_STR = String.valueOf(QUOTE);

        /**
         * Character for escaping values
         */
        private static final char BACKSLASH = '\\';

        /**
         * String for escaping values
         */
        private static final String BACKSLASH_STR = String.valueOf(BACKSLASH);

        /**
         * Strings that, if found, require a value to be escaped
         */
        private final String[] searchStrings;

        /**
         * Replacements for the search strings found
         */
        private final String[] replacementStrings;

        /**
         * Constructs a {@code DelimiterEscaper} with the specified delimiter.
         *
         * @param delimiter the delimiter
         */
        DelimiterEscaper(@Nonnull final String delimiter) {
            searchStrings = new String[]{delimiter, QUOTE_STR, Character.toString('\n'), Character.toString('\r') };
            replacementStrings = new String[]{delimiter, BACKSLASH_STR+QUOTE_STR, "\\\\n", "\\\\r"};
        }

        @Override
        public int translate(@Nonnull final CharSequence input, final int index, @Nonnull final Writer out) throws IOException {
            Preconditions.checkState(index == 0, "Unsupported translation index %d", index);
            String inputString = input.toString();
            if (StringUtils.containsAny(inputString, searchStrings)) {
                out.write(QUOTE);
                out.write(StringUtils.replaceEach(inputString, searchStrings, replacementStrings));
                out.write(QUOTE);
            } else {
                out.write(input.toString());
            }

            return Character.codePointCount(input, 0, input.length());
        }
    }

    /**
     * Get schema in the format for setting up the feed table
     * @param schema Avro Schema
     * @return formatted schema for setting up feed table
     */
    public static String getAvroSchemaForFeedSetup (Schema schema) {
        if (schema == null) {
            return "";
        }

        final String PIPE = "|";
        final String description = "";
        final String primaryKey = "0";
        final String createdTracker = "0";
        final String updatedTracker = "0";
        final String newLine = "\n";

        StringBuffer retVal = new StringBuffer();

        int totalFields = schema.getFields().size();
        int counter = 1;

        for (Schema.Field field: schema.getFields()) {
            String name = field.name().toLowerCase();
            String dataType = field.schema().getType().name().toLowerCase();

            if (dataType.equals("union")) {
                    for (Schema fieldSchemaType: field.schema().getTypes()) {
                        if (!fieldSchemaType.getName().toLowerCase().equals("null")) {
                            dataType = getHiveTypeForAvroType(fieldSchemaType.getName().toLowerCase());
                            break;
                        }
                    }

                    if (dataType.equals("union")) {
                        dataType = "void";
                    }
                }
            else {
                dataType = getHiveTypeForAvroType(dataType);
            }

            retVal.append(name)
                .append(PIPE)
                .append(dataType)
                .append(PIPE)
                .append(description)
                .append(PIPE)
                .append(primaryKey)
                .append(PIPE)
                .append(createdTracker)
                .append(PIPE)
                .append(updatedTracker);

            if (counter++ < totalFields) {
                retVal.append(newLine);
            }
        }

        return retVal.toString();
    }

    /*
     * Mapping of Avro's data types to Hive's data types
     */
    private static String getHiveTypeForAvroType(String avroType) {

        String hiveType;

        switch (avroType) {
            case "null":
                hiveType = "void";
                break;

            case "boolean":
                hiveType = "boolean";
                break;

            case "int":
                hiveType = "int";
                break;

            case "long":
                hiveType = "bigint";
                break;

            case "float":
                hiveType = "float";
                break;

            case "double":
                hiveType = "double";
                break;

            case "bytes":
                hiveType = "binary";
                break;

            case "string":
                hiveType = "string";
                break;

            case "record":
                hiveType = "struct";
                break;

            case "map":
                hiveType = "map";
                break;

            case "list":
                hiveType = "array";
                break;

            case "enum":
                hiveType = "string";
                break;

            case "fixed":
                hiveType = "binary";
                break;

            default:
                hiveType = "string";
                break;
        }

        return hiveType;
    }

}
