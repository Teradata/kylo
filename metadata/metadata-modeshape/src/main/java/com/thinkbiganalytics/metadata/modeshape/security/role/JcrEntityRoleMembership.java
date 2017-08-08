/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.security.role;

import java.security.Principal;
import java.util.stream.Stream;

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

import com.thinkbiganalytics.metadata.api.security.RoleMembership;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActions;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.security.action.AllowedActions;

/**
 * Role memberships that affect an individual entity and controls access to that entity's allowed actions.
 */
public class JcrEntityRoleMembership extends JcrAbstractRoleMembership {
    
    private transient JcrAllowedActions allowedActions;

    /**
     * Wraps a parent node containing the tba:accessControlled mixin.
     * @param node the parent entity node
     */
    public JcrEntityRoleMembership(Node node, JcrAllowedActions allowed) {
        super(node);
        this.allowedActions = allowed;
    }
    
    /**
     * Wraps a parent node containing the tba:accessControlled mixin.
     * @param node the parent entity node
     * @param roleNode the node for the role entity
     * @param 
     */
    public JcrEntityRoleMembership(Node node, Node roleNode, JcrAllowedActions allowed) {
        super(node, roleNode);
        this.allowedActions = allowed;
    }
    
    @Override
    protected void enable(Principal principal) {
        enableOnly(principal, streamAllRoleMemberships(), getAllowedActions());
    }

    @Override
    protected void disable(Principal principal) {
        enableOnly(principal, streamAllRoleMemberships(), getAllowedActions());
    }

    protected Stream<RoleMembership> streamAllRoleMemberships() {
        Node parentNode = JcrUtil.getParent(getNode());
        return JcrUtil.getNodeList(parentNode, NODE_NAME).stream()
                        .map(node -> JcrUtil.getJcrObject(node, JcrEntityRoleMembership.class, this.allowedActions));
    }

    protected AllowedActions getAllowedActions() {
        return this.allowedActions;
    }
}
