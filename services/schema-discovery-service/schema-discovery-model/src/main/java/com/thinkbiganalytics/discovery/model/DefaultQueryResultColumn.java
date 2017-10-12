package com.thinkbiganalytics.discovery.model;

/*-
 * #%L
 * thinkbig-schema-discovery-model2
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.thinkbiganalytics.discovery.schema.QueryResultColumn;

/**
 * The Model used to pass query result columns
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultQueryResultColumn implements QueryResultColumn {

    private String displayName;
    private String hiveColumnLabel;
    private String field;
    private String dataType;
    private String nativeDataType;
    private String tableName;
    private String databaseName;
    private int index;
    private String comment;
    private String precisionScale;

    @Override
    public String getField() {
        return field;
    }

    @Override
    public void setField(String field) {
        this.field = field;
    }

    @Override
    public String getDataType() {
        return dataType;
    }

    @Override
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    @Override
    public String getNativeDataType() {
        return nativeDataType;
    }

    @Override
    public void setNativeDataType(String nativeDataType) {
        this.nativeDataType = nativeDataType;
    }

    @Override
    public String getTableName() {
        return tableName;
    }

    @Override
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public String getDatabaseName() {
        return databaseName;
    }

    @Override
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String getHiveColumnLabel() {
        return hiveColumnLabel;
    }

    @Override
    public void setHiveColumnLabel(String hiveColumnLabel) {
        this.hiveColumnLabel = hiveColumnLabel;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public String getComment() {
        return comment;
    }

    @Override
    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getPrecisionScale() {
        return precisionScale;
    }

    public void setPrecisionScale(String precisionScale) {
        this.precisionScale = precisionScale;
    }
}
