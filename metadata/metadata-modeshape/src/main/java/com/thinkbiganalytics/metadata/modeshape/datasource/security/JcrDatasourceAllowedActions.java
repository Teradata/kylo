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
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowableAction;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActions;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.security.action.Action;
import com.thinkbiganalytics.security.action.AllowedActions;

import java.security.Principal;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

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
        enableEntityAccess(principal, actions);
        return super.enable(principal, actions);
    }

    @Override
    public boolean enableOnly(Principal principal, Set<Action> actions) {
        enableOnlyEntityAccess(principal, actions);
        return super.enableOnly(principal, actions);
    }

    @Override
    public boolean enableOnly(Principal principal, AllowedActions actions) {
        enableOnlyEntityAccess(principal, actions.getAvailableActions());
        return super.enableOnly(principal, actions);
    }

    @Override
    public boolean disable(Principal principal, Set<Action> actions) {
        disableEntityAccess(principal, actions);
        return super.disable(principal, actions);
    }

    @Override
    public boolean disable(Principal principal, AllowedActions actions) {
        disableEntityAccess(principal, actions.getAvailableActions());
        return super.disable(principal, actions);
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
    protected void enableEntityAccess(@Nonnull final Principal principal, @Nonnull final Collection<? extends Action> actions) {
        actions.forEach(action -> {
            if (action.implies(DatasourceAccessControl.CHANGE_PERMS)) {
                final Node allowedActionsNode = ((JcrAllowedActions) datasource.getAllowedActions()).getNode();
                JcrAccessControlUtil.addRecursivePermissions(allowedActionsNode, JcrAllowableAction.NODE_TYPE, principal, Privilege.JCR_ALL);
            } else if (action.implies(DatasourceAccessControl.EDIT_DETAILS)) {
                datasource.getDetails().ifPresent(details -> JcrAccessControlUtil.addHierarchyPermissions(details.getNode(), principal, datasource.getNode(), Privilege.JCR_ALL, Privilege.JCR_READ));
            } else if (action.implies(DatasourceAccessControl.EDIT_SUMMARY)) {
                JcrAccessControlUtil.addPermissions(datasource.getNode(), principal, Privilege.JCR_ALL, Privilege.JCR_READ);
            } else if (action.implies(DatasourceAccessControl.ACCESS_DETAILS)) {
                datasource.getDetails().ifPresent(details -> JcrAccessControlUtil.addHierarchyPermissions(details.getNode(), principal, datasource.getNode(), Privilege.JCR_READ));
            } else if (action.implies(DatasourceAccessControl.ACCESS_DATASOURCE)) {
                JcrAccessControlUtil.addPermissions(datasource.getNode(), principal, Privilege.JCR_READ);
            }
        });
    }

    /**
     * Enables the specified actions and disables all others for the specified principal.
     *
     * @param principal the subject
     * @param actions   the allowed actions
     */
    protected void enableOnlyEntityAccess(@Nonnull final Principal principal, @Nonnull final Collection<? extends Action> actions) {
        // Determine the allowed actions
        final AtomicBoolean summaryAccess = new AtomicBoolean(false);
        final AtomicBoolean detailsAccess = new AtomicBoolean(false);
        final AtomicBoolean summaryEdit = new AtomicBoolean(false);
        final AtomicBoolean detailsEdit = new AtomicBoolean(false);
        final AtomicBoolean changePerms = new AtomicBoolean(false);

        actions.forEach(action -> {
            summaryAccess.compareAndSet(false, action.implies(DatasourceAccessControl.ACCESS_DATASOURCE));
            detailsAccess.compareAndSet(false, action.implies(DatasourceAccessControl.ACCESS_DETAILS));
            summaryEdit.compareAndSet(false, action.implies(DatasourceAccessControl.EDIT_SUMMARY));
            detailsEdit.compareAndSet(false, action.implies(DatasourceAccessControl.EDIT_DETAILS));
            changePerms.compareAndSet(false, action.implies(DatasourceAccessControl.CHANGE_PERMS));
        });

        // Update JCR permissions
        if (detailsEdit.get()) {
            datasource.getDetails().ifPresent(details -> JcrAccessControlUtil.addHierarchyPermissions(details.getNode(), principal, datasource.getNode(), Privilege.JCR_ALL, Privilege.JCR_READ));
        } else {
            datasource.getDetails().ifPresent(details -> JcrAccessControlUtil.removeHierarchyPermissions(details.getNode(), principal, datasource.getNode(), Privilege.JCR_ALL, Privilege.JCR_READ));
        }

        if (summaryEdit.get()) {
            JcrAccessControlUtil.addHierarchyPermissions(datasource.getNode(), principal, datasource.getNode(), Privilege.JCR_ALL, Privilege.JCR_READ);
        } else {
            JcrAccessControlUtil.removeHierarchyPermissions(datasource.getNode(), principal, datasource.getNode(), Privilege.JCR_ALL, Privilege.JCR_READ);
        }

        if (detailsAccess.get()) {
            datasource.getDetails().ifPresent(details -> JcrAccessControlUtil.addHierarchyPermissions(details.getNode(), principal, datasource.getNode(), Privilege.JCR_READ));
        } else {
            datasource.getDetails().ifPresent(details -> JcrAccessControlUtil.removeHierarchyPermissions(details.getNode(), principal, datasource.getNode(), Privilege.JCR_READ));
        }

        if (summaryAccess.get()) {
            JcrAccessControlUtil.addHierarchyPermissions(datasource.getNode(), principal, datasource.getNode(), Privilege.JCR_READ);
        } else {
            JcrAccessControlUtil.removeHierarchyPermissions(datasource.getNode(), principal, datasource.getNode(), Privilege.JCR_READ);
        }

        final Node allowedActionsNode = ((JcrAllowedActions) datasource.getAllowedActions()).getNode();
        if (changePerms.get()) {
            JcrAccessControlUtil.addRecursivePermissions(allowedActionsNode, JcrAllowableAction.NODE_TYPE, principal, Privilege.JCR_ALL);
        } else {
            JcrAccessControlUtil.removeRecursivePermissions(allowedActionsNode, JcrAllowableAction.NODE_TYPE, principal, Privilege.JCR_ALL);
        }
    }

    /**
     * Disables the specified actions for the specified principal.
     *
     * @param principal the subject
     * @param actions   the allowed actions
     */
    protected void disableEntityAccess(@Nonnull final Principal principal, @Nonnull final Collection<? extends Action> actions) {
        actions.forEach(action -> {
            if (action.implies(DatasourceAccessControl.CHANGE_PERMS)) {
                final Node allowedActionsNode = ((JcrAllowedActions) datasource.getAllowedActions()).getNode();
                JcrAccessControlUtil.removeRecursivePermissions(allowedActionsNode, JcrAllowableAction.NODE_TYPE, principal, Privilege.JCR_ALL);
            } else if (action.implies(DatasourceAccessControl.EDIT_DETAILS)) {
                datasource.getDetails().ifPresent(details -> JcrAccessControlUtil.removePermissions(details.getNode(), principal, Privilege.JCR_ALL));
            } else if (action.implies(DatasourceAccessControl.EDIT_SUMMARY)) {
                JcrAccessControlUtil.removePermissions(datasource.getNode(), principal, Privilege.JCR_ALL);
            } else if (action.implies(DatasourceAccessControl.ACCESS_DETAILS)) {
                datasource.getDetails().ifPresent(details -> JcrAccessControlUtil.removePermissions(details.getNode(), principal, Privilege.JCR_ALL, Privilege.JCR_READ));
            } else if (action.implies(DatasourceAccessControl.ACCESS_DATASOURCE)) {
                JcrAccessControlUtil.removePermissions(datasource.getNode(), principal, Privilege.JCR_ALL, Privilege.JCR_READ);
            }
        });
    }
}
