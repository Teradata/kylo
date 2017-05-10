package com.thinkbiganalytics.metadata.modeshape.feed.security;

/*-
 * #%L
 * kylo-metadata-modeshape
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

import com.thinkbiganalytics.metadata.api.category.security.CategoryAccessControl;
import com.thinkbiganalytics.metadata.api.feed.security.FeedAccessControl;
import com.thinkbiganalytics.metadata.api.feed.security.FeedOpsAccessControlProvider;
import com.thinkbiganalytics.metadata.api.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.api.template.security.TemplateAccessControl;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.feed.FeedData;
import com.thinkbiganalytics.metadata.modeshape.feed.FeedDetails;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeed;
import com.thinkbiganalytics.metadata.modeshape.security.JcrAccessControlUtil;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowableAction;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActions;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.security.action.Action;
import com.thinkbiganalytics.security.action.AllowedActions;

import java.security.Principal;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import javax.jcr.Node;
import javax.jcr.security.Privilege;


/**
 * A type of allowed actions that applies to feeds.  It intercepts certain action enable/disable
 * calls related to visibility to update the underlying JCR node structure's ACL lists.
 */
public class JcrFeedAllowedActions extends JcrAllowedActions {

    private JcrFeed feed;

    public JcrFeedAllowedActions(Node allowedActionsNode) {
        super(allowedActionsNode);
        this.feed = JcrUtil.getJcrObject(JcrUtil.getParent(allowedActionsNode), JcrFeed.class);
    }

    public JcrFeedAllowedActions(Node allowedActionsNode, FeedOpsAccessControlProvider opsAccessProvider) {
        super(allowedActionsNode);
        this.feed = JcrUtil.getJcrObject(JcrUtil.getParent(allowedActionsNode), JcrFeed.class, opsAccessProvider);
    }

    @Override
    public boolean enable(Principal principal, Set<Action> actions) {
        enableEntityAccess(principal, actions.stream());
        return super.enable(principal, actions);
    }

    @Override
    public boolean enableOnly(Principal principal, Set<Action> actions) {
        enableOnlyEntityAccess(principal, actions.stream());
        return super.enableOnly(principal, actions);
    }

    @Override
    public boolean enableOnly(Principal principal, AllowedActions actions) {
        enableOnlyEntityAccess(principal, actions.getAvailableActions().stream());
        return super.enableOnly(principal, actions);
    }

    @Override
    public boolean disable(Principal principal, Set<Action> actions) {
        disableEntityAccess(principal, actions.stream());
        return super.disable(principal, actions);
    }

    @Override
    public boolean disable(Principal principal, AllowedActions actions) {
        disableEntityAccess(principal, actions.getAvailableActions().stream());
        return super.disable(principal, actions);
    }

    @Override
    public void setupAccessControl(Principal owner) {
        enable(owner, FeedAccessControl.EDIT_DETAILS);
        enable(owner, FeedAccessControl.ACCESS_OPS);
        enable(JcrMetadataAccess.ADMIN, FeedAccessControl.EDIT_DETAILS);
        enable(JcrMetadataAccess.ADMIN, FeedAccessControl.ACCESS_OPS);

        super.setupAccessControl(owner);
    }
    
    @Override
    public void removeAccessControl(Principal owner) {
        super.removeAccessControl(owner);
        
        this.feed.getFeedDetails().ifPresent(d -> JcrAccessControlUtil.clearHierarchyPermissions(d.getNode(), feed.getNode()));
        this.feed.getFeedData().ifPresent(d -> JcrAccessControlUtil.clearHierarchyPermissions(d.getNode(), feed.getNode()));
    }

