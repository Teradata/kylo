package com.thinkbiganalytics.metadata.modeshape.security;

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

import com.thinkbiganalytics.metadata.api.security.HadoopSecurityGroup;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;

import java.io.Serializable;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 */
public class JcrHadoopSecurityGroup extends JcrEntity implements HadoopSecurityGroup {

    public static final String DESCRIPTION = "jcr:description";
    public static final String NAME = "jcr:title";
    public static final String HADOOP_SECURITY_GROUP_ID = "tba:groupId";
    public static final String NODE_TYPE = "tba:hadoopSecurityGroup";

    public JcrHadoopSecurityGroup(Node node) {
        super(node);
    }

    @Override
    public String getGroupId() {
        return JcrPropertyUtil.getString(this.node, HADOOP_SECURITY_GROUP_ID);
    }

    public void setGroupId(String id) {
        JcrPropertyUtil.setProperty(this.node, HADOOP_SECURITY_GROUP_ID, id);
    }

    @Override
    public String getName() {
        return JcrPropertyUtil.getString(this.node, NAME);
    }

    public void setName(String name) {
        JcrPropertyUtil.setProperty(this.node, NAME, name);
    }

    @Override
    public String getDescription() {
        return JcrPropertyUtil.getString(this.node, DESCRIPTION);
    }

    public void setDescription(String description) {
        JcrPropertyUtil.setProperty(this.node, DESCRIPTION, description);
    }

    @Override
    public HadoopSecurityGroupId getId() {
        try {
            return new JcrHadoopSecurityGroup.HadoopSecurityGroupId(this.node.getIdentifier());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the entity id", e);
        }
    }

    public static class HadoopSecurityGroupId extends JcrEntity.EntityId implements HadoopSecurityGroup.ID {

        public HadoopSecurityGroupId(Serializable ser) {
            super(ser);
        }
    }
}
