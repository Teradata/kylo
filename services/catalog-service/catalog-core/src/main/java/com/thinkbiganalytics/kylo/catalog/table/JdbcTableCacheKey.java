package com.thinkbiganalytics.kylo.catalog.table;

/*-
 * #%L
 * kylo-catalog-core
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

import com.thinkbiganalytics.kylo.catalog.rest.model.DataSetTemplate;

import org.apache.commons.lang3.StringUtils;

public class JdbcTableCacheKey {

    private DataSetTemplate template;
    private String filter;
    private String schemaPattern;
    private String tablePattern;
    private DefaultCatalogTableManager.FilterMode filterMode;

    /**
     * The key to use to determin object equality along with the template
     */
    private String equalityKey;

    public JdbcTableCacheKey(DataSetTemplate template, String filter, DefaultCatalogTableManager.FilterMode filterMode) {
        this.template = template;
        this.filter = filter;
        this.filterMode = filterMode;

        if (this.filter == null) {
            this.filter = "";
        }
        String schema = "%";
        String table = "%";
        if (this.filter.indexOf(".") > 0) {
            schema = this.filter.substring(0, this.filter.indexOf("."));
            table = this.filter.substring(this.filter.indexOf(".") + 1);
            this.equalityKey = schema + "." + table;
        } else {
            //switch the equality key and table filter depending upon th emode
            this.equalityKey = filterMode == DefaultCatalogTableManager.FilterMode.TABLES_AND_SCHEMA ? null : filter;
            table = filterMode == DefaultCatalogTableManager.FilterMode.TABLES_AND_SCHEMA ? table : filter;
        }
        if (!schema.endsWith("%")) {
            schema += "%";
        }
        if (!table.endsWith("%")) {
            table += "%";
        }
        this.schemaPattern = schema;
        this.tablePattern = table;
    }

    public DataSetTemplate getTemplate() {
        return template;
    }

    public String getFilter() {
        return filter;
    }

    public String getSchemaPattern() {
        return schemaPattern;
    }

    public String getTablePattern() {
        return tablePattern;
    }

    public String getSchemaJavaFilter() {
        return this.schemaPattern.replaceAll("%", "").toLowerCase();
    }

    public String getTableJavaFilter() {
        return this.tablePattern.replaceAll("%", "").toLowerCase();
    }

    public boolean isTableFiltered() {
        return StringUtils.isNotBlank(this.getTableJavaFilter());
    }

    public String getJavaFilter() {
        return !this.isTableFiltered() ? this.filter.replace("%", "").toLowerCase() : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof JdbcTableCacheKey)) {
            return false;
        }

        JdbcTableCacheKey cacheKey = (JdbcTableCacheKey) o;

        if (template != null ? !template.equals(cacheKey.template) : cacheKey.template != null) {
            return false;
        }
        if (equalityKey != null ? !equalityKey.equals(cacheKey.equalityKey) : cacheKey.equalityKey != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = template != null ? template.hashCode() : 0;
        result = 31 * result + (equalityKey != null ? equalityKey.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JdbcTableCacheKey{");
        sb.append("template=").append(template);
        sb.append(", filter='").append(filter).append('\'');
        sb.append(", schemaPattern='").append(schemaPattern).append('\'');
        sb.append(", tablePattern='").append(tablePattern).append('\'');
        sb.append(", equalityKey='").append(equalityKey).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
