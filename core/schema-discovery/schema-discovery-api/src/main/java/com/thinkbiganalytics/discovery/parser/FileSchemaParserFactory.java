package com.thinkbiganalytics.discovery.parser;

import java.io.InputStream;

public interface FileSchemaParserFactory {


    FileSchemaParser getFileSchemaParser(InputStream inputStream, String fileName) throws Exception;

}
