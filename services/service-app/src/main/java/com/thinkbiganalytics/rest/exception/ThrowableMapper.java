package com.thinkbiganalytics.rest.exception;


import com.thinkbiganalytics.rest.model.RestResponseStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

/**
 * Created by sr186054 on 9/22/15.
 */
@Provider
@Configuration
public class ThrowableMapper extends BaseExceptionMapper implements javax.ws.rs.ext.ExceptionMapper<Throwable> {

    private static final Logger log = LoggerFactory.getLogger(ThrowableMapper.class);

    @Override
    public Response toResponse(Throwable exception) {
        log.error("toResponse() caught throwable", exception);
        RestResponseStatus.ResponseStatusBuilder builder = new RestResponseStatus.ResponseStatusBuilder();
        builder.url(req.getRequestURI());
        builder.message("An unknown error has occurred. This is very likely due to invalid input");

        if(debugMode) {
            builder.setDeveloperMessage(exception);
        }
        return Response.accepted(builder.buildError()).status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
}