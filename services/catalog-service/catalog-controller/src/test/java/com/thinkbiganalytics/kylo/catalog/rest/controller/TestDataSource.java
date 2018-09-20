/**
 * 
 */
package com.thinkbiganalytics.kylo.catalog.rest.controller;

import com.thinkbiganalytics.metadata.api.catalog.Connector;
import com.thinkbiganalytics.metadata.api.catalog.DataSet;
import com.thinkbiganalytics.metadata.api.catalog.DataSetSparkParameters;
import com.thinkbiganalytics.metadata.api.catalog.DataSource;

import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

/**
 * Test class to be further mocked/spied.
 */
public abstract class TestDataSource implements DataSource {

    @Override
    public String getSystemName() {
        return "mysql";
    }

    @Override
    public String getTitle() {
        return "MySQL";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public DataSetSparkParameters getSparkParameters() {
        return Mockito.spy(TestSparkParameters.class);
    }

    @Override
    public DataSetSparkParameters getEffectiveSparkParameters() {
        return getSparkParameters();
    }

    @Override
    public Connector getConnector() {
        return Mockito.spy(TestConnector.class);
    }

    @Override
    public List<? extends DataSet> getDataSets() {
        return Collections.emptyList();
    }

}
