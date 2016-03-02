/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.api;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Sean Felten
 */
@Component
@Path("/metadata/feed")
public class FeedsResource {
    
    public FeedsResource() {
        super();
    }

    @GET
    @Path("/names")
    @Produces("application/json")
    public List<String> getFeedNames() {
        return Arrays.asList("feed1", "feed2");
    }
}
