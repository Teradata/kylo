/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.security.mixin;

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

import javax.jcr.Node;
import javax.jcr.security.Privilege;

import com.thinkbiganalytics.metadata.api.security.AccessControlled;
import com.thinkbiganalytics.metadata.api.security.RoleAssignments;
import com.thinkbiganalytics.metadata.modeshape.common.mixin.NodeEntityMixin;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActions;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.security.UsernamePrincipal;
import com.thinkbiganalytics.security.action.AllowedActions;

/**
 *
 */
public interface AccessControlledMixin extends AccessControlled, NodeEntityMixin {
    
    default RoleAssignments getRoleAssignments() {
//      return JcrUtil.getPropertyObjectSet(node, JcrRoleAssignments.NODE_NAME, JcrRoleAssignments.class).stream()
//                      .map(RoleAssignments.class::cast)
//                      .collect(Collectors.toSet());
      return null;
    }
    
    @Override
    default AllowedActions getAllowedActions() {
        return getJcrAllowedActions();
    }

    default JcrAllowedActions getJcrAllowedActions() {
        Node allowedNode = JcrUtil.getNode(getNode(), JcrAllowedActions.NODE_NAME);
        return JcrUtil.createJcrObject(allowedNode, getJcrAllowedActionsType());
    }

    default void setupAccessControl(JcrAllowedActions prototype, UsernamePrincipal owner) {
        JcrAllowedActions allowed = getJcrAllowedActions();
        prototype.copy(allowed.getNode(), owner, Privilege.JCR_ALL);
        allowed.setupAccessControl(owner);
    }

    Class<? extends JcrAllowedActions> getJcrAllowedActionsType();
}
