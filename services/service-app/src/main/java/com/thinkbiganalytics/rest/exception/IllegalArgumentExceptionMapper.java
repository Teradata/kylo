package com.thinkbiganalytics.rest.exception;

import com.thinkbiganalytics.rest.model.RestResponseStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

/**
 * Created by Jeremy Merrifield on 6/14/16.
 */
@Provider
@Configuration
public class IllegalArgumentExceptionMapper extends BaseExceptionMapper implements javax.ws.rs.ext.ExceptionMapper<IllegalArgumentException> {
    private static final Logger log = LoggerFactory.getLogger(IllegalArgumentExceptionMapper.class);

    @Override
    public Response toResponse(IllegalArgumentException exception) {
        log.error("toResponse() caught IllegalArgumentException", exception);
        RestResponseStatus.ResponseStatusBuilder builder = new RestResponseStatus.ResponseStatusBuilder();
        builder.url(req.getRequestURI());
        builder.message(exception.getMessage());

        return Response.accepted(builder.buildError()).status(Response.Status.BAD_REQUEST).build();
    }

}
