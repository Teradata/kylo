package com.thinkbiganalytics.spark.dataquality.util;

/*-
 * #%L
 * kylo-spark-job-dataquality-app
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

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.thinkbiganalytics.spark.dataquality.util.FlowAttributes;

public class FlowAttributesTest {

    private static final String SOURCE_RECORD_COUNT_NAME = "source.record.count";
    private final String RESOURCE_LOCATION = "src/test/resources/";
    private final String ATTRIBUTE_JSON_PATH = RESOURCE_LOCATION + "sample.json";

    FlowAttributes flowAttributes = new FlowAttributes();

    @Before
    public void setUp() {
        flowAttributes.setAttributes(ATTRIBUTE_JSON_PATH);
    }

    @Test
    public void testLoadAttributes() {

        String expectedVal = "110";
        int expectedAttrCount = 38;

        try {
            String actualVal = flowAttributes.getAttributeValue(SOURCE_RECORD_COUNT_NAME);

            assertTrue("Attributes for source.record.count do not match"
                       + "Expected = "
                       + expectedVal
                       + " Actual = "
                       + actualVal,
                       expectedVal.equals(actualVal));

            assertTrue("Attribute counts do not match. "
                       + "Expected = "
                       + expectedAttrCount
                       + " Actual = "
                       + flowAttributes.count(),
                       expectedAttrCount == flowAttributes.count());

        } catch (MissingAttributeException e) {
            e.printStackTrace();
            assert (false);
        }
    }

    @Test
    public void testMissingAttributes() {

        String expectedMsg = "Attribute: source.record.count does not exist";

        try {
            FlowAttributes testFlowAttr = new FlowAttributes();
            testFlowAttr.getAttributeValue(SOURCE_RECORD_COUNT_NAME);

            assert (false);

        } catch (MissingAttributeException e) {
            assertTrue("Missing Atrribute message not correct. ",
                       expectedMsg.equals(e.getMessage()));
        }
    }

    @Test
    public void testContainAttributes() {
        assertTrue("Missing Atrribute. ",
                   flowAttributes.containsAttribute(SOURCE_RECORD_COUNT_NAME));
    }
}
