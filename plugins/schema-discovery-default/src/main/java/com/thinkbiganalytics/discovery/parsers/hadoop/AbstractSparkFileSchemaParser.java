package com.thinkbiganalytics.discovery.parsers.hadoop;

/*-
 * #%L
 * thinkbig-schema-discovery-default
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thinkbiganalytics.discovery.parser.SampleFileSparkScript;
import com.thinkbiganalytics.discovery.parser.SparkFileSchemaParser;
import com.thinkbiganalytics.discovery.schema.Schema;
import com.thinkbiganalytics.discovery.util.TableSchemaType;
import com.thinkbiganalytics.spring.SpringApplicationContext;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * abstract Spark file schema parser
 */
public abstract class AbstractSparkFileSchemaParser implements SparkFileSchemaParser {

    /**
     * how many rows should the script limit
     */
    protected Integer limit = 10;
    /**
     * if supplied, what should the variable name of the dataframe be called in the generated script.
     */
    protected String dataFrameVariable;

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public void setDataFrameVariable(String dataFrameVariable) {
        this.dataFrameVariable = dataFrameVariable;
    }

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

    public Schema parse(InputStream is, Charset charset, TableSchemaType target) throws IOException {
        return getSparkParserService().doParse(is, getSparkFileType(), target, getSparkCommandBuilder());
    }

    /**
     * Set spark service for unit testing
     */
    protected void setSparkParserService(SparkFileSchemaParserService service) {
        this.parserService = service;
    }


    public SampleFileSparkScript getSparkScript(InputStream is) throws IOException {
        return getSparkParserService().getSparkScript(is, getSparkFileType(), getSparkCommandBuilder());
    }

    @Override
    public SparkCommandBuilder getSparkCommandBuilder() {
        return new DefaultSparkCommandBuilder(dataFrameVariable, limit, getSparkFileType().name().toLowerCase());
    }

}
