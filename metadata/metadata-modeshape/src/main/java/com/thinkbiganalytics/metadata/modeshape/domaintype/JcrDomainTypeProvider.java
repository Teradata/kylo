package com.thinkbiganalytics.metadata.modeshape.domaintype;

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

import com.thinkbiganalytics.metadata.api.domaintype.DomainType;
import com.thinkbiganalytics.metadata.api.domaintype.DomainTypeProvider;
import com.thinkbiganalytics.metadata.modeshape.BaseJcrProvider;
import com.thinkbiganalytics.metadata.modeshape.common.EntityUtil;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;

/**
 * An implementation of {@link DomainTypeProvider} backed by a JCR store.
 */
public class JcrDomainTypeProvider extends BaseJcrProvider<DomainType, DomainType.ID> implements DomainTypeProvider {

    @Nonnull
    @Override
    public DomainType create() {
        return findOrCreateEntity(EntityUtil.pathForDomainTypes(), UUID.randomUUID().toString(), (Map<String, Object>) null);
    }

    @Override
    public Class<? extends DomainType> getEntityClass() {
        return JcrDomainType.class;
    }

    @Override
    public Class<? extends JcrEntity> getJcrEntityClass() {
        return JcrDomainType.class;
    }

    @Override
    public String getNodeType(Class<? extends JcrEntity> jcrEntityType) {
        return JcrDomainType.NODE_TYPE;
    }

    @Override
    public DomainType.ID resolveId(final Serializable fid) {
        return new JcrDomainType.DomainTypeId(fid);
    }

    @Override
    protected String getEntityQueryStartingPath() {
        return EntityUtil.pathForDomainTypes();
    }

}
