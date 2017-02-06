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

import com.thinkbiganalytics.discovery.schema.DataTypeDescriptor;

/**
 * A model to pass the data type
 */
public class DefaultDataTypeDescriptor implements DataTypeDescriptor {

    boolean isDate;

    boolean isNumeric;

    boolean isComplex;

    @Override
    public Boolean isDate() {
        return isDate;
    }

    @Override
    public Boolean isNumeric() {
        return isNumeric;
    }

    @Override
    public Boolean isComplex() {
        return isComplex;
    }

    public void setDate(boolean date) {
        isDate = date;
    }

    public void setNumeric(boolean numeric) {
        isNumeric = numeric;
    }

    public void setComplex(boolean complex) {
        isComplex = complex;
    }
}
