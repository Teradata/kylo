package com.thinkbiganalytics.spark.rest.filemetadata.tasks;

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

import com.thinkbiganalytics.json.ObjectMapperSerializer;
import com.thinkbiganalytics.spark.rest.controller.SparkShellScriptRunner;
import com.thinkbiganalytics.spark.rest.controller.TransformResponseUtil;
import com.thinkbiganalytics.spark.rest.filemetadata.FileMetadataTransformResponseModifier;
import com.thinkbiganalytics.spark.rest.model.FileMetadataResponse;
import com.thinkbiganalytics.spark.rest.model.ModifiedTransformResponse;
import com.thinkbiganalytics.spark.rest.model.TransformResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The final task that will get invoked when all other tasks FileMetadataSchema tasks have been completed
 */
public class FileMetadataCompletionTask implements Runnable {

    FileMetadataTransformResponseModifier finalResult;

    Map<SparkShellScriptRunner, List<com.thinkbiganalytics.spark.rest.model.FileMetadataResponse.ParsedFileMetadata>> fileSchemaScriptRunnerMap = new HashMap<>();
    List<SparkShellScriptRunner> tasks;
    private boolean complete;

    private ModifiedTransformResponse<FileMetadataResponse> modifiedTransformResponse;

    public FileMetadataCompletionTask(ModifiedTransformResponse<FileMetadataResponse> modifiedTransformResponse, FileMetadataTransformResponseModifier finalResult) {
        this.finalResult = finalResult;
        this.modifiedTransformResponse = modifiedTransformResponse;
        this.tasks = new ArrayList<>();
    }

    public String getTableId() {
        return finalResult.getTableId();
    }

    public void addTask(SparkShellScriptRunner task, List<com.thinkbiganalytics.spark.rest.model.FileMetadataResponse.ParsedFileMetadata> files) {
        this.tasks.add(task);
        this.fileSchemaScriptRunnerMap.put(task, files);
    }

    public class FileMetadataSchema {

        String mimeType;
        String filePath;
        Map<String, String> schema;

        public FileMetadataSchema(String mimeType, String filePath, Map<String, String> schema) {
            this.mimeType = mimeType;
            this.filePath = filePath;
            this.schema = schema;
        }

        public String getMimeType() {
            return mimeType;
        }

        public void setMimeType(String mimeType) {
            this.mimeType = mimeType;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public Map<String, String> getSchema() {
            return schema;
        }

        public void setSchema(Map<String, String> schema) {
            this.schema = schema;
        }
    }

    @Override
    public void run() {
        List<String> exceptions = new ArrayList<>();
        //aggregrate
        if(this.tasks != null){
            exceptions.addAll(this.tasks.stream().filter(task -> task.hasExecutionException()).map(task -> task.getExecutionException()).collect(Collectors.toList()));
        }
        try {
            for (Map.Entry<SparkShellScriptRunner, List<com.thinkbiganalytics.spark.rest.model.FileMetadataResponse.ParsedFileMetadata>> e : this.fileSchemaScriptRunnerMap.entrySet()) {

                SparkShellScriptRunner runner = e.getKey();
                TransformResponse response = runner.getFinalResponse();
                if (response != null) {
                    TransformResponseUtil responseTransformer = new TransformResponseUtil(response);

                    Map<String, FileMetadataSchema> schemaMap = response.getResults().getRows().stream().map(row -> {
                        String mimeType = (String) responseTransformer.getRowValue("mimeType", row);
                        String filePath = (String) responseTransformer.getRowValue("resource", row);
                        Map<String, String> schema = responseTransformer.getRowValue("schema", row, Map.class);
                        return new FileMetadataSchema(mimeType, filePath, schema);
                    }).collect(Collectors.toMap(key -> key.getFilePath(), key -> key));

                    e.getValue().stream().forEach(originalData -> {
                                                      FileMetadataSchema updatedSchema = schemaMap.get(originalData.getFilePath());
                                                      if (updatedSchema != null) {
                                                          String headers = ObjectMapperSerializer.serialize(updatedSchema.getSchema());
                                                          originalData.getProperties().put("headers", headers);
                                                          originalData.getProperties().put("headerCount", updatedSchema.getSchema().size() + "");
                                                      }
                                                  }
                    );


                }
            }
        }catch (Exception e){
            exceptions.add(e.getMessage());
        }
        if(!exceptions.isEmpty()){
            String message = exceptions.stream().collect(Collectors.joining("\n"));
            this.modifiedTransformResponse.setMessage(message);
        }

        this.finalResult.complete(this.modifiedTransformResponse);
        this.complete = true;
    }

    public boolean isComplete() {
        return this.complete;
    }

    public ModifiedTransformResponse getModifiedTransformResponse() {
        return this.modifiedTransformResponse;
    }


}
