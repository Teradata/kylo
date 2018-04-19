/**
 * 
 */
package com.thinkbiganalytics.spark.multiexec;

/*-
 * #%L
 * kylo-spark-merge-table-app
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.IParameterSplitter;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NavigableMap;

/**
 *
 */
public class MultiSparkExecArguments implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public static final String APPS_SWITCH = "--apps";
    
    private static final ObjectWriter APP_CMD_WRITER;
    private static final ObjectReader APP_CMD_READER;
    private static final TypeReference<List<SparkApplicationCommand>> SPEC_LIST_TYPE = new TypeReference<List<SparkApplicationCommand>>() { };

    static {
        ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);
//        APP_CMD_READER = mapper.reader().forType(SPEC_LIST_TYPE);
//        APP_CMD_WRITER = mapper.writer().forType(SPEC_LIST_TYPE);
        APP_CMD_READER = mapper.reader().withType(SPEC_LIST_TYPE);
        APP_CMD_WRITER = mapper.writer().withType(SPEC_LIST_TYPE);
    }

    
    @Parameter(names = APPS_SWITCH, required=true, converter = AppCommandConverter.class, listConverter = AppCommandConverter.class)
//    @Parameter(names = APPS_SWITCH, required=true, converter = AppCommandConverter.class, splitter=NonSplitter.class)
//    @Parameter(names = APPS_SWITCH, required=true, converter = AppCommandConverter.class, listConverter=NonSplitter.class)
    private List<SparkApplicationCommand> commands;
    
    public static String[] createCommandLine(List<SparkApplicationCommand> specs) {
        try {
            return new String[] { APPS_SWITCH, APP_CMD_WRITER.writeValueAsString(specs) };
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Unable to serialize Spark application commands", e);
        }
    }
    
    /**
     * 
     */
    public MultiSparkExecArguments(String... args) {
        super();
        
        new JCommander(this).parse(args);
    }
    
    /**
     * @return the commands
     */
    public List<SparkApplicationCommand> getCommands() {
        return commands;
    }
    
    /**
     * Supports separating values by commas and/or whitespace.
     */
    public static class WhitespaceCommaSplitter implements IParameterSplitter {
        public List<String> split(String value) {
            List<String> values = new ArrayList<>();
            String cleaned = value.replaceAll("\\s+", ",");
            cleaned = cleaned.replaceAll(",+", ",");
            String[] tokens = cleaned.split(",");
            
            for (String token : tokens) {
                if (StringUtils.isNotBlank(token)) {
                    values.add(token);
                }
            }
            
            return values;
        }
    }
    
    public static class NonSplitter implements IParameterSplitter {
        @Override
        public List<String> split(String value) {
            return Collections.singletonList(value);
        }
    }

    public static class AppCommandConverter implements IStringConverter<List<SparkApplicationCommand>> {
        @Override
        public List<SparkApplicationCommand> convert(String value) {
            try {
                return APP_CMD_READER.readValue(value);
            } catch (IOException e) {
                throw new ParameterException("Unable to deserialize Spark application commands", e);
            }
        }
    }
}
