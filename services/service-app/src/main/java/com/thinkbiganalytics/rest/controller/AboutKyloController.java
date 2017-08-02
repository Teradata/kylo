package com.thinkbiganalytics.rest.controller;

/*-
 * #%L
 * thinkbig-service-app
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

import com.thinkbiganalytics.KyloVersion;
import com.thinkbiganalytics.metadata.api.app.KyloVersionProvider;
import com.thinkbiganalytics.security.GroupPrincipal;
import com.thinkbiganalytics.security.rest.model.User;

import org.springframework.security.authentication.jaas.JaasGrantedAuthority;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Controller used by 'About Kylo' popup
 */
@Api(tags = "Configuration", produces = "application/text")
@Path("/v1/about")
@Component
public class AboutKyloController {

    @Inject
    KyloVersionProvider kyloVersionProvider;

    /**
     * Gets information about the current user.
     */
    @GET
    @Path("/me")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets information about the current user.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the user.", response = User.class)
    )
    public Response getCurrentUser() {
        // Create principal from current user
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        final User user = new User();
        user.setEnabled(true);

        if (auth.getPrincipal() instanceof UserDetails) {
            final UserDetails details = (UserDetails) auth.getPrincipal();
            user.setGroups(details.getAuthorities().stream()
                               .map(GrantedAuthority::getAuthority)
                               .collect(Collectors.toSet()));
            user.setSystemName(details.getUsername());
        } else {
            user.setGroups(auth.getAuthorities().stream()
                               .filter(JaasGrantedAuthority.class::isInstance)
                               .map(JaasGrantedAuthority.class::cast)
                               .filter(authority -> authority.getPrincipal() instanceof GroupPrincipal)
                               .map(JaasGrantedAuthority::getAuthority)
                               .collect(Collectors.toSet()));
            user.setSystemName(auth.getPrincipal().toString());
        }

        // Return principal
        return Response.ok(user).build();
    }

    /**
     * Get Kylo Version for showing in UI About Dialog Box.
     */
    @GET
    @Path("/version")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation("Gets the version number of Kylo.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the version number.", response = String.class)
    )
    public Response getKyloVersion() {

        final String VERSION_NOT_AVAILABLE = "Not Available";
        KyloVersion kyloVersion = kyloVersionProvider.getCurrentVersion();

        if (kyloVersion != null) {
            return Response.ok(kyloVersion.getVersion()).build();
        } else {
            return Response.ok(VERSION_NOT_AVAILABLE).build();
        }
    }
}
