/**
 * 
 */
package com.thinkbiganalytics.security.rest.controller;

/*-
 * #%L
 * kylo-security-controller
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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Component;

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.security.RoleNotFoundException;
import com.thinkbiganalytics.rest.model.RestResponseStatus;
import com.thinkbiganalytics.security.rest.model.UserGroup;
import com.thinkbiganalytics.security.rest.model.Role;
import com.thinkbiganalytics.security.role.SecurityRole;
import com.thinkbiganalytics.security.role.SecurityRoleProvider;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;

/**
 *
 */
@Api(tags = "Security - Roles")
@Component
@Path("/v1/security/roles")
@SwaggerDefinition(tags = @Tag(name = "Security - Roles", description = "manages roles"))
public class RolesController {

    @Inject
    private MetadataAccess metadata;
    
    @Inject
    private SecurityRoleProvider roleProvider;
    
    @Inject
    private SecurityModelTransform securityTransform;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Returns all the roles defined for each kind of entity.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "The list of entity/roles mappings.", response = UserGroup.class),
                      @ApiResponse(code = 500, message = "There was a problem accessing the roles.", response = RestResponseStatus.class)
                  })
    public Map<String, List<Role>> getRoles() {
        return metadata.read(() -> {
            Map<String, List<SecurityRole>> roleMap = this.roleProvider.getRoles();
            return roleMap.entrySet().stream()
                            .collect(Collectors.toMap(e -> e.getKey(), 
                                                      e -> securityTransform.toRoles().apply(e.getValue())));
        });
    }

    @GET
    @Path("/{entity}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Returns all the roles defined for the a particular kind of entity.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "The list of roles defined for the entity.", response = UserGroup.class),
                      @ApiResponse(code = 500, message = "There was a problem accessing the roles.", response = RestResponseStatus.class)
                  })
    public List<Role> getEntityRoles(@PathParam("entity") String entityName) {
        return metadata.read(() -> {
            try {
                return this.roleProvider.getEntityRoles(entityName).stream()
                    .map(securityTransform.toRole())
                    .collect(Collectors.toList());
            }catch (RoleNotFoundException e){
                return Collections.emptyList();
            }
        });     
    }
}
