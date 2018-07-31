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

import org.junit.Assert;
import org.junit.Test;

public class BooleanOrAccumulatorParamTest {

    @Test
    public void addAccumulator() {
        final BooleanOrAccumulatorParam param = new BooleanOrAccumulatorParam();
        Assert.assertFalse(param.addAccumulator(false, false));
        Assert.assertTrue(param.addAccumulator(false, true));
        Assert.assertTrue(param.addAccumulator(true, false));
        Assert.assertTrue(param.addAccumulator(true, true));
    }

    @Test
    public void addInPlace() {
        final BooleanOrAccumulatorParam param = new BooleanOrAccumulatorParam();
        Assert.assertFalse(param.addInPlace(false, false));
        Assert.assertTrue(param.addInPlace(false, true));
        Assert.assertTrue(param.addInPlace(true, false));
        Assert.assertTrue(param.addInPlace(true, true));
    }

    @Test
    public void zero() {
        final BooleanOrAccumulatorParam param = new BooleanOrAccumulatorParam();
        Assert.assertFalse(param.zero(false));
        Assert.assertTrue(param.zero(true));
    }
}
