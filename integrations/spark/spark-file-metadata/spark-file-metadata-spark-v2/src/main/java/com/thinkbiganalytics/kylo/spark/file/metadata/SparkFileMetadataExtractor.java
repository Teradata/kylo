package com.thinkbiganalytics.kylo.spark.file.metadata;

/*-
 * #%L
 * spark-file-metadata-spark-v2
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

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoder;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.SQLContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.ArrayList;
import java.util.List;

public class SparkFileMetadataExtractor extends AbstractSparkMetadataExtractor {

    public SparkFileMetadataExtractor(SQLContext sqlContext) {
        super(sqlContext);
    }

    @Override
    public List<FileMetadata> parse(String[] filePaths) {
        List<Dataset> dataFrameList = new ArrayList<>();
        for (String path : filePaths) {
            Dataset df = (Dataset) sqlContext.read().format("com.thinkbiganalytics.spark.file.metadata").load(path);
            dataFrameList.add(df);
        }
        Dataset unionDf = unionAll(dataFrameList);

        Encoder<FileMetadata> encoder = Encoders.bean(FileMetadata.class);
        Dataset fileData = unionDf.as(encoder);
        return fileData.collectAsList();
    }

    public static void main(String[] args) {
        try (final ConfigurableApplicationContext ctx = new AnnotationConfigApplicationContext("com.thinkbiganalytics.spark", "com.thinkbiganalytics.kylo.spark")) {
            final SparkFileMetadataExtractor extractor = new SparkFileMetadataExtractor(
                ctx.getBean(SQLContext.class));
            //ctx.getBean(SparkContextService.class)
            extractor.run(args);
        }
    }

    private Dataset unionAll(List<Dataset> dataFrameList) {
        Dataset unionDf = null;
        for (Dataset df : dataFrameList) {
            if (unionDf == null) {
                unionDf = df;
            } else {
                unionDf = unionDf.unionAll(df);
            }
        }
        return unionDf;
    }
}
