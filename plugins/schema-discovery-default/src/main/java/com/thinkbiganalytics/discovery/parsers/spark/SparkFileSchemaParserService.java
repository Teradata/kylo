/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.discovery.parsers.spark;

import com.thinkbiganalytics.db.model.query.QueryResult;
import com.thinkbiganalytics.db.model.query.QueryResultColumn;
import com.thinkbiganalytics.discovery.model.DefaultField;
import com.thinkbiganalytics.discovery.model.DefaultHiveSchema;
import com.thinkbiganalytics.discovery.schema.Schema;
import com.thinkbiganalytics.discovery.util.TableSchemaType;
import com.thinkbiganalytics.spark.metadata.TransformRequest;
import com.thinkbiganalytics.spark.metadata.TransformResponse;
import com.thinkbiganalytics.spark.service.TransformService;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptException;

/**
 * Utilizes Spark's support to infer schema from a sample file
 */
@Component
public class SparkFileSchemaParserService {

    public enum SparkFileType {
        PARQUET, AVRO, JSON, ORC
    }

    @Autowired
    TransformService transformService;
    // Port: 8450

    /**
     * Delegate to spark shell service to load the file into a temporary table and loading it
     */
    public Schema doParse(InputStream inputStream, SparkFileType fileType, TableSchemaType tableSchemaType) throws IOException {

        /* TODO: Support yarn-cluster mode. Not supported now since file will be written to edge */
        File tempFile = toFile(inputStream);
        try {
            TransformResponse response = transformService.execute(createTransformRequest(tempFile, fileType));
            List<com.thinkbiganalytics.db.model.query.QueryResultColumn> columns = response.getResults().getColumns();
            return toSchema(response.getResults(), fileType, tableSchemaType);

        } catch (ScriptException e) {
            throw new IOException("Unexpected script exception ", e);

        } finally {
            tempFile.delete();
        }
    }

    private TransformRequest createTransformRequest(File localFile, SparkFileType fileType) {
        TransformRequest transformRequest = new TransformRequest();
        transformRequest.setScript(toScript(localFile, fileType));
        return transformRequest;
    }

    private String toScript(File localFile, SparkFileType fileType) {
        String path = "file://" + localFile.getAbsolutePath();

        StringBuffer sb = new StringBuffer();
        sb.append("import sqlContext.implicits._\n");
        sb.append("import org.apache.spark.sql._\n");

        String method;
        switch (fileType) {
            case AVRO:
                method = "sqlContext.read.%s(\"%s\").toDF()";
                break;
            case JSON:
                method = "sqlContext.read.%s(\"%s\").toDF()";
                break;
            case PARQUET:
                method = "sqlContext.read.%s(\"%s\").toDF()";
                break;
            case ORC:
                method = "sqlContext.read.%s(\"%s\").toDF()";
                break;
            default:
                throw new UnsupportedOperationException("Type not supported [" + fileType + "]");
        }
        sb.append(String.format("sqlContext.read.%s(\"%s\").toDF()", method, path));
        sb.append(".limit(10).registerAsTempTable(\"${tableName}\"");
        return sb.toString();
    }

    private Schema toSchema(QueryResult results, SparkFileType fileType, TableSchemaType tableSchemaType) throws IOException {

        switch (tableSchemaType) {
            case HIVE:
                return toHiveSchema(results, fileType);
            default:
                throw new IOException("Unsupported schema type [" + tableSchemaType + "]");
        }
    }

    private DefaultHiveSchema toHiveSchema(QueryResult result, SparkFileType fileType) {
        DefaultHiveSchema schema = new DefaultHiveSchema();
        schema.setHiveFormat("STORED AS " + fileType);
        ArrayList<DefaultField> fields = new ArrayList<>();
        List<QueryResultColumn> columns = result.getColumns();
        for (QueryResultColumn column : columns) {
            DefaultField field = new DefaultField();
            field.setName(column.getDisplayName());
            field.setDerivedDataType(column.getDataType());
            fields.add(field);
        }
        schema.setFields(fields);
        return schema;
    }
    private File toFile(InputStream is) throws IOException {
        File tempFile = File.createTempFile("kylo-spark-parser", "dat");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            IOUtils.copyLarge(is, fos);
        }
        return tempFile;
    }

}