    protected void enableEntityAccess(Principal principal, Stream<? extends Action> actions) {
        actions.forEach(action -> {
            if (action.implies(FeedAccessControl.ACCESS_OPS)) {
                this.feed.getOpsAccessProvider().ifPresent(provider -> provider.grantAccess(feed.getId(), principal));
            } else if (action.implies(FeedAccessControl.CHANGE_PERMS)) {
                //When Change Perms comes through the user needs write access to the allowed actions tree to grant additional access
                final Node allowedActionsNode = ((JcrAllowedActions) this.feed.getAllowedActions()).getNode();
                JcrAccessControlUtil.addRecursivePermissions(allowedActionsNode, JcrAllowableAction.NODE_TYPE, principal, Privilege.JCR_ALL);
            } else if (action.implies(FeedAccessControl.EDIT_DETAILS)) {
                //also add read to the category summary
                final AllowedActions categoryAllowedActions = feed.getCategory().getAllowedActions();
                if (categoryAllowedActions.hasPermission(CategoryAccessControl.CHANGE_PERMS)) {
                    categoryAllowedActions.enable(principal, CategoryAccessControl.ACCESS_DETAILS);
                }
                //If a user has Edit access for the feed, they need to be able to also Read the template
                this.feed.getFeedDetails()
                    .map(FeedDetails::getTemplate)
                    .map(FeedManagerTemplate::getAllowedActions)
                    .filter(allowedActions -> allowedActions.hasPermission(TemplateAccessControl.CHANGE_PERMS))
                    .ifPresent(allowedActions -> allowedActions.enable(principal, TemplateAccessControl.ACCESS_TEMPLATE));
                this.feed.getFeedDetails().ifPresent(d -> JcrAccessControlUtil.addHierarchyPermissions(d.getNode(), principal, feed.getNode(), Privilege.JCR_ALL, Privilege.JCR_READ));
                this.feed.getFeedData().ifPresent(d -> JcrAccessControlUtil.addHierarchyPermissions(d.getNode(), principal, feed.getNode(), Privilege.JCR_ALL, Privilege.JCR_READ));
            } else if (action.implies(FeedAccessControl.EDIT_SUMMARY)) {
                //also add read to the category summary
                final AllowedActions categoryAllowedActions = feed.getCategory().getAllowedActions();
                if (categoryAllowedActions.hasPermission(CategoryAccessControl.CHANGE_PERMS)) {
                    categoryAllowedActions.enable(principal, CategoryAccessControl.ACCESS_CATEGORY);
                }
                this.feed.getFeedSummary().ifPresent(s -> JcrAccessControlUtil.addHierarchyPermissions(s.getNode(), principal, feed.getNode(), Privilege.JCR_ALL, Privilege.JCR_READ));
            } else if (action.implies(FeedAccessControl.ACCESS_DETAILS)) {
                //also add read to the category summary
                final AllowedActions categoryAllowedActions = feed.getCategory().getAllowedActions();
                if (categoryAllowedActions.hasPermission(CategoryAccessControl.CHANGE_PERMS)) {
                    categoryAllowedActions.enable(principal, CategoryAccessControl.ACCESS_DETAILS);
                }
                //If a user has Read access for the feed, they need to be able to also Read the template
                this.feed.getFeedDetails()
                    .map(FeedDetails::getTemplate)
                    .map(FeedManagerTemplate::getAllowedActions)
                    .filter(allowedActions -> allowedActions.hasPermission(TemplateAccessControl.CHANGE_PERMS))
                    .ifPresent(allowedActions -> allowedActions.enable(principal, TemplateAccessControl.ACCESS_TEMPLATE));
                this.feed.getFeedDetails().ifPresent(d -> JcrAccessControlUtil.addHierarchyPermissions(d.getNode(), principal, feed.getNode(), Privilege.JCR_READ));
                this.feed.getFeedData().ifPresent(d -> JcrAccessControlUtil.addHierarchyPermissions(d.getNode(), principal, feed.getNode(), Privilege.JCR_READ));
            } else if (action.implies(FeedAccessControl.ACCESS_FEED)) {
                this.feed.getFeedSummary().ifPresent(s -> JcrAccessControlUtil.addHierarchyPermissions(s.getNode(), principal, feed.getNode(), Privilege.JCR_READ));
                //also add read to the category summary
                final AllowedActions categoryAllowedActions = feed.getCategory().getAllowedActions();
                if (categoryAllowedActions.hasPermission(CategoryAccessControl.CHANGE_PERMS)) {
                    categoryAllowedActions.enable(principal, CategoryAccessControl.ACCESS_CATEGORY);
                }
            }
        });
    }

    protected void enableOnlyEntityAccess(Principal principal, Stream<? extends Action> actions) {
        final AtomicBoolean summaryAccess = new AtomicBoolean(false);
        final AtomicBoolean detailsAccess = new AtomicBoolean(false);
        final AtomicBoolean summaryEdit = new AtomicBoolean(false);
        final AtomicBoolean detailsEdit = new AtomicBoolean(false);
        final AtomicBoolean accessOps = new AtomicBoolean(false);
        final AtomicBoolean changePerms = new AtomicBoolean(false);

        actions.forEach(action -> {
            accessOps.compareAndSet(false, action.implies(FeedAccessControl.ACCESS_OPS));
            summaryAccess.compareAndSet(false, action.implies(FeedAccessControl.ACCESS_FEED));
            detailsAccess.compareAndSet(false, action.implies(FeedAccessControl.ACCESS_DETAILS));
            summaryEdit.compareAndSet(false, action.implies(FeedAccessControl.EDIT_SUMMARY));
            detailsEdit.compareAndSet(false, action.implies(FeedAccessControl.EDIT_DETAILS));
            changePerms.compareAndSet(false, action.implies(FeedAccessControl.CHANGE_PERMS));
        });

        if (accessOps.get()) {
            this.feed.getOpsAccessProvider().ifPresent(provider -> provider.grantAccess(feed.getId(), principal));
        } else {
            this.feed.getOpsAccessProvider().ifPresent(provider -> provider.revokeAccess(feed.getId(), principal));
        }

        if (detailsEdit.get()) {
            this.feed.getFeedDetails().ifPresent(s -> JcrAccessControlUtil.addHierarchyPermissions(s.getNode(), principal, feed.getNode(), Privilege.JCR_ALL));
            this.feed.getFeedData().ifPresent(s -> JcrAccessControlUtil.addHierarchyPermissions(s.getNode(), principal, feed.getNode(), Privilege.JCR_ALL));
        } else {
            this.feed.getFeedDetails().ifPresent(s -> JcrAccessControlUtil.removeHierarchyPermissions(s.getNode(), principal, feed.getNode(), Privilege.JCR_ALL));
            this.feed.getFeedData().ifPresent(s -> JcrAccessControlUtil.removeHierarchyPermissions(s.getNode(), principal, feed.getNode(), Privilege.JCR_ALL));
        }

        if (summaryEdit.get()) {
            this.feed.getFeedSummary().ifPresent(s -> JcrAccessControlUtil.addHierarchyPermissions(s.getNode(), principal, feed.getNode(), Privilege.JCR_ALL));
        } else {
            this.feed.getFeedSummary().ifPresent(s -> JcrAccessControlUtil.removeHierarchyPermissions(s.getNode(), principal, feed.getNode(), Privilege.JCR_ALL));
        }

        if (detailsAccess.get()) {
            this.feed.getFeedDetails().ifPresent(s -> JcrAccessControlUtil.addHierarchyPermissions(s.getNode(), principal, feed.getNode(), Privilege.JCR_READ));
            this.feed.getFeedData().ifPresent(s -> JcrAccessControlUtil.addHierarchyPermissions(s.getNode(), principal, feed.getNode(), Privilege.JCR_READ));
        } else {
            this.feed.getFeedDetails().ifPresent(s -> JcrAccessControlUtil.removeHierarchyPermissions(s.getNode(), principal, feed.getNode(), Privilege.JCR_READ));
            this.feed.getFeedData().ifPresent(s -> JcrAccessControlUtil.removeHierarchyPermissions(s.getNode(), principal, feed.getNode(), Privilege.JCR_READ));
        }

        if (summaryAccess.get()) {
            this.feed.getFeedSummary().ifPresent(s -> JcrAccessControlUtil.addHierarchyPermissions(s.getNode(), principal, feed.getNode(), Privilege.JCR_READ));
        } else {
            this.feed.getFeedSummary().ifPresent(s -> JcrAccessControlUtil.removeHierarchyPermissions(s.getNode(), principal, feed.getNode(), Privilege.JCR_READ));
        }

        final Node allowedActionsNode = ((JcrAllowedActions) this.feed.getAllowedActions()).getNode();
        if (changePerms.get()) {
            JcrAccessControlUtil.addRecursivePermissions(allowedActionsNode, JcrAllowableAction.NODE_TYPE, principal, Privilege.JCR_ALL);
        } else {
            JcrAccessControlUtil.removeRecursivePermissions(allowedActionsNode, JcrAllowableAction.NODE_TYPE, principal, Privilege.JCR_ALL);
        }
    }

