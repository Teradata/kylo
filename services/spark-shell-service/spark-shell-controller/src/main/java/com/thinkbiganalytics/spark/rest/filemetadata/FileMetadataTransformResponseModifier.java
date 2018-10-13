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
import com.thinkbiganalytics.spark.rest.controller.AbstractTransformResponseModifier;
import com.thinkbiganalytics.spark.rest.filemetadata.tasks.FileMetadataTaskService;
import com.thinkbiganalytics.spark.rest.model.FileMetadataResponse;
import com.thinkbiganalytics.spark.rest.model.ModifiedTransformResponse;
import com.thinkbiganalytics.spark.rest.model.TransformResponse;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Once the initial Spark job finishes returning general information about the file this will be invoked to read other header information in the file
 */
public class FileMetadataTransformResponseModifier extends AbstractTransformResponseModifier<FileMetadataResponse> {

    private FileMetadataTaskService trackerService;

    public FileMetadataTransformResponseModifier(FileMetadataTaskService trackerService) {
        this.trackerService = trackerService;
    }

    private FileMetadataResponse.ParsedFileMetadata parseRow(List<Object> row) {
        String mimeType = (String) getRowValue("mimeType", row);
        String delimiter = (String) getRowValue("delimiter", row);
        Integer headerCount = getRowValue("headerCount", row, Integer.class, 0);
        String filePath = (String) getRowValue("resource", row);
        String encoding = (String) getRowValue("encoding", row);
        String rowTag = (String) getRowValue("rowTag", row);
        Map<String, String> properties = getRowValue("properties", row, Map.class);

        FileMetadataResponse.ParsedFileMetadata metadata = new FileMetadataResponse.ParsedFileMetadata();
        metadata.setMimeType(mimeType);
        metadata.setDelimiter(delimiter);
        metadata.setEncoding(encoding);
        metadata.setFilePath(filePath);
        metadata.setRowTag(rowTag);
        metadata.setProperties(properties);
        metadata.setTransformResultRow(row);
        return metadata;
    }


    public Map<String, List<FileMetadataResponse.ParsedFileMetadata>> groupByMimeType() {
        return response.getResults().getRows().stream().map(r -> parseRow(r)).collect(Collectors
                                                                                          .groupingBy(FileMetadataResponse.ParsedFileMetadata::getMimeType));
    }

    /**
     * This will be called once the initial spark job finishes
     * This will then invoke the additional jobs (if applicable) to retreive header information about the files if it can and add the header information back into the final ModifiedTransformResponse
     *
     * @param modifiedTransformResponse the response to populate if needed
     */
    public void modifySuccessfulResults(ModifiedTransformResponse<FileMetadataResponse> modifiedTransformResponse) {

        //we are done with the first pass
        //now we need to invoke the threads to look up the header schema info for the other files
        //set this status as pending so the calling threads will know to poll for status
        modifiedTransformResponse.setStatus(TransformResponse.Status.PENDING);
        //notify the tracking service to start finding the additional schema information
        trackerService.findFileMetadataSchemas(modifiedTransformResponse, this);
    }

    public void complete(ModifiedTransformResponse<FileMetadataResponse> modifiedTransformResponse) {
        FileMetadataResponse fileMetadataResponse = new FileMetadataResponse();
        String tableId = modifiedTransformResponse.getTable();

        Map<String, List<FileMetadataResponse.ParsedFileMetadata>> groupedMimeTypeMetadata = response.getResults().getRows().stream().map(r -> parseRow(r)).collect(Collectors.groupingBy(
            FileMetadataResponse.ParsedFileMetadata::getMimeType));

        Map<String, List<FileMetadataResponse.ParsedFileMetadata>> groupedMetadata = response.getResults().getRows().stream().map(r -> parseRow(r)).collect(Collectors.groupingBy(
            FileMetadataResponse.ParsedFileMetadata::getKey));

        //create user friendly names from the groupings
        //the key in this new map will be the first file found
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
                schemaParserDescriptor.getProperties().stream().filter(property -> property.getObjectProperty().equals("rowTag")).findFirst().ifPresent(property -> {
                    property.setValue(firstFile.getRowTag());
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
        modifiedTransformResponse.setStatus(TransformResponse.Status.SUCCESS);
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
