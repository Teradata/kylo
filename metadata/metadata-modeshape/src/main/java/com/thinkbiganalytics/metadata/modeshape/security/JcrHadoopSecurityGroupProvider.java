package com.thinkbiganalytics.metadata.modeshape.security;

import com.thinkbiganalytics.metadata.api.security.HadoopSecurityGroup;
import com.thinkbiganalytics.metadata.api.security.HadoopSecurityGroupProvider;
import com.thinkbiganalytics.metadata.modeshape.BaseJcrProvider;
import com.thinkbiganalytics.metadata.modeshape.common.EntityUtil;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;

import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jeremy Merrifield on 9/20/16.
 */
@Service
public class JcrHadoopSecurityGroupProvider  extends BaseJcrProvider<HadoopSecurityGroup, HadoopSecurityGroup.ID> implements HadoopSecurityGroupProvider {

    @Override
    public HadoopSecurityGroup ensureSecurityGroup(String name) {
        String path = EntityUtil.pathForHadoopSecurityGroups();
        Map<String, Object> props = new HashMap<>();
        props.put(JcrHadoopSecurityGroup.NAME, name);
        return findOrCreateEntity(path, name, props);
    }

    public HadoopSecurityGroup.ID resolveId(Serializable fid) {
        return new JcrHadoopSecurityGroup.HadoopSecurityGroupId(fid);
    }

    @Override
    public Class<? extends HadoopSecurityGroup> getEntityClass() {
        return JcrHadoopSecurityGroup.class;
    }

    @Override
    public Class<? extends JcrEntity> getJcrEntityClass() {
        return JcrHadoopSecurityGroup.class;
    }

    @Override
    public String getNodeType(Class<? extends JcrEntity> jcrEntityType) {
        return JcrHadoopSecurityGroup.NODE_TYPE;
    }

}
