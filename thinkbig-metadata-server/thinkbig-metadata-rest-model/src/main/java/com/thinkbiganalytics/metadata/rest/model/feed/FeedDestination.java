/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.model.feed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.thinkbiganalytics.metadata.rest.model.data.Datasource;

/**
 *
 * @author Sean Felten
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeedDestination {

    private String id;
    private FieldsPolicy fieldsPolicy;
    private String datasourceId;
    private Datasource datasource;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public FieldsPolicy getFieldsPolicy() {
        return fieldsPolicy;
    }

    public void setFieldsPolicy(FieldsPolicy fieldsPolicy) {
        this.fieldsPolicy = fieldsPolicy;
    }

    public String getDatasourceId() {
        return datasourceId;
    }

    public void setDatasourceId(String datasourceId) {
        this.datasourceId = datasourceId;
    }

    public Datasource getDatasource() {
        return datasource;
    }

    public void setDatasource(Datasource datasource) {
        this.datasource = datasource;
    }
}
