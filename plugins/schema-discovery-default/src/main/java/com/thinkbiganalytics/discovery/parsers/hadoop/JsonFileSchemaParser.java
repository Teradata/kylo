/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.discovery.parsers.hadoop;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thinkbiganalytics.discovery.parser.FileSchemaParser;
import com.thinkbiganalytics.discovery.parser.SchemaParser;
import com.thinkbiganalytics.discovery.schema.HiveTableSchema;
import com.thinkbiganalytics.discovery.schema.Schema;
import com.thinkbiganalytics.discovery.util.TableSchemaType;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

@SchemaParser(name = "JSON", description = "Supports JSON formatted files.", tags = {"JSON"})
public class JsonFileSchemaParser implements FileSchemaParser {

    @Autowired
    @JsonIgnore
    private transient SparkFileSchemaParserService parserService;

    @Override
    public Schema parse(InputStream is, Charset charset, TableSchemaType target) throws IOException {
        HiveTableSchema schema = (HiveTableSchema) parserService.doParse(is, SparkFileSchemaParserService.SparkFileType.JSON, target);
        schema.setHiveFormat("ROW FORMAT SERDE 'org.apache.hive.hcatalog.data.JsonSerDe' STORED AS TEXTFILE");
        return schema;
    }

}
