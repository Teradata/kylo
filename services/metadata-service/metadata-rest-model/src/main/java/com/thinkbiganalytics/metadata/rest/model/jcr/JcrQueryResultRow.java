package com.thinkbiganalytics.metadata.rest.model.jcr;

import java.util.List;

/**
 * Created by sr186054 on 8/31/17.
 */
public class JcrQueryResultRow {
    List<JcrQueryResultColumnValue> columnValues;

    public List<JcrQueryResultColumnValue> getColumnValues() {
        return columnValues;
    }

    public void setColumnValues(List<JcrQueryResultColumnValue> columnValues) {
        this.columnValues = columnValues;
    }
}
