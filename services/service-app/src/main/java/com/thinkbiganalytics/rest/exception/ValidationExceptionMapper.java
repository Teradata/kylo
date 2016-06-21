package com.thinkbiganalytics.rest.exception;

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
 * Created by Jeremy Merrifield on 6/13/16.
 *
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
        builder.validationError(true);
        for (final ConstraintViolation violation : exception.getConstraintViolations()) {
            String invalidValue = null;
            if (violation.getInvalidValue() instanceof String) {
                invalidValue = (String) violation.getInvalidValue();
            }
            ValidationError error = new ValidationError(invalidValue, violation.getMessage());
            builder.validationErrors(error);
        }

        builder.message("A validation error has occurred");

        if (debugMode) {
            builder.setDeveloperMessage(exception);
        }
        return Response.accepted(builder.buildError()).status(Response.Status.BAD_REQUEST).build();
    }

}
