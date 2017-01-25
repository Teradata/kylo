package com.thinkbiganalytics.discovery.parsers.hadoop;

import com.thinkbiganalytics.discovery.parser.FileSchemaParser;
import com.thinkbiganalytics.discovery.parser.SchemaParser;
import com.thinkbiganalytics.discovery.schema.Schema;
import com.thinkbiganalytics.discovery.util.TableSchemaType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

@SchemaParser(name = "Avro", description = "Supports Avro formatted files.", tags = {"Avro"})
public class AvroFileSchemaParser extends AbstractSparkFileSchemaParser implements FileSchemaParser {

    @Override
    public Schema parse(InputStream is, Charset charset, TableSchemaType target) throws IOException {
        return getSparkParserService().doParse(is, SparkFileSchemaParserService.SparkFileType.AVRO, target);
    }

}
