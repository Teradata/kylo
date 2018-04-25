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
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents the arguments used by MultiSparkExeApp.
 */
@SuppressWarnings("deprecation")
public class MultiSparkExecArguments implements Serializable {
    
    public static final String APPS_SWITCH = "--apps";
    
    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(MultiSparkExecArguments.class);
    private static final Pattern QUOTED = Pattern.compile("'(.*)'");
    
    private static final ObjectWriter APP_CMD_WRITER;
    private static final ObjectReader APP_CMD_READER;
    private static final TypeReference<List<SparkApplicationCommand>> SPEC_LIST_TYPE = new TypeReference<List<SparkApplicationCommand>>() { };

    static {
        ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);
        APP_CMD_READER = mapper.reader().withType(SPEC_LIST_TYPE);
        APP_CMD_WRITER = mapper.writer().withType(SPEC_LIST_TYPE);
    }

    
    @Parameter(names = APPS_SWITCH, required=true, converter = AppCommandConverter.class, listConverter = AppCommandConverter.class)
    private List<SparkApplicationCommand> commands;
    
    public static String[] createCommandLine(List<SparkApplicationCommand> specs) {
        try {
            String json = APP_CMD_WRITER.writeValueAsString(specs);
            String escaped = json.replaceAll("\\\\n", "\\\\\\\\n");
            
            String[] args = new String[] { APPS_SWITCH, "'" + escaped + "'" };
            log.debug("MultiSparkExeApp args: {} {}", args[0], args[1]);
            return args;
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Unable to serialize Spark application commands", e);
        }
    }
    
    public MultiSparkExecArguments(String... args) {
        super();
        
        new JCommander(this).parse(args);
    }
    
    public List<SparkApplicationCommand> getCommands() {
        return commands;
    }

    public static class AppCommandConverter implements IStringConverter<List<SparkApplicationCommand>> {
        @Override
        public List<SparkApplicationCommand> convert(String string) {
            try {
                String value = string.trim();
                Matcher matcher = QUOTED.matcher(value);
                
                if (matcher.matches()) {
                    return APP_CMD_READER.readValue(matcher.group(1));
                } else {
                    return APP_CMD_READER.readValue(value);
                }
            } catch (IOException e) {
                throw new ParameterException("Unable to deserialize Spark application commands", e);
            }
        }
    }
}
