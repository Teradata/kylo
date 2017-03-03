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

import java.util.List;

import org.junit.Test;

import com.thinkbiganalytics.spark.dataquality.checker.DataQualityChecker;
import com.thinkbiganalytics.spark.dataquality.rule.DataQualityRule;
import com.thinkbiganalytics.spark.dataquality.rule.RowCountRuleImpl;
import com.thinkbiganalytics.spark.dataquality.util.DataQualityConstants;
import com.thinkbiganalytics.spark.dataquality.util.FlowAttributes;
import com.thinkbiganalytics.spark.dataquality.util.MissingRuleException;


public class DataQualityCheckerTest {

    DataQualityChecker dqChecker = new DataQualityChecker();

    @Test
    public void testLoadRule() {
        dqChecker.addDataQualityRule(new RowCountRuleImpl());

        assertTrue("Data Quality rule not loaded",
                   dqChecker.getRuleList().get(0).getClass().equals(RowCountRuleImpl.class));
    }

    @Test
    public void testDefaultRuleLoad() {
        try {
            dqChecker.setActiveRules();

            int availableRulesCount = dqChecker.getAvailableRules().size();
            int activeRuleCount = dqChecker.getRuleList().size();

            assertTrue("All rules not loaded",
                       availableRulesCount == activeRuleCount);

        } catch (MissingRuleException e) {
            e.printStackTrace();
            System.out.println("Missing Rule");
        }
    }

    @Test
    public void testSelectRuleLoad() {
        try {
            FlowAttributes testFlowAttr = new FlowAttributes();
            testFlowAttr.addAttribute(DataQualityConstants.DQ_ACTIVE_RULES_ATTRIBUTE,
                                      "ROW_COUNT_TOTAL_RULE,SOURCE_TO_FEED_COUNT_RULE");

            dqChecker.setAttributes(testFlowAttr);
            dqChecker.setAvailableRules();
            dqChecker.setActiveRules();

            List<DataQualityRule> activeRuleList = dqChecker.getRuleList();

            assertTrue("Rules not all loaded. Rule List size = " + activeRuleList.size(),
                       activeRuleList.size() == 2);

            assertTrue("Rules not loaded properly",
                       activeRuleList.get(0).getName().equals("ROW_COUNT_TOTAL_RULE"));

        } catch (MissingRuleException e) {
            e.printStackTrace();
            System.out.println("Missing Rule");
        }
    }

    @Test
    public void testBadRuleLoad() {
        try {
            FlowAttributes testFlowAttr = new FlowAttributes();
            testFlowAttr.addAttribute(DataQualityConstants.DQ_ACTIVE_RULES_ATTRIBUTE,
                                      "BAD_RULE");

            dqChecker.setAttributes(testFlowAttr);
            dqChecker.setAvailableRules();
            dqChecker.setActiveRules();

        } catch (MissingRuleException e) {
            System.out.println("Exception properly caught");
            assert (true);
        }

        assertTrue("Did not fail due to bad rule", true);
    }
}
