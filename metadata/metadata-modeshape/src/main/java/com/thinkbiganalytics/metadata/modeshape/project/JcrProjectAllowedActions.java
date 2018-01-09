package com.thinkbiganalytics.metadata.modeshape.project;

/*-
 * #%L
 * kylo-metadata-modeshape
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics
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


import com.thinkbiganalytics.metadata.api.project.security.ProjectAccessControl;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.security.JcrAccessControlUtil;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActions;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.security.action.Action;

import org.modeshape.jcr.security.SimplePrincipal;

import java.security.Principal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.security.Privilege;

public class JcrProjectAllowedActions extends JcrAllowedActions {

    private JcrProject project;

    public JcrProjectAllowedActions(Node allowedActionsNode) {
        super(allowedActionsNode);
        this.project = JcrUtil.getJcrObject(JcrUtil.getParent(allowedActionsNode), JcrProject.class);
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
        if (!principal.equals(this.project.getOwner())) {
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
        if (!this.project.getOwner().equals(principal)) {
            boolean changed = super.disable(principal, actions);
            updateEntityAccess(principal, getEnabledActions(principal));
            return changed;
        } else {
            return false;
        }
    }

    @Override
    public void setupAccessControl(Principal owner) {
        enable(JcrMetadataAccess.getActiveUser(), ProjectAccessControl.EDIT_PROJECT);
        //Granting everyone access to Modify properties on a project.
        //this is needed since when a feed is created it needs to set the project bi-directional relationship.
        //Kylo will handle the explicitly permission checks to modify a project using its permissions rather than JCR privileges.
        JcrAccessControlUtil.addPermissions(project.getNode(), SimplePrincipal.EVERYONE, Privilege.JCR_MODIFY_PROPERTIES);
        enable(JcrMetadataAccess.ADMIN, ProjectAccessControl.EDIT_PROJECT);

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
            if (action.implies(ProjectAccessControl.CHANGE_PERMS)) {
                Collections.addAll(privs, Privilege.JCR_READ_ACCESS_CONTROL, Privilege.JCR_MODIFY_ACCESS_CONTROL);
            } else if (action.implies(ProjectAccessControl.EDIT_PROJECT)) {
                privs.add(Privilege.JCR_ALL);
            } else if (action.implies(ProjectAccessControl.ACCESS_PROJECT)) {
                privs.add(javax.jcr.security.Privilege.JCR_READ);
            }
        });

        JcrAccessControlUtil.setPermissions(this.project.getNode(), principal, privs);
    }

    @Override
    protected boolean isAdminAction(Action action) {
        return action.implies(ProjectAccessControl.CHANGE_PERMS);
    }

}
