package com.thinkbiganalytics.metadata.rest.model.jcr;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sr186054 on 8/31/17.
 */
public class JcrQueryResult {
    private List<JcrQueryResultColumn> columns;
    private List<JcrQueryResultRow> rows;

    private long queryTime = 0L;

    public List<JcrQueryResultColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<JcrQueryResultColumn> columns) {
        this.columns = columns;
    }

    public List<JcrQueryResultRow> getRows() {
        if(rows == null){
            rows = new ArrayList<>();
        }
        return rows;
    }

    public void setRows(List<JcrQueryResultRow> rows) {
        this.rows = rows;
    }

    public long getQueryTime() {
        return queryTime;
    }

    public void setQueryTime(long queryTime) {
        this.queryTime = queryTime;
    }

    public void addRow(JcrQueryResultRow row){
        getRows().add(row);
    }
}
