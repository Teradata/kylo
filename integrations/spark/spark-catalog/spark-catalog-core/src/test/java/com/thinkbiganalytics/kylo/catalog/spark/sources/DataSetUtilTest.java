package com.thinkbiganalytics.kylo.catalog.spark.sources;

import com.thinkbiganalytics.kylo.catalog.api.MissingOptionException;
import com.thinkbiganalytics.kylo.catalog.spi.DataSetOptions;

import org.junit.Assert;
import org.junit.Test;

public class DataSetUtilTest {

    /**
     * Verify retrieving a defined option.
     */
    @Test
    public void getOptionDefined() {
        final DataSetOptions options = new DataSetOptions();
        options.setOption("key", "value");
        Assert.assertEquals("value", DataSetUtil.getOptionOrThrow(options, "key", null));
    }

    /**
     * Verify exception when retrieving an undefined option.
     */
    @Test(expected = MissingOptionException.class)
    public void getOptionUndefined() {
        DataSetUtil.getOptionOrThrow(new DataSetOptions(), "key", null);
    }
}
