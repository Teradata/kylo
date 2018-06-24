package com.thinkbiganalytics.kylo.tika.detector;

/*-
 * #%L
 * kylo-file-metadata-core
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

import org.apache.tika.detect.TextStatistics;
import org.apache.tika.io.LookaheadInputStream;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Detect delimiter for a given CSV file based upon some known ones
 *    ',', ';', '|', '\t', '-'
 */
public class CSVDelimiterDetector {

    boolean quoted = false;
    boolean firstChar;

    Character[] defaultSeparators = new Character[]{',', ';', '|', '\t', '-'};
    List<Character> seperators;
    Map<Character, AtomicInteger> separatorsCount = new HashMap<>();
    Character delimiter = null;
    Integer maxCount = 0;
    List<String> headers = new ArrayList<>();
    StringBuffer currentHeader = new StringBuffer();
    String line;
    Integer DEFAULT_MAX_BYTES = 512;

    Integer maxBytes = DEFAULT_MAX_BYTES;
    int character;

    int row = 0;

    private void init() {
        if(this.maxBytes == null){
            this.maxBytes = DEFAULT_MAX_BYTES;
        }

        if (seperators == null) {
            seperators = Arrays.asList(defaultSeparators);
        }
        for (Character c : seperators) {
            separatorsCount.put(c, new AtomicInteger(0));
        }
    }

    public String getLine() {
        return line;
    }

    public boolean isCSV() {
        return headers.size() >0;
    }

    public void parse(InputStream stream, List<Character> seperators, Integer maxBytes) throws Exception {
        this.seperators = seperators;
        if(maxBytes != null) {
            this.maxBytes = maxBytes;
        }
        parse(stream);
    }
    public void parse(InputStream stream, Integer maxBytes) throws Exception {
        if(maxBytes != null) {
            this.maxBytes = maxBytes;
        }
        parse(stream);
    }



    public void parse(InputStream stream) throws Exception {
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader reader = null;
        try {
            init();
            inputStream = stream;// new LookaheadInputStream(stream, maxBytes);
            inputStreamReader = new InputStreamReader(inputStream);
            reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            this.line = line;
            if(line != null) {
                char[] characters = line.toCharArray();
                byte[] bytes = line.getBytes();
                TextStatistics textStatistics = new TextStatistics();
                textStatistics.addData(bytes,0,bytes.length);
                boolean isAscii = textStatistics.isMostlyAscii();
                boolean isUtf8 = textStatistics.looksLikeUTF8();
                if(isUtf8) {
                    for (int i = 0; i < characters.length; i++) {
                        Character nextChar = null;
                        if (i != characters.length - 1) {
                            nextChar = characters[i + 1];
                        }
                        parseChars(characters[i], nextChar);
                    }
                }
            }
        }finally {
            if(reader != null){
                reader.close();
            }
            if(inputStreamReader != null){
                inputStreamReader.close();
            }

        }
    }

    public boolean isQuoted() {
        return quoted;
    }

    public Map<Character, AtomicInteger> getSeparatorsCount() {
        return separatorsCount;
    }

    public Character getDelimiter() {
        return delimiter;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public void parseChars(Character character, Character nextChar) {

        switch (character) {
            case '"':
                if (quoted) {
                    if (nextChar != null && nextChar != '"') { // Value is quoted and
                        // current character is " and next character is not ".
                        currentHeader.append(character);
                        quoted = false;
                    }
                    // next characters are "" - read (skip) peeked qoute.
                } else {
                    if (firstChar) {        // Set value as quoted only if this quote is the
                        // first char in the value.
                        quoted = true;
                    }
                }
                break;
            case '\n':
                if (!quoted) {
                    ++row;
                    firstChar = true;
                }
                break;
            default:
                if (!quoted) {
                    if (separatorsCount.containsKey(character)) {
                        headers.add(currentHeader.toString());
                        currentHeader.delete(0, currentHeader.length());
                        Integer count = separatorsCount.get(character).incrementAndGet();
                        if (count > maxCount) {
                            maxCount = count;
                            delimiter = character;
                        }
                        firstChar = true;
                    } else {
                        currentHeader.append(character);
                    }
                }
                break;
        }

        if (firstChar) {
            firstChar = false;
        }
    }


}
