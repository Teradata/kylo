/**
 * 
 */
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

import java.security.Principal;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import javax.jcr.Node;
import javax.jcr.security.Privilege;

import com.thinkbiganalytics.metadata.api.category.security.CategoryAccessControl;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.category.JcrCategory;
import com.thinkbiganalytics.metadata.modeshape.security.JcrAccessControlUtil;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActions;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.security.UsernamePrincipal;
import com.thinkbiganalytics.security.action.Action;
import com.thinkbiganalytics.security.action.AllowedActions;

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
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActions#setupAccessControl(com.thinkbiganalytics.security.UsernamePrincipal)
     */
    @Override
    public void setupAccessControl(UsernamePrincipal owner) {
        super.setupAccessControl(owner);
        
        enable(JcrMetadataAccess.getActiveUser(), CategoryAccessControl.EDIT_DETAILS);
        enable(JcrMetadataAccess.ADMIN, CategoryAccessControl.EDIT_DETAILS);
    }

    protected void enableEntityAccess(Principal principal, Stream<? extends Action> actions) {
        actions.forEach(action -> {
            if (action.implies(CategoryAccessControl.EDIT_DETAILS)) {
                this.category.getDetails().ifPresent(d -> JcrAccessControlUtil.addHierarchyPermissions(d.getNode(), principal, category.getNode(), Privilege.JCR_ALL, Privilege.JCR_READ));
            } else if (action.implies(CategoryAccessControl.EDIT_SUMMARY)) {
                JcrAccessControlUtil.addPermissions(category.getNode(), principal, Privilege.JCR_ALL, Privilege.JCR_READ);
            } else if (action.implies(CategoryAccessControl.ACCESS_DETAILS)) {
                this.category.getDetails().ifPresent(d -> JcrAccessControlUtil.addHierarchyPermissions(d.getNode(), principal, category.getNode(), Privilege.JCR_READ));
            } else if (action.implies(CategoryAccessControl.ACCESS_CATEGORY)) {
                JcrAccessControlUtil.addPermissions(category.getNode(), principal, Privilege.JCR_READ);
            }
        });
    }
    
    protected void enableOnlyEntityAccess(Principal principal, Stream<? extends Action> actions) {
        AtomicBoolean summaryAccess = new AtomicBoolean(false);
        AtomicBoolean detailsAccess = new AtomicBoolean(false);
        AtomicBoolean summaryEdit = new AtomicBoolean(false);
        AtomicBoolean detailsEdit = new AtomicBoolean(false);
        
        actions.forEach(action -> {
            summaryAccess.compareAndSet(false, action.implies(CategoryAccessControl.ACCESS_CATEGORY));
            detailsAccess.compareAndSet(false, action.implies(CategoryAccessControl.ACCESS_DETAILS));
            summaryEdit.compareAndSet(false, action.implies(CategoryAccessControl.EDIT_SUMMARY));
            detailsEdit.compareAndSet(false, action.implies(CategoryAccessControl.EDIT_DETAILS));
        });
        
        if (detailsEdit.get()) {
            this.category.getDetails().ifPresent(s -> JcrAccessControlUtil.addHierarchyPermissions(s.getNode(), principal, category.getNode(), Privilege.JCR_ALL, Privilege.JCR_READ));
        } else {
            this.category.getDetails().ifPresent(s -> JcrAccessControlUtil.removeHierarchyPermissions(s.getNode(), principal, category.getNode(), Privilege.JCR_ALL, Privilege.JCR_READ));
        }
        
        if (summaryEdit.get()) {
            JcrAccessControlUtil.addHierarchyPermissions(category.getNode(), principal, category.getNode(), Privilege.JCR_ALL, Privilege.JCR_READ);
        } else {
            JcrAccessControlUtil.removeHierarchyPermissions(category.getNode(), principal, category.getNode(), Privilege.JCR_ALL, Privilege.JCR_READ);
        }
        
        if (detailsAccess.get()) {
            this.category.getDetails().ifPresent(s -> JcrAccessControlUtil.addHierarchyPermissions(s.getNode(), principal, category.getNode(), Privilege.JCR_READ));
        } else {
            this.category.getDetails().ifPresent(s -> JcrAccessControlUtil.removeHierarchyPermissions(s.getNode(), principal, category.getNode(), Privilege.JCR_READ));
        }
        
        if (summaryAccess.get()) {
            JcrAccessControlUtil.addHierarchyPermissions(category.getNode(), principal, category.getNode(), Privilege.JCR_READ);
        } else {
            JcrAccessControlUtil.removeHierarchyPermissions(category.getNode(), principal, category.getNode(), Privilege.JCR_READ);
        }
    }
    
    protected void disableEntityAccess(Principal principal, Stream<? extends Action> actions) {
        actions.forEach(action -> {
            if (action.implies(CategoryAccessControl.EDIT_DETAILS)) {
                this.category.getDetails().ifPresent(d -> JcrAccessControlUtil.removePermissions(d.getNode(), principal, Privilege.JCR_ALL));
            } else if (action.implies(CategoryAccessControl.EDIT_SUMMARY)) {
                JcrAccessControlUtil.removePermissions(category.getNode(), principal, Privilege.JCR_ALL);
            } else if (action.implies(CategoryAccessControl.ACCESS_DETAILS)) {
                this.category.getDetails().ifPresent(d -> JcrAccessControlUtil.removePermissions(d.getNode(), principal, Privilege.JCR_ALL, Privilege.JCR_READ));
            } else if (action.implies(CategoryAccessControl.ACCESS_CATEGORY)) {
                JcrAccessControlUtil.removePermissions(category.getNode(), principal, Privilege.JCR_ALL, Privilege.JCR_READ);
            }
        });
    }
}
