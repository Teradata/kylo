package com.thinkbiganalytics.policy.standardization;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by matthutton on 5/7/16.
 */
public class StripNonNumericTest {

    @Test
    public void testConvertValue() throws Exception {

        assertEquals("24.32", StripNonNumeric.instance().convertValue("$abc24.e32"));
        assertEquals("190000232.1234", StripNonNumeric.instance().convertValue("190000232.1234"));
        assertEquals("999100000", StripNonNumeric.instance().convertValue("999,100,000"));
    }
}
