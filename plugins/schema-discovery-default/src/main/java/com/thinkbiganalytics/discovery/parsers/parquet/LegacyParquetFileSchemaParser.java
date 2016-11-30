/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.discovery.parsers.parquet;

import com.thinkbiganalytics.discovery.model.DefaultField;
import com.thinkbiganalytics.discovery.model.DefaultHiveSchema;
import com.thinkbiganalytics.discovery.parser.FileSchemaParser;
import com.thinkbiganalytics.discovery.parser.SchemaParser;
import com.thinkbiganalytics.discovery.schema.Field;
import com.thinkbiganalytics.discovery.schema.HiveTableSchema;
import com.thinkbiganalytics.discovery.schema.Schema;
import com.thinkbiganalytics.discovery.util.TableSchemaType;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.column.ColumnDescriptor;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.hadoop.metadata.ParquetMetadata;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.Type;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Vector;

import static org.apache.parquet.format.converter.ParquetMetadataConverter.NO_FILTER;

/**
 * Extracts schema from a parquet file
 */
@SchemaParser(name = "Parquet", description = "Supports parquet formatted files.", tags = {"Parquet"})
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
            return toSchema(schema);


        } finally {
            tempFile.delete();
        }
    }


    private HiveTableSchema toSchema(MessageType schema) {

        List<Type> fields2 = schema.getFields();

        for (Type t : fields2) {

            System.out.println("Name: " + t.getName());
            System.out.println("    Original: " + t.getOriginalType());
            if (t.isPrimitive()) {

                //System.out.println(t.asPrimitiveType());
                System.out.println("    Primitive Name: " + t.asPrimitiveType().getPrimitiveTypeName());
                System.out.println("    Decimal Metadata: " + t.asPrimitiveType().getDecimalMetadata());
                System.out.println("    Length: " + t.asPrimitiveType().getTypeLength());
            } else {
                System.out.println("Group Type: " + t.asGroupType());
            }
        }

        DefaultHiveSchema hiveSchema = new DefaultHiveSchema();
        hiveSchema.setHiveFormat("STORED AS PARQUET");

        List<Field> fields = new Vector<>();

        List<ColumnDescriptor> columnDescriptorList = schema.getColumns();

        for (ColumnDescriptor cd : columnDescriptorList) {
            DefaultField field = new DefaultField();
            field.setName(cd.getPath()[cd.getPath().length - 1]);
            switch (cd.getType()) {
                case BOOLEAN:
                    field.setNativeDataType("BOOLEAN");
                    break;
                case INT64:
                case INT96:
                    field.setNativeDataType("BIGINT");
                    break;
                case FIXED_LEN_BYTE_ARRAY:
                    field.setNativeDataType("ARRAY<?>");
                    break;
                case BINARY:
                case INT32:
                    field.setNativeDataType("INT");
                    break;
                case DOUBLE:
                    field.setNativeDataType("DOUBLE");
                    break;
                case FLOAT:
                    field.setNativeDataType("FLOAT");
                    break;
                default:
                    throw new RuntimeException("");
            }

        }
        hiveSchema.setFields(fields);
        return hiveSchema;

    }

}
