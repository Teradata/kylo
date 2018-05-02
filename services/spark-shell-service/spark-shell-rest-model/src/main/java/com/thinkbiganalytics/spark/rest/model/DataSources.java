package com.thinkbiganalytics.spark.rest.model;

/*-
 * #%L
 * Spark Shell Service REST Model
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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

import java.util.List;

public  class DataSources {
    public List<String> downloads;
    public List<String> tables;

    public void setDownloads(List<String> downloads) {
        this.downloads = downloads;
    }

    public List<String> getDownloads() {
        return downloads;
    }

    public List<String> getTables() {
        return tables;
    }

    public void setTables(List<String> tables) {
        this.tables = tables;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("DataSourcesResponse{");
        sb.append("downloads=").append(downloads);
        sb.append(", tables=").append(tables);
        sb.append('}');
        return sb.toString();
    }
}
