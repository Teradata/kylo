package com.thinkbiganalytics.kylo.catalog.spark.sources.spark;

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

import org.apache.spark.AccumulatorParam;

import javax.annotation.Nonnull;

/**
 * Accumulates boolean values by performing the boolean OR operation.
 */
public class BooleanOrAccumulatorParam implements AccumulatorParam<Boolean> {

    private static final long serialVersionUID = -2182232570500108718L;

    @Nonnull
    @Override
    public Boolean addAccumulator(@Nonnull final Boolean t1, @Nonnull final Boolean t2) {
        return t1 || t2;
    }

    @Nonnull
    @Override
    public Boolean addInPlace(@Nonnull final Boolean r1, @Nonnull final Boolean r2) {
        return r1 || r2;
    }

    @Nonnull
    @Override
    public Boolean zero(@Nonnull final Boolean initialValue) {
        return initialValue;
    }
}
