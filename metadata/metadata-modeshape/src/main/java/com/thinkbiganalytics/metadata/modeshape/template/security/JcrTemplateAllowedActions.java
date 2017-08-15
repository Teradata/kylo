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

import com.thinkbiganalytics.metadata.api.template.security.TemplateAccessControl;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.security.JcrAccessControlUtil;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActions;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.metadata.modeshape.template.JcrFeedTemplate;
import com.thinkbiganalytics.security.action.Action;
import com.thinkbiganalytics.security.action.AllowedActions;

import org.modeshape.jcr.security.SimplePrincipal;

import java.security.Principal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.security.Privilege;

/**
 * A type of allowed actions that applies to templates.  It intercepts certain action enable/disable
 * calls related to visibility to update the underlying JCR node structure's ACL lists.
 */
public class JcrTemplateAllowedActions extends JcrAllowedActions {

    private JcrFeedTemplate template;

    /**
     * @param allowedActionsNode
     */
    public JcrTemplateAllowedActions(Node allowedActionsNode) {
        super(allowedActionsNode);
        this.template = JcrUtil.getJcrObject(JcrUtil.getParent(allowedActionsNode), JcrFeedTemplate.class);
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
        if (! principal.equals(this.template.getOwner())) {
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
        if (! principal.equals(this.template.getOwner())) {
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
        if (! this.template.getOwner().equals(principal)) {
            boolean changed = super.disable(principal, actions);
            updateEntityAccess(principal, getEnabledActions(principal));
            return changed;
        } else {
            return false;
        }
    }

    @Override
    public void setupAccessControl(Principal owner) {
        enable(JcrMetadataAccess.getActiveUser(), TemplateAccessControl.EDIT_TEMPLATE);
        //Granting everyone access to Modify properties on a template.
        //this is needed since when a feed is created it needs to set the template bi-directional relationship.
        //Kylo will handle the explicitly permission checks to modify a template using its permissions rather than JCR privileges.
        JcrAccessControlUtil.addPermissions(template.getNode(), SimplePrincipal.EVERYONE,Privilege.JCR_MODIFY_PROPERTIES);
        enable(JcrMetadataAccess.ADMIN, TemplateAccessControl.EDIT_TEMPLATE);

        super.setupAccessControl(owner);
    }
    
    @Override
    public void removeAccessControl(Principal owner) {
        super.removeAccessControl(owner);
        
        JcrAccessControlUtil.clearPermissions(getNode());
    }

    protected void updateEntityAccess(Principal principal, Set<? extends Action> actions) {
        Set<String> privs = new HashSet<>();

        actions.forEach(action -> {
            //When Change Perms comes through the user needs write access to the allowed actions tree to grant additonal access
            if (action.implies(TemplateAccessControl.CHANGE_PERMS)) {
                Collections.addAll(privs, Privilege.JCR_READ_ACCESS_CONTROL, Privilege.JCR_MODIFY_ACCESS_CONTROL);
            } else if (action.implies(TemplateAccessControl.EDIT_TEMPLATE)) {
                privs.add(Privilege.JCR_ALL); 
            } else if (action.implies(TemplateAccessControl.ACCESS_TEMPLATE)) {
                privs.add(Privilege.JCR_READ); 
            }
        });
        
        JcrAccessControlUtil.setPermissions(this.template.getNode(), principal, privs);
    }
    
    @Override
    protected boolean isAdminAction(Action action) {
        return action.implies(TemplateAccessControl.CHANGE_PERMS);
    }
}
