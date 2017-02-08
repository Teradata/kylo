package com.thinkbiganalytics.validation;

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

import com.thinkbiganalytics.policy.validation.CreditCardValidator;
import com.thinkbiganalytics.policy.validation.IPAddressValidator;
import com.thinkbiganalytics.policy.validation.TimestampValidator;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class ValidatorTests {

    @Test
    public void timestampTests() {

        TimestampValidator validator = new TimestampValidator(false);
        Assert.assertTrue(validator.validate("2004-12-01 12:00:00"));
        Assert.assertTrue(validator.validate("2004-12-01T12:00:00"));
        Assert.assertTrue(validator.validate("2004-12-01T12:00:00.124Z"));
        Assert.assertTrue(validator.validate("2009-06-18T18:50:57-06:00"));
        Assert.assertTrue(validator.validate("2009-06-18T18:30:45Z"));
        Assert.assertTrue(validator.validate("2009-06-18T18:39Z"));
        Assert.assertTrue(validator.validate("2001-01-12"));
        Assert.assertTrue(validator.validate("1984-10-16"));

        Assert.assertFalse(validator.validate("2004-1-1 12:00:00"));
        Assert.assertFalse(validator.validate("2004-12-0112:00:00"));
        Assert.assertFalse(validator.validate("20041201120000"));

        // Test NULL value
        TimestampValidator nullValidator = new TimestampValidator(true);
        Assert.assertTrue(nullValidator.validate(null));
    }


    @Test
    public void ipAddressTests() {
        IPAddressValidator validator = IPAddressValidator.instance();
        Assert.assertTrue(validator.validate("106.72.28.74"));
        Assert.assertTrue(validator.validate("156.243.130.166"));
        Assert.assertTrue(validator.validate("28.55.168.128"));
        Assert.assertTrue(validator.validate("185.81.160.85"));
        Assert.assertTrue(validator.validate("158.137.238.6"));
        Assert.assertTrue(validator.validate("141.122.136.144"));
        Assert.assertTrue(validator.validate("104.179.97.82"));
        Assert.assertTrue(validator.validate("28.77.158.48"));
        Assert.assertTrue(validator.validate("72.129.239.24"));
        Assert.assertTrue(validator.validate("51.211.70.30"));

        Assert.assertFalse(validator.validate("51.211.70"));
        Assert.assertFalse(validator.validate("51..211.70.30"));
        Assert.assertFalse(validator.validate("a1.211.70.30"));
        Assert.assertFalse(validator.validate("51.211.70.30.0"));
    }

    @Test
    public void creditCardTests() {
        CreditCardValidator validator = CreditCardValidator.instance();

        Assert.assertTrue(validator.validate("4508242795214770"));
        Assert.assertTrue(validator.validate("3534550235909500"));
        Assert.assertTrue(validator.validate("3563436733386890"));

        Assert.assertTrue(validator.validate("30485245023962"));

        Assert.assertTrue(validator.validate("3559979696602300"));
        Assert.assertTrue(validator.validate("3546330084792460"));
        Assert.assertTrue(validator.validate("3571014044514510"));
        Assert.assertTrue(validator.validate("30166467912021"));
        Assert.assertTrue(validator.validate("4074771539744790"));
        Assert.assertTrue(validator.validate("374283138983226"));
        Assert.assertTrue(validator.validate("5100145505218790"));
        Assert.assertTrue(validator.validate("30501574577558"));

        Assert.assertFalse(validator.validate("676306013856639000"));
        Assert.assertFalse(validator.validate("5018278895598920000"));
        Assert.assertFalse(validator.validate("5602249431899030"));
        Assert.assertFalse(validator.validate("5002353015111220"));
        Assert.assertFalse(validator.validate("6771208405057810000"));

    }


}
