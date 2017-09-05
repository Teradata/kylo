package com.thinkbiganalytics.discovery.util;

/*-
 * #%L
 * thinkbig-schema-discovery-api
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

import com.thinkbiganalytics.discovery.schema.DataTypeDescriptor;
import com.thinkbiganalytics.discovery.schema.Field;
import com.thinkbiganalytics.discovery.schema.Tag;

import java.util.List;
import java.util.Vector;

public class TestField implements Field {

    public List<String> sampleValues = new Vector<>();
    private String name;
    private String description = "";
    private String nativeDataType;
    private String derivedDataType;
    private Boolean primaryKey = false;
    private Boolean nullable = true;
    private Boolean modifiable = false;
    private DataTypeDescriptor dataTypeDescriptor;

    @Override
    public DataTypeDescriptor getDataTypeDescriptor() {
        return dataTypeDescriptor;
    }

    @Override
    public void setDataTypeDescriptor(DataTypeDescriptor dataTypeDescriptor) {
        this.dataTypeDescriptor = dataTypeDescriptor;
    }

    public String getDerivedDataType() {
        return derivedDataType;
    }

    @Override
    public void setDerivedDataType(String type) {
        this.derivedDataType = type;
    }

    @Override
    public Boolean isPrimaryKey() {
        return primaryKey;
    }

    @Override
    public Boolean isNullable() {
        return nullable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = (this.description == null ? "" : description);
    }

    public Boolean getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(Boolean primaryKey) {
        this.primaryKey = (primaryKey == null ? false : primaryKey);
    }

    public Boolean getNullable() {
        return nullable;
    }

    public void setNullable(Boolean nullable) {
        this.nullable = (nullable == null ? true : nullable);
    }

    public List<String> getSampleValues() {
        return sampleValues;
    }

    public void setSampleValues(List<String> sampleValues) {
        this.sampleValues = sampleValues;
    }

    public String asFieldStructure(String otherName) {
        return name + "|" + derivedDataType;
    }

    @Override
    public String getNativeDataType() {
        return this.nativeDataType;
    }

    public void setNativeDataType(String nativeDataType) {
        this.nativeDataType = nativeDataType;
    }

    @Override
    public Boolean isModifiable() {
        return modifiable;
    }

    @Override
    public void setModifiable(Boolean isModifiable) {
        modifiable = isModifiable;
    }

    @Override
    public String getDataTypeWithPrecisionAndScale() {
        return null;
    }

    @Override
    public String getPrecisionScale() {
        return null;
    }

    @Override
    public Boolean getCreatedTracker() {
        return null;
    }

    @Override
    public Boolean getUpdatedTracker() {
        return null;
    }

    @Override
    public List<Tag> getTags() {
        return null;
    }
}
