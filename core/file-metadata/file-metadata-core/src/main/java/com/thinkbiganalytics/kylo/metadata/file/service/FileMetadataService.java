package com.thinkbiganalytics.kylo.metadata.file.service;

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

import com.thinkbiganalytics.kylo.metadata.file.FileMetadata;
import com.thinkbiganalytics.kylo.tika.detector.CSVDetector;
import com.thinkbiganalytics.kylo.tika.detector.InputStreamUtil;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.AutoDetectReader;
import org.apache.tika.detect.XmlRootExtractor;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

/**
 * Detect file metadata using Apache Tika
 */
public class FileMetadataService {

    static int bytesToTest = 512000;

    /**
     * Detect file format and metadata/mimetype from the incoming input sream
     *
     * @param is       the file input stream
     * @param fileName the name of the file (optional)
     * @return the metadata object
     */
    public static FileMetadata detectFromStream(InputStream is, String fileName) throws Exception {
        Tika tika = new Tika();
        TikaConfig tikaConfig = new TikaConfig();
        Metadata md = new Metadata();
        md.set(Metadata.RESOURCE_NAME_KEY, fileName);

        Charset charset = StandardCharsets.ISO_8859_1;

        byte[] header = InputStreamUtil.readHeader(is, bytesToTest);

        AutoDetectReader reader = new AutoDetectReader(InputStreamUtil.asStream(header));
        charset = reader.getCharset();

        MediaType mediaType = null;

        if (fileName != null && fileName.endsWith(".csv")) {
            mediaType = detectCsv(InputStreamUtil.asStream(header), md);
        } else {
            mediaType = tika.getDetector().detect(TikaInputStream.get(InputStreamUtil.asStream(header)), md);
            //manually call the csv detector if it is a text file that is not matched.
            //this is needed, and cannot be added to the /META-INF/services/org.apache.tika.detect.Detector file since there is no guarantee of order
            //we first want to detect the type from the core tika mimetypes.
            // then if we didnt get a concrete type attempt to parse it via csv
            if (mediaType.equals(MediaType.TEXT_PLAIN)) {
                mediaType = detectCsv(InputStreamUtil.asStream(header), md);
            } else if (mediaType.equals(MediaType.APPLICATION_XML)) {
                XmlRootExtractor rowTagExtractor = new XmlRootExtractor();
                QName root = rowTagExtractor.extractRootElement(InputStreamUtil.asStream(header));
                if (root != null) {
                    String rowTag = root.getLocalPart();
                    md.set("rowTag", rowTag);
                }
                else {
                   //unable to detect RowTag from XML!!
                }
            }
        }
        if (mediaType == null) {
            mediaType = MediaType.OCTET_STREAM;
        }
        FileMetadata fileMetadata = new FileMetadata(mediaType.toString());
        fileMetadata.addProperties(metadataToMap(md));
        fileMetadata.setSubType(mediaType.getSubtype());
        String encoding = charset.name();
        fileMetadata.setEncoding(StringUtils.isBlank(encoding) ? StandardCharsets.UTF_8.name() : encoding);
        return fileMetadata;
    }

    private static MediaType detectCsv(InputStream inputStream, Metadata md) throws Exception {
        CSVDetector d = new CSVDetector();
        return d.detect(inputStream, md);
    }

    private static Map<String, String> metadataToMap(Metadata metadata) {
        Map<String, String> map = new HashMap<>();
        for (String name : metadata.names()) {
            String firstValue = metadata.get(name);
            map.put(name, firstValue);
        }
        return map;
    }

    /**
     * Detect metadata for a specific file
     *
     * @param file the file to inspect
     * @return the metadata for that file
     */
    public static FileMetadata detectFromFile(File file) throws Exception {
        InputStream inputStream = FileUtils.openInputStream(file);
        return detectFromStream(inputStream, file.getName());
    }


}
