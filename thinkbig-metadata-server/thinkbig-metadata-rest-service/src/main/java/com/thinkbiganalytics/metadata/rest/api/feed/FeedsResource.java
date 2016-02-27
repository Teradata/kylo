/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.api.feed;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.springframework.stereotype.Component;

/**
 *
 * @author Sean Felten
 */
@Component
@Path("/metadata/feed")
public class FeedsResource {

    @GET
    @Path("/names")
    @Produces("application/json")
    public List<String> getFeedNames() {
        return Arrays.asList("feed1", "feed2");
    }
}
