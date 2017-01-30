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
 * List of supported formats for landing extracted data on HDFS
 */
public enum ExtractDataFormat {
    TEXT,
    AVRO,
    SEQUENCE_FILE,
    PARQUET;

    @Override
    public String toString() {
        switch (this) {
            case TEXT:
                return "TEXT";
            case AVRO:
                return "AVRO";
            case SEQUENCE_FILE:
                return "SEQUENCE_FILE";
            case PARQUET:
                return "PARQUET";
        }
        return "";
    }
}
