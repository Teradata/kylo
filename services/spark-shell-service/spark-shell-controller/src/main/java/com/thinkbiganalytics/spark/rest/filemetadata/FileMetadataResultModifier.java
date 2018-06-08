package com.thinkbiganalytics.spark.rest.filemetadata;
/*-
 * #%L
 * kylo-spark-shell-controller
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

import com.thinkbiganalytics.discovery.FileParserFactory;
import com.thinkbiganalytics.discovery.model.SchemaParserDescriptor;
import com.thinkbiganalytics.discovery.parser.FileSchemaParser;
import com.thinkbiganalytics.discovery.parser.SampleFileSparkScript;
import com.thinkbiganalytics.discovery.parser.SparkFileSchemaParser;
import com.thinkbiganalytics.discovery.rest.controller.SchemaParserAnnotationTransformer;
import com.thinkbiganalytics.kylo.metadata.file.FileMetadata;
import com.thinkbiganalytics.spark.rest.controller.AbstractTransformResponseModifier;
import com.thinkbiganalytics.spark.rest.model.FileMetadataResponse;
import com.thinkbiganalytics.spark.rest.model.ModifiedTransformResponse;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Deprecated
public class FileMetadataResultModifier extends AbstractTransformResponseModifier<FileMetadataResponse> {


    @Override
    public void modifySuccessfulResults(ModifiedTransformResponse<FileMetadataResponse> modifiedTransformResponse) {
        FileMetadataResponse fileMetadataResponse = new FileMetadataResponse();
        Map<String, List<FileMetadataResponse.ParsedFileMetadata>> groupedMetadata = response.getResults().getRows().stream().map(objects -> {
            FileMetadata fileMetadata = new FileMetadata();
            String mimeType = (String) getRowValue("mimeType", objects);
            String delimiter = (String) getRowValue("delimiter", objects);
            Integer headerCount = getRowValue("headerCount", objects, Integer.class, 0);
            String filePath = (String) getRowValue("resource", objects);
            String encoding = (String) getRowValue("encoding", objects);

            FileMetadataResponse.ParsedFileMetadata metadata = new FileMetadataResponse.ParsedFileMetadata();
            metadata.setMimeType(mimeType);
            metadata.setDelimiter(delimiter);
            metadata.setEncoding(encoding);
            metadata.setFilePath(filePath);
            return metadata;

        }).collect(Collectors.groupingBy(FileMetadataResponse.ParsedFileMetadata::getKey));

        //create user friendly names from the groupings
        //the key here is going to be
        Map<String, FileMetadataResponse.FileDataSet> fileDatasets = groupedMetadata.keySet().stream().collect(Collectors.toMap(key -> groupedMetadata.get(key).get(0).getFilePath(), key -> {
            FileMetadataResponse.FileDataSet dataSet = new FileMetadataResponse.FileDataSet();
            List<FileMetadataResponse.ParsedFileMetadata> files = groupedMetadata.get(key);
            dataSet.setFiles(files);

            FileMetadataResponse.ParsedFileMetadata firstFile = files.get(0);
            dataSet.setMimeType(firstFile.getMimeType());

            findSchemaParserForMimeType(firstFile.getMimeType()).ifPresent(schemaParserDescriptor -> {
                dataSet.setSchemaParser(schemaParserDescriptor);
                //if the parser has a delimiter property set it
                schemaParserDescriptor.getProperties().stream().filter(property -> property.getObjectProperty().equals("separatorChar")).findFirst().ifPresent(property -> {
                    property.setValue(firstFile.getDelimiter());
                });
                Optional<FileSchemaParser> fileSchemaParser = fileSchemaParser(schemaParserDescriptor);
                if (fileSchemaParser.isPresent() && fileSchemaParser.get() instanceof SparkFileSchemaParser) {
                    List<String> paths = files.stream().map(parsedFileMetadata -> parsedFileMetadata.getFilePath()).collect(Collectors.toList());
                    SampleFileSparkScript sparkScript = ((SparkFileSchemaParser) fileSchemaParser.get()).getSparkScript(paths);
                    dataSet.setSparkScript(sparkScript);

                }
            });

            return dataSet;

        }));
        fileMetadataResponse.setDatasets(fileDatasets);
        modifiedTransformResponse.setResults(fileMetadataResponse);
    }

    private Optional<SchemaParserDescriptor> findSchemaParserForMimeType(String mimeType) {
        return FileParserFactory.instance().getSparkSchemaParserDescriptors().stream().filter(parser -> Arrays.asList(parser.getMimeTypes()).contains(mimeType)).findFirst();
    }

    private Optional<FileSchemaParser> fileSchemaParser(String mimeType) {
        return findSchemaParserForMimeType(mimeType).map(parser -> fileSchemaParser(parser).get());
    }

    private Optional<FileSchemaParser> fileSchemaParser(SchemaParserDescriptor descriptor) {
        try {
            SchemaParserAnnotationTransformer transformer = new SchemaParserAnnotationTransformer();
            FileSchemaParser p = transformer.fromUiModel(descriptor);
            if (p instanceof SparkFileSchemaParser) {
                SparkFileSchemaParser sparkFileSchemaParser = (SparkFileSchemaParser) p;
                sparkFileSchemaParser.setDataFrameVariable("df");
                sparkFileSchemaParser.setLimit(-1);
            }
            return Optional.of(p);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static void main(String args[]) {

        List<String> files = new ArrayList<>();
        files.add("file://test.csv");
        files.add("file://test2.csv");
        String filePaths = "\"" + StringUtils.join(files, "\",\"") + "\"";
        files.remove(0);
        String filePaths2 = StringUtils.join(files, "\",\"");
        int i = 0;
    }


}
