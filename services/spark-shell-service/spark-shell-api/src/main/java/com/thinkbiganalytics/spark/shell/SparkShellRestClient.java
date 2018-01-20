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

import com.thinkbiganalytics.spark.rest.model.SaveRequest;
import com.thinkbiganalytics.spark.rest.model.SaveResponse;
import com.thinkbiganalytics.spark.rest.model.TransformRequest;
import com.thinkbiganalytics.spark.rest.model.TransformResponse;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.ws.rs.core.Response;


/**
 * Communicates with a Spark Shell process.
 */
public interface SparkShellRestClient {

    /**
     * Downloads the results of a save running on the specified Spark Shell process.
     *
     * @param process the Spark Shell process
     * @param queryId the query identifier
     * @param saveId  the save identifier
     * @return the results, if the save exists
     */
    @Nonnull
    Optional<Response> downloadQuery(@Nonnull SparkShellProcess process, @Nonnull String queryId, @Nonnull String saveId);

    /**
     * Downloads the results of a save running on the specified Spark Shell process.
     *
     * @param process     the Spark Shell process
     * @param transformId the transform identifier
     * @param saveId      the save identifier
     * @return the results, if the save exists
     */
    @Nonnull
    Optional<Response> downloadTransform(@Nonnull SparkShellProcess process, @Nonnull String transformId, @Nonnull String saveId);

    /**
     * Gets the Spark data sources supported by the specified Spark Shell process.
     */
    @Nonnull
    List<String> getDataSources(@Nonnull SparkShellProcess process);

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
     * Fetches the status of a save running on the specified Spark Shell process.
     *
     * @param process the Spark Shell process
     * @param queryId the query identifier
     * @param saveId  the save identifier
     * @return the save status, if the query exists
     * @throws SparkShellSaveException if the save fails
     */
    @Nonnull
    Optional<SaveResponse> getQuerySave(@Nonnull SparkShellProcess process, @Nonnull String queryId, @Nonnull String saveId);

    /**
     * Fetches the status of a save running on the specified Spark Shell process.
     *
     * @param process     the Spark Shell process
     * @param transformId the transform identifier
     * @param saveId      the save identifier
     * @return the save status, if the transform exists
     * @throws SparkShellSaveException if the save fails
     */
    @Nonnull
    Optional<SaveResponse> getTransformSave(@Nonnull SparkShellProcess process, @Nonnull String transformId, @Nonnull String saveId);

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
     * Saves the results of a SQL query using the specified Spark Shell process.
     *
     * @param process the Spark Shell process
     * @param id      the query identifier
     * @param request the save request
     * @return the save status
     * @throws IllegalArgumentException if the {@code id} does not exist
     * @throws SparkShellSaveException  if the save fails
     */
    @Nonnull
    SaveResponse saveQuery(@Nonnull SparkShellProcess process, @Nonnull String id, @Nonnull SaveRequest request);

    /**
     * Saves the results of a Scala script using the specified Spark Shell process.
     *
     * @param process the Spark Shell process
     * @param id      the destination table name
     * @param request the save request
     * @return the save status
     * @throws IllegalArgumentException if the {@code id} does not exist
     * @throws SparkShellSaveException  if the save fails
     */
    @Nonnull
    SaveResponse saveTransform(@Nonnull SparkShellProcess process, @Nonnull String id, @Nonnull SaveRequest request);

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
