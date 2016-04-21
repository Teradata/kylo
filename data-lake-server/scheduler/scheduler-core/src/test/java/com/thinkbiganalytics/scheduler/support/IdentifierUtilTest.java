package com.thinkbiganalytics.scheduler.support;

import org.junit.Test;

/**
 * Created by sr186054 on 9/25/15.
 */
public class IdentifierUtilTest {

    @Test
    public void testId(){
        IdentifierUtil.createUniqueName("test",10);

    }
}
