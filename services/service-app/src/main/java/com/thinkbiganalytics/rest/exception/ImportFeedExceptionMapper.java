package com.thinkbiganalytics.rest.exception;

import com.thinkbiganalytics.feedmgr.service.feed.ImportFeedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;

/**
 * Created by sr186054 on 11/17/16.
 */
public class ImportFeedExceptionMapper extends BaseExceptionMapper implements javax.ws.rs.ext.ExceptionMapper<ImportFeedException> {

    private static final Logger log = LoggerFactory.getLogger(ImportFeedExceptionMapper.class);

    @Override
    public Response toResponse(ImportFeedException e) {
        return defaultResponse(e);
    }

    {

    }
}
