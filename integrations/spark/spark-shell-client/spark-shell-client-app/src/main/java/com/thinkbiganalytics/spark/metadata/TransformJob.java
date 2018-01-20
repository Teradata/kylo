package com.thinkbiganalytics.spark.metadata;

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

import com.google.common.base.Supplier;
import com.thinkbiganalytics.spark.rest.model.TransformResponse;

import org.apache.spark.SparkContext;

import javax.annotation.Nonnull;

/**
 * A Spark transformation job.
 *
 * <p>This class is thread-safe but {@link #run()} should only be invoked once.</p>
 */
public class TransformJob extends Job<TransformResponse> {

    /**
     * Constructs a {@code TransformJob}.
     */
    public TransformJob(@Nonnull final String groupId, @Nonnull final Supplier<TransformResponse> supplier, @Nonnull final SparkContext sparkContext) {
        super(groupId, supplier, sparkContext);
    }
}