    protected void disableEntityAccess(Principal principal, Stream<? extends Action> actions) {
        actions.forEach(action -> {
            if (action.implies(FeedAccessControl.ACCESS_OPS)) {
                this.feed.getOpsAccessProvider().ifPresent(provider -> provider.revokeAccess(feed.getId(), principal));
            } else if (action.implies(FeedAccessControl.CHANGE_PERMS)) {
                final Node allowedActionsNode = ((JcrAllowedActions) this.feed.getAllowedActions()).getNode();
                JcrAccessControlUtil.removeRecursivePermissions(allowedActionsNode, JcrAllowableAction.NODE_TYPE, principal, Privilege.JCR_ALL);
            } else if (action.implies(FeedAccessControl.EDIT_DETAILS)) {
                this.feed.getFeedDetails().ifPresent(d -> JcrAccessControlUtil.removePermissions(d.getNode(), principal, Privilege.JCR_ALL));
                this.feed.getFeedData().ifPresent(d -> JcrAccessControlUtil.removePermissions(d.getNode(), principal, Privilege.JCR_ALL));
            } else if (action.implies(FeedAccessControl.EDIT_SUMMARY)) {
                this.feed.getFeedSummary().ifPresent(s -> JcrAccessControlUtil.removeRecursivePermissions(s.getNode(), FeedDetails.NODE_TYPE, principal, Privilege.JCR_ALL));
                this.feed.getFeedSummary().ifPresent(s -> JcrAccessControlUtil.removeRecursivePermissions(s.getNode(), FeedData.NODE_TYPE, principal, Privilege.JCR_ALL));
            } else if (action.implies(FeedAccessControl.ACCESS_DETAILS)) {
                this.feed.getFeedDetails().ifPresent(d -> JcrAccessControlUtil.removePermissions(d.getNode(), principal, Privilege.JCR_ALL, Privilege.JCR_READ));
                this.feed.getFeedData().ifPresent(d -> JcrAccessControlUtil.removePermissions(d.getNode(), principal, Privilege.JCR_ALL, Privilege.JCR_READ));
            } else if (action.implies(FeedAccessControl.ACCESS_FEED)) {
                this.feed.getFeedSummary().ifPresent(s -> JcrAccessControlUtil.removeRecursivePermissions(s.getNode(), FeedDetails.NODE_TYPE, principal, Privilege.JCR_ALL, Privilege.JCR_READ));
                this.feed.getFeedSummary().ifPresent(s -> JcrAccessControlUtil.removeRecursivePermissions(s.getNode(), FeedData.NODE_TYPE, principal, Privilege.JCR_ALL, Privilege.JCR_READ));
                JcrAccessControlUtil.removePermissions(this.feed.getNode(), principal, Privilege.JCR_ALL, Privilege.JCR_READ);
            }
        });
    }
}
