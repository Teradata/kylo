package com.thinkbiganalytics.nifi.rest.client;

/*-
 * #%L
 * thinkbig-nifi-rest-client-api
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

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.NoHttpResponseException;
import org.apache.http.conn.HttpHostConnectException;

import java.net.ConnectException;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.ProcessingException;

/**
 */
public class NifiRestClientExceptionTranslator {


    public static Throwable translateException(Throwable e) {

        //Return if its already translated to a NifiClientRuntimeException
        if (e instanceof NifiClientRuntimeException) {
            return e;
        }
        if (e instanceof NotFoundException) {
            return new NifiComponentNotFoundException(e.getMessage());
        } else if (e instanceof NullPointerException) {
            return new NifiConnectionException("Verify NiFi is running and try again", e);
        } else if (e instanceof ProcessingException) {
            int throwables = ExceptionUtils.getThrowableCount(e);
            if (throwables > 1) {
                Throwable rootCause = ExceptionUtils.getRootCause(e);
                if (rootCause instanceof NoHttpResponseException || rootCause instanceof HttpHostConnectException || rootCause instanceof ConnectException) {
                    //connection error
                    return new NifiConnectionException(e.getMessage(), e);

                }
            }
        }
        return new NifiClientRuntimeException(e);
    }

}


