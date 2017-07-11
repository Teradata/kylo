/**
 *
 */
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

import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.rest.model.RestResponseStatus;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import sun.security.x509.AccessDescription;

import java.security.AccessControlException;

import javax.jcr.AccessDeniedException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 *
 */
@Provider
@Component
public class AccessDeniedExceptionMapper implements ExceptionMapper<MetadataRepositoryException> {

    private static final Logger log = LoggerFactory.getLogger(AccessDeniedExceptionMapper.class);

    @Context
    protected HttpServletRequest req;

    public AccessDeniedExceptionMapper() {
        super();
    }

    @Override
    public Response toResponse(MetadataRepositoryException e) {
        Throwable cause = ExceptionUtils.getRootCause(e);
        if (cause instanceof AccessDeniedException) {
            log.debug("Access denied violation", e);
            RestResponseStatus.ResponseStatusBuilder builder = new RestResponseStatus.ResponseStatusBuilder();
            builder.url(req.getRequestURI());
            builder.message(cause.getMessage());
            return Response.accepted(builder.buildError()).status(Response.Status.FORBIDDEN).build();
        } else {
            return new ThrowableMapper().toResponse(cause);
        }
    }
}
