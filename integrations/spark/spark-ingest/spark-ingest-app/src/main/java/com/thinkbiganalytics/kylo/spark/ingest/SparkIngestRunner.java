package com.thinkbiganalytics.kylo.spark.ingest;

/*-
 * #%L
 * Kylo Spark Ingest App
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics
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

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogClient;
import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogReader;
import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogWriter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * Reads command-line options and transfers any source to any sink.
 */
@Component
@SuppressWarnings({"unused", "WeakerAccess"})
public class SparkIngestRunner implements CommandLineRunner {

    @Parameter(names = {"--source-files"}, splitter = SemicolonParameterSplitter.class)
    public List<String> sourceFiles;

    @Parameter(names = {"--source-format"}, required = true)
    public String sourceFormat;

    @Parameter(names = {"--source-jars"}, splitter = SemicolonParameterSplitter.class)
    public List<String> sourceJars;

    @Parameter(names = {"--source-options"}, converter = OptionsConverter.class)
    public Map<String, String> sourceOptions;

    @Parameter(names = {"--source-path"})
    public String sourcePath;

    @Parameter(names = {"--target-files"}, splitter = SemicolonParameterSplitter.class)
    public List<String> targetFiles;

    @Parameter(names = {"--target-format"}, required = true)
    public String targetFormat;

    @Parameter(names = {"--target-jars"}, splitter = SemicolonParameterSplitter.class)
    public List<String> targetJars;

    @Parameter(names = {"--target-options"}, converter = OptionsConverter.class)
    public Map<String, String> targetOptions;

    @Parameter(names = {"--target-path"})
    public String targetPath;

    /**
     * Kylo catalog client
     */
    private final KyloCatalogClient<Object> client;

    /**
     * Constructs a {@code SparkIngestRunner}.
     */
    @Autowired
    @SuppressWarnings("unchecked")
    public SparkIngestRunner(KyloCatalogClient kyloCatalogClient) {
        client = kyloCatalogClient;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run(@Nonnull final String... args) {
        new JCommander(this).parse(args);

        // Read source
        final KyloCatalogReader reader = client.read().format(sourceFormat).addFiles(sourceFiles).addJars(sourceJars).options(sourceOptions);
        final Object source = (sourcePath != null) ? reader.load(sourcePath) : reader.load();

        // Write to target
        final KyloCatalogWriter writer = client.write(source).format(targetFormat).addFiles(targetFiles).addJars(targetJars).options(targetOptions);
        if (targetPath != null) {
            writer.save(targetPath);
        } else {
            writer.save();
        }
    }
}
