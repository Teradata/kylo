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

import com.thinkbiganalytics.rest.model.RestResponseStatus;
import com.thinkbiganalytics.rest.model.ValidationError;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

/**
 * If you use the Bean Validation annotations this mapper will catch exception thrown
 */
@Provider
@Configuration
public class ValidationExceptionMapper extends BaseExceptionMapper implements javax.ws.rs.ext.ExceptionMapper<ConstraintViolationException> {

    private static final Logger log = LoggerFactory.getLogger(ValidationExceptionMapper.class);

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        log.error("toResponse() Validation Exception", exception);
        RestResponseStatus.ResponseStatusBuilder builder = new RestResponseStatus.ResponseStatusBuilder();
        builder.validationError(true);
        builder.url(req.getRequestURI());
        for (final ConstraintViolation violation : exception.getConstraintViolations()) {
            String invalidValue = null;
            if (violation.getInvalidValue() instanceof String) {
                invalidValue = (String) violation.getInvalidValue();
            }
            ValidationError error = new ValidationError(invalidValue, violation.getMessage());
            builder.addValidationError(error);
        }

        builder.message("A validation error has occurred");

        if (debugMode) {
            builder.setDeveloperMessage(exception);
        }
        return Response.accepted(builder.buildError()).status(Response.Status.BAD_REQUEST).build();
    }

}
