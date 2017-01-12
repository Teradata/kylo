package com.thinkbiganalytics.rest.controller;

import com.thinkbiganalytics.metadata.api.app.KyloVersion;
import com.thinkbiganalytics.metadata.api.app.KyloVersionProvider;
import com.wordnik.swagger.annotations.Api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Controller used by 'About Kylo' popup
 * @author Jagrut Sharma
 */
@Api(value = "about-kylo", produces = "application/text")
@Path("/v1/about")
@Component
public class AboutKyloController {
    private static final Logger log = LoggerFactory.getLogger(AboutKyloController.class);

    @Inject
    KyloVersionProvider kyloVersionProvider;

    /*
        Get Kylo Version for showing in UI About Dialog Box
     */
    @GET
    @Path("/version")
    @Produces({MediaType.TEXT_PLAIN})
    public Response getKyloVersion() {

        final String VERSION_NOT_AVAILABLE = "Not Available";
        KyloVersion kyloVersion = kyloVersionProvider.getKyloVersion();

        if (kyloVersion != null) {
            return Response.ok(kyloVersion.getVersion()).build();
        }
        else {
            return Response.ok(VERSION_NOT_AVAILABLE).build();
        }
    }
}

