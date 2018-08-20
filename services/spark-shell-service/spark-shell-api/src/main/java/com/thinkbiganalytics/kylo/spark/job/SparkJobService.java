package com.thinkbiganalytics.kylo.spark.job;

/*-
 * #%L
 * Spark Shell Service API
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

import com.thinkbiganalytics.kylo.spark.rest.model.job.SparkJobRequest;

import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * Provides access to Spark jobs.
 */
public interface SparkJobService {

    /**
     * Creates a new Spark job.
     */
    @Nonnull
    SparkJobContext create(@Nonnull SparkJobRequest request);

    /**
     * Finds an existing Spark job with the specified identifier.
     */
    @Nonnull
    Optional<SparkJobContext> findById(@Nonnull String id);
}
