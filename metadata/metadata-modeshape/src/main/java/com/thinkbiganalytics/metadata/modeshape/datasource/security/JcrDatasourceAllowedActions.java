package com.thinkbiganalytics.metadata.modeshape.datasource.security;

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

import com.thinkbiganalytics.metadata.api.datasource.security.DatasourceAccessControl;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrUserDatasource;
import com.thinkbiganalytics.metadata.modeshape.security.JcrAccessControlUtil;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActions;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.security.action.Action;
import com.thinkbiganalytics.security.action.AllowedActions;

import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.jcr.Node;
import javax.jcr.security.Privilege;

/**
 * A type of allowed actions that applies to data sources. It intercepts certain action enable/disable calls related to visibility to update the underlying JCR node structure's ACL lists.
 */
public class JcrDatasourceAllowedActions extends JcrAllowedActions {

    /**
     * The target of the permissions change
     */
    private JcrUserDatasource datasource;

    /**
     * Constructs a {@code JcrDatasourceAllowedActions} for modifying the specified allowed actions node.
     *
     * @param allowedActionsNode the allowed actions node
     */
    public JcrDatasourceAllowedActions(@Nonnull final Node allowedActionsNode) {
        super(allowedActionsNode);
        datasource = JcrUtil.getJcrObject(JcrUtil.getParent(allowedActionsNode), JcrUserDatasource.class);
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
        if (! principal.equals(this.datasource.getOwner())) {
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
        if (! principal.equals(this.datasource.getOwner())) {
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
        if (! this.datasource.getOwner().equals(principal)) {
            boolean changed = super.disable(principal, actions);
            updateEntityAccess(principal, getEnabledActions(principal));
            return changed;
        } else {
            return false;
        }
    }

    @Override
    public void setupAccessControl(Principal owner) {
        enable(owner, DatasourceAccessControl.EDIT_DETAILS);
        enable(JcrMetadataAccess.ADMIN, DatasourceAccessControl.EDIT_DETAILS);

        super.setupAccessControl(owner);
    }
    
    @Override
    public void removeAccessControl(Principal owner) {
        super.removeAccessControl(owner);
        
        JcrAccessControlUtil.clearPermissions(getNode());
    }

    /**
     * Enables the specified actions for the specified principal.
     *
     * @param principal the subject
     * @param actions   the allowed actions
     */
    protected void updateEntityAccess(@Nonnull final Principal principal, @Nonnull final Collection<? extends Action> actions) {
        Set<String> detailPrivs = new HashSet<>();
        Set<String> summaryPrivs = new HashSet<>();
        
        actions.forEach(action -> {
            if (action.implies(DatasourceAccessControl.CHANGE_PERMS)) {
                Collections.addAll(detailPrivs, Privilege.JCR_READ_ACCESS_CONTROL, Privilege.JCR_MODIFY_ACCESS_CONTROL);
                Collections.addAll(summaryPrivs, Privilege.JCR_READ_ACCESS_CONTROL, Privilege.JCR_MODIFY_ACCESS_CONTROL);
            } else if (action.implies(DatasourceAccessControl.EDIT_DETAILS)) {
                detailPrivs.add(Privilege.JCR_ALL); 
            } else if (action.implies(DatasourceAccessControl.EDIT_SUMMARY)) {
                summaryPrivs.add(Privilege.JCR_ALL); 
            } else if (action.implies(DatasourceAccessControl.ACCESS_DETAILS)) {
                detailPrivs.add(Privilege.JCR_READ); 
            } else if (action.implies(DatasourceAccessControl.ACCESS_DATASOURCE)) {
                summaryPrivs.add(Privilege.JCR_READ); 
            }
        });
        
        JcrAccessControlUtil.setPermissions(this.datasource.getNode(), principal, summaryPrivs);
        this.datasource.getDetails().ifPresent(d -> JcrAccessControlUtil.setPermissions(d.getNode(), principal, detailPrivs));
    }
    
    @Override
    protected boolean isAdminAction(Action action) {
        return action.implies(DatasourceAccessControl.CHANGE_PERMS);
    }
}
