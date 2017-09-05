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
import com.thinkbiganalytics.metadata.api.security.HadoopSecurityGroupProvider;
import com.thinkbiganalytics.metadata.modeshape.BaseJcrProvider;
import com.thinkbiganalytics.metadata.modeshape.common.EntityUtil;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;

import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 */
@Service
public class JcrHadoopSecurityGroupProvider extends BaseJcrProvider<HadoopSecurityGroup, HadoopSecurityGroup.ID> implements HadoopSecurityGroupProvider {

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
    protected String getEntityQueryStartingPath() {
        return EntityUtil.pathForHadoopSecurityGroups();
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
