package com.thinkbiganalytics.nifi.v1.rest.util;

/*-
 * #%L
 * kylo-nifi-rest-client-v1
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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

import com.thinkbiganalytics.functions.QuadFunction;
import com.thinkbiganalytics.functions.QuintFunction;
import com.thinkbiganalytics.functions.TriFunction;
import com.thinkbiganalytics.nifi.rest.client.NifiConnectionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiFunction;

import javax.ws.rs.ProcessingException;

public class CatchNifiConnectionExceptionsUtil {

    private static final Logger logger = LoggerFactory.getLogger(CatchNifiConnectionExceptionsUtil.class);

    private CatchNifiConnectionExceptionsUtil() {
        throw new UnsupportedOperationException();
    }

    public static <P1, P2, R> R catchConnectionExceptions(BiFunction<P1, P2, R> function, P1 param1, P2 param2) {
        try {
            return function.apply(param1, param2);
        } catch (ProcessingException pe) {
            throw toNifiConnectionException(pe);
        }
    }

    public static <P1, P2, P3, R> R catchConnectionExceptions(TriFunction<P1, P2, P3, R> function, P1 param1, P2 param2, P3 param3) {
        try {
            return function.apply(param1, param2, param3);
        } catch (ProcessingException pe) {
            throw toNifiConnectionException(pe);
        }
    }

    public static <P1, P2, P3, P4, R> R catchConnectionExceptions(QuadFunction<P1, P2, P3, P4, R> function, P1 param1, P2 param2, P3 param3, P4 param4) {
        try {
            return function.apply(param1, param2, param3, param4);
        } catch (ProcessingException pe) {
            throw toNifiConnectionException(pe);
        }
    }

    public static <P1, P2, P3, P4, P5, R> R catchConnectionExceptions(QuintFunction<P1, P2, P3, P4, P5, R> function, P1 param1, P2 param2, P3 param3, P4 param4, P5 param5) {
        try {
            return function.apply(param1, param2, param3, param4, param5);
        } catch (ProcessingException pe) {
            throw toNifiConnectionException(pe);
        }
    }

    private static NifiConnectionException toNifiConnectionException(Throwable t) {
        logger.debug("ProcessingException encountered querying NiFi", t);

        if (t.getCause() != null) {
            if (t.getMessage() != null) {
                return new NifiConnectionException(t.getMessage(), t.getCause());
            }
            return new NifiConnectionException(t.getCause());
        }
        return new NifiConnectionException();
    }
}
