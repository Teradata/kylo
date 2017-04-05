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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.springframework.stereotype.Component;

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.security.rest.model.Role;
import com.thinkbiganalytics.security.role.SecurityRole;
import com.thinkbiganalytics.security.role.SecurityRoleProvider;

import io.swagger.annotations.Api;
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
    public List<Role> getEntityRoles(@PathParam("entity") String entityName) {
        return metadata.read(() -> {
            return this.roleProvider.getEntityRoles(entityName).stream()
                            .map(securityTransform.toRole())
                            .collect(Collectors.toList());
        });     
    }
}
