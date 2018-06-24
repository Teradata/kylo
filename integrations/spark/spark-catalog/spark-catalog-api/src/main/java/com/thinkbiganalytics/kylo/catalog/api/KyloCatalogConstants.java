package com.thinkbiganalytics.kylo.catalog.api;

/*-
 * #%L
 * Kylo Catalog API
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

/**
 * Constants used by the Kylo Catalog.
 */
public class KyloCatalogConstants {

    /**
     * Option key for data set format of Spark data set provider.
     */
    public static final String FORMAT_OPTION = "format";

    /**
     * Option key for data set format of Spark data set provider.
     */
    public static final String FORMAT_OPTION2 = "kylo.catalog.format";

    /**
     * Option key prefix for Hadoop configuration options
     */
    public static final String HADOOP_CONF_PREFIX = "spark.hadoop.";

    /**
     * Data source for accessing Hive tables.
     */
    public static final String HIVE_FORMAT = "hive";

    /**
     * Option for reading from or writing to a file path.
     */
    public static final String PATH_OPTION = "path";

    /**
     * Instances of {@code KyloCatalogConstants} should not be constructed.
     */
    private KyloCatalogConstants() {
        throw new UnsupportedOperationException();
    }
}
