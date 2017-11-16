package com.thinkbiganalytics.spark.shell;

/*-
 * #%L
 * Spark Shell Service API
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

import com.thinkbiganalytics.spark.rest.model.TransformRequest;
import com.thinkbiganalytics.spark.rest.model.TransformResponse;

import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * Communicates with a Spark Shell process.
 */
public interface SparkShellRestClient {

    /**
     * Fetches the status of a query running on the specified Spark Shell process.
     *
     * @param id the query identifier
     * @return the query status if the query exists
     * @throws SparkShellTransformException if the query fails
     */
    @Nonnull
    Optional<TransformResponse> getQueryResult(@Nonnull SparkShellProcess process, @Nonnull String id);

    /**
     * Fetches the status of a transformation running on the specified Spark Shell process.
     *
     * @param table the destination table name
     * @return the transformation status if the table exists
     * @throws SparkShellTransformException if the transformation fails
     */
    @Nonnull
    Optional<TransformResponse> getTransformResult(@Nonnull SparkShellProcess process, @Nonnull String table);

    /**
     * Executes a SQL query on the specified Spark Shell process.
     *
     * @param request the query request
     * @return the query status
     * @throws SparkShellTransformException if the query fails
     */
    @Nonnull
    TransformResponse query(@Nonnull SparkShellProcess process, @Nonnull TransformRequest request);

    /**
     * Executes a Scala script on the specified Spark Shell process.
     *
     * @param request the transformation request
     * @return the transformation status
     * @throws SparkShellTransformException if the transformation fails
     */
    @Nonnull
    TransformResponse transform(@Nonnull SparkShellProcess process, @Nonnull TransformRequest request);
}
