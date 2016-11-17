/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.policy.standardization;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by matthutton on 5/7/16.
 */
public class MaskLeavingLastFourDigitStandardizerTest {

    @Test
    public void testConvertValue() throws Exception {

        MaskLeavingLastFourDigitStandardizer cc = MaskLeavingLastFourDigitStandardizer.instance();
        assertEquals("XXXXXXXXXXXX8790", cc.convertValue("5100145505218790"));
        assertEquals("XXXX-XXXX-XXXX-8790", cc.convertValue("5100-1455-0521-8790"));
        assertEquals("XXX-XX-2015", cc.convertValue("560-60-2015"));
        assertEquals("2015", cc.convertValue("2015"));
        assertEquals("20", cc.convertValue("20"));
        assertEquals("", cc.convertValue(""));
        assertEquals("XXXXXXXXXXX9966", cc.convertValue("373327123279966"));
    }
}