/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.template.security;

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

import com.thinkbiganalytics.metadata.api.template.security.TemplateAccessControl;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.security.JcrAccessControlUtil;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActions;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.metadata.modeshape.template.JcrFeedTemplate;
import com.thinkbiganalytics.security.UsernamePrincipal;
import com.thinkbiganalytics.security.action.Action;
import com.thinkbiganalytics.security.action.AllowedActions;

/**
 * A type of allowed actions that applies to templates.  It intercepts certain action enable/disable
 * calls related to visibility to update the underlying JCR node structure's ACL lists.
 */
public class JcrTemplateAllowedActions extends JcrAllowedActions {
    
    private JcrFeedTemplate category;

    /**
     * @param allowedActionsNode
     */
    public JcrTemplateAllowedActions(Node allowedActionsNode) {
        super(allowedActionsNode);
        this.category = JcrUtil.getJcrObject(JcrUtil.getParent(allowedActionsNode), JcrFeedTemplate.class);
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
        
        enable(JcrMetadataAccess.getActiveUser(), TemplateAccessControl.EDIT_TEMPLATE);
        enable(JcrMetadataAccess.ADMIN, TemplateAccessControl.EDIT_TEMPLATE);
    }

    protected void enableEntityAccess(Principal principal, Stream<? extends Action> actions) {
        actions.forEach(action -> {
            if (action.implies(TemplateAccessControl.EDIT_TEMPLATE)) {
                JcrAccessControlUtil.addPermissions(category.getNode(), principal, Privilege.JCR_ALL, Privilege.JCR_READ);
            } else if (action.implies(TemplateAccessControl.ACCESS_TEMPLATE)) {
                JcrAccessControlUtil.addPermissions(category.getNode(), principal, Privilege.JCR_READ);
            }
        });
    }
    
    protected void enableOnlyEntityAccess(Principal principal, Stream<? extends Action> actions) {
        AtomicBoolean access = new AtomicBoolean(false);
        AtomicBoolean edit = new AtomicBoolean(false);
        
        actions.forEach(action -> {
            access.compareAndSet(false, action.implies(TemplateAccessControl.ACCESS_TEMPLATE));
            edit.compareAndSet(false, action.implies(TemplateAccessControl.EDIT_TEMPLATE));
        });
        
        if (edit.get()) {
            JcrAccessControlUtil.addHierarchyPermissions(category.getNode(), principal, category.getNode(), Privilege.JCR_ALL, Privilege.JCR_READ);
        } else {
            JcrAccessControlUtil.removeHierarchyPermissions(category.getNode(), principal, category.getNode(), Privilege.JCR_ALL, Privilege.JCR_READ);
        }
        
        if (access.get()) {
            JcrAccessControlUtil.addHierarchyPermissions(category.getNode(), principal, category.getNode(), Privilege.JCR_READ);
        } else {
            JcrAccessControlUtil.removeHierarchyPermissions(category.getNode(), principal, category.getNode(), Privilege.JCR_READ);
        }
    }
    
    protected void disableEntityAccess(Principal principal, Stream<? extends Action> actions) {
        actions.forEach(action -> {
            if (action.implies(TemplateAccessControl.EDIT_TEMPLATE)) {
                JcrAccessControlUtil.removePermissions(category.getNode(), principal, Privilege.JCR_ALL);
            } else if (action.implies(TemplateAccessControl.ACCESS_TEMPLATE)) {
                JcrAccessControlUtil.removePermissions(category.getNode(), principal, Privilege.JCR_ALL, Privilege.JCR_READ);
            }
        });
    }
}
