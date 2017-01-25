package com.thinkbiganalytics.discovery.parsers.hadoop;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thinkbiganalytics.spring.SpringApplicationContext;

import javax.inject.Inject;

public class AbstractSparkFileSchemaParser {

    @Inject
    @JsonIgnore
    private transient SparkFileSchemaParserService parserService;

    public SparkFileSchemaParserService getSparkParserService() {
        // Since this class is created by reflection we need to call autowire
        if (parserService == null) {
            SpringApplicationContext.autowire(this);
        }
        return parserService;
    }

}
