package com.thinkbiganalytics.spark.rest;

/*-
 * #%L
 * kylo-spark-shell-client-app
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

import com.thinkbiganalytics.spark.rest.model.SimpleResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Component
@Provider
public class ThrowableMapper implements ExceptionMapper<Throwable> {

    private static final Logger log = LoggerFactory.getLogger(ThrowableMapper.class);

    @Override
    public Response toResponse(@Nonnull final Throwable e) {
        log.error("Failed to process request due to error: {}", e);

        final SimpleResponse entity = new SimpleResponse();
        entity.setMessage(e.toString());
        entity.setStatus(SimpleResponse.Status.ERROR);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).entity(entity).build();
    }
}
