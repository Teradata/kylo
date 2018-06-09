package com.thinkbiganalytics.kylo.spark.file.metadata;

/*-
 * #%L
 * spark-file-metadata-core
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

import com.thinkbiganalytics.kylo.metadata.file.FileMetadata;
import com.thinkbiganalytics.kylo.metadata.file.FileMetadataExtractor;

import org.apache.spark.sql.SQLContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

public abstract class AbstractSparkMetadataExtractor implements FileMetadataExtractor {

    protected SQLContext sqlContext;

    private SparkMetadataExtractorArguments arguments;


    private ApplicationContext applicationContext;

    public AbstractSparkMetadataExtractor(SQLContext sqlContext) {
        this.sqlContext = sqlContext;
    }

    public List<FileMetadata> parse() {
        return parse(this.arguments.getPaths());
    }

    private ApplicationContext getApplicationContext() {
        if (applicationContext == null) {
            this.applicationContext = new AnnotationConfigApplicationContext("com.thinkbiganalytics.spark", "com.thinkbiganalytics.kylo.spark");
        }
        return applicationContext;
    }

    private SQLContext getSqlContext() {
        if (sqlContext == null) {
            this.sqlContext = getApplicationContext().getBean(SQLContext.class);
        }
        return this.sqlContext;
    }


    public abstract List<FileMetadata> parse(String[] filePaths);


    public void run(String[] args) {
        try {
            SparkMetadataExtractorArguments arguments = parseCommandLineArgs(args);
            setArguments(arguments);
            List<FileMetadata> list = parse(arguments.getPaths());
            int i = 0;

        } catch (Exception e) {

        }
    }


    private SparkMetadataExtractorArguments parseCommandLineArgs(final String[] args) {
        SparkMetadataExtractorArguments arguments = new SparkMetadataExtractorArguments(args);
        return arguments;
    }

    public void setArguments(SparkMetadataExtractorArguments arguments) {
        this.arguments = arguments;
    }


}
