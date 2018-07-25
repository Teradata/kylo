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

import org.joda.time.format.ISODateTimeFormat;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;

import scala.runtime.AbstractFunction1;

public class JdbcHighWaterMarkTest {

    /**
     * Verify accumulating values.
     */
    @Test
    public void accumulate() {
        // Test initial null value
        final JdbcHighWaterMark highWaterMark = new JdbcHighWaterMark();
        Assert.assertNull("Expected initial high water mark value to be null", highWaterMark.getValue());

        highWaterMark.accumulate(null);
        Assert.assertNull("Expected initial high water mark value to be null", highWaterMark.getValue());

        // Test first non-null value
        highWaterMark.accumulate(2L);
        Assert.assertEquals(new Long(2L), highWaterMark.getValue());

        // Test lower value
        highWaterMark.accumulate(1L);
        Assert.assertEquals(new Long(2L), highWaterMark.getValue());

        // Test null value
        highWaterMark.accumulate(null);
        Assert.assertEquals(new Long(2L), highWaterMark.getValue());

        // Test higher value
        highWaterMark.accumulate(3L);
        Assert.assertEquals(new Long(3L), highWaterMark.getValue());
    }

    /**
     * Verify interactions with a {@code KyloCatalogClient}.
     */
    @Test
    public void testClient() {
        // Test setting high water mark
        final KyloCatalogClient client = Mockito.mock(KyloCatalogClient.class);
        final JdbcHighWaterMark highWaterMark = new JdbcHighWaterMark("mockWaterMark", client);

        highWaterMark.accumulate(6L);
        Mockito.verify(client).setHighWaterMarks(Collections.singletonMap("mockWaterMark", "6"));

        // Test with formatter
        highWaterMark.setFormatter(new AbstractFunction1<Long, String>() {
            @Override
            public String apply(final Long value) {
                return ISODateTimeFormat.dateTimeNoMillis().withZoneUTC().print(value);
            }
        });
        highWaterMark.accumulate(1524960000000L);
        Mockito.verify(client).setHighWaterMarks(Collections.singletonMap("mockWaterMark", "2018-04-29T00:00:00Z"));
    }
}
