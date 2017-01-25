package com.thinkbiganalytics.discovery.rest.controller;

import com.thinkbiganalytics.discovery.model.DefaultHiveSchema;
import com.thinkbiganalytics.discovery.parser.FileSchemaParser;
import com.thinkbiganalytics.discovery.parser.SchemaParser;
import com.thinkbiganalytics.discovery.schema.Schema;
import com.thinkbiganalytics.discovery.util.TableSchemaType;
import com.thinkbiganalytics.policy.PolicyProperty;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

@SchemaParser(name = "Test2", description = "Test parser 2", tags = {"XML", "JSON"}, clientHelper = "Test2Helper")
public class MockSchemaParser2 implements FileSchemaParser {

    @PolicyProperty(name = "Auto Detect?", hint = "Auto detect will attempt to infer delimiter from the sample file.", value = "true")
    private boolean autoDetect = true;

    @PolicyProperty(name = "Header?", hint = "Whether file has a header.", value = "true")
    private boolean headerRow = true;

    @Override
    public Schema parse(InputStream is, Charset charset, TableSchemaType target) throws IOException {
        return new DefaultHiveSchema();
    }
}
