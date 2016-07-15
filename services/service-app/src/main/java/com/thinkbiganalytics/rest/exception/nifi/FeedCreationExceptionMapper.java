package com.thinkbiganalytics.rest.exception.nifi;

import com.thinkbiganalytics.nifi.feedmgr.FeedCreationException;
import com.thinkbiganalytics.rest.exception.BaseExceptionMapper;

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
public class FeedCreationExceptionMapper extends BaseExceptionMapper implements ExceptionMapper<FeedCreationException> {

    private static final Logger log = LoggerFactory.getLogger(FeedCreationExceptionMapper.class);

    @Override
    public Response toResponse(FeedCreationException e) {
        return defaultResponse(e);
    }

}
