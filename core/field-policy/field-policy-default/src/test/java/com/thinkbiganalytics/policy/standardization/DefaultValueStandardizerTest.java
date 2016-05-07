/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.policy.standardization;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by matthutton on 5/7/16.
 */
public class DefaultValueStandardizerTest {

    @Test
    public void testConvertValue() throws Exception {

        DefaultValueStandardizer standardizer = new DefaultValueStandardizer("default");
        assertEquals("default", standardizer.convertValue(""));
        assertEquals("default", standardizer.convertValue(null));
        assertEquals("foo", standardizer.convertValue("foo"));
    }
}