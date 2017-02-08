/**
 *
 */
package com.thinkbiganalytics.metadata.modeshape.security.action;

/*-
 * #%L
 * thinkbig-metadata-modeshape
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

import com.thinkbiganalytics.metadata.modeshape.common.JcrPropertyConstants;
import com.thinkbiganalytics.metadata.modeshape.security.JcrAccessControlUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.security.action.Action;
import com.thinkbiganalytics.security.action.config.ActionBuilder;

import org.modeshape.jcr.security.SimplePrincipal;

import javax.jcr.Node;
import javax.jcr.security.Privilege;

/**
 *
 */
public class JcrActionBuilder<P> extends JcrAbstractActionsBuilder implements ActionBuilder<P> {

    private Node actionNode;
    private P parentBuilder;

    public JcrActionBuilder(Node node, P parent) {
        this.actionNode = node;
        this.parentBuilder = parent;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.config.ActionsTreeBuilder.ActionBuilder#title(java.lang.String)
     */
    @Override
    public ActionBuilder<P> title(String name) {
        JcrPropertyUtil.setProperty(this.actionNode, JcrPropertyConstants.TITLE, name);
        return this;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.config.ActionsTreeBuilder.ActionBuilder#description(java.lang.String)
     */
    @Override
    public ActionBuilder<P> description(String descr) {
        JcrPropertyUtil.setProperty(this.actionNode, JcrPropertyConstants.DESCRIPTION, descr);
        return this;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.config.ActionBuilder#subAction(com.thinkbiganalytics.security.action.Action)
     */
    @Override
    public ActionBuilder<ActionBuilder<P>> subAction(Action action) {
        return subAction(action.getSystemName());
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.config.ActionsTreeBuilder.ActionBuilder#subAction(java.lang.String)
     */
    @Override
    public ActionBuilder<ActionBuilder<P>> subAction(String name) {
        Node actionNode = JcrUtil.getOrCreateNode(this.actionNode, name, JcrAllowableAction.NODE_TYPE);
        return new JcrActionBuilder<>(actionNode, this);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.config.ActionsTreeBuilder.ActionBuilder#add()
     */
    @Override
    public P add() {
        JcrAccessControlUtil.addPermissions(this.actionNode, getManagementPrincipal(), Privilege.JCR_ALL);
        JcrAccessControlUtil.addPermissions(this.actionNode, SimplePrincipal.EVERYONE, Privilege.JCR_READ);
        return this.parentBuilder;
    }

}
