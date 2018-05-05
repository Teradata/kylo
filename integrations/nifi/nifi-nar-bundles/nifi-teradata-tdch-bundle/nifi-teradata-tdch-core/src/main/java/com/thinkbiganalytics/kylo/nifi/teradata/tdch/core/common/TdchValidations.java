package com.thinkbiganalytics.kylo.nifi.teradata.tdch.core.common;

/*-
 * #%L
 * nifi-teradata-tdch-core
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

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.components.ValidationResult;
import org.apache.nifi.components.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Validations used by TDCH processors
 */
public class TdchValidations {

    private static final Logger log = LoggerFactory.getLogger(TdchValidations.class);

    /* Validates that one or more files or directories exist, as specified in a single property (delimited values) */
    public static class MultipleFilesOrDirectoriesExistValidator implements Validator {

        private final String delimiter;

        public MultipleFilesOrDirectoriesExistValidator(final String delimiter) {
            checkNotNull(delimiter);
            this.delimiter = delimiter;
        }

        @Override
        public ValidationResult validate(final String subject, final String value, final ValidationContext context) {
            try {
                final String[] files = value.split(delimiter);

                for (String filename : files) {
                    try {
                        final File file = new File(filename.trim());
                        if (!file.exists()) {
                            final String message = "file/directory " + filename + " does not exist.";
                            return new ValidationResult.Builder()
                                .subject(subject)
                                .input(value)
                                .valid(false)
                                .explanation(message)
                                .build();
                        } else if (!file.isFile() && !file.isDirectory()) {
                            final String message = filename + " is neither a file nor a directory.";
                            return new ValidationResult.Builder()
                                .subject(subject)
                                .input(value)
                                .valid(false)
                                .explanation(message)
                                .build();
                        } else if (!file.canRead()) {
                            final String message = "could not read " + filename;
                            return new ValidationResult.Builder()
                                .subject(subject)
                                .input(value)
                                .valid(false)
                                .explanation(message)
                                .build();
                        }
                    } catch (SecurityException e) {
                        final String message = "unable to access " + filename + " due to " + e.getMessage();
                        return new ValidationResult.Builder()
                            .subject(subject)
                            .input(value)
                            .valid(false)
                            .explanation(message)
                            .build();
                    }
                }
            } catch (Exception e) {
                return new ValidationResult.Builder()
                    .subject(subject)
                    .input(value)
                    .valid(false)
                    .explanation("error evaluating value. Please sure that value is provided as file1<delimiter>file2<delimiter>directory1<delimiter>file3 and so on. "
                                 + "Also, the files/directories should exist and be readable.")
                    .build();
            }

            return new ValidationResult.Builder()
                .subject(subject)
                .input(value)
                .valid(true)
                .build();
        }
    }


    /* Validates that the TDCH jar exists */
    public static class TdchJarExistsValidator implements Validator {

        private final static String JAR_IDENTIFIER = "/teradata-connector-";

        @Override
        public ValidationResult validate(final String subject, final String value, final ValidationContext context) {
            checkNotNull(value);
            if (!StringUtils.contains(value, JAR_IDENTIFIER)) {
                return new ValidationResult.Builder()
                    .subject(subject)
                    .input(value)
                    .valid(false)
                    .explanation("The teradata connector jar does not appear correct. It should be a file name starting with 'teradata-connector-'.")
                    .build();
            }

            return new ValidationResult.Builder()
                .subject(subject)
                .input(value)
                .valid(true)
                .build();
        }
    }

    /* Validates that Hive dependencies required by TDCH are available */
    public static class TdchRequiredHiveDependenciesValidator implements Validator {

        final Set<String> hiveDependencies;

        public TdchRequiredHiveDependenciesValidator(final Set<String> hiveDependencies) {
            this.hiveDependencies = hiveDependencies;
        }

        @Override
        public ValidationResult validate(final String subject, final String pathToSearch, final ValidationContext context) {

            int depth = 1;
            List<String> classpath = new ArrayList<>();

            for (String hiveDependency : hiveDependencies) {
                try (Stream<Path> paths = Files.find(Paths.get(pathToSearch), depth,
                                                     (path, attributes) -> checkMatch(path.toString(), attributes, hiveDependency))) {
                    int classPathSizeBefore = classpath.size();
                    paths.forEach(path -> {
                        log.debug("Hive dependency validation: Checking for: " + hiveDependency + " gave path: " + path.toString());
                        classpath.add(path.toString());
                    });
                    int classPathSizeAfter = classpath.size();
                    if (classPathSizeBefore == classPathSizeAfter) {
                        return new ValidationResult.Builder()
                            .subject(subject)
                            .input(pathToSearch)
                            .valid(false)
                            .explanation("Hive dependency having name " + hiveDependency + " not found. Please check Hive installation's lib directory.")
                            .build();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return new ValidationResult.Builder()
                        .subject(subject)
                        .input(pathToSearch)
                        .valid(false)
                        .explanation("Unable to check for Hive dependencies required by TDCH. Tried looking at: " + pathToSearch)
                        .build();
                }
            }

            return new ValidationResult.Builder()
                .subject(subject)
                .input(pathToSearch)
                .valid(true)
                .build();
        }
    }

    public static boolean checkMatch(String path, BasicFileAttributes attributes, String match) {
        return !attributes.isDirectory() && path.contains(match);
    }


    /*
       Validates that the JDBC connection url does not contain the database information.
       That will be derived from the processor configuration.
      */
    public static class JdbcConnectionUrlDoesNotContainDatabaseSpecValidator implements Validator {

        @Override
        public ValidationResult validate(final String subject, final String jdbcConnectionUrl, final ValidationContext context) {

            if (StringUtils.contains(jdbcConnectionUrl, "/database=")) {
                return new ValidationResult.Builder()
                    .subject(subject)
                    .input(jdbcConnectionUrl)
                    .valid(false)
                    .explanation("The JDBC connection url should not specify the database. This is provided via a processor using this service.")
                    .build();
            }

            return new ValidationResult.Builder()
                .subject(subject)
                .input(jdbcConnectionUrl)
                .valid(true)
                .build();
        }
    }
}