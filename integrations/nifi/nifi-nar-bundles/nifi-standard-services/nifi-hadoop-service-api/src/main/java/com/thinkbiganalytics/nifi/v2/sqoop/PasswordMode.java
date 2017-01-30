package com.thinkbiganalytics.nifi.v2.sqoop;

/*-
 * #%L
 * thinkbig-nifi-hadoop-service-api
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
 * Password modes to provide authentication information for connection to source system
 */
public enum PasswordMode {
    CLEAR_TEXT_ENTRY,
    ENCRYPTED_TEXT_ENTRY,
    ENCRYPTED_ON_HDFS_FILE;

    @Override
    public String toString() {
        switch (this) {
            case CLEAR_TEXT_ENTRY:
                return "CLEAR_TEXT_ENTRY";
            case ENCRYPTED_TEXT_ENTRY:
                return "ENCRYPTED_TEXT_ENTRY";
            case ENCRYPTED_ON_HDFS_FILE:
                return "ENCRYPTED_ON_HDFS_FILE";
        }
        return "";
    }
}

