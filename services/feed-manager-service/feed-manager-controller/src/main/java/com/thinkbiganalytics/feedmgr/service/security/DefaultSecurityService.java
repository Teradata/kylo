/**
 * 
 */
package com.thinkbiganalytics.feedmgr.service.security;

import java.security.Principal;
import java.security.acl.Group;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import com.thinkbiganalytics.feedmgr.security.FeedServicesAccessControl;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.category.CategoryProvider;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.api.template.FeedManagerTemplateProvider;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.security.action.Action;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.action.AllowedEntityActionsProvider;
import com.thinkbiganalytics.security.rest.controller.ActionsModelTransform;
import com.thinkbiganalytics.security.rest.model.ActionGroup;
import com.thinkbiganalytics.security.rest.model.PermissionsChange;
import com.thinkbiganalytics.security.rest.model.PermissionsChange.ChangeType;

/**
 *
 */
public class DefaultSecurityService implements SecurityService {

    @Inject
    CategoryProvider categoryProvider;

    @Inject
    FeedManagerTemplateProvider templateProvider;

    @Inject
    FeedProvider feedProvider;

    @Inject
    private AllowedEntityActionsProvider actionsProvider;

    @Inject
    private MetadataAccess metadata;

    @Inject
    private ActionsModelTransform actionsTransform;

    @Inject
    private AccessController accessController;

    @Override
    public Optional<ActionGroup> getAvailableFeedActions(String id) {
        return getAvailableActions(() -> {
            return accessFeed(id).flatMap(f -> actionsProvider.getAvailableActions(AllowedActions.FEED));
        });
    }
    
    @Override
    public Optional<ActionGroup> getAllowedFeedActions(String id, Set<Principal> principals) {
        return getAllowedActions(principals, supplyFeedActions(id));
    }
    
    @Override
    public Optional<ActionGroup> changeFeedPermissions(String id, PermissionsChange changes) {
        return changePermissions(changes, supplyFeedActions(id));
    }
    
    @Override
    public Optional<ActionGroup> getAvailableCategoryActions(String id) {
        return getAvailableActions(() -> {
            return accessCategory(id).flatMap(c -> actionsProvider.getAvailableActions(AllowedActions.CATEGORY));
        });
    }
    
    @Override
    public Optional<ActionGroup> getAllowedCategoryActions(String id, Set<Principal> principals) {
        return getAllowedActions(principals, supplyCategoryActions(id));
    }

    @Override
    public Optional<ActionGroup> changeCategoryPermissions(String id, PermissionsChange changes) {
        return changePermissions(changes, supplyCategoryActions(id));
    }

    @Override
    public Optional<ActionGroup> getAvailableTemplateActions(String id) {
        return getAvailableActions(() -> {
            return accessTemplate(id).flatMap(t -> actionsProvider.getAvailableActions(AllowedActions.TEMPLATE));
        });
    }

    @Override
    public Optional<ActionGroup> getAllowedTemplateActions(String id, Set<Principal> principals) {
        return getAllowedActions(principals, supplyTemplateActions(id));
    }

    @Override
    public Optional<ActionGroup> changeTemplatePermissions(String id, PermissionsChange changes) {
        return changePermissions(changes, supplyTemplateActions(id));
    }

    
    @Override
    public Optional<PermissionsChange> createFeedPermissionChange(String id, ChangeType changeType, Set<Principal> members) {
        return createPermissionChange(id, changeType, members, supplyFeedActions(id));
    }

    @Override
    public Optional<PermissionsChange> createCategoryPermissionChange(String id, ChangeType changeType, Set<Principal> members) {
        return createPermissionChange(id, changeType, members, supplyCategoryActions(id));
    }

    @Override
    public Optional<PermissionsChange> createTemplatePermissionChange(String id, ChangeType changeType, Set<Principal> members) {
        return createPermissionChange(id, changeType, members, supplyTemplateActions(id));
    }

    private Optional<Feed> accessFeed(String id) {
        this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_FEEDS);
    
