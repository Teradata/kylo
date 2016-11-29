/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.discovery.parsers.parquet;

import com.thinkbiganalytics.discovery.parser.FileSchemaParser;
import com.thinkbiganalytics.discovery.parser.SchemaParser;
import com.thinkbiganalytics.discovery.schema.Schema;
import com.thinkbiganalytics.discovery.util.TableSchemaType;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.hadoop.ParquetInputSplit;
import org.apache.parquet.hadoop.metadata.ParquetMetadata;
import org.apache.parquet.schema.MessageType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import static org.apache.parquet.format.converter.ParquetMetadataConverter.NO_FILTER;

/**
 * Created by matthutton on 11/28/16.
 */
@SchemaParser(name = "Legacy Parquet", description = "Supports parquet formatted files.", tags = {"Parquet"})
public class LegacyParquetFileSchemaParser implements FileSchemaParser {

    @Override
    public Schema parse(InputStream inputStream, Charset charset, TableSchemaType tableSchemaType) throws IOException {
        Configuration conf = new Configuration();
        ParquetMetadata metaData;
        Path file = null;

        File tempFile = File.createTempFile("thinkbig-parquet", "dat");

        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            IOUtils.copyLarge(inputStream, fos);

            file = new Path(tempFile.toURI());

            metaData = ParquetFileReader.readFooter(conf, file, NO_FILTER);
            MessageType schema = metaData.getFileMetaData().getSchema();
            System.out.println(schema);

        } finally {
            tempFile.delete();
        }
        return null;

    }
}
