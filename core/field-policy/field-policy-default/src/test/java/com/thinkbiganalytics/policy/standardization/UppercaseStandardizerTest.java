/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.policy.standardization;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by matthutton on 5/7/16.
 */
public class UppercaseStandardizerTest {

    @Test
    public void testConvertValue() throws Exception {
        UppercaseStandardizer standardizer = UppercaseStandardizer.instance();
        assertEquals("ABC", standardizer.convertValue("abc"));
        assertEquals("HOW ARE YOU?", standardizer.convertValue("how are you?"));
        assertEquals("12 PYRAMID ST.", standardizer.convertValue("12 pyramid st."));
        assertEquals("12 PYRAMID ST.", standardizer.convertValue("12 PYRAMID ST."));
        assertEquals("", standardizer.convertValue(""));
    }
}