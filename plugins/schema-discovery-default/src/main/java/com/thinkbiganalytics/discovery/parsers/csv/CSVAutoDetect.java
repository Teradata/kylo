/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.discovery.parsers.csv;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.QuoteMode;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;

/**
 * Infers CSV format for a sample file
 */

class CSVAutoDetect {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(CSVFileSchemaParser.class);

    /**
     * Parses a sample file to allow schema specification when creating a new feed.
     *
     * @param sampleText the sample text
     * @return A configured parser
     * @throws IOException If there is an error parsing the sample file
     */
    public CSVFormat detectCSVFormat(String sampleText) throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withAllowMissingColumnNames();
        try (BufferedReader br = new BufferedReader(new StringReader(sampleText))) {
            List<LineStats> lineStats = generateStats(br);
            Character delim = guessDelimiter(lineStats);
            //boolean quotes = hasQuotes(lineStats);
            //boolean escapes = hasEscapes(lineStats);
            if (delim == null) {
                throw new IOException("Unrecognized format");
            }
            format = format.withDelimiter(delim);
            format.withQuoteMode(QuoteMode.MINIMAL);
        }
        return format;
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
        frequency = sortMapByValue(frequency);
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
            firstDelimFreq = sortMapByValue(frequency);
            // Check if all rows agree
            if (firstDelimFreq.size() != 1) {
                LOG.warn("Warning: initial delimiter changes");
            }
            // All agree so character delim should be the sole element
            retVal = (firstDelimFreq.keySet().iterator().next());
        }
        return retVal;
    }


    private static <K, V extends Comparable<? super V>> Map<K, V> sortMapByValue(Map<K, V> map) {
        return map.entrySet()
            .stream()
            .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
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
                    case ';':
                        increment(';', true);
                        break;
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
                    if (val == 1) {
                        firstDelim = c;
                    }
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

        public Map<Character, Integer> calcDelimCountsOrdered() {
            return sortMapByValue(delimStats);
        }

    }
}