package com.thinkbiganalytics.spark.util;

import org.junit.Assert;
import org.junit.Test;

public class HiveUtilsTest
{
    /** Verify quoting an identifier. */
    @Test
    public void quoteIdentifier ()
    {
        Assert.assertEquals("`test`", HiveUtils.quoteIdentifier("test"));
        Assert.assertEquals("`test``s`", HiveUtils.quoteIdentifier("test`s"));
        Assert.assertEquals("`test`", HiveUtils.quoteIdentifier(null, "test"));
        Assert.assertEquals("`default`.`test`", HiveUtils.quoteIdentifier("default", "test"));
    }
}
