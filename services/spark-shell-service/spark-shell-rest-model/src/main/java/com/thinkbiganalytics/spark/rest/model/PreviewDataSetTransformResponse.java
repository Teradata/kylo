package com.thinkbiganalytics.spark.rest.model;
/*-
 * #%L
 * Spark Shell Service REST Model
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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.thinkbiganalytics.discovery.model.SchemaParserDescriptor;
import com.thinkbiganalytics.spark.dataprofiler.output.OutputRow;

import java.util.List;
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PreviewDataSetTransformResponse extends TransformResponse {

    public PreviewDataSetTransformResponse(){
        super();
    }


    public PreviewDataSetTransformResponse(TransformResponse response, SchemaParserDescriptor schemaParser){
      this.setStatus(response.getStatus());
      this.setMessage(response.getMessage());
      this.setProfile(response.getProfile());
      this.setProgress(response.getProgress());
      this.setResults(response.getResults());
      this.setTable(response.getTable());
      this.setActualCols(response.getActualCols());
      this.setActualRows(response.getActualRows());
      this.schemaParser = schemaParser;
    }

    /**
     * the schema parser used, if any
     */
    private SchemaParserDescriptor schemaParser;

    public SchemaParserDescriptor getSchemaParser() {
        return schemaParser;
    }

    public void setSchemaParser(SchemaParserDescriptor schemaParser) {
        this.schemaParser = schemaParser;
    }
}
