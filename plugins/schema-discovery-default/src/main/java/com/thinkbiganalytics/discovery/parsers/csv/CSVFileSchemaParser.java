package com.thinkbiganalytics.discovery.parsers.csv;

/*-
 * #%L
 * thinkbig-schema-discovery-default
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

import com.thinkbiganalytics.discovery.model.DefaultField;
import com.thinkbiganalytics.discovery.model.DefaultFileSchema;
import com.thinkbiganalytics.discovery.model.DefaultHiveSchema;
import com.thinkbiganalytics.discovery.model.DefaultTableSchema;
import com.thinkbiganalytics.discovery.parser.FileSchemaParser;
import com.thinkbiganalytics.discovery.parser.SchemaParser;
import com.thinkbiganalytics.discovery.schema.Field;
import com.thinkbiganalytics.discovery.schema.Schema;
import com.thinkbiganalytics.discovery.util.ParserHelper;
import com.thinkbiganalytics.discovery.util.TableSchemaType;
import com.thinkbiganalytics.policy.PolicyProperty;
import com.thinkbiganalytics.policy.PolicyPropertyTypes;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;

import javax.annotation.Nonnull;

@SchemaParser(name = "CSV", allowSkipHeader = true, description = "Supports delimited text files with a field delimiter and optional escape and quote characters.", tags = {"CSV", "TSV"})
public class CSVFileSchemaParser implements FileSchemaParser {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(CSVFileSchemaParser.class);

    private static final int MAX_ROWS = 1000;

    private int numRowsToSample = 100;

    @PolicyProperty(name = "Auto Detect?", hint = "Auto detect will attempt to infer delimiter from the sample file.", type = PolicyPropertyTypes.PROPERTY_TYPE.select,
                    selectableValues = {"true", "false"})
    private boolean autoDetect = true;

    @PolicyProperty(name = "Header?", hint = "Whether file has a header.", value = "true", type = PolicyPropertyTypes.PROPERTY_TYPE.select, selectableValues = {"true", "false"})
    private boolean headerRow = true;

    @PolicyProperty(name = "Delimiter Char", hint = "Character separating fields", value = ",")
    private String separatorChar = ",";

    @PolicyProperty(name = "Quote Char", hint = "Character enclosing a quoted string", value = "\'")
    private String quoteChar = "\'";

    @PolicyProperty(name = "Escape Char", hint = "Escape character", value = "\\")
    private String escapeChar = "\\";

    private CSVFormat createCSVFormat(String sampleData) throws IOException {
        CSVFormat format;
        if (autoDetect) {
            CSVAutoDetect autoDetect = new CSVAutoDetect();
            format = autoDetect.detectCSVFormat(sampleData, this.headerRow, this.separatorChar);
            this.separatorChar = Character.toString(format.getDelimiter());
            this.quoteChar = Character.toString(format.getQuoteCharacter());
        } else {
            format = CSVFormat.DEFAULT.withAllowMissingColumnNames();

            if (StringUtils.isNotEmpty(separatorChar)) {
                format = format.withDelimiter(toChar(separatorChar).charAt(0));
            }
            if (StringUtils.isNotEmpty(escapeChar)) {
                format = format.withEscape(toChar(escapeChar).charAt(0));
            }
            if (StringUtils.isNotEmpty(quoteChar)) {
                format = format.withQuoteMode(QuoteMode.MINIMAL).withQuote(toChar(quoteChar).charAt(0));
            }
        }

        return format;
    }

    @Override
    public Schema parse(InputStream is, Charset charset, TableSchemaType target) throws IOException {

        Validate.notNull(target, "target must not be null");
        Validate.notNull(is, "stream must not be null");
        Validate.notNull(charset, "charset must not be null");
        validate();

        // Parse the file
        String sampleData = ParserHelper.extractSampleLines(is, charset, numRowsToSample);
        Validate.notEmpty(sampleData, "No data in file");
        CSVFormat format = createCSVFormat(sampleData);
        try (Reader reader = new StringReader(sampleData)) {

            CSVParser parser = format.parse(reader);
            DefaultFileSchema fileSchema = populateSchema(parser);
            fileSchema.setCharset(charset.name());

            // Convert to target schema with proper derived types
            Schema targetSchema = convertToTarget(target, fileSchema);
            return targetSchema;
        }
    }

    private DefaultFileSchema populateSchema(CSVParser parser) {
        DefaultFileSchema fileSchema = new DefaultFileSchema();
        int i = 0;
        ArrayList<Field> fields = new ArrayList<>();
        for (CSVRecord record : parser) {
            if (i > 9) {
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
                        field.setName("Col_" + (j + 1));
                    }
                    fields.add(field);
                } else {
                    try {
                        field = (DefaultField) fields.get(j);
                        field.getSampleValues().add(StringUtils.defaultString(record.get(j), ""));

                    } catch (IndexOutOfBoundsException e) {
                        LOG.warn("Sample file has potential sparse column problem at row [?] field [?]", i + 1, j + 1);
                    }
                }
            }
            i++;
        }
        fileSchema.setFields(fields);
        return fileSchema;
    }

    /**
     * Converts the raw file schema to the target schema with correctly derived types
     *
     * @param target       the target schema
     * @param sourceSchema the source
     * @return the schema
     */
    protected Schema convertToTarget(TableSchemaType target, Schema sourceSchema) {
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


    private String stringForCharacter(String s) {
        if (StringUtils.isEmpty(s)) {
            return null;
        }
        Character c = s.charAt(0);

        switch (c) {
            case ';':
                return "\\;";
            case '\t':
                return "\\t";
            case '\'':
                return "\\\'";
            case '\\':
                return "\\\\";
            default:
                return StringEscapeUtils.escapeJava(c.toString());
        }
    }

    public String deriveHiveRecordFormat() {
        String template = "ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde'\n" +
                          " WITH SERDEPROPERTIES (" +
                          deriveSeparatorRecordFormat() +
                          deriveEscapeCharRecordFormat() +
                          deriveQuoteRecordFormat() +
                          ") STORED AS TEXTFILE";
        return String.format(template, separatorChar, escapeChar, quoteChar);
    }

    private String deriveSeparatorRecordFormat() {
        String template = " 'separatorChar' = '%s'";
        return String.format(template, stringForCharacter(separatorChar));
    }

    private String deriveQuoteRecordFormat() {
        if (StringUtils.isEmpty(quoteChar)) {
            return "";
        }
        String template = " ,'quoteChar' = '%s'";
        return String.format(template, stringForCharacter(quoteChar));
    }

    private String deriveEscapeCharRecordFormat() {
        if (StringUtils.isEmpty(escapeChar)) {
            return "";
        }
        String template = " ,'escapeChar' = '%s'";
        return String.format(template, stringForCharacter(escapeChar));
    }


    private void validate() {
        Validate.isTrue(separatorChar != null && (separatorChar.length() == 1 || separatorChar.length() == 2), "Legal separator character required.");
        Validate.isTrue(StringUtils.isEmpty(quoteChar) || quoteChar.length() <= 2, "Legal quote character required.");
        Validate.isTrue(StringUtils.isEmpty(escapeChar) || escapeChar.length() <= 2, "Legal escape character required.");
        Validate.inclusiveBetween(1, MAX_ROWS, numRowsToSample, "Cannot sample more than " + MAX_ROWS + ".");
    }

    public void setAutoDetect(boolean autoDetect) {
        this.autoDetect = autoDetect;
    }

    public void setHeaderRow(boolean headerRow) {
        this.headerRow = headerRow;
    }

    public void setNumRowsToSample(int numRowsToSample) {
        this.numRowsToSample = numRowsToSample;
    }

    public String getSeparatorChar() {
        return separatorChar;
    }

    public void setSeparatorChar(String separatorChar) {
        this.separatorChar = separatorChar;
    }

    public String getQuoteChar() {
        return quoteChar;
    }

    public void setQuoteChar(String quoteChar) {
        this.quoteChar = quoteChar;
    }

    public String getEscapeChar() {
        return escapeChar;
    }

    public void setEscapeChar(String escapeChar) {
        this.escapeChar = escapeChar;
    }

    /**
     * Converts the specified string to a character.
     *
     * @param character the escaped string
     * @return the character
     */
    @Nonnull
    private String toChar(@Nonnull final String character) {
        if (character.length() == 1) {
            return character;
        } else if (character.length() == 2 && character.charAt(0) == '\\') {
            return StringEscapeUtils.unescapeJava(character);
        }
        throw new IllegalArgumentException("Not a valid character: " + character);
    }
}
