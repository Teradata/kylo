package com.thinkbiganalytics.discovery;
/*
 * Copyright (c) 2016. Teradata Inc.
 */

import com.thinkbiganalytics.discovery.parser.FileSchemaParser;
import com.thinkbiganalytics.discovery.parser.SchemaParser;
import com.thinkbiganalytics.spring.SpringApplicationContext;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Provides available parsers
 */
public class FileParserFactory {

    private static final Logger log = LoggerFactory.getLogger(FileParserFactory.class);

    private static FileParserFactory instance = new FileParserFactory();

    public static FileParserFactory instance() {
        return instance;
    }

    /**
     * Returns a list of available schema parsers. Parsers are guaranteed to be annotated with @SchemaParser and implement FileSchemaParser interface
     */
    private List<Class<SchemaParser>> listSchemaParsersClasses() {

        List<Class<SchemaParser>> supportedParsers = new ArrayList<>();

        Set<Class<?>> parsers = new Reflections("").getTypesAnnotatedWith(SchemaParser.class);
        for (Class c : parsers) {
            if (FileSchemaParser.class.isAssignableFrom(c)) {
                supportedParsers.add(c);
            }
        }
        return supportedParsers;
    }

    /**
     * Returns a list of available schema parsers. Parsers are guaranteed to be annotated with @SchemaParser and implement FileSchemaParser interface
     */
    public List<FileSchemaParser> listSchemaParsers() {

        List<FileSchemaParser> supportedParsers = new ArrayList<>();
        List<Class<SchemaParser>> supportedParsersClazzes = listSchemaParsersClasses();
        for (Class<SchemaParser> clazz : supportedParsersClazzes) {
            try {
               FileSchemaParser newInstance = (FileSchemaParser)clazz.newInstance();
               newInstance = (FileSchemaParser)SpringApplicationContext.autowire(newInstance);

                supportedParsers.add(newInstance);

            } catch (InstantiationException | IllegalAccessException e) {
                log.warn("Failed to instantiate registered schema parser [?]. Missing default constructor?", clazz.getAnnotation(SchemaParser.class).name(), e);
            }
        }
        return supportedParsers;
    }

}
