package com.thinkbiganalytics.metadata.modeshape.category.security;

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
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.category.JcrCategory;
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

import javax.annotation.Nonnull;
import javax.jcr.Node;
import javax.jcr.security.Privilege;

/**
 * A type of allowed actions that applies to category.  It intercepts certain action enable/disable
 * calls related to visibility to update the underlying JCR node structure's ACL lists.
 */
public class JcrCategoryAllowedActions extends JcrAllowedActions {

    private JcrCategory category;

    /**
     * @param allowedActionsNode
     */
    public JcrCategoryAllowedActions(Node allowedActionsNode) {
        super(allowedActionsNode);
        this.category = JcrUtil.getJcrObject(JcrUtil.getParent(allowedActionsNode), JcrCategory.class);
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
        super.setupAccessControl(owner);

        enable(JcrMetadataAccess.getActiveUser(), CategoryAccessControl.EDIT_DETAILS);
        enable(JcrMetadataAccess.ADMIN, CategoryAccessControl.EDIT_DETAILS);
    }
    
    @Override
    public void removeAccessControl(Principal owner) {
        super.removeAccessControl(owner);
        
        this.category.getDetails().ifPresent(d -> JcrAccessControlUtil.clearHierarchyPermissions(d.getNode(), category.getNode()));
    }

    protected void enableEntityAccess(Principal principal, Stream<? extends Action> actions) {
        actions.forEach(action -> {
            //When Change Perms comes through the user needs write access to the allowed actions tree to grant additional access
            if (action.implies(FeedAccessControl.CHANGE_PERMS)) {
                final Node allowedActionsNode = ((JcrAllowedActions) this.category.getAllowedActions()).getNode();
                JcrAccessControlUtil.addRecursivePermissions(allowedActionsNode, JcrAllowableAction.NODE_TYPE, principal, Privilege.JCR_ALL);
            } else if (action.implies(CategoryAccessControl.EDIT_DETAILS)) {
                this.category.getDetails().ifPresent(details -> JcrAccessControlUtil.addHierarchyPermissions(details.getNode(), principal, category.getNode(), Privilege.JCR_ALL, Privilege.JCR_READ));
            } else if (action.implies(CategoryAccessControl.EDIT_SUMMARY)) {
                JcrAccessControlUtil.addPermissions(category.getNode(), principal, Privilege.JCR_ALL, Privilege.JCR_READ);
            } else if (action.implies(CategoryAccessControl.ACCESS_DETAILS)) {
                this.category.getDetails().ifPresent(details -> JcrAccessControlUtil.addHierarchyPermissions(details.getNode(), principal, category.getNode(), Privilege.JCR_READ));
            } else if (action.implies(CategoryAccessControl.ACCESS_CATEGORY)) {
                JcrAccessControlUtil.addPermissions(category.getNode(), principal, Privilege.JCR_READ);
            }
        });
    }

    protected void enableOnlyEntityAccess(@Nonnull final Principal principal, @Nonnull final Stream<? extends Action> actions) {
        // Determine enabled permissions
        final AtomicBoolean accessDetails = new AtomicBoolean(false);
        final AtomicBoolean accessSummary = new AtomicBoolean(false);
        final AtomicBoolean changePerms = new AtomicBoolean(false);
        final AtomicBoolean editDetails = new AtomicBoolean(false);
        final AtomicBoolean editSummary = new AtomicBoolean(false);

        actions.forEach(action -> {
            accessDetails.compareAndSet(false, action.implies(CategoryAccessControl.ACCESS_DETAILS));
            accessSummary.compareAndSet(false, action.implies(CategoryAccessControl.ACCESS_CATEGORY));
            changePerms.compareAndSet(false, action.implies(CategoryAccessControl.CHANGE_PERMS));
            editDetails.compareAndSet(false, action.implies(CategoryAccessControl.EDIT_DETAILS));
            editSummary.compareAndSet(false, action.implies(CategoryAccessControl.EDIT_SUMMARY));
        });

        // Set permissions
        if (editDetails.get()) {
            this.category.getDetails().ifPresent(details -> JcrAccessControlUtil.addHierarchyPermissions(details.getNode(), principal, category.getNode(), Privilege.JCR_ALL, Privilege.JCR_READ));
        } else {
            this.category.getDetails().ifPresent(details -> JcrAccessControlUtil.removeHierarchyPermissions(details.getNode(), principal, category.getNode(), Privilege.JCR_ALL, Privilege.JCR_READ));
        }

        if (editSummary.get()) {
            JcrAccessControlUtil.addHierarchyPermissions(category.getNode(), principal, category.getNode(), Privilege.JCR_ALL, Privilege.JCR_READ);
        } else {
            JcrAccessControlUtil.removeHierarchyPermissions(category.getNode(), principal, category.getNode(), Privilege.JCR_ALL, Privilege.JCR_READ);
        }

        if (accessDetails.get()) {
            this.category.getDetails().ifPresent(details -> JcrAccessControlUtil.addHierarchyPermissions(details.getNode(), principal, category.getNode(), Privilege.JCR_READ));
        } else {
            this.category.getDetails().ifPresent(details -> JcrAccessControlUtil.removeHierarchyPermissions(details.getNode(), principal, category.getNode(), Privilege.JCR_READ));
        }

        if (accessSummary.get()) {
            JcrAccessControlUtil.addHierarchyPermissions(category.getNode(), principal, category.getNode(), Privilege.JCR_READ);
        } else {
            JcrAccessControlUtil.removeHierarchyPermissions(category.getNode(), principal, category.getNode(), Privilege.JCR_READ);
        }

        final Node allowedActionsNode = ((JcrAllowedActions) category.getAllowedActions()).getNode();
        if (changePerms.get()) {
            JcrAccessControlUtil.addRecursivePermissions(allowedActionsNode, JcrAllowableAction.NODE_TYPE, principal, Privilege.JCR_ALL);
        } else {
            JcrAccessControlUtil.removeRecursivePermissions(allowedActionsNode, JcrAllowableAction.NODE_TYPE, principal, Privilege.JCR_ALL);
        }
    }

    protected void disableEntityAccess(Principal principal, Stream<? extends Action> actions) {
        actions.forEach(action -> {
            if (action.implies(CategoryAccessControl.CHANGE_PERMS)) {
                final Node allowedActionsNode = ((JcrAllowedActions) this.category.getAllowedActions()).getNode();
                JcrAccessControlUtil.removeRecursivePermissions(allowedActionsNode, JcrAllowableAction.NODE_TYPE, principal, Privilege.JCR_ALL);
            } else if (action.implies(CategoryAccessControl.EDIT_DETAILS)) {
                this.category.getDetails().ifPresent(details -> JcrAccessControlUtil.removePermissions(details.getNode(), principal, Privilege.JCR_ALL));
            } else if (action.implies(CategoryAccessControl.EDIT_SUMMARY)) {
                JcrAccessControlUtil.removePermissions(category.getNode(), principal, Privilege.JCR_ALL);
            } else if (action.implies(CategoryAccessControl.ACCESS_DETAILS)) {
                this.category.getDetails().ifPresent(details -> JcrAccessControlUtil.removePermissions(details.getNode(), principal, Privilege.JCR_ALL, Privilege.JCR_READ));
            } else if (action.implies(CategoryAccessControl.ACCESS_CATEGORY)) {
                JcrAccessControlUtil.removePermissions(category.getNode(), principal, Privilege.JCR_ALL, Privilege.JCR_READ);
            }
        });
    }
}
