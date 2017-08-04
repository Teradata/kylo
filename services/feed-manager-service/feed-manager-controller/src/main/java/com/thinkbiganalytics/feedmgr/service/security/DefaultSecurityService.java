package com.thinkbiganalytics.feedmgr.service.security;

/*-
 * #%L
 * kylo-feed-manager-controller
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

import com.thinkbiganalytics.feedmgr.security.FeedServicesAccessControl;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.category.CategoryProvider;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.api.datasource.UserDatasource;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.security.AccessControlled;
import com.thinkbiganalytics.metadata.api.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.api.template.FeedManagerTemplateProvider;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.security.action.Action;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.action.AllowedEntityActionsProvider;
import com.thinkbiganalytics.security.rest.controller.SecurityModelTransform;
import com.thinkbiganalytics.security.rest.model.ActionGroup;
import com.thinkbiganalytics.security.rest.model.PermissionsChange;
import com.thinkbiganalytics.security.rest.model.PermissionsChange.ChangeType;
import com.thinkbiganalytics.security.rest.model.RoleMembership;
import com.thinkbiganalytics.security.rest.model.RoleMembershipChange;
import com.thinkbiganalytics.security.rest.model.RoleMemberships;

import java.security.Principal;
import java.security.acl.Group;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

/**
 * Default service implementation for changing the permissions and role memberships of 
 * various metadata entities.
 * 
 */
//=====================
// TODO!! Currently all permission and role changing methods are synchronized as a temporary
// fix for KYLO-518.  The synchronization should be removed when that issue is corrected
// in a more appropriate way.
//=====================
public class DefaultSecurityService implements SecurityService {
    
    private static final Supplier<Optional<Set<com.thinkbiganalytics.metadata.api.security.RoleMembership>>> EMPTY_SUPPLIER = () -> Optional.empty();

    @Inject
    CategoryProvider categoryProvider;

    @Inject
    FeedManagerTemplateProvider templateProvider;

    @Inject
    FeedProvider feedProvider;

    @Inject
    private DatasourceProvider datasourceProvider;

    @Inject
    private AllowedEntityActionsProvider actionsProvider;

    @Inject
    private MetadataAccess metadata;

    @Inject
    private SecurityModelTransform securityTransform;

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
    public synchronized Optional<ActionGroup> changeFeedPermissions(String id, PermissionsChange changes) {
        return changePermissions(changes, supplyFeedActions(id));
    }

    @Override
    public Optional<RoleMemberships> getFeedRoleMemberships(String id) {
        return getRoleMemberships(supplyInheritedFeedRoleMemberships(id),
                                  supplyFeedRoleMemberships(id));
    }

