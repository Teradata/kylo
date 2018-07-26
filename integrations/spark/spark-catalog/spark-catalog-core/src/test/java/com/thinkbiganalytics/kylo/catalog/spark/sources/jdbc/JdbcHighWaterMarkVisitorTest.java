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

import org.apache.spark.Accumulable;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.Serializable;

import scala.runtime.AbstractFunction1;

public class JdbcHighWaterMarkVisitorTest {

    /**
     * Verify visiting a value.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void apply() {
        final Accumulable<JdbcHighWaterMark, Long> accumulable = Mockito.mock(Accumulable.class);
        final JdbcHighWaterMarkVisitor<Long> visitor = new JdbcHighWaterMarkVisitor<>(accumulable, new IdentityFunction<Long>());

        visitor.apply(1L);
        Mockito.verify(accumulable).$plus$eq(1L);
    }

    /**
     * Identity function.
     */
    private static class IdentityFunction<T> extends AbstractFunction1<T, T> implements Serializable {

        private static final long serialVersionUID = -1710620880744749656L;

        @Override
        public T apply(final T value) {
            return value;
        }
    }
}
