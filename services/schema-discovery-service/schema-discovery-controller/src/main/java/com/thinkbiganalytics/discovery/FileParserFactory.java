package com.thinkbiganalytics.discovery;

/*-
 * #%L
 * thinkbig-schema-discovery-controller
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

import com.thinkbiganalytics.discovery.parser.FileSchemaParser;
import com.thinkbiganalytics.discovery.parser.SchemaParser;
import com.thinkbiganalytics.policy.ReflectionPolicyAnnotationDiscoverer;
import com.thinkbiganalytics.spring.SpringApplicationContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Provides available parsers
 */
public class FileParserFactory {

    private static final Logger log = LoggerFactory.getLogger(FileParserFactory.class);

    private static final FileParserFactory instance = new FileParserFactory();

    /**
     * File parser factory returns the current instance
     *
     * @return the current instance of the file parser factory
     */
    public static FileParserFactory instance() {
        return instance;
    }

    /**
     * Returns a list of available schema parsers. Parsers are guaranteed to be annotated with @SchemaParser and implement FileSchemaParser interface
     */
    @SuppressWarnings("unchecked")
    private List<Class<SchemaParser>> listSchemaParsersClasses() {
        List<Class<SchemaParser>> supportedParsers = new ArrayList<>();

        Set<Class<?>> parsers = ReflectionPolicyAnnotationDiscoverer.getTypesAnnotatedWith(SchemaParser.class);
        for (Class c : parsers) {
            if (FileSchemaParser.class.isAssignableFrom(c)) {
                supportedParsers.add(c);
            } else {
                log.warn("[" + c + "] is annotated with @SchemaParser and does not implement the FileSchemaParser interface so will be ignored.");
            }
        }
        return supportedParsers;
    }

    /**
     * Returns a list of available schema parsers. Parsers are guaranteed to be annotated with @SchemaParser and implement FileSchemaParser interface
     *
     * @return a list of file schema parsers
     */
    public List<FileSchemaParser> listSchemaParsers() {

        List<FileSchemaParser> supportedParsers = new ArrayList<>();
        List<Class<SchemaParser>> supportedParsersClazzes = listSchemaParsersClasses();
        for (Class<SchemaParser> clazz : supportedParsersClazzes) {
            try {
                FileSchemaParser newInstance = (FileSchemaParser) clazz.newInstance();
                newInstance = (FileSchemaParser) SpringApplicationContext.autowire(newInstance);

                supportedParsers.add(newInstance);

            } catch (InstantiationException | IllegalAccessException e) {
                log.warn("Failed to instantiate registered schema parser [?]. Missing default constructor?", clazz.getAnnotation(SchemaParser.class).name(), e);
            }
        }
        return supportedParsers;
    }

}
