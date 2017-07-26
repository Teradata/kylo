/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.security.role;

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
import java.util.Arrays;
import java.util.Collection;

import javax.jcr.Node;

import com.thinkbiganalytics.metadata.modeshape.common.JcrObject;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAbstractActionsBuilder;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrActionTreeBuilder;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActions;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.security.RolePrincipal;
import com.thinkbiganalytics.security.action.Action;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.role.SecurityRole;

/**
 *
 * @author Sean Felten
 */
public class JcrSecurityRole extends JcrObject implements SecurityRole {

    public static final String NODE_TYPE = "tba:securityRole";
    
    public static final String TITLE = "jcr:title";
    public static final String DESCR = "jcr:description";
    public static final String ALLOWED_ACTIONS = "tba:allowedActions";

    public JcrSecurityRole(Node node) {
        super(node);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.role.SecurityRole#getPrincipal()
     */
    @Override
    public Principal getPrincipal() {
        return new RolePrincipal(getSystemName());
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.role.SecurityRole#getSystemName()
     */
    @Override
    public String getSystemName() {
        return JcrPropertyUtil.getName(getNode());
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.role.SecurityRole#getTitle()
     */
    @Override
    public String getTitle() {
        return getProperty(TITLE, String.class);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.role.SecurityRole#getDescription()
     */
    @Override
    public String getDescription() {
        return getProperty(DESCR, String.class);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.role.SecurityRole#getAllowedActions()
     */
    @Override
    public AllowedActions getAllowedActions() {
        return JcrUtil.getJcrObject(getNode(), ALLOWED_ACTIONS, JcrAllowedActions.class);
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.role.SecurityRole#setPermissions(com.thinkbiganalytics.security.action.Action[])
     */
    @Override
    public void setPermissions(Action... actions) {
        setPermissions(Arrays.asList(actions));
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.role.SecurityRole#setPermissions(java.util.Collection)
     */
    @Override
    public void setPermissions(Collection<Action> actions) {
        Node actionsNode = getAllowedActionsNode();
        JcrActionTreeBuilder<JcrAbstractActionsBuilder> bldr = new JcrActionTreeBuilder<>(actionsNode, null);
        
        actions.forEach(action -> bldr.action(action));
        bldr.add();
    }

    public void setTitle(String title) {
        setProperty(TITLE, title);
    }

    public void setDescription(String descr) {
        setProperty(DESCR, descr);
    }

    public Node getAllowedActionsNode() {
        return JcrUtil.getNode(getNode(), ALLOWED_ACTIONS);
    }
}
