package com.thinkbiganalytics.metadata.modeshape.security;

import java.io.Serializable;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import com.thinkbiganalytics.metadata.api.security.HadoopSecurityGroup;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;

/**
 * Created by Jeremy Merrifield on 9/20/16.
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
    public long getGroupId() {
        return JcrPropertyUtil.getLong(this.node, HADOOP_SECURITY_GROUP_ID);
    }

    public void setGroupId(long id) {
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
