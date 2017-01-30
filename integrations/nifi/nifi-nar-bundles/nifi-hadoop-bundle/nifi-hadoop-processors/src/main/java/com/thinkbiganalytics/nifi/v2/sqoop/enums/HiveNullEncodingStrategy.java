package com.thinkbiganalytics.nifi.v2.sqoop.enums;

/*-
 * #%L
 * thinkbig-nifi-hadoop-processors
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

/**
 * List of supported strategies for encoding null values for use with Hive
 */
public enum HiveNullEncodingStrategy {
    ENCODE_STRING_AND_NONSTRING,
    DO_NOT_ENCODE,
    ENCODE_ONLY_STRING,
    ENCODE_ONLY_NONSTRING;

    @Override
    public String toString() {
        switch (this) {
            case ENCODE_STRING_AND_NONSTRING:
                return "ENCODE_STRING_AND_NONSTRING";
            case DO_NOT_ENCODE:
                return "DO_NOT_ENCODE";
            case ENCODE_ONLY_STRING:
                return "ENCODE_ONLY_STRING";
            case ENCODE_ONLY_NONSTRING:
                return "ENCODE_ONLY_NONSTRING";
        }
        return "";
    }
}
