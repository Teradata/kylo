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

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.lang3.Validate;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.detect.TextStatistics;
import org.apache.tika.io.LookaheadInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import com.thinkbiganalytics.file.parsers.csv.CSVAutoDetect;
import com.thinkbiganalytics.file.parsers.util.ParserUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

public class CSVDetector implements Detector {
    private static final MediaType SUPPORTED_TYPE = MediaType.text("csv");
    public static final String MIME_TYPE = "text/csv";
    private static final int DEFAULT_NUMBER_OF_BYTES_TO_TEST = 512; //65536

    private int bytesToTest = DEFAULT_NUMBER_OF_BYTES_TO_TEST;

    @Override
    public MediaType detect(InputStream inputStream, Metadata metadata) throws IOException {
        MediaType type = MediaType.OCTET_STREAM;


         int numRowsToSample = 100;

        if (inputStream == null) {
            return MediaType.OCTET_STREAM;
        }
        try {
            CSVAutoDetect autoDetect = new CSVAutoDetect();
            // Parse the file
            String sampleData = ParserUtil.extractSampleLines(inputStream, Charset.defaultCharset(), numRowsToSample);
            Validate.notEmpty(sampleData, "No data in file");
            //parse it
            CSVFormat format = autoDetect.detectCSVFormat(sampleData, true, ",");

            String delimiter = Character.toString(format.getDelimiter());
            String quoteChar = Character.toString(format.getQuoteCharacter());
            List<String> header = autoDetect.getHeader();
            Integer headerCount =header.size();

                ObjectMapper mapper = new ObjectMapper();
                String headerString = mapper.writeValueAsString(header);
                metadata.set("delimiter", delimiter);
                metadata.set("headers", headerString);
                metadata.set("headerCount", headerCount.toString());
                return SUPPORTED_TYPE;

        } catch (Exception e) {

        } finally {
            if (inputStream != null) {
                inputStream.reset();
            }
        }

        return MediaType.OCTET_STREAM;

    }

/* **
    @Override
    public MediaType detect(InputStream inputStream, Metadata metadata) throws IOException {
        MediaType type = MediaType.OCTET_STREAM;

        if (inputStream == null) {
            return MediaType.OCTET_STREAM;
        }
       // InputStream ect(inputStream,md);
        //    }stream = InputStreamUtil.readHeaderAsStream(inputStream, bytesToTest);

            CSVDelimiterDetector delimiterDetector = new CSVDelimiterDetector();
            CSVAutoDetect autoDetect = new CSVAutoDetect();

        // Parse the file
        String sampleData = ParserUtil.extractSampleLines(is, charset, numRowsToSample);
        Validate.notEmpty(sampleData, "No data in file");

            try {
                inputStream.mark(bytesToTest);
                format = autoDetect.detectCSVFormat(sampleData, this.headerRow, this.separatorChar);
                this.separatorChar = Character.toString(format.getDelimiter());
                this.quoteChar = Character.toString(format.getQuoteCharacter());
                delimiterDetector.parse(inputStream);
                if (delimiterDetector.isCSV()) {
                    metadata.set("delimiter", delimiterDetector.getDelimiter().toString());
                    ObjectMapper mapper = new ObjectMapper();
                    String headers = mapper.writeValueAsString(delimiterDetector.getHeaders());
                    Integer headerCount = delimiterDetector.getHeaders().size();
                    metadata.set("headers", headers);
                    metadata.set("headerCount", headerCount.toString());
                    return SUPPORTED_TYPE;
                }
            } catch (Exception e) {

            } finally {
                if (inputStream != null) {
                    inputStream.reset();
                }
            }

        return MediaType.OCTET_STREAM;



    }
    */







}