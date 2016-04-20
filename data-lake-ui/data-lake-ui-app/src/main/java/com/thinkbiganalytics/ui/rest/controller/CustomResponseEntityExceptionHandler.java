package com.thinkbiganalytics.ui.rest.controller;


import com.thinkbiganalytics.rest.model.RestResponseStatus;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

/**
 * Created by sr186054 on 9/22/15.
 */
@Provider
public class CustomResponseEntityExceptionHandler implements  javax.ws.rs.ext.ExceptionMapper<Exception> {

    @Context
    private  HttpServletRequest req;

    @Override
    public Response toResponse(Exception e) {
       return  Response.accepted(new RestResponseStatus.ResponseStatusBuilder().messageFromException(e).url(req.getRequestURI()).buildError()).status(Response.Status.BAD_REQUEST).build();
    }
}