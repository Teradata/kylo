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
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.category.JcrCategory;
import com.thinkbiganalytics.metadata.modeshape.security.JcrAccessControlUtil;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActions;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.security.action.Action;
import com.thinkbiganalytics.security.action.AllowedActions;

import java.security.Principal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
        boolean changed = super.enable(principal, actions);
        updateEntityAccess(principal, getEnabledActions(principal));
        return changed;
    }

    @Override
    public boolean enableOnly(Principal principal, Set<Action> actions) {
        // Never replace permissions of the owner
        if (! principal.equals(this.category.getOwner())) {
            boolean changed = super.enableOnly(principal, actions);
            updateEntityAccess(principal, getEnabledActions(principal));
            return changed;
        } else {
            return false;
        }
    }

    @Override
    public boolean enableOnly(Principal principal, AllowedActions actions) {
        // Never replace permissions of the owner
        if (! principal.equals(this.category.getOwner())) {
            boolean changed = super.enableOnly(principal, actions);
            updateEntityAccess(principal, getEnabledActions(principal));
            return changed;
        } else {
            return false;
        }
    }

    @Override
    public boolean disable(Principal principal, Set<Action> actions) {
        // Never disable permissions of the owner
        if (! principal.equals(this.category.getOwner())) {
            boolean changed = super.disable(principal, actions);
            updateEntityAccess(principal, getEnabledActions(principal));
            return changed;
        } else {
            return false;
        }
    }

    @Override
    public void setupAccessControl(Principal owner) {
        enable(JcrMetadataAccess.getActiveUser(), CategoryAccessControl.EDIT_DETAILS);
        enable(JcrMetadataAccess.ADMIN, CategoryAccessControl.EDIT_DETAILS);

        super.setupAccessControl(owner);
    }
    
    @Override
    public void removeAccessControl(Principal owner) {
        super.removeAccessControl(owner);
        
        this.category.getDetails().ifPresent(d -> JcrAccessControlUtil.clearHierarchyPermissions(d.getNode(), category.getNode()));
    }

    protected void updateEntityAccess(Principal principal, Set<? extends Action> actions) {
        Set<String> detailPrivs = new HashSet<>();
        Set<String> summaryPrivs = new HashSet<>();

        actions.forEach(action -> {
            //When Change Perms comes through the user needs write access to the allowed actions tree to grant additional access
            if (action.implies(CategoryAccessControl.CHANGE_PERMS)) {
                Collections.addAll(detailPrivs, Privilege.JCR_READ_ACCESS_CONTROL, Privilege.JCR_MODIFY_ACCESS_CONTROL);
                Collections.addAll(summaryPrivs, Privilege.JCR_READ_ACCESS_CONTROL, Privilege.JCR_MODIFY_ACCESS_CONTROL);
            } else if (action.implies(CategoryAccessControl.EDIT_DETAILS)) {
                detailPrivs.add(Privilege.JCR_ALL); 
            } else if (action.implies(CategoryAccessControl.EDIT_SUMMARY)) {
                summaryPrivs.add(Privilege.JCR_ALL); 
            } else if (action.implies(CategoryAccessControl.CREATE_FEED)) {
                // Privilege.JCR_MODIFY_ACCESS_CONTROL is needed here since anyone creating a new feed will inherit the ACL access from this parent 
                // category details node before the access control is update in the new feed node, and the new feed owner needs rights to change
                // the access control of the feed it is creating.  TODO: Perhaps we should refactor in a future release to create a simple child node 
                // that the feed nodes attach to so that that node only can have Privilege.JCR_MODIFY_ACCESS_CONTROL for the user
               Collections.addAll(detailPrivs, Privilege.JCR_ADD_CHILD_NODES, Privilege.JCR_REMOVE_CHILD_NODES, Privilege.JCR_MODIFY_PROPERTIES, Privilege.JCR_READ_ACCESS_CONTROL,
                                  Privilege.JCR_MODIFY_ACCESS_CONTROL);
               Collections.addAll(summaryPrivs, Privilege.JCR_MODIFY_PROPERTIES);
            } else if (action.implies(CategoryAccessControl.ACCESS_DETAILS)) {
                detailPrivs.add(Privilege.JCR_READ); 
            } else if (action.implies(CategoryAccessControl.ACCESS_CATEGORY)) {
                summaryPrivs.add(Privilege.JCR_READ); 
            }
        });
        
        JcrAccessControlUtil.setPermissions(this.category.getNode(), principal, summaryPrivs);
        this.category.getDetails().ifPresent(d -> JcrAccessControlUtil.setPermissions(d.getNode(), principal, detailPrivs));
    }
    
    @Override
    protected boolean isAdminAction(Action action) {
        return action.implies(CategoryAccessControl.CHANGE_PERMS);
    }
}
