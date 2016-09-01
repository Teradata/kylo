/**
 * 
 */
package com.thinkbiganalytics.security.rest.controller;

import java.security.Principal;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

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

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.security.action.Action;
import com.thinkbiganalytics.security.action.AllowedActions;
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
public class AccessControlController {

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
    public ActionSet getAvailableActions(@PathParam("name") String moduleName) {
        return metadata.read(() -> {
            return actionsProvider.getAvailableActions(moduleName)
                            .map(this.actionsTransform.availableActionsToActionSet("services"))
                            .orElseThrow(() -> new WebApplicationException("The available service actions were not found", Status.NOT_FOUND));
        });
    }
    
    @GET
    @Path("{name}/allowed")
    @Produces(MediaType.APPLICATION_JSON)
    public ActionSet getAllowedActions(@PathParam("name") String moduleName,
                                       @QueryParam("user") Set<String> userNames,
                                       @QueryParam("group") Set<String> groupNames) {
        return metadata.read(() -> {
            Set<Principal> users = this.actionsTransform.toUserPrincipals(userNames);
            Set<Principal> roles = this.actionsTransform.toGroupPrincipals(userNames);
            return actionsProvider.getAllowedActions(moduleName)
                            .map(this.actionsTransform.availableActionsToActionSet("services"))
                            .orElseThrow(() -> new WebApplicationException("The available service actions were not found", Status.NOT_FOUND));
        });
    }
    
    @POST
    @Path("{name}/allowed")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ActionSet postPermissionsChange(@PathParam("name") String moduleName,
                                           PermissionsChange changes) {
        Set<Action> actionSet = collectActions(changes);
        Set<Principal> principals = collectPrincipals(changes);
        final Consumer<Principal> permChange;
        
        switch(changes.getChange()) {
            case ADD:
                permChange = (principal -> {
                    actionsProvider.getAllowedActions(moduleName).ifPresent(allowed -> allowed.enable(principal, actionSet));
                });
                break;
            case REMOVE:
                permChange = (principal -> {
                    actionsProvider.getAllowedActions(moduleName).ifPresent(allowed -> allowed.disable(principal, actionSet));
                });
                break;
            default:
                permChange = (principal -> {
                    actionsProvider.getAllowedActions(moduleName).ifPresent(allowed -> allowed.enableOnly(principal, actionSet));
                });
        }
        
        metadata.commit(() -> {
            principals.stream().forEach(permChange);
            return null;
        });
        
        return getAllowedActions(moduleName, changes.getUsers(), changes.getGroups());
    }
    
    
    @GET
    @Path("{name}/change/allowed")
    @Produces(MediaType.APPLICATION_JSON)
    public PermissionsChange getAllowedPermissionsChange(@PathParam("name") String moduleName,
                                                         @QueryParam("type") String changeType,
                                                         @QueryParam("user") Set<String> users,
                                                         @QueryParam("group") Set<String> groups) {
        if (StringUtils.isBlank(changeType)) {
            throw new WebApplicationException("The query parameter \"type\" is required", Status.BAD_REQUEST);
        }
        
        return metadata.read(() -> {
            return actionsProvider.getAvailableActions(moduleName)
                            .map(this.actionsTransform.availableActionsToPermissionsChange(ChangeType.valueOf(changeType.toUpperCase()), moduleName, users, groups))
                            .orElseThrow(() -> new WebApplicationException("The available service actions were not found", Status.NOT_FOUND));
        });
    }


    private Set<Principal> collectPrincipals(PermissionsChange changes) {
        Set<Principal> set = new HashSet<>();
        
        set.addAll(this.actionsTransform.toUserPrincipals(changes.getUsers()));
        set.addAll(this.actionsTransform.toGroupPrincipals(changes.getGroups()));
        
        return set;
    }

    /**
     * Creates a set of domain actions from the REST model actions.  The resulting set will
     * contain only the leaf actions from domain action hierarchy.
     */
    private Set<Action> collectActions(PermissionsChange changes) {
        Set<Action> set = new HashSet<>();
        
        for (com.thinkbiganalytics.security.rest.model.Action modelAction : changes.getActionSet().getActions()) {
            loadActionSet(modelAction, Action.create(modelAction.getSystemName()), set);
        }
        
        return set;
    }

    /**
     * Adds an new domain action to the set if the REST model action represents a leaf of the action hierarchy.  
     * Otherwise, it loads the child actions recursively.
     */
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
