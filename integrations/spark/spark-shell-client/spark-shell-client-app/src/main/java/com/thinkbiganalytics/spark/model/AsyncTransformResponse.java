package com.thinkbiganalytics.spark.model;

/*-
 * #%L
 * kylo-spark-shell-client-app
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

import com.thinkbiganalytics.discovery.schema.QueryResult;
import com.thinkbiganalytics.spark.dataprofiler.output.OutputRow;
import com.thinkbiganalytics.spark.rest.model.TransformResponse;

import java.util.List;
import java.util.concurrent.Callable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Asynchronously calculates the result of a Spark transformation and returns the result.
 */
public class AsyncTransformResponse extends TransformResponse implements Callable<TransformResponse> {

    /**
     * Calculated result.
     */
    @Nullable
    private TransformResponse delegate;

    /**
     * Method for calculating the result.
     */
    @Nonnull
    private final Callable<TransformResponse> result;

    /**
     * Construct an {@code AsyncTransformResponse}.
     */
    public AsyncTransformResponse(@Nonnull final Callable<TransformResponse> result) {
        this.result = result;
    }

    @Override
    public TransformResponse call() throws Exception {
        delegate = result.call();
        return this;
    }

    @Override
    public String getMessage() {
        return (delegate != null) ? delegate.getMessage() : super.getMessage();
    }

    @Override
    public List<OutputRow> getProfile() {
        return (delegate != null) ? delegate.getProfile() : super.getProfile();
    }

    @Override
    public Double getProgress() {
        return (delegate != null) ? delegate.getProgress() : super.getProgress();
    }

    @Override
    public QueryResult getResults() {
        return (delegate != null) ? delegate.getResults() : super.getResults();
    }

    @Override
    public Status getStatus() {
        return (delegate != null) ? delegate.getStatus() : super.getStatus();
    }

    @Override
    public String getTable() {
        return (delegate != null) ? delegate.getTable() : super.getTable();
    }
}
