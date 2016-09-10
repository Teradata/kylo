/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.discovery.parsers.csv;

import com.thinkbiganalytics.discovery.model.DefaultField;
import com.thinkbiganalytics.discovery.model.DefaultFileSchema;
import com.thinkbiganalytics.discovery.model.DefaultHiveSchema;
import com.thinkbiganalytics.discovery.model.DefaultTableSchema;
import com.thinkbiganalytics.discovery.parser.FileSchemaParser;
import com.thinkbiganalytics.discovery.parser.SchemaParser;
import com.thinkbiganalytics.discovery.schema.Schema;
import com.thinkbiganalytics.discovery.util.ParserHelper;
import com.thinkbiganalytics.discovery.util.TableSchemaType;
import com.thinkbiganalytics.policy.PolicyProperty;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.BeanUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;

@SchemaParser(name = "CSV", description = "Supports delimited text files with a field delimiter and optional escape and quote characters.", tags = {"CSV", "TSV"})
public class CSVFileSchemaParser implements FileSchemaParser {

    private static int SAMPLE_ROWS = 10;

    @PolicyProperty(name = "Auto Detect?", hint = "Auto detect will attempt to infer delimiter from the sample file.", value = "true")
    private boolean autoDetect;

    @PolicyProperty(name = "Header?", hint = "Whether file has a header.", value = "true")
    private boolean headerRow;

    @PolicyProperty(name = "Delimiter Char", hint = "Character separating fields", value = ",")
    private String separatorChar = ",";

    @PolicyProperty(name = "Quote Char", hint = "Character enclosing a quoted string", value = "'")
    private String quoteChar = "'";

    @PolicyProperty(name = "Escape Char", hint = "Escape character", value = "\\")
    private String escapeChar = "\\";

    private CSVFormat createCSVFormat() {
        CSVFormat format = CSVFormat.DEFAULT.withAllowMissingColumnNames();

        if (StringUtils.isNotEmpty(separatorChar)) {
            format = format.withDelimiter(separatorChar.charAt(0));
        }
        if (StringUtils.isNotEmpty(escapeChar)) {
            format = format.withEscape(escapeChar.charAt(0));
        }
        if (StringUtils.isNotEmpty(quoteChar)) {
            format = format.withQuoteMode(QuoteMode.MINIMAL).withQuote(quoteChar.charAt(0));
        }
        return format;
    }

    @Override
    public Schema parse(InputStream is, Charset charset, TableSchemaType target) throws IOException {
        Validate.notNull(target, "target must not be null");
        Validate.notNull(is, "stream must not be null");
        Validate.notNull(charset, "charset must not be null");

        DefaultFileSchema fileSchema = new DefaultFileSchema();
        CSVFormat format = createCSVFormat();

        // Parse the file
        try (Reader reader = ParserHelper.extractSampleLines(is, charset, SAMPLE_ROWS)) {
            CSVParser parser = format.parse(reader);
            int i = 0;

            ArrayList<DefaultField> fields = new ArrayList<>();
            for (CSVRecord record : parser) {
                if (i > SAMPLE_ROWS) {
                    break;
                }
                int size = record.size();
                for (int j = 0; j < size; j++) {
                    DefaultField field = null;
                    if (i == 0) {
                        field = new DefaultField();
                        if (headerRow) {
                            field.setName(record.get(j));
                        } else {
                            field.setName("Col" + j + 1);
                        }
                        fields.add(field);
                    } else {
                        // Add sample values for rows
                        field = fields.get(j);
                        field.getSampleValues().add(StringUtils.defaultString(record.get(j), ""));
                    }
                }
                i++;
            }
            fileSchema.setFields(fields);

            // Convert to target schema with proper derived types
            Schema targetSchema = convertToTarget(target, fileSchema);
            return targetSchema;

        }
    }

    /**
     * Converts the raw file schema to the target schema with correctly derived types
     *
     * @param target       the target schema
     * @param sourceSchema the source
     * @return the schema
     */
    public Schema convertToTarget(TableSchemaType target, Schema sourceSchema) {
        Schema targetSchema;
        switch (target) {
            case RAW:
                targetSchema = sourceSchema;
                break;
            case HIVE:
                DefaultHiveSchema hiveSchema = new DefaultHiveSchema();
                BeanUtils.copyProperties(sourceSchema, hiveSchema);
                hiveSchema.setHiveFormat(deriveHiveRecordFormat());
                ParserHelper.deriveDataTypes(target, hiveSchema.getFields());
                targetSchema = hiveSchema;
                break;
            case RDBMS:
                DefaultTableSchema rdbmsSchema = new DefaultTableSchema();
                BeanUtils.copyProperties(sourceSchema, rdbmsSchema);
                ParserHelper.deriveDataTypes(target, rdbmsSchema.getFields());
                targetSchema = rdbmsSchema;
                break;
            default:
                throw new IllegalArgumentException(target.name() + " is not supported by this parser");
        }
        return targetSchema;
    }

    public String deriveHiveRecordFormat() {
        String template = "ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde'" +
                          " WITH SERDEPROPERTIES (" +
                          " \"separatorChar\" = \"%s\"," +
                          " \"quoteChar\" = \"%s\", " +
                          " \"escapeChar\" = \"%s\" " +
                          ") STORED AS TEXTFILE";

        return String.format(template, separatorChar, escapeChar, quoteChar);
    }

}
