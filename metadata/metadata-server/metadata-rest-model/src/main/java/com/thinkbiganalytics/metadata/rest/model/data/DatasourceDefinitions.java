package com.thinkbiganalytics.metadata.rest.model.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sr186054 on 11/18/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DatasourceDefinitions {

    private List<DatasourceDefinition> definitions;

    public DatasourceDefinitions() {

    }

    public DatasourceDefinitions(List<DatasourceDefinition> definitions) {
        this.definitions = definitions;
    }

    public List<DatasourceDefinition> getDefinitions() {
        if (definitions == null) {
            definitions = new ArrayList<>();
        }
        return definitions;
    }

    public void setDefinitions(List<DatasourceDefinition> definitions) {
        this.definitions = definitions;
    }
}