    @Override
    public synchronized Optional<RoleMembership> changeFeedRoleMemberships(String id, RoleMembershipChange change) {
        return changeRoleMemberships(change, supplyFeedRoleMembership(id, change.getRoleName()));
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
    public synchronized Optional<ActionGroup> changeCategoryPermissions(String id, PermissionsChange changes) {
        return changePermissions(changes, supplyCategoryActions(id));
    }

    @Override
    public Optional<RoleMemberships> getCategoryRoleMemberships(String id) {
        return getRoleMemberships(supplyCategoryRoleMemberships(id));
    }

    @Override
    public synchronized Optional<RoleMembership> changeCategoryRoleMemberships(String id, RoleMembershipChange change) {
        return changeRoleMemberships(change, supplyCategoryRoleMembership(id, change.getRoleName()));
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.feedmgr.service.security.SecurityService#getCategoryFeedRoleMemberships(java.lang.String)
     */
    @Override
    public Optional<RoleMemberships> getCategoryFeedRoleMemberships(String id) {
        return getRoleMemberships(supplyCategoryFeedRoleMemberships(id));
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.feedmgr.service.security.SecurityService#changeCategoryFeedRoleMemberships(java.lang.String, com.thinkbiganalytics.security.rest.model.RoleMembershipChange)
     */
    @Override
    public Optional<RoleMembership> changeCategoryFeedRoleMemberships(String id, RoleMembershipChange change) {
        return changeRoleMemberships(change, supplyCategoryFeedRoleMembership(id, change.getRoleName()));
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
    public synchronized Optional<ActionGroup> changeTemplatePermissions(String id, PermissionsChange changes) {
        return changePermissions(changes, supplyTemplateActions(id));
    }

    @Override
    public Optional<RoleMemberships> getTemplateRoleMemberships(String id) {
        return getRoleMemberships(supplyTemplateRoleMemberships(id));
    }

    @Override
    public synchronized Optional<RoleMembership> changeTemplateRoleMemberships(String id, RoleMembershipChange change) {
        return changeRoleMemberships(change, supplyTemplateRoleMembership(id, change.getRoleName()));
    }

    @Override
    public Optional<ActionGroup> getAvailableDatasourceActions(String id) {
        return getAvailableActions(() -> accessDatasource(id).flatMap(c -> actionsProvider.getAvailableActions(AllowedActions.DATASOURCE)));
    }

    @Override
    public Optional<ActionGroup> getAllowedDatasourceActions(String id, Set<Principal> principals) {
        return getAllowedActions(principals, supplyDatasourceActions(id));
    }

    @Override
    public synchronized Optional<ActionGroup> changeDatasourcePermissions(String id, PermissionsChange changes) {
        return changePermissions(changes, supplyDatasourceActions(id));
    }

    @Override
    public Optional<RoleMemberships> getDatasourceRoleMemberships(String id) {
        return getRoleMemberships(supplyDatasourceRoleMemberships(id));
    }

    @Override
    public synchronized Optional<RoleMembership> changeDatasourceRoleMemberships(String id, RoleMembershipChange change) {
        return changeRoleMemberships(change, supplyDatasourceRoleMembership(id, change.getRoleName()));
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

    @Override
    public Optional<PermissionsChange> createDatasourcePermissionChange(String id, ChangeType changeType, Set<Principal> members) {
        return createPermissionChange(id, changeType, members, supplyDatasourceActions(id));
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

        FeedManagerTemplate.ID templateId = templateProvider.resolveId(id);
        FeedManagerTemplate template = templateProvider.findById(templateId);

        return Optional.ofNullable(template);
    }

    /**
     * Retrieves the data source with the specified id.
     *
     * @param id the data source id
     * @return the data source, if found
     */
    @Nonnull
    private Optional<UserDatasource> accessDatasource(@Nonnull final String id) {
        accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_DATASOURCES);
        return Optional.of(id)
            .map(datasourceProvider::resolve)
            .map(datasourceProvider::getDatasource)
            .map(datasource -> (datasource instanceof UserDatasource) ? (UserDatasource) datasource : null);
    }

    private Supplier<Optional<AllowedActions>> supplyFeedActions(String id) {
        return () -> {
            return accessFeed(id).map(Feed::getAllowedActions);
        };
    }

    private Supplier<Optional<Set<com.thinkbiganalytics.metadata.api.security.RoleMembership>>> supplyFeedRoleMemberships(String id) {
        return () -> {
            return accessFeed(id).map(Feed::getRoleMemberships);
        };
    }
    
    private Supplier<Optional<Set<com.thinkbiganalytics.metadata.api.security.RoleMembership>>> supplyInheritedFeedRoleMemberships(String id) {
        return () -> {
            return accessFeed(id).map(Feed::getInheritedRoleMemberships);
        };
    }

    private Supplier<Optional<com.thinkbiganalytics.metadata.api.security.RoleMembership>> supplyFeedRoleMembership(String id, String roleName) {
        return () -> {
            return accessFeed(id).flatMap(f -> f.getRoleMembership(roleName));
        };
    }

    private Supplier<Optional<AllowedActions>> supplyCategoryActions(String id) {
        return () -> {
            return accessCategory(id).map(Category::getAllowedActions);
        };
    }

    private Supplier<Optional<Set<com.thinkbiganalytics.metadata.api.security.RoleMembership>>> supplyCategoryRoleMemberships(String id) {
        return () -> {
            return accessCategory(id).map(Category::getRoleMemberships);
        };
    }

    private Supplier<Optional<com.thinkbiganalytics.metadata.api.security.RoleMembership>> supplyCategoryRoleMembership(String id, String roleName) {
        return () -> {
            return accessCategory(id).flatMap(c -> c.getRoleMembership(roleName));
        };
    }
    
    private Supplier<Optional<Set<com.thinkbiganalytics.metadata.api.security.RoleMembership>>> supplyCategoryFeedRoleMemberships(String id) {
        return () -> {
            return accessCategory(id).map(Category::getFeedRoleMemberships);
        };
    }
    
    private Supplier<Optional<com.thinkbiganalytics.metadata.api.security.RoleMembership>> supplyCategoryFeedRoleMembership(String id, String roleName) {
        return () -> {
            return accessCategory(id).flatMap(c -> c.getFeedRoleMembership(roleName));
        };
    }

    private Supplier<Optional<AllowedActions>> supplyTemplateActions(String id) {
        return () -> {
            return accessTemplate(id).map(FeedManagerTemplate::getAllowedActions);
        };
    }

    private Supplier<Optional<Set<com.thinkbiganalytics.metadata.api.security.RoleMembership>>> supplyTemplateRoleMemberships(String id) {
        return () -> {
            return accessTemplate(id).map(FeedManagerTemplate::getRoleMemberships);
        };
    }

    private Supplier<Optional<com.thinkbiganalytics.metadata.api.security.RoleMembership>> supplyTemplateRoleMembership(String id, String roleName) {
        return () -> {
            return accessTemplate(id).flatMap(t -> t.getRoleMembership(roleName));
        };
    }

    private Supplier<Optional<AllowedActions>> supplyDatasourceActions(String id) {
        return () -> accessDatasource(id).map(AccessControlled::getAllowedActions);
    }

    private Supplier<Optional<Set<com.thinkbiganalytics.metadata.api.security.RoleMembership>>> supplyDatasourceRoleMemberships(String id) {
        return () -> accessDatasource(id).map(AccessControlled::getRoleMemberships);
    }

    private Supplier<Optional<com.thinkbiganalytics.metadata.api.security.RoleMembership>> supplyDatasourceRoleMembership(String id, String roleName) {
        return () -> accessDatasource(id).flatMap(t -> t.getRoleMembership(roleName));
    }

    private Optional<ActionGroup> changePermissions(PermissionsChange changes, Supplier<Optional<AllowedActions>> allowedSupplier) {
        Set<Action> actionSet = this.securityTransform.collectActions(changes);
        Set<Principal> principals = this.securityTransform.collectPrincipals(changes);

        metadata.commit(() -> {
            allowedSupplier.get().ifPresent(allowed -> {
                principals.forEach(principal -> {
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
            return allowedSupplier.get().map(allowed -> this.securityTransform.toActionGroup(null).apply(allowed));
        }, principals.stream().toArray(Principal[]::new));
    }

    private Optional<ActionGroup> getAvailableActions(Supplier<Optional<AllowedActions>> allowedSupplier) {
        return this.metadata.read(() -> {
            return Optional.of(actionsProvider.getAvailableActions(AllowedActions.TEMPLATE)
                                   .map(this.securityTransform.toActionGroup(AllowedActions.TEMPLATE))
                                   .orElseThrow(() -> new WebApplicationException("The available actions were not found",
                                                                                  Status.NOT_FOUND)));
        });
    }
    
    private Optional<RoleMemberships> getRoleMemberships(Supplier<Optional<Set<com.thinkbiganalytics.metadata.api.security.RoleMembership>>> assignedSupplier) {
        return getRoleMemberships(EMPTY_SUPPLIER, assignedSupplier);
    }

    private Optional<RoleMemberships> getRoleMemberships(Supplier<Optional<Set<com.thinkbiganalytics.metadata.api.security.RoleMembership>>> inheritedSupplier,
                                                         Supplier<Optional<Set<com.thinkbiganalytics.metadata.api.security.RoleMembership>>> assignedSupplier) {
        return this.metadata.read(() -> {
            Optional<Map<String, RoleMembership>> inheritedMap = inheritedSupplier.get()
                            .map(members -> members.stream()
                                .collect(Collectors.toMap(m -> m.getRole().getSystemName(),
                                                          securityTransform.toRoleMembership())));
            Optional<Map<String, RoleMembership>> assignedMap = assignedSupplier.get()
                            .map(members -> members.stream()
                                 .collect(Collectors.toMap(m -> m.getRole().getSystemName(),
                                                           securityTransform.toRoleMembership())));
            if (assignedMap.isPresent()) {
                return Optional.of(new RoleMemberships(inheritedMap.orElse(null), assignedMap.get()));
            } else {
                return Optional.empty();
            }
        });
    }

    private Optional<RoleMembership> changeRoleMemberships(RoleMembershipChange change, Supplier<Optional<com.thinkbiganalytics.metadata.api.security.RoleMembership>> domainSupplier) {
        return this.metadata.commit(() -> {
            return domainSupplier.get().map(domain -> {
                switch (change.getChange()) {
                    case ADD:
                        Arrays.stream(securityTransform.asUserPrincipals(change.getUsers())).forEach(p -> domain.addMember(p));
                        Arrays.stream(securityTransform.asGroupPrincipals(change.getGroups())).forEach(p -> domain.addMember(p));
                        break;
                    case REMOVE:
                        Arrays.stream(securityTransform.asUserPrincipals(change.getUsers())).forEach(p -> domain.removeMember(p));
                        Arrays.stream(securityTransform.asGroupPrincipals(change.getGroups())).forEach(p -> domain.removeMember(p));
                        break;
                    case REPLACE:
                        domain.setMemebers(securityTransform.asUserPrincipals(change.getUsers()));
                        domain.setMemebers(securityTransform.asGroupPrincipals(change.getGroups()));
                        break;
                    default:
                        break;
                }

                return securityTransform.toRoleMembership().apply(domain);
            });
        });
    }

    private Optional<PermissionsChange> createPermissionChange(String id, ChangeType changeType, Set<Principal> members, Supplier<Optional<AllowedActions>> allowedSupplier) {
        Set<String> groupNames = extractGroupNames(members);
        Set<String> userNames = extractUserNames(members);

        return this.metadata.read(() -> {
            return supplyTemplateActions(id).get().map(securityTransform.toPermissionsChange(changeType, null, userNames, groupNames));
        });
    }

    private Set<String> extractUserNames(Set<Principal> members) {
        return members.stream()
            .filter(p -> !(p instanceof Group))
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
