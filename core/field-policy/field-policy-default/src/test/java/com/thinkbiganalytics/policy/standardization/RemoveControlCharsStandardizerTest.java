/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.policy.standardization;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Created by matthutton on 5/7/16.
 */
public class RemoveControlCharsStandardizerTest {

    @Test
    public void testConversion() throws Exception {
        RemoveControlCharsStandardizer c = RemoveControlCharsStandardizer.instance();
        assertEquals("abcde", c.convertValue("a\u0000b\u0007c\u008fd\ne"));
        assertEquals("abc", c.convertValue("a\n\fbc"));
        assertEquals(" ", c.convertValue(" "));
        assertEquals("", c.convertValue(""));
    }
}