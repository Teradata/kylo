package com.thinkbiganalytics.metadata.rest.api;

/*-
 * #%L
 * thinkbig-metadata-rest-controller
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

import com.google.common.collect.Collections2;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleType;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleTypeProvider;
import com.thinkbiganalytics.metadata.api.security.MetadataAccessControl;
import com.thinkbiganalytics.metadata.rest.ExtensiblesModel;
import com.thinkbiganalytics.metadata.rest.model.extension.ExtensibleTypeDescriptor;
import com.thinkbiganalytics.security.AccessController;

import org.apache.catalina.connector.Response;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.Api;

/**
 * Allow creation and storing of new Entities with dynamic types.
 */
@Api(tags = "Internal", produces = "application/json")
@Component
@Path("/v1/metadata/extension")
public class ExtensionsController {
    
    @Inject
    private AccessController accessController;

    @Inject
    private MetadataAccess metadata;

    @Inject
    private ExtensibleTypeProvider typeProvider;

    /**
     * gets the extensible types in Kylo
     *
     * @return a list of extensible type descriptors
     */
    @Path("type")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<ExtensibleTypeDescriptor> getTypes() {
        return metadata.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, MetadataAccessControl.ACCESS_METADATA);
            
            List<ExtensibleType> list = this.typeProvider.getTypes();
            return new ArrayList<>(Collections2.transform(list, ExtensiblesModel.DOMAIN_TO_TYPE));
        });
    }

    /**
     * gets an extensible type descriptor by its' name or ID
     *
     * @param id the name or id of the desired extensible type descriptor
     * @return the extensible type descriptor
     */
    @Path("type/{nameOrId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ExtensibleTypeDescriptor getType(@PathParam("nameOrId") String id) {
        return metadata.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, MetadataAccessControl.ACCESS_METADATA);
            
            ExtensibleType.ID domainId = null;
            String name = null;
            ExtensibleType type = null;

            try {
                domainId = this.typeProvider.resolve(id);
            } catch (IllegalArgumentException e) {
                name = id;
            }

            if (domainId != null) {
                type = this.typeProvider.getType(domainId);
            } else {
                type = this.typeProvider.getType(name);
            }

            if (type != null) {
                return ExtensiblesModel.DOMAIN_TO_TYPE.apply(type);
            } else {
                throw new WebApplicationException("No type with the given ID exists: " + id, Response.SC_NOT_FOUND);
            }
        });
    }

    /**
     * delete the extensible type with the id given
     *
     * @param id the id of the type
     */
    @Path("type/{id}")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteType(@PathParam("id") String id) {
        metadata.commit(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, MetadataAccessControl.ADMIN_METADATA);
            
            ExtensibleType.ID domainId = this.typeProvider.resolve(id);

            return this.typeProvider.deleteType(domainId);
        });
    }

    /**
     * creates a new extensible type
     *
     * @param descr a model of the extensible type descriptor
     * @return the type that was persisted to the metadata store
     */
    @Path("type")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ExtensibleTypeDescriptor createType(ExtensibleTypeDescriptor descr) {
        return metadata.commit(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, MetadataAccessControl.ADMIN_METADATA);
            
            ExtensibleType type = ExtensiblesModel.createType(descr, this.typeProvider);

            return ExtensiblesModel.DOMAIN_TO_TYPE.apply(type);
        });
    }

    /**
     * an endpoint to allow updating of an extensible type
     *
     * @param id    the id of the extensible type
     * @param descr a descriptor with the type info
     * @return the extensible type as persisted to the metadata store
     */
    @Path("type/{id}")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ExtensibleTypeDescriptor updateType(@PathParam("id") String id,
                                               ExtensibleTypeDescriptor descr) {
        return metadata.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, MetadataAccessControl.ADMIN_METADATA);
            
            ExtensibleType.ID domainId = this.typeProvider.resolve(id);
            ExtensibleType type = ExtensiblesModel.updateType(descr, domainId, this.typeProvider);

            return ExtensiblesModel.DOMAIN_TO_TYPE.apply(type);
        });
    }

}
