package com.thinkbiganalytics.nifi.rest.config;

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

import com.thinkbiganalytics.nifi.rest.client.NifiRestClientExceptionTranslator;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ClientErrorException;

/**
 */
@Aspect
public class NifiRestClientAroundAspect {

    private static final Logger logger = LoggerFactory.getLogger(NifiRestClientAroundAspect.class);

    @Around("execution(* com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient.*(..))")
    public Object NifiRestClientAroundAspect(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            Object obj = joinPoint.proceed();
            return obj;
        } catch (Throwable ex) {
            if (ex instanceof ClientErrorException) {
                String err = ((ClientErrorException) ex).getResponse().readEntity(String.class);
                logger.error("Nifi Client Error Message: {}", err);
            }
            ex = NifiRestClientExceptionTranslator.translateException(ex);
            throw ex;
        }
    }

}