        Feed.ID feedId = feedProvider.resolveFeed(id);
        Feed feed = feedProvider.getFeed(feedId);
    
        return Optional.ofNullable(feed);    
    }

    private Optional<Category> accessCategory(String id) {
        this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_CATEGORIES);
        
        Category.ID catId = categoryProvider.resolveId(id);
        Category category = categoryProvider.findById(catId);
        
        return Optional.ofNullable(category);
    }

    private Optional<FeedManagerTemplate> accessTemplate(String id) {
        this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_TEMPLATES);
        
        FeedManagerTemplate.ID catId = templateProvider.resolveId(id);
        FeedManagerTemplate template = templateProvider.findById(catId);
    
        return Optional.ofNullable(template);
    }

    private Supplier<Optional<AllowedActions>> supplyFeedActions(String id) {
        return () -> {
            return accessFeed(id).map(Feed::getAllowedActions);
        };
    }
    
    private Supplier<Optional<AllowedActions>> supplyCategoryActions(String id) {
        return () -> {
            return accessCategory(id).map(Category::getAllowedActions);
        };
    }
    
    private Supplier<Optional<AllowedActions>> supplyTemplateActions(String id) {
        return () -> {
            return accessTemplate(id).map(FeedManagerTemplate::getAllowedActions);
        };
    }

    private Optional<ActionGroup> changePermissions(PermissionsChange changes, Supplier<Optional<AllowedActions>> allowedSupplier) {
        Set<Action> actionSet = this.actionsTransform.collectActions(changes);
        Set<Principal> principals = this.actionsTransform.collectPrincipals(changes);
        
        metadata.commit(() -> {
            allowedSupplier.get().ifPresent(allowed -> { 
                principals.stream().forEach(principal -> {
                    switch (changes.getChange()) {
                        case ADD:
                            allowed.enable(principal, actionSet);
                            break;
                        case REMOVE:
                            allowed.disable(principal, actionSet);
                            break;
                        default:
                            allowed.enableOnly(principal, actionSet);
                    }
                });
            });
        });
        
        // TODO: look into maybe showing the actions even if the principals no longer have access to the entity
        // For now just returning action as seen by the user changing the permissions.
        return metadata.read(() -> getAllowedActions(principals, allowedSupplier));
    }

    private Optional<ActionGroup> getAllowedActions(Set<Principal> principals, Supplier<Optional<AllowedActions>> allowedSupplier) {
        return this.metadata.read(() -> {
            return allowedSupplier.get().map(allowed -> this.actionsTransform.allowedActionsToActionSet(null).apply(allowed));
        }, principals.stream().toArray(s -> new Principal[s]));
    }

    private Optional<ActionGroup> getAvailableActions(Supplier<Optional<AllowedActions>> allowedSupplier) {
        return this.metadata.read(() -> {
            return Optional.of(actionsProvider.getAvailableActions(AllowedActions.TEMPLATE)
                                .map(this.actionsTransform.allowedActionsToActionSet(AllowedActions.TEMPLATE))
                                .orElseThrow(() -> new WebApplicationException("The available actions were not found",
                                                                               Status.NOT_FOUND)));
        });
    }

    private Optional<PermissionsChange> createPermissionChange(String id, ChangeType changeType, Set<Principal> members, Supplier<Optional<AllowedActions>> allowedSupplier) {
        Set<String> groupNames = extractGroupNames(members);
        Set<String> userNames = extractUserNames(members);
        
        return this.metadata.read(() -> {
            return supplyTemplateActions(id).get().map(actionsTransform.availableActionsToPermissionsChange(changeType, null, userNames, groupNames));
        });
    }

    private Set<String> extractUserNames(Set<Principal> members) {
        return members.stream()
                        .filter(p -> ! (p instanceof Group))
                        .map(p -> p.getName())
                        .collect(Collectors.toSet());
    }

    private Set<String> extractGroupNames(Set<Principal> members) {
        return members.stream()
                        .filter(p -> p instanceof Group)
                        .map(p -> p.getName())
                        .collect(Collectors.toSet());
    }

}
