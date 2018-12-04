package com.thinkbiganalytics.rest.exception;

/*-
 * #%L
 * thinkbig-service-app
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

import com.thinkbiganalytics.hive.exceptions.ThriftConnectionException;
import com.thinkbiganalytics.rest.model.RestResponseStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 */
@Provider
@Component
public class ThriftConnectionExceptionMapper extends BaseExceptionMapper implements ExceptionMapper<ThriftConnectionException> {

    private static final Logger log = LoggerFactory.getLogger(ThriftConnectionExceptionMapper.class);

    @Override
    public Response toResponse(ThriftConnectionException e) {
        log.error("ThriftConnectionExceptionMapper caught ThriftConnectionException: ", e);

        RestResponseStatus.ResponseStatusBuilder builder = new RestResponseStatus.ResponseStatusBuilder();
        builder.url(req.getRequestURI());
        if (e.getCause() != null) {
            builder.setDeveloperMessage(e.getCause());
        }
        builder.message("Unable to connect to Thrift Service.");

        return Response.accepted(builder.buildError()).status(Response.Status.SERVICE_UNAVAILABLE).build();
    }

}
