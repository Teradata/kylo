/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.discovery.parsers.spark;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thinkbiganalytics.discovery.parser.FileSchemaParser;
import com.thinkbiganalytics.discovery.parser.SchemaParser;
import com.thinkbiganalytics.discovery.schema.Schema;
import com.thinkbiganalytics.discovery.util.TableSchemaType;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

@SchemaParser(name = "Parquet", description = "Supports parquet formatted files.", tags = {"Parquet"})
public class ParquetFileSchemaParser implements FileSchemaParser {

    @Autowired
    @JsonIgnore
    private transient SparkFileSchemaParserService parserService;

    @Override
    public Schema parse(InputStream is, Charset charset, TableSchemaType target) throws IOException {
        return parserService.doParse(is, SparkFileSchemaParserService.SparkFileType.PARQUET, target);
    }
}
