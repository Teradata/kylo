package com.thinkbiganalytics.rest.exception;

import com.thinkbiganalytics.rest.model.RestResponseStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 * Created by Jeremy Merrifield on 6/14/16.
 */
public class BaseExceptionMapper {

    private static final Logger log = LoggerFactory.getLogger(BaseExceptionMapper.class);
    @Context
    protected HttpServletRequest req;

    @Value("${application.debug}")
    protected boolean debugMode;


    public Response defaultResponse(Throwable e) {
        log.error("toResponse() caught Exception: {}, {} ",e.getClass().getName(),e.getMessage(), e);
        RestResponseStatus.ResponseStatusBuilder builder = new RestResponseStatus.ResponseStatusBuilder();
        builder.url(req.getRequestURI());
        builder.message(e.getMessage());

        return Response.accepted(builder.buildError()).status(Response.Status.BAD_REQUEST).build();
    }



}
