package com.thinkbiganalytics.spark.rest.controller;
/*-
 * #%L
 * Spark Shell Service Controllers
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

import com.thinkbiganalytics.discovery.model.SchemaParserDescriptor;
import com.thinkbiganalytics.kylo.catalog.datasource.DataSourceUtil;
import com.thinkbiganalytics.kylo.catalog.rest.model.Connector;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSetTemplate;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSource;
import com.thinkbiganalytics.spark.rest.model.PreviewDataSetRequest;
import com.thinkbiganalytics.spark.rest.model.KyloCatalogReadRequest;
import com.thinkbiganalytics.spark.rest.model.PageSpec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Take a dataset request and convert it to KyloCatalog request
 * TODO: This might be able to be removed now that we have the CatalogDataSetProvider in SparkShell
 */
public class KyloCatalogReaderUtil {

    public static KyloCatalogReadRequest toKyloCatalogRequest(PreviewDataSetRequest previewRequest){
        DataSource dataSource = previewRequest.getDataSource();
        Connector connector = dataSource.getConnector();
        //merge template
        DataSetTemplate dataSetTemplate = DataSourceUtil.mergeTemplates(dataSource);

        //get data out of the dataset template
        List<String> jars =dataSetTemplate.getJars();
        List<String> paths = dataSetTemplate.getPaths();
        List<String> files = dataSetTemplate.getFiles();
        String format = dataSetTemplate.getFormat();

        Map<String,String> options =dataSetTemplate.getOptions();
        if(options == null){
            options = new HashMap<>();
        }
        //parse the SchemaParser if it exists and add options and update the format
        if(previewRequest.getSchemaParser() != null){
            SchemaParserDescriptor schemaParser = previewRequest.getSchemaParser();
            Map<String,String>  sparkOptions =  schemaParser.getProperties().stream()
                .collect(Collectors.toMap(p->
                                              p.getAdditionalProperties()
                                                  .stream()
                                                  .filter(labelValue -> "spark.option".equalsIgnoreCase(labelValue.getLabel()))
                                                  .map(labelValue -> labelValue.getValue()
                                                  ).findFirst().orElse("")
                    ,p->p.getValue()));
            //remove any options that produced an empty key
            sparkOptions.remove("");
            //supplied options by the schema parse take precedence over the template options
            options.putAll(sparkOptions);
            format = schemaParser.getSparkFormat();
        }

        //add in additional preview options
        if(previewRequest.getProperties() != null && !previewRequest.getProperties().isEmpty()){
            options.putAll(previewRequest.getProperties());
        }


        KyloCatalogReadRequest request = new KyloCatalogReadRequest();
        request.setFiles(files);
        request.setJars(jars);
        request.setFormat(format);
        request.setOptions(options);
        if(previewRequest.getPreviewItem() != null && previewRequest.isAddPreviewItemToPath()) {
         request.addPath(previewRequest.getPreviewItem());
        }
        PageSpec pageSpec = previewRequest.getPageSpec();
        if(pageSpec == null){
            pageSpec = new PageSpec();
        }
        request.setPageSpec(pageSpec);
        return request;
    }

}
