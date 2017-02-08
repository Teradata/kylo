package com.thinkbiganalytics.policy;

/*-
 * #%L
 * thinkbig-field-policy-core
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

import com.thinkbiganalytics.policy.rest.model.FieldStandardizationRule;
import com.thinkbiganalytics.policy.rest.model.FieldValidationRule;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 */
public class AvailablePoliciesTest {

    private static final Logger log = LoggerFactory.getLogger(AvailablePoliciesTest.class);

    @Test
    public void testAvailablePolicies() {

        List<FieldStandardizationRule> standardizationRules = AvailablePolicies.discoverStandardizationRules();
        List<FieldValidationRule> validationRules = AvailablePolicies.discoverValidationRules();

        log.info("Available Standardizers: {}, Validators: {} ", standardizationRules.size(), validationRules.size());

        Assert.assertTrue(standardizationRules.size() > 0);
        Assert.assertTrue(validationRules.size() > 0);

        FieldStandardizationRule rule = standardizationRules.get(0);
        log.info("First Standardizer is {}", rule);
        Assert.assertNotNull(rule.getObjectClassType());

    }


}
