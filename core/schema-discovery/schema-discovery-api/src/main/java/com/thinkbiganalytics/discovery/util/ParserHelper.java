/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.discovery.util;

import com.thinkbiganalytics.discovery.schema.Field;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.sql.JDBCType;
import java.sql.Types;
import java.util.List;

/**
 * Provides utility methods useful for writing parsers
 */
public class ParserHelper {

    private static final Logger log = LoggerFactory.getLogger(ParserHelper.class);

    /**
     * Maximum number of characters to sample from a file protecting from memory
     */
    private static int MAX_CHARS = 64000;

    /**
     * Extracts the given number of rows from the file and returns a new reader for the sample.  This method protects memory in the case where a large file can be submitted with no delimiters.
     */
    public static Reader extractSampleLines(InputStream is, Charset charset, int rows) throws IOException {

        StringWriter sw = new StringWriter();

        try (InputStreamReader reader = new InputStreamReader(is, charset)) {
            String line;
            int numRows = 0;
            int chars = 0;
            int intValueOfChar;
            while ((intValueOfChar = reader.read()) != -1 && chars <= MAX_CHARS && numRows < rows) {
                char c = (char) intValueOfChar;
                chars++;
                if (c == '\n') {
                    numRows++;
                }
                sw.append((char) intValueOfChar);
            }
            reader.close();
            if (chars >= MAX_CHARS && numRows == 0) {
                throw new IOException("Unexpected file format. Failed to identify a line delimiter  after [" + MAX_CHARS + "] characters");
            }
        }
        return new StringReader(sw.toString());
    }


    /*
    Convert the JDBC sql type to a hive type
     */
    public static String sqlTypeToHiveType(JDBCType jdbcType) {
        if (jdbcType != null) {
            Integer type = jdbcType.getVendorTypeNumber();
            switch (type) {
                case Types.BIGINT:
                    return "bigint";
                case Types.NUMERIC:
                case Types.DOUBLE:
                case Types.DECIMAL:
                    return "double";
                case Types.INTEGER:
                    return "int";
                case Types.FLOAT:
                    return "float";
                case Types.TINYINT:
                    return "tinyint";
                case Types.DATE:
                    return "date";
                case Types.TIMESTAMP:
                    return "timestamp";
                case Types.BOOLEAN:
                    return "boolean";
                case Types.BINARY:
                    return "binary";
                default:
                    return "string";
            }
        }
        return null;
    }

    /**
     * Derive the corresponding data type from sample values
     *
     * @param values a list of string values
     * @return the JDBC data type
     */
    public static JDBCType deriveJDBCDataType(List<String> values) {

        JDBCType guess = null;
        if (values != null) {
            for (String v : values) {
                JDBCType currentPass;
                try {
                    Integer.parseInt(v);
                    currentPass = JDBCType.INTEGER;
                } catch (NumberFormatException e) {
                    try {
                        Double.parseDouble(v);
                        currentPass = JDBCType.DOUBLE;
                    } catch (NumberFormatException ex) {
                        // return immediately if we know its non-numeric
                        return JDBCType.VARCHAR;
                    }
                }
                // If we ever see a double then use that
                if (guess == null || currentPass == JDBCType.DOUBLE) {
                    guess = currentPass;
                }
            }
        }
        return (guess == null ? JDBCType.VARCHAR : guess);
    }

    /**
     * Derive data types
     *
     * @param type   the target database platform
     * @param fields the fields
     */
    public static void deriveDataTypes(TableSchemaType type, List<? extends Field> fields) {
        for (Field field : fields) {
            if (StringUtils.isEmpty(field.getDerivedDataType())) {
                JDBCType jdbcType = JDBCType.VARCHAR;;
                try {
                    if (!StringUtils.isEmpty(field.getNativeDataType())) {
                        jdbcType = JDBCType.valueOf(field.getNativeDataType());
                    } else {
                        jdbcType = deriveJDBCDataType(field.getSampleValues());
                    }
                } catch (IllegalArgumentException e) {
                    log.warn("Unable to convert data type [?] will be converted to VARCHAR", field.getNativeDataType());
                }

                switch (type) {
                    case HIVE:
                        String hiveType = sqlTypeToHiveType(jdbcType);
                        field.setDerivedDataType(hiveType);
                        break;
                    case RDBMS:
                        field.setDerivedDataType(jdbcType.getName());
                }
            }
        }
    }


}