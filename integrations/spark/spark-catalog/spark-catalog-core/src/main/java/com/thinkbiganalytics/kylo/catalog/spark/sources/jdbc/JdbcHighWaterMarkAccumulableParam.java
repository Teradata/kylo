package com.thinkbiganalytics.kylo.catalog.spark.sources.jdbc;

/*-
 * #%L
 * Kylo Catalog Core
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

import org.apache.spark.AccumulableParam;

import java.io.Serializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Defines how to accumulate instances of {@link JdbcHighWaterMark} in a Spark job.
 */
public class JdbcHighWaterMarkAccumulableParam implements AccumulableParam<JdbcHighWaterMark, Long>, Serializable {

    private static final long serialVersionUID = -3763101429608512945L;

    @Nonnull
    @Override
    public JdbcHighWaterMark addAccumulator(@Nonnull final JdbcHighWaterMark accumulator, @Nullable final Long value) {
        accumulator.accumulate(value);
        return accumulator;
    }

    @Nonnull
    @Override
    public JdbcHighWaterMark addInPlace(@Nonnull final JdbcHighWaterMark left, @Nonnull final JdbcHighWaterMark right) {
        left.accumulate(right.getValue());
        return left;
    }

    @Nonnull
    @Override
    public JdbcHighWaterMark zero(@Nonnull final JdbcHighWaterMark template) {
        return new JdbcHighWaterMark();
    }
}
