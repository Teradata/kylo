package com.thinkbiganalytics.feedmgr.rest.controller;

/*-
 * #%L
 * thinkbig-feed-manager-controller
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.thinkbiganalytics.datalake.authorization.service.HadoopAuthorizationService;
import com.thinkbiganalytics.datalake.authorization.model.HadoopAuthorizationGroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;

@Api(tags = "Feed Manager: Security", produces = "application/json")
@Path("/v1/feedmgr/hadoop-authorization")
public class HadoopAuthorizationController {
    private static final Logger log = LoggerFactory.getLogger(HadoopAuthorizationController.class);

    // I had to use autowired instead of Inject to allow null values.
    @Autowired(required = false)
    @Qualifier("hadoopAuthorizationService")
    private HadoopAuthorizationService hadoopAuthorizationService;

    @GET
    @Path("/groups")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getGroups() {
        try {

            List<HadoopAuthorizationGroup> groups = null;
            if(hadoopAuthorizationService != null) {
                groups = hadoopAuthorizationService.getAllGroups();
            }
            else {
                groups = new ArrayList<>();
                log.debug("No plugin for hadoop authorization was loaded. Returning empty results");
            }

            return Response.ok(groups).build();
        }catch (Exception e){
            throw new RuntimeException("Unable to get the external security groups ", e);
        }
    }

    @GET
    @Path("/enabled")
    @Produces({MediaType.APPLICATION_JSON})
    public Response groupEnabled() {
        try {
            boolean isEnabled = false;

            if(hadoopAuthorizationService != null) {
                isEnabled = true;
            }

            return Response.ok("[{\"enabled\":" + isEnabled + "}]").build();
        }catch (Exception e){
            throw new RuntimeException("Unable to get the external security groups ", e);
        }
    }

}
