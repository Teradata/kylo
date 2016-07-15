package com.thinkbiganalytics.rest.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Created by sr186054 on 6/20/16.
 */
@Provider
@Configuration
public class UnsupportedOperationExceptionMapper extends BaseExceptionMapper implements ExceptionMapper<UnsupportedOperationException> {

    private static final Logger log = LoggerFactory.getLogger(UnsupportedOperationExceptionMapper.class);

    @Override
    public Response toResponse(UnsupportedOperationException e) {
        return defaultResponse(e);
    }
}
