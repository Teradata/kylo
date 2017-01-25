package com.thinkbiganalytics.jobrepo.rest.model.datatables;

/*-
 * #%L
 * thinkbig-job-repository-controller
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


import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sr186054 on 8/12/15.
 */
public class DataTablesQueryParams {

    private List<Column> columns;
    private Integer draw;
    private Integer start;
    private Integer length;
    private List<Order> order;
    private Search search;

    public DataTablesQueryParams() {

    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    public Integer getDraw() {
        return draw;
    }

    public void setDraw(Integer draw) {
        this.draw = draw;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public List<Order> getOrder() {
        return order;
    }

    public void setOrder(List<Order> order) {
        this.order = order;
    }

    public Search getSearch() {
        return search;
    }

    public void setSearch(Search search) {
        this.search = search;
    }

    public Map<String, String> getFilterMap() {
        Map<String, String> filter = new HashMap<String, String>();
        if (columns != null) {
            for (Column col : getColumns()) {
                Search s = col.getSearch();
                if (StringUtils.isNotBlank(s.getValue())) {
                    String name = col.getName();

                    if (StringUtils.isBlank(name)) {
                        name = col.getData();
                    }
                    filter.put(name, s.getValue());
                }
            }
        }
        return filter;
    }

    public Map<String, String> getOrderMap() {
        Map<String, String> orderMap = new HashMap<String, String>();
        if (this.order != null) {
            for (Order o : order) {
                Integer colIndex = o.getColumn();
                String dir = o.getDir();
                Column col = this.getColumns().get(colIndex);
                String name = col.getName();
                if (StringUtils.isBlank(name)) {
                    name = col.getData();
                }
                orderMap.put(name, dir);

            }
        }
        return orderMap;
    }
}
