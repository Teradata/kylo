/**
 * 
 */
package com.thinkbiganalytics.security.rest.controller;

import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.springframework.stereotype.Component;

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.security.RolePrincipal;
import com.thinkbiganalytics.security.UsernamePrincipal;
import com.thinkbiganalytics.security.action.Action;
import com.thinkbiganalytics.security.action.AllowedModuleActionsProvider;
import com.thinkbiganalytics.security.rest.model.ActionSet;
import com.thinkbiganalytics.security.rest.model.PermissionsChange;
import com.thinkbiganalytics.security.rest.model.PermissionsChange.ChangeType;

import io.swagger.annotations.Api;

/**
 *
 * @author Sean Felten
 */
@Component
@Api(value = "security-acesscontrol", produces = "application/json", description = "Obtain and manage access control information for users and groups")
@Path("/v1/security/actions")
public class AccessController {

    @Inject
    private MetadataAccess metadata;
    
    @Inject
    private AllowedModuleActionsProvider actionsProvider;
    
    @Inject
    @Named("actionsModelTransform")
    private ActionsModelTransform actionsTransform;
    
    @GET
    @Path("{name}/available")
    @Produces(MediaType.APPLICATION_JSON)
    public ActionSet getAvailableActions(@PathParam("name") String groupName) {
        return metadata.read(() -> {
            return actionsProvider.getAvailavleActions("services")
                            .map(this.actionsTransform.availableActionsToActionSet("services"))
                            .orElseThrow(() -> new WebApplicationException("The available service actions were not found", Status.NOT_FOUND));
        });
    }
    
    @GET
    @Path("{name}/allowed")
    @Produces(MediaType.APPLICATION_JSON)
    public ActionSet getAllowedActions(@PathParam("name") String groupName) {
        return metadata.read(() -> {
            return actionsProvider.getAllowedActions("services")
                            .map(this.actionsTransform.availableActionsToActionSet("services"))
                            .orElseThrow(() -> new WebApplicationException("The available service actions were not found", Status.NOT_FOUND));
        });
    }
    
    @POST
    @Path("{name}/allowed")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ActionSet putAvailableServicesActions(@PathParam("name") String groupName,
                                                 PermissionsChange changes) {
        Set<Action> actionSet = collectActions(changes);
        Set<Principal> principals = collectPrincipals(changes);
        
        metadata.commit(() -> {
            principals.stream().forEach(principal -> {
                actionsProvider.getAvailavleActions("services").ifPresent(available -> available.enableOnly(principal, actionSet));
            });
            return null;
        });
        
        return getAvailableActions(groupName);
    }
    
    
    @GET
    @Path("permschange/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public PermissionsChange permissionsChange(@PathParam("name") String name,
                                               @QueryParam("type") ChangeType changeType,
                                               @QueryParam("users") Set<String> users,
                                               @QueryParam("roles") Set<String> roles) {
        return metadata.read(() -> {
            PermissionsChange change = new PermissionsChange(changeType, name);
            actionsProvider.getAvailavleActions(name)
                .ifPresent(allowed -> actionsTransform.addAction(change, allowed.getAvailableActions()));
            return change;
        });
    }


    private Set<Principal> collectPrincipals(PermissionsChange changes) {
        Set<Principal> set = new HashSet<>();
        
        for (String user : changes.getUsers()) {
            set.add(new UsernamePrincipal(user));
        }
        for (String group : changes.getGroups()) {
            set.add(new RolePrincipal(group));
        }
        
        return set;
    }

    private Set<Action> collectActions(PermissionsChange changes) {
        Set<Action> set = new HashSet<>();
        
        for (com.thinkbiganalytics.security.rest.model.Action modelAction : changes.getActions().getActions()) {
            loadActionSet(modelAction, Action.create(modelAction.getSystemName()), set);
        }
        
        return set;
    }

    private void loadActionSet(com.thinkbiganalytics.security.rest.model.Action modelAction, Action action, Set<Action> set) {
        if (modelAction.getActions().isEmpty()) {
            set.add(action);
        } else {
            for (com.thinkbiganalytics.security.rest.model.Action modelChild : modelAction.getActions()) {
                loadActionSet(modelChild, action.subAction(modelChild.getSystemName()), set);
            }
        }
    }

}
