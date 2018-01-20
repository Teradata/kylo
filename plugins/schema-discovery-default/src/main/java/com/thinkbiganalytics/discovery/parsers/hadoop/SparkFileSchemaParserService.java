package com.thinkbiganalytics.discovery.parsers.hadoop;

/*-
 * #%L
 * thinkbig-schema-discovery-default
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


import com.google.common.util.concurrent.Uninterruptibles;
import com.thinkbiganalytics.discovery.model.DefaultField;
import com.thinkbiganalytics.discovery.model.DefaultHiveSchema;
import com.thinkbiganalytics.discovery.schema.Field;
import com.thinkbiganalytics.discovery.schema.QueryResultColumn;
import com.thinkbiganalytics.discovery.schema.Schema;
import com.thinkbiganalytics.discovery.util.ParserHelper;
import com.thinkbiganalytics.discovery.util.TableSchemaType;
import com.thinkbiganalytics.spark.rest.model.TransformQueryResult;
import com.thinkbiganalytics.spark.rest.model.TransformRequest;
import com.thinkbiganalytics.spark.rest.model.TransformResponse;
import com.thinkbiganalytics.spark.shell.SparkShellProcess;
import com.thinkbiganalytics.spark.shell.SparkShellProcessManager;
import com.thinkbiganalytics.spark.shell.SparkShellRestClient;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

/**
 * Utilizes Spark's support to infer schema from a sample file
 */
@Component
public class SparkFileSchemaParserService {

    private static final Logger log = LoggerFactory.getLogger(SparkFileSchemaParserService.class);
    @Inject
    private SparkShellProcessManager shellProcessManager;


    private static String DATATYPE_PRECISION_SCALE_REGEX = "(.*)((\\([0-9]+,[0-9]+\\))|(\\([0-9]+\\)))";

    /**
     * Communicates with Spark Shell processes
     */
    @Inject
    private SparkShellRestClient restClient;

    /**
     * Delegate to spark shell service to load the file into a temporary table and loading it
     */
    public Schema doParse(InputStream inputStream, SparkFileType fileType, TableSchemaType tableSchemaType) throws IOException {

        File tempFile = toFile(inputStream);
        try {
            SparkShellProcess shellProcess = shellProcessManager.getSystemProcess();
            TransformResponse response = restClient.transform(shellProcess, createTransformRequest(tempFile, fileType));
            while (response.getStatus() != TransformResponse.Status.SUCCESS) {
                if (response.getStatus() == TransformResponse.Status.ERROR) {
                    throw new IOException("Failed to process data [" + response.getMessage() + "]");
                } else {
                    Uninterruptibles.sleepUninterruptibly(100L, TimeUnit.MILLISECONDS);
                }

                final Optional<TransformResponse> optionalResponse = restClient.getTransformResult(shellProcess, response.getTable());
                if (optionalResponse.isPresent()) {
                    response = optionalResponse.get();
                }
            }
            return toSchema(response.getResults(), fileType, tableSchemaType);

        } catch (Exception e) {
            log.error("Error parsing file {}: {}", fileType, e.getMessage());
            throw new IOException("Unexpected exception. Verify file is the proper format", e);
        } finally {
            if (!tempFile.delete()) {
                log.error("The temp file was not deleted successfully: " + tempFile.getName());
            }
        }
    }
    // Port: 8450

    private TransformRequest createTransformRequest(File localFile, SparkFileType fileType) {
        TransformRequest transformRequest = new TransformRequest();
        transformRequest.setScript(toScript(localFile, fileType));
        return transformRequest;
    }

    private String toScript(File localFile, SparkFileType fileType) {
        String path = "file://" + localFile.getAbsolutePath();
        // IDE testing:
        //path = "file:///var/sample/signups.orc";
        //path = "file:///var/sample/HiveGroup.parquet";
        StringBuffer sb = new StringBuffer();
        sb.append("import sqlContext.implicits._\n");
        sb.append("import org.apache.spark.sql._\n");

        String method;
        switch (fileType) {
            case AVRO:
                method = "avro";
                sb.append("import com.databricks.spark.avro._\n");
                sb.append("sqlContext.sparkContext.hadoopConfiguration.set(\"avro.mapred.ignore.inputs.without.extension\", \"false\")\n");
                break;
            case JSON:
                method = "json";
                break;
            case PARQUET:
                method = "parquet";
                break;
            case ORC:
                method = "orc";
                break;
            default:
                throw new UnsupportedOperationException("Type not supported [" + fileType + "]");
        }
        sb.append(String.format("sqlContext.read.%s(\"%s\").limit(10).toDF()", method, path));
        return sb.toString();
    }

    private Schema toSchema(TransformQueryResult results, SparkFileType fileType, TableSchemaType tableSchemaType) throws IOException {

        switch (tableSchemaType) {
            case HIVE:
                return toHiveSchema(results, fileType);
            default:
                throw new IOException("Unsupported schema type [" + tableSchemaType + "]");
        }
    }

    /**
     * Strip out the (precision,scale) from the datatype and assign it to the proper field.precisionScale property
     *
     * @param field the field to inspect
     */
    private void setPrecisionAndScale(DefaultField field) {
        String dataType = field.getDerivedDataType();
        Pattern pattern = Pattern.compile(DATATYPE_PRECISION_SCALE_REGEX);
        Matcher matcher = pattern.matcher(dataType);
        if (matcher.find()) {
            //group 1 is the string datatype
            //group 2 is the precision and scale
            String newDataType = matcher.group(1);
            String precisionAndScale = matcher.group(2);
            //replace the ()
            precisionAndScale = precisionAndScale.replaceAll("\\(|\\)", "");
            field.setDerivedDataType(newDataType);
            field.setPrecisionScale(precisionAndScale);
        }
    }

    private DefaultHiveSchema toHiveSchema(TransformQueryResult result, SparkFileType fileType) {
        DefaultHiveSchema schema = new DefaultHiveSchema();
        schema.setHiveFormat("STORED AS " + fileType);
        schema.setStructured(true);
        ArrayList<Field> fields = new ArrayList<>();
        List<? extends QueryResultColumn> columns = result.getColumns();
        for (int i = 0; i < columns.size(); ++i) {
            QueryResultColumn column = columns.get(i);
            DefaultField field = new DefaultField();
            field.setName(column.getDisplayName());
            field.setNativeDataType(column.getDataType());
            field.setDerivedDataType(column.getDataType());
            field.setDataTypeDescriptor(ParserHelper.hiveTypeToDescriptor(column.getDataType()));
            //strip the precisionScale and assign to the field property
            setPrecisionAndScale(field);
            // Add sample values
            List<List<Object>> values = result.getRows();
            for (List<Object> colMap : values) {
                Object oVal = colMap.get(i);
                if (oVal != null) {
                    field.getSampleValues().add(oVal.toString());
                }
            }
            fields.add(field);
        }
        schema.setFields(fields);
        return schema;
    }

    private File toFile(InputStream is) throws IOException {
        File tempFile = File.createTempFile("kylo-spark-parser", ".dat");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            IOUtils.copyLarge(is, fos);
        }
        log.info("Created temporary file {} success? {}", tempFile.getAbsoluteFile().toURI(), tempFile.exists());
        return tempFile;
    }

    public enum SparkFileType {
        PARQUET, AVRO, JSON, ORC
    }
}
