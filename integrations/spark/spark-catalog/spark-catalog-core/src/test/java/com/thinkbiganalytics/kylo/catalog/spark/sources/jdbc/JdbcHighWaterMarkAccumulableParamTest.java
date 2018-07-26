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

import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogClient;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class JdbcHighWaterMarkAccumulableParamTest {

    /**
     * Verify adding a value to a high water mark.
     */
    @Test
    public void addAccumulator() {
        final JdbcHighWaterMark highWaterMark = new JdbcHighWaterMark();
        final JdbcHighWaterMarkAccumulableParam param = new JdbcHighWaterMarkAccumulableParam();
        param.addAccumulator(highWaterMark, 1L);
        Assert.assertEquals(new Long(1L), highWaterMark.getValue());

        param.addAccumulator(highWaterMark, 2L);
        Assert.assertEquals(new Long(2L), highWaterMark.getValue());
    }

    /**
     * Verify adding high water mark values.
     */
    @Test
    public void addInPlace() {
        final JdbcHighWaterMark left = new JdbcHighWaterMark();
        left.accumulate(1L);

        final JdbcHighWaterMark right = new JdbcHighWaterMark();
        right.accumulate(2L);

        final JdbcHighWaterMark result = new JdbcHighWaterMarkAccumulableParam().addInPlace(left, right);
        Assert.assertEquals(new Long(2L), result.getValue());
        Assert.assertEquals(left, result);
    }

    /**
     * Verify zero value.
     */
    @Test
    public void zero() {
        final JdbcHighWaterMark template = new JdbcHighWaterMark("mockWaterMark", Mockito.mock(KyloCatalogClient.class));
        final JdbcHighWaterMark zero = new JdbcHighWaterMarkAccumulableParam().zero(template);
        Assert.assertNull("Expected high water mark value to be null", zero.getValue());
    }
}
