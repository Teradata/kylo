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

import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.JcrObject;
import com.thinkbiganalytics.metadata.modeshape.common.JcrPropertyConstants;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.security.action.Action;
import com.thinkbiganalytics.security.action.AllowableAction;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

/**
 *
 */
public class JcrAllowableAction extends JcrObject implements AllowableAction {

    public static final String NODE_TYPE = "tba:allowableAction";

    private volatile int hash = 0;

    public JcrAllowableAction(Node node) {
        super(node);
    }

    @Override
    public Stream<AllowableAction> stream() {
        return Stream.concat(Stream.of(this),
                             getSubActions().stream().flatMap(AllowableAction::stream));
    }

    @Override
    public int hashCode() {
        // Hierarchy is fixed so hash code need only be calculated once.
        if (this.hash == 0) {
            try {
                List<String> hierList = new ArrayList<>();
                Node current = getNode();

                while (JcrUtil.isNodeType(current, NODE_TYPE)) {
                    hierList.add(0, current.getName());
                    current = current.getParent();
                }

                this.hash = hierList.hashCode();
            } catch (RepositoryException e) {
                throw new MetadataRepositoryException("Failed to access action hierarchy of node: " + this.getNode(), e);
            }
        }

        return this.hash;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Action && obj.hashCode() == hashCode();
    }

    @Override
    public List<Action> getHierarchy() {
        try {
            List<Action> list = new ArrayList<>();
            Node current = getNode();

            while (current.getPrimaryNodeType().isNodeType(NODE_TYPE)) {
                list.add(0, JcrUtil.createJcrObject(current, JcrAllowableAction.class));
                current = current.getParent();
            }

            return list;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to derive the perentage of action node: " + getNode(), e);
        }
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.AllowableAction#getSystemName()
     */
    @Override
    public String getSystemName() {
        return JcrPropertyUtil.getName(this.node);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.AllowableAction#getTitle()
     */
    @Override
    public String getTitle() {
        return JcrPropertyUtil.getString(node, JcrPropertyConstants.TITLE);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.AllowableAction#getDescription()
     */
    @Override
    public String getDescription() {
        return JcrPropertyUtil.getString(node, JcrPropertyConstants.DESCRIPTION);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.AllowableAction#getSubFunctions()
     */
    @Override
    public List<AllowableAction> getSubActions() {
        NodeType type = JcrUtil.getNodeType(JcrMetadataAccess.getActiveSession(), NODE_TYPE);
        return JcrUtil.getJcrObjects(this.node, type, JcrAllowableAction.class).stream().collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return getSystemName();
    }
    
    /**
     * @return a stream on only the leaf actions of this action hierarchy.
     */
    protected Stream<AllowableAction> streamLeafActions() {
        return stream().filter(action -> ! JcrUtil.hasNodeOfType(((JcrAllowableAction) action).getNode(), NODE_TYPE));
    }
}
