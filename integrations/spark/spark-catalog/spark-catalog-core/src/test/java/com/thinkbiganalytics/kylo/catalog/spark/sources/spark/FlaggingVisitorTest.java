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

import org.apache.spark.Accumulator;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class FlaggingVisitorTest {

    /**
     * Verify setting the flag.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void test() {
        final Accumulator<Boolean> accumulator = Mockito.mock(Accumulator.class);
        final FlaggingVisitor visitor = new FlaggingVisitor(accumulator);
        Assert.assertEquals(Void.class, visitor.apply(Void.class));
        Mockito.verify(accumulator).$plus$eq(true);
    }
}
