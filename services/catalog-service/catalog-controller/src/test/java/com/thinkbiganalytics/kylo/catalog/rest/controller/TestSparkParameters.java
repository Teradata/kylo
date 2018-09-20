/**
 * 
 */
package com.thinkbiganalytics.kylo.catalog.rest.controller;

import com.thinkbiganalytics.metadata.api.catalog.DataSetSparkParameters;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Test class to be further mocked/spied.
 */
public abstract class TestSparkParameters implements DataSetSparkParameters {

    @Override
    public String getFormat() {
        return "jdbc";
    }

    @Override
    public List<String> getFiles() {
        return Collections.emptyList();
    }

    @Override
    public List<String> getJars() {
        return Collections.emptyList();
    }

    @Override
    public List<String> getPaths() {
        return Collections.emptyList();
    }

    @Override
    public Map<String, String> getOptions() {
        return Collections.emptyMap();
    }

}
