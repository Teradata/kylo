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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SparkMetadataExtractorArguments {

    private static final Logger log = LoggerFactory.getLogger(SparkMetadataExtractorArguments.class);


    private String path;

    public SparkMetadataExtractorArguments(final String[] args) {

        if (log.isInfoEnabled()) {
            log.info("Running MetadataExtractor with the following command line {} args (comma separated): {}", args.length, StringUtils.join(args, ","));
        }

        if (args.length < 1) {
            log.error("Invalid number of command line arguments ({})", args.length);
            throw new IllegalArgumentException("Invalid number of command line arguments (" + args.length + ")");
        }

        this.path = args[0];
    }

    public String getPath() {
        return path;
    }

    public String[] getPaths() {
        return this.path.split(",");
    }

}
