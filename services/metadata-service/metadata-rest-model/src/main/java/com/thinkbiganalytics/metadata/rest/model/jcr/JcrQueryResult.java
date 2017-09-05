package com.thinkbiganalytics.metadata.rest.model.jcr;

/*-
 * #%L
 * thinkbig-metadata-rest-model
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sr186054 on 8/31/17.
 */
public class JcrQueryResult {
    private List<JcrQueryResultColumn> columns;
    private List<JcrQueryResultRow> rows;
    private String explainPlan;

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

    public String getExplainPlan() {
        return explainPlan;
    }

    public void setExplainPlan(String explainPlan) {
        this.explainPlan = explainPlan;
    }
}
