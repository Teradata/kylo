package com.thinkbiganalytics.spark.dataquality.rule;

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

import org.junit.Test;

import com.thinkbiganalytics.spark.dataquality.util.DataQualityConstants;
import com.thinkbiganalytics.spark.dataquality.util.FlowAttributes;

public class InvalidRowTotalCountRuleTest extends DataQualityRuleTest {

    InvalidRowTotalCountRuleImpl InvalidRowCountRuleImpl = new InvalidRowTotalCountRuleImpl();

    @Test
    public void testMissingAttributes() {
        assertTrue("Attributes not checked while loading rule",
                   !InvalidRowCountRuleImpl.loadAttributes(new FlowAttributes()));
    }

    @Test
    public void testInvalidRowCount() {

        assertTrue("Invalid Row Count larger than threshold",
                   runRule(InvalidRowCountRuleImpl));
    }

    @Test
    public void testInvalidRows() {
        flowAttributes.addAttribute(DataQualityConstants.DQ_INVALID_ROW_COUNT_ATTRIBUTE, "10");
        flowAttributes.addAttribute(DataQualityConstants.DQ_INVALID_ALLOWED_COUNT_ATTRIBUTE, "0");
        
        assertTrue("Invalid Rows do not exist",
                   !runRule(InvalidRowCountRuleImpl));
    }
    
    @Test
    public void testInvalidRowsThreshold() {
        flowAttributes.addAttribute(DataQualityConstants.DQ_INVALID_ROW_COUNT_ATTRIBUTE, "10");
        flowAttributes.addAttribute(DataQualityConstants.DQ_INVALID_ALLOWED_COUNT_ATTRIBUTE, "10");
        
        assertTrue("Invalid Row count larger than accepted threshold",
                   runRule(InvalidRowCountRuleImpl));
    }
}
