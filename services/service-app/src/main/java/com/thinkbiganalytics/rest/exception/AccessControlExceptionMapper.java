/**
 * 
 */
package com.thinkbiganalytics.rest.exception;

import java.security.AccessControlException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.thinkbiganalytics.rest.model.RestResponseStatus;

/**
 *
 * @author Sean Felten
 */
@Provider
@Component
public class AccessControlExceptionMapper implements ExceptionMapper<AccessControlException> {
    
    private static final Logger log = LoggerFactory.getLogger(AccessControlExceptionMapper.class);
    
    @Context
    protected HttpServletRequest req;
    
    public AccessControlExceptionMapper() {
        super();
    }

    @Override
    public Response toResponse(AccessControlException e) {
        log.debug("Access control violation", e);
        
        RestResponseStatus.ResponseStatusBuilder builder = new RestResponseStatus.ResponseStatusBuilder();
        builder.url(req.getRequestURI());
        builder.message("Access control violation: " + e.getMessage());

        return Response.accepted(builder.buildError()).status(Response.Status.FORBIDDEN).build();
    }

}
