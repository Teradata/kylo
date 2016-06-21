package com.thinkbiganalytics.rest.exception;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

/**
 * Created by Jeremy Merrifield on 6/14/16.
 */
public class BaseExceptionMapper {
    @Context
    protected HttpServletRequest req;

    @Value("${application.debug}")
    protected boolean debugMode;


}
