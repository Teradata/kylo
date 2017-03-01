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

import com.thinkbiganalytics.spark.dataquality.util.FlowAttributes;


public class InvalidRowPercentRuleTest extends DataQualityRuleTest {

    InvalidRowPercentRuleImpl invalidRowPercentRuleImpl = new InvalidRowPercentRuleImpl();

    @Test
    public void testMissingAttributes() {
        assertTrue("Attributes not checked while loading rule",
                   !invalidRowPercentRuleImpl.loadAttributes(new FlowAttributes()));
    }

    @Test
    public void testInvalidRowCount() {

        assertTrue("Invalid Row Count larger than threshold",
                   runRule(invalidRowPercentRuleImpl));
    }

}
