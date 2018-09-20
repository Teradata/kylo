/**
 * 
 */
package com.thinkbiganalytics.kylo.catalog.rest.controller;

import com.thinkbiganalytics.metadata.api.catalog.DataSet;
import com.thinkbiganalytics.metadata.api.catalog.DataSetSparkParameters;
import com.thinkbiganalytics.metadata.api.catalog.DataSource;

import org.mockito.Mockito;

/**
 * Test class to be further mocked/spied.
 */
public abstract class TestDataSet implements DataSet {

    @Override
    public DataSetSparkParameters getSparkParameters() {
        return Mockito.spy(TestSparkParameters.class);
    }

    @Override
    public DataSetSparkParameters getEffectiveSparkParameters() {
        return getSparkParameters();
    }

    @Override
    public String getSystemName() {
        return "mysql_test";
    }

    @Override
    public String getTitle() {
        return "MySQL Test";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public DataSource getDataSource() {
        return Mockito.spy(TestDataSource.class);
    }

}
