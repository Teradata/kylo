package com.thinkbiganalytics.kylo.nifi.teradata.tdch.core.processor.base;

/*-
 * #%L
 * kylo-nifi-teradata-tdch-core
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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

/**
 * Tests for class {@link TdchOperationType}
 */
public class TdchOperationTypeTest {

    @Test
    public void testTdchExportOperationType() {
        TdchOperationType tdchOperationType = TdchOperationType.TDCH_EXPORT;
        Assert.assertEquals("TDCH_EXPORT", tdchOperationType.toString());
        Assert.assertThat(TdchOperationType.valueOf("TDCH_EXPORT"), is(notNullValue()));
    }

    @Test
    public void testTdchImportOperationType() {
        TdchOperationType tdchOperationType = TdchOperationType.TDCH_IMPORT;
        Assert.assertEquals("TDCH_IMPORT", tdchOperationType.toString());
        Assert.assertThat(TdchOperationType.valueOf("TDCH_IMPORT"), is(notNullValue()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTdchUnknownOperationType() {
        TdchOperationType tdchOperationType = TdchOperationType.valueOf("UNKNOWN_TYPE");
    }
}
