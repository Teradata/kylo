/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.discovery.parsers.hadoop;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thinkbiganalytics.discovery.parser.FileSchemaParser;
import com.thinkbiganalytics.discovery.parser.SchemaParser;
import com.thinkbiganalytics.discovery.schema.Schema;
import com.thinkbiganalytics.discovery.util.TableSchemaType;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import javax.inject.Inject;

@SchemaParser(name = "Parquet", description = "Supports parquet formatted files.", tags = {"Parquet"})
public class ParquetFileSchemaParser extends AbstractSparkFileSchemaParser implements FileSchemaParser {

    @Override
    public Schema parse(InputStream is, Charset charset, TableSchemaType target) throws IOException {
        return getSparkParserService().doParse(is, SparkFileSchemaParserService.SparkFileType.PARQUET, target);
    }
}
