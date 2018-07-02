package com.thinkbiganalytics.spark.service;

/*-
 * #%L
 * kylo-spark-shell-client-app
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

import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorConverters;
import org.apache.spark.sql.types.DataType;

import javax.annotation.Nonnull;

public interface DataSetConverterService {

    /**
     * Gets a converter for transforming objects from the specified Spark SQL type to a Hive type.
     *
     * @param dataType the Spark SQL type
     * @return the converter to a Hive object
     */
    @Nonnull
    ObjectInspectorConverters.Converter getHiveObjectConverter(@Nonnull DataType dataType);

    /**
     * Converts the specified Spark SQL type to a Hive ObjectInspector.
     *
     * @param dataType the Spark SQL type
     * @return the Hive ObjectInspector
     */
    @Nonnull
    ObjectInspector getHiveObjectInspector(@Nonnull DataType dataType);
}
