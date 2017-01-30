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
 * List of supported strategies for handling case where target HDFS directory exists
 */
public enum TargetHdfsDirExistsStrategy {
    DELETE_DIR_AND_IMPORT,
    FAIL_IMPORT;

    @Override
    public String toString() {
        switch (this) {
            case DELETE_DIR_AND_IMPORT:
                return "DELETE_DIR_AND_IMPORT";
            case FAIL_IMPORT:
                return "FAIL_IMPORT";
        }
        return "";
    }
}
