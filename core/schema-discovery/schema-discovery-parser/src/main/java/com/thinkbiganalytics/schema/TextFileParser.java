
/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.schema;

import com.thinkbiganalytics.db.model.schema.Field;
import com.thinkbiganalytics.db.model.schema.TableSchema;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * Infers schema for a delimited text file. Current identifies typical delimited file
 */

public class TextFileParser {

    private static final Logger log = LoggerFactory.getLogger(TextFileParser.class);

    private static final Character DEFAULT_DELIM = ',';

    private Character delim;
    private Character quote;
    private Character escape;

    private void TextFileParser() {
    }

    /**
     * Parses a sample file to allow schema specification when creating a new feed.
     *
     * @param is        The sample file
     * @param delimiter Allows user to manually select the delimiter used in the sample file
     * @return A <code>TableSchema</code> representing the schema
     * @throws IOException If there is an error parsing the sample file
     */
    public TableSchema parse(InputStream is, Character delimiter) throws IOException {
        TableSchema schema = null;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            List<LineStats> lineStats = generateStats(br);
            if (delimiter == null) {
                this.delim = guessDelimiter(lineStats);
            } else {
                this.delim = delimiter;
            }
            this.quote = deriveQuote(lineStats);
            this.escape = deriveEscape(lineStats);
            if (delim == null) {
                throw new IOException("Unrecognized format");
            }

            schema = createSchema(br);
        }
        return schema;
    }

    private List<LineStats> generateStats(BufferedReader br) throws IOException {
        List<LineStats> lineStats = new Vector<>();
        String line;
        int rows = 0;
        br.mark(32765);
        while ((line = br.readLine()) != null && rows < 10) {
            LineStats stats = new LineStats(line);
            rows++;
            lineStats.add(stats);
        }
        br.reset();
        return lineStats;
    }

    private String stringForCharacter(Character c) {
        if (c == null) return null;
        switch (c) {
            case '\t':
                return "\\t";
            case '"':
                return "\\\"";
            case '\\':
                return "\\\\";
            default:
                return c.toString();
        }
    }

    private TableSchema createSchema(BufferedReader br) throws IOException {
        TableSchema schema = new TableSchema();
        schema.setDelim(stringForCharacter(delim));
        schema.setEscape(stringForCharacter(escape));
        schema.setQuote(stringForCharacter(quote));

        // Parse the file and derive types
        CSVFormat format = CSVFormat.DEFAULT.withAllowMissingColumnNames().withDelimiter(delim);
        if (escape != null) {
            format = format.withEscape(escape);
        }
        if (quote != null) {
            format = format.withQuoteMode(QuoteMode.MINIMAL).withQuote(quote);
        }
        // Parse the file
        CSVParser parser = format.parse(br);
        int i = 0;

        Vector<Field> fields = new Vector<>();
        for (CSVRecord record : parser) {
            if (i > 10) {
                break;
            }
            int size = record.size();
            for (int j = 0; j < size; j++) {
                Field field = null;
                // Assume field is header name
                if (i == 0) {
                    field = new Field();
                    field.setName(record.get(j));
                    fields.add(field);
                } else {
                    // Add field if needed
                    if (j > fields.size()) {
                        field = new Field();
                        field.setName("Field " + j);
                        fields.add(field);
                    }
                    field = fields.get(j);
                    field.getSampleValues().add(StringUtils.defaultString(record.get(j), ""));
                }
            }
            i++;
        }
        schema.setFields(fields);
        deriveDataTypes(fields);
        schema.deriveHiveRecordFormat();
        return schema;

    }

    private static void deriveDataTypes(List<Field> fields) {

        for (Field field : fields) {
            if (field.getDataType() == null || !field.getDataType().equalsIgnoreCase("string")) {
                // Test all the values trying to convert to numeric
                List<String> values = field.getSampleValues();
                for (String value : values) {
                    try {
                        Integer.parseInt(value);
                        field.setDataType("int");
                    } catch (NumberFormatException e) {
                        try {
                            Double.parseDouble(value);
                            field.setDataType("double");
                        } catch (NumberFormatException ex) {
                            field.setDataType("string");
                        }
                    }
                }
            }
        }
    }

    private Character deriveEscape(List<LineStats> lineStats) {
        for (LineStats lineStat : lineStats) {
            if (lineStat.hasEscapes) {
                return '\\';
            }
        }
        return null;
    }

    private Character deriveQuote(List<LineStats> lineStats) {
        for (LineStats lineStat : lineStats) {
            Character quoteChar = lineStat.deriveQuoteChar();
            if (quoteChar != null) {
                return quoteChar;
            }
        }
        return null;
    }

    private Character guessDelimiter(List<LineStats> lineStats) {
        // Guess using most frequent occurrence of delimiter
        Character retVal = null;
        Map<Character, Integer> frequency = new HashMap<>();
        for (LineStats lineStat : lineStats) {
            Map<Character, Integer> sorted = lineStat.calcDelimCountsOrdered();
            if (sorted.size() > 0) {
                Set<Character> keys = sorted.keySet();
                for (Character c : keys) {
                    Integer val = frequency.get(c);
                    val = (val == null ? 1 : val.intValue() + 1);
                    frequency.put(c, val);
                }
            }
        }
        frequency = sortByValue(frequency);
        // Check if all rows agree
        if (frequency.size() == 1) {
            // All agree so character delim should be the sole element
            return (frequency.keySet().iterator().next());
        } else {
            // Since we don't agree try the first delim found
            Map<Character, Integer> firstDelimFreq = new HashMap<>();
            for (LineStats lineStat : lineStats) {
                Character firstDelim = lineStat.firstDelim;
                if (firstDelim != null) {
                    Integer val = firstDelimFreq.get(firstDelim);
                    val = (val == null ? 1 : val.intValue() + 1);
                    firstDelimFreq.put(firstDelim, val);
                }
            }
            firstDelimFreq = sortByValue(frequency);
            // Check if all rows agree
            if (firstDelimFreq.size() != 1) {
                log.warn("Warning: initial delimiter changes");
            }
            // All agree so character delim should be the sole element
            retVal = (firstDelimFreq.size() > 0 ? firstDelimFreq.keySet().iterator().next() : DEFAULT_DELIM);
        }
        return retVal;
    }


    public static LinkedHashMap<Character, Integer> sortByValue(Map<Character, Integer> passedMap) {
        List mapKeys = new ArrayList(passedMap.keySet());
        List mapValues = new ArrayList(passedMap.values());
        Collections.reverse(mapValues);
        Collections.sort(mapKeys);

        LinkedHashMap sortedMap = new LinkedHashMap();

        Iterator valueIt = mapValues.iterator();
        while (valueIt.hasNext()) {
            Object val = valueIt.next();
            Iterator keyIt = mapKeys.iterator();

            while (keyIt.hasNext()) {
                Object key = keyIt.next();
                String comp1 = passedMap.get(key).toString();
                String comp2 = val.toString();

                if (comp2.equals(comp1)) {
                    passedMap.remove(key);
                    mapKeys.remove(key);
                    sortedMap.put(key, val);
                    break;
                }
            }
        }
        return sortedMap;
    }

    static class LineStats {

        Map<Character, Integer> delimStats = new HashMap<Character, Integer>();
        Map<Character, Integer> nonDelimStats = new HashMap<Character, Integer>();
        Character lastChar;
        boolean hasEscapes;
        Character firstDelim;

        public LineStats(String line) {
            // Look for delimiters
            char[] chars = line.toCharArray();
            // ignore stats on next character if escape
            boolean escape = false;
            for (int i = 0; i < chars.length; i++) {

                char c = chars[i];

                switch (c) {
                    case ',':
                        increment(',', true);
                        break;
                    case '|':
                        increment('|', true);
                        break;
                    case '\t':
                        increment('\t', true);
                        break;
                    case '\'':
                        increment('\'', false);
                        break;
                    case '"':
                        increment('"', false);
                        break;
                    case '<':
                        increment('<', false);
                        break;
                    case '>':
                        increment('>', false);
                        break;
                    case '\\':
                        increment('\\', false);
                        break;
                    default:

                }

            }
        }

        boolean isQuoteChar(Character c) {
            Integer count = nonDelimStats.get(c);
            return (count != null && count.intValue() % 2 == 0);
        }

        Character deriveQuoteChar() {
            if (isQuoteChar('"')) {
                return '"';
            }
            if (isQuoteChar('\'')) {
                return '\'';
            }
            return null;
        }

        void increment(Character c, boolean delim) {
            if (delim) {
                if (lastChar == null || (lastChar != '\\') || (lastChar == '\\' && c == '\\')) {
                    Integer val = delimStats.get(c);
                    val = (val == null ? 1 : val.intValue() + 1);
                    if (val == 1) {
                        firstDelim = c;
                    }
                    delimStats.put(c, val);
                } else {
                    hasEscapes = true;
                }
            } else {
                Integer val = nonDelimStats.get(c);
                val = (val == null ? 1 : val.intValue() + 1);
                nonDelimStats.put(c, val);
            }
            lastChar = c;
        }

        public LinkedHashMap<Character, Integer> calcDelimCountsOrdered() {
            return sortByValue(delimStats);
        }

    }
}
