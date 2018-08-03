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

import org.apache.spark.Accumulable;

import java.io.Serializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import scala.runtime.AbstractFunction1;

/**
 * Indicates the function has been called by triggering a flag.
 */
public class FlaggingVisitor extends AbstractFunction1<Object, Object> implements Serializable {

    private static final long serialVersionUID = 4020472617231592044L;

    /**
     * Flag for indicating method call
     */
    @Nonnull
    private final Accumulable<Boolean, Boolean> accumulable;

    /**
     * Constructs a {@code FlaggingVisitor} using the specified accumulator.
     */
    public FlaggingVisitor(@Nonnull final Accumulable<Boolean, Boolean> accumulable) {
        this.accumulable = accumulable;
    }

    @Nullable
    @Override
    public Object apply(@Nullable final Object value) {
        accumulable.$plus$eq(Boolean.TRUE);
        return value;
    }
}
