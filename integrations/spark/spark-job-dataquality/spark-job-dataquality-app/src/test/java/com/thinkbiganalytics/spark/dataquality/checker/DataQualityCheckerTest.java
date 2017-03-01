package com.thinkbiganalytics.spark.dataquality.checker;

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

import com.thinkbiganalytics.spark.dataquality.checker.DataQualityChecker;
import com.thinkbiganalytics.spark.dataquality.rule.RowCountRuleImpl;


public class DataQualityCheckerTest {

    @Test
    public void testLoadRule() {
        DataQualityChecker dqChecker = new DataQualityChecker();

        dqChecker.addDataQualityRule(new RowCountRuleImpl());

        assertTrue("Data Quality rule not loaded",
                   dqChecker.getRuleList().get(0).getClass().equals(RowCountRuleImpl.class));
    }

}
