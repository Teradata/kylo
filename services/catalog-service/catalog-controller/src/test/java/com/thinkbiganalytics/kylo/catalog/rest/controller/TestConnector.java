/**
 * 
 */
package com.thinkbiganalytics.kylo.catalog.rest.controller;

import com.thinkbiganalytics.metadata.api.catalog.Connector;
import com.thinkbiganalytics.metadata.api.catalog.DataSetSparkParameters;
import com.thinkbiganalytics.metadata.api.catalog.DataSource;

import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

/**
 * Test class to be further mocked/spied.
 */
public abstract class TestConnector implements Connector {

    @Override
    public String getSystemName() {
        return "sql_source";
    }

    @Override
    public String getTitle() {
        return "SQL Source";
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
    public boolean isActive() {
        return true;
    }

    @Override
    public String getPluginId() {
        return "mysql";
    }

    @Override
    public String getIcon() {
        return "amazon";
    }

    @Override
    public String getColor() {
        return "orange-700";
    }

    @Override
    public List<? extends DataSource> getDataSources() {
        return Collections.emptyList();
    }

}
