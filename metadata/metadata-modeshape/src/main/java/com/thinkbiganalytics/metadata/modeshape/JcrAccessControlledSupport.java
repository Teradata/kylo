/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape;

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

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.security.AccessControlled;
import com.thinkbiganalytics.metadata.api.security.RoleAssignments;
import com.thinkbiganalytics.metadata.modeshape.common.JcrObject;
import com.thinkbiganalytics.metadata.modeshape.security.JcrAccessControlUtil;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowableAction;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActions;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.security.action.AllowedActions;

/**
 * Support for JcrObjects that implement AccessControlled and whose JCR type uses the mix:accessControlled mixin.
 */
public class JcrAccessControlledSupport extends JcrObject implements AccessControlled {
    
    /**
     * @param node
     */
    public JcrAccessControlledSupport(Node node) {
        super(node);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.security.AccessControlled#getRoleAssignments()
     */
    @Override
    public RoleAssignments getRoleAssignments() {
//        return JcrUtil.getPropertyObjectSet(node, JcrRoleAssignments.NODE_NAME, JcrRoleAssignments.class).stream()
//                        .map(RoleAssignments.class::cast)
//                        .collect(Collectors.toSet());
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.security.AccessControlled#getAllowedActions()
     */
    @Override
    public AllowedActions getAllowedActions() {
        Node allowedNode = JcrUtil.getNode(node, JcrAllowedActions.NODE_NAME);
        return JcrUtil.createJcrObject(allowedNode, JcrAllowedActions.class);
    }

    
    public void initializeActions(JcrAllowedActions prototype) {
        JcrAllowedActions allowed = (JcrAllowedActions) getAllowedActions();
        prototype.copy(allowed.getNode(), JcrMetadataAccess.getActiveUser(), Privilege.JCR_ALL);
        JcrAccessControlUtil.addRecursivePermissions(allowed.getNode(), JcrAllowableAction.NODE_TYPE, MetadataAccess.ADMIN, Privilege.JCR_ALL);
    }
}
