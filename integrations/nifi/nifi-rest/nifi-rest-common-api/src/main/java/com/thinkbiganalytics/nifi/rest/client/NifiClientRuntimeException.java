package com.thinkbiganalytics.nifi.rest.client;

/*-
 * #%L
 * thinkbig-nifi-rest-common-api
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
 * Thrown to indicate an exception when communicating with the NiFi REST API.
 */
public class NifiClientRuntimeException extends RuntimeException {


    public NifiClientRuntimeException() {
    }

    public NifiClientRuntimeException(String message) {
        super(message);
    }

    public NifiClientRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public NifiClientRuntimeException(Throwable cause) {
        super(cause);
    }

    public NifiClientRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
