package com.thinkbiganalytics.metadata.rest.api;

import com.google.common.collect.Collections2;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleType;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleTypeProvider;
import com.thinkbiganalytics.metadata.rest.ExtensiblesModel;
import com.thinkbiganalytics.metadata.rest.model.extension.ExtensibleTypeDescriptor;

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

@Component
@Path("/metadata/extension")
@Api(tags = "Internal: Debugging", produces = "application/json", description = "Allow creation and storing of new Entities with dynamic types ")
public class ExtensionsController {

    @Inject
    private MetadataAccess metadata;

    @Inject
    private ExtensibleTypeProvider typeProvider;

    @Path("type")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<ExtensibleTypeDescriptor> getTypes() {
        return metadata.read(() -> {
            List<ExtensibleType> list = this.typeProvider.getTypes();
            return new ArrayList<>(Collections2.transform(list, ExtensiblesModel.DOMAIN_TO_TYPE));
        });
    }

    @Path("type/{nameOrId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ExtensibleTypeDescriptor getType(@PathParam("nameOrId") String id) {
        return metadata.read(() -> {
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

    @Path("type/{id}")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteType(@PathParam("id") String id) {
        metadata.commit(() -> {
            ExtensibleType.ID domainId = this.typeProvider.resolve(id);

            return this.typeProvider.deleteType(domainId);
        });
    }

    @Path("type")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ExtensibleTypeDescriptor createType(ExtensibleTypeDescriptor descr) {
        return metadata.commit(() -> {
            ExtensibleType type = ExtensiblesModel.createType(descr, this.typeProvider);

            return ExtensiblesModel.DOMAIN_TO_TYPE.apply(type);
        });
    }

    @Path("type/{id}")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ExtensibleTypeDescriptor updateType(@PathParam("id") String id,
                                               ExtensibleTypeDescriptor descr) {
        return metadata.read(() -> {
            ExtensibleType.ID domainId = this.typeProvider.resolve(id);
            ExtensibleType type =  ExtensiblesModel.updateType(descr, domainId, this.typeProvider);

            return ExtensiblesModel.DOMAIN_TO_TYPE.apply(type);
        });
    }

}
