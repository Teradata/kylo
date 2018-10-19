package com.thinkbiganalytics.feedmgr.service.feed.importing.model;
/*-
 * #%L
 * thinkbig-feed-manager-controller
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
public class LegacyDatasource {
    private String table;
    private String datasourceId;

    public LegacyDatasource(String table, String datasourceId) {
        this.table = table;
        this.datasourceId = datasourceId;
    }

    public String getTable() {
        return table;
    }

    public String getDatasourceId() {
        return datasourceId;
    }

    public String getKey(){
        return getTable()+"-"+getDatasourceId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LegacyDatasource)) {
            return false;
        }

        LegacyDatasource that = (LegacyDatasource) o;

        if (table != null ? !table.equals(that.table) : that.table != null) {
            return false;
        }
        if (datasourceId != null ? !datasourceId.equals(that.datasourceId) : that.datasourceId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = table != null ? table.hashCode() : 0;
        result = 31 * result + (datasourceId != null ? datasourceId.hashCode() : 0);
        return result;
    }
}