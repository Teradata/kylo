package com.thinkbiganalytics.spark.shell;

import com.thinkbiganalytics.spark.rest.model.TransformRequest;
import com.thinkbiganalytics.spark.rest.model.TransformResponse;

import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * Communicates with a Spark Shell process.
 */
public interface SparkShellRestClient {

    /**
     * Fetches the status of a transformation running on the specified Spark Shell process.
     *
     * @param table the destination table name
     * @return the transformation status if the table exists
     */
    @Nonnull
    Optional<TransformResponse> getTable(@Nonnull SparkShellProcess process, @Nonnull String table);

    /**
     * Executes a Scala script on the specified Spark Shell process.
     *
     * @param request the transformation request
     * @return the transformation status
     */
    @Nonnull
    TransformResponse transform(@Nonnull SparkShellProcess process, @Nonnull TransformRequest request);
}
