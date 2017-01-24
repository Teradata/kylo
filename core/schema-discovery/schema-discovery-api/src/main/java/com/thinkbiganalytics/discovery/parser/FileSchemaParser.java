package com.thinkbiganalytics.discovery.parser;

import com.thinkbiganalytics.discovery.schema.Schema;
import com.thinkbiganalytics.discovery.util.TableSchemaType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Parses a file to determine its structure and format.
 */
public interface FileSchemaParser {

    /**
     * Parse the provided file and builds a schema with data types that fit the target
     * @param is the raw data to parse
     * @param charset the character-set (e.g. UTF-8)
     * @param target the target platform (e.g. Hive)
     * @return a derived schema for the source
     */
    Schema parse(InputStream is, Charset charset, TableSchemaType target) throws IOException;

}
