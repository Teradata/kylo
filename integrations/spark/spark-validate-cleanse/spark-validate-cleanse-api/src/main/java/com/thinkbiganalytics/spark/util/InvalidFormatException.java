package com.thinkbiganalytics.spark.util;

/*-
 * #%L
 * thinkbig-spark-validate-cleanse-api
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


import com.thinkbiganalytics.spark.validation.HCatDataType;

/**
 * Represents a string type that cannot be converted to its numerical corresponding type
 */
public class InvalidFormatException extends Exception {

    private HCatDataType type;
    private String value;

    public InvalidFormatException(HCatDataType type, String value) {
        super("Value [" + value + "] cannot be converted to type " + type.getName());
        this.type = type;
        this.value = value;
    }

    public HCatDataType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }
}
