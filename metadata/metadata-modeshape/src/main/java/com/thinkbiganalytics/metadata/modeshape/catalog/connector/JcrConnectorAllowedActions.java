/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.catalog.connector;

import com.thinkbiganalytics.metadata.api.catalog.security.ConnectorAccessControl;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.security.JcrAccessControlUtil;

/*-
 * #%L
 * kylo-metadata-modeshape
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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
 *
 */
public class JcrConnectorAllowedActions extends JcrAllowedActions {
    
    private JcrConnector connector;

    /**
     * @param allowedActionsNode
     */
    public JcrConnectorAllowedActions(Node allowedActionsNode) {
        super(allowedActionsNode);
        this.connector = JcrUtil.getJcrObject(JcrUtil.getParent(allowedActionsNode), JcrConnector.class);
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
        if (! principal.equals(this.connector.getOwner())) {
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
        if (! principal.equals(this.connector.getOwner())) {
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
        if (! principal.equals(this.connector.getOwner())) {
            boolean changed = super.disable(principal, actions);
            updateEntityAccess(principal, getEnabledActions(principal));
            return changed;
        } else {
            return false;
        }
    }

    @Override
    public void setupAccessControl(Principal owner) {
        enableAll(owner);
        enableAll(JcrMetadataAccess.ADMIN);

        super.setupAccessControl(owner);
    }
    
    @Override
    public void removeAccessControl(Principal owner) {
        super.removeAccessControl(owner);
        
        JcrAccessControlUtil.clearPermissions(getNode());
    }

    @Override
    protected boolean isAdminAction(Action action) {
        return action.implies(ConnectorAccessControl.CHANGE_PERMS);
    }

    protected void updateEntityAccess(Principal principal, Set<? extends Action> actions) {
        Set<String> priveleges = new HashSet<>();

        // Collect all JCR privilege changes based on the specified actions.
        actions.forEach(action -> {
            if (action.implies(ConnectorAccessControl.CHANGE_PERMS)) {
                Collections.addAll(priveleges, Privilege.JCR_READ_ACCESS_CONTROL, Privilege.JCR_MODIFY_ACCESS_CONTROL);
            } else if (action.implies(ConnectorAccessControl.EDIT_CONNECTOR)) {
                priveleges.add(Privilege.JCR_ALL);
            } else if (action.implies(ConnectorAccessControl.ACCESS_CONNECTOR)) {
                priveleges.add(Privilege.JCR_READ);                
            }
        });
        
        JcrAccessControlUtil.setPermissions(this.connector.getNode(), principal, priveleges);
    }

}
