package com.thinkbiganalytics.rest.exception;

import com.thinkbiganalytics.rest.model.RestResponseStatus;

import org.springframework.context.annotation.Configuration;

import javax.annotation.Nonnull;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Maps {@link WebApplicationException} objects to a standard response format.
 */
@Provider
@Configuration
public class WebApplicationExceptionMapper extends BaseExceptionMapper implements ExceptionMapper<WebApplicationException> {

    @Nonnull
    @Override
    public Response toResponse(@Nonnull final WebApplicationException exception) {
        if (exception.getResponse().getEntity() == null) {
            final RestResponseStatus.ResponseStatusBuilder builder = new RestResponseStatus.ResponseStatusBuilder()
                    .message(exception.getMessage())
                    .url(req.getRequestURI());
            if (debugMode && exception.getCause() != null) {
                builder.setDeveloperMessage(exception.getCause());
            }
            return Response.fromResponse(exception.getResponse()).entity(builder.buildError()).build();
        } else {
            return exception.getResponse();
        }
    }
}
