/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.discovery.parsers.hadoop;

import com.thinkbiganalytics.discovery.parser.FileSchemaParser;
import com.thinkbiganalytics.discovery.parser.SchemaParser;
import com.thinkbiganalytics.discovery.schema.HiveTableSchema;
import com.thinkbiganalytics.discovery.schema.Schema;
import com.thinkbiganalytics.discovery.util.TableSchemaType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

@SchemaParser(name = "JSON", description = "Supports JSON formatted files.", tags = {"JSON"})
public class JsonFileSchemaParser extends AbstractSparkFileSchemaParser implements FileSchemaParser {

    @Override
    public Schema parse(InputStream is, Charset charset, TableSchemaType target) throws IOException {
        HiveTableSchema schema = (HiveTableSchema) getSparkParserService().doParse(is, SparkFileSchemaParserService.SparkFileType.JSON, target);
        schema.setHiveFormat("ROW FORMAT SERDE 'org.apache.hive.hcatalog.data.JsonSerDe' STORED AS INPUTFORMAT 'org.apache.hadoop.mapred.TextInputFormat'");

        return schema;
    }

}
