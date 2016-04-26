
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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

    private Character delim;
    private boolean quotes;
    private boolean escapes;

    private void TextFileParser() {
    }

    public TableSchema parse(InputStream is) throws IOException {
        TableSchema schema = null;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            List<LineStats> lineStats = generateStats(br);
            this.delim = guessDelimiter(lineStats);
            this.quotes = hasQuotes(lineStats);
            this.escapes = hasEscapes(lineStats);
            if (delim == null) throw new IOException("Unrecognized format");

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

    private TableSchema createSchema(BufferedReader br) throws IOException {
        TableSchema schema = new TableSchema();
        schema.setDelim(delim);
        schema.setEscapes(escapes);
        schema.setQuotes(quotes);

        // Parse the file and derive types
        CSVFormat format = CSVFormat.DEFAULT.withAllowMissingColumnNames().withDelimiter(delim);
        if (escapes) {
            format = format.withEscape('\\');
        }
        if (quotes) {
            format = format.withQuoteMode(QuoteMode.MINIMAL).withQuote('\"');
        }
        // Parse the file
        CSVParser parser = format.parse(br);
        int i = 0;

        Vector<Field> fields = new Vector<>();
        for (CSVRecord record : parser) {
            if (i > 10) break;
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
        return schema;

    }

    private static void deriveDataTypes(List<Field> fields) {

        for (Field field : fields) {
            if (field.getDataType() == null || !field.getDataType().equalsIgnoreCase("string")) {
                // Test all the values trying to convert to numeric
                List<String> values = field.getSampleValues();
                for (String value : values) {
                    try {
                        Long.parseLong(value);
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

    private boolean hasEscapes(List<LineStats> lineStats) {
        for (LineStats lineStat : lineStats) {
            if (lineStat.escapes) {
                return true;
            }
        }
        return false;
    }

    private boolean hasQuotes(List<LineStats> lineStats) {
        for (LineStats lineStat : lineStats) {
            if (lineStat.hasQuotedStrings()) {
                return true;
            }
        }
        return false;
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
                System.out.println("Warning: initial delimiter changes");
            }
            // All agree so character delim should be the sole element
            retVal =  (firstDelimFreq.keySet().iterator().next());
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
        boolean escapes;
        Character firstDelim;

        public LineStats(String line) {
            // Look for delimiters
            char[] chars = line.toCharArray();
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

        boolean hasQuotedStrings() {
            Integer count = nonDelimStats.get('"');
            return (count != null && count.intValue() % 2 == 0);
        }

        void increment(Character c, boolean delim) {
            if (delim) {
                if (lastChar == null || (lastChar != '\\') || (lastChar == '\\' && c == '\\')) {
                    Integer val = delimStats.get(c);
                    val = (val == null ? 1 : val.intValue() + 1);
                    if (val == 1) firstDelim = c;
                    delimStats.put(c, val);
                } else {
                    escapes = true;
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

    public static void main(String[] args) throws IOException {
        File file = new File("/tmp/foo/somefile.txt");
        try (FileInputStream fis = new FileInputStream(file)) {
            TextFileParser parser = new TextFileParser();
            TableSchema schema = parser.parse(fis);

            System.out.println("delim: " + schema.getDelim());
            System.out.println("quotes: " + schema.isQuotes());
            System.out.println("escape: " + schema.isEscapes());

            List<Field> fields = schema.getFields();
            for (Field field : fields) {
                System.out.println("name: " + field);
                System.out.println("datatype: " + field.getDataType());
            }
            System.out.println(schema);

        }
    }


}
