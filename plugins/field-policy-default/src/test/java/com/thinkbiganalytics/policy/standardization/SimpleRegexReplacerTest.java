/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.policy.standardization;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by matthutton on 5/7/16.
 */
public class SimpleRegexReplacerTest {

    @Test
    public void testConversions() throws Exception {
        SimpleRegexReplacer regexReplacer = new SimpleRegexReplacer("(?i)foo", "bar");
        assertEquals("barfeebarfie", regexReplacer.convertValue("foofeefoofie"));
        assertEquals("", regexReplacer.convertValue(""));
        assertEquals("barfeebarfie", regexReplacer.convertValue("barfeebarfie"));

        SimpleRegexReplacer regexReplacer2 = new SimpleRegexReplacer("N/A", null);
        assertEquals("", regexReplacer2.convertValue("N/A"));

        SimpleRegexReplacer regexReplacer3 = new SimpleRegexReplacer("N/A", "");
        assertEquals("", regexReplacer3.convertValue("N/A"));


    }


    @Test
    public void testSpaces() throws Exception {
        SimpleRegexReplacer regexReplacer = new SimpleRegexReplacer("\\s", "");
        assertEquals("", regexReplacer.convertValue("        "));
    }

}