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
import com.thinkbiganalytics.spark.model.SaveResult;

import org.apache.spark.SparkContext;

import javax.annotation.Nonnull;

/**
 * Monitors a Spark job that produces a {@link SaveResult}.
 */
public class SaveJob extends Job<SaveResult> {

    /**
     * Constructs a {@code SaveJob} with the specified job configuration.
     */
    public SaveJob(@Nonnull final String groupId, @Nonnull final Supplier<SaveResult> supplier, @Nonnull final SparkContext sparkContext) {
        super(groupId, supplier, sparkContext);
    }
}
