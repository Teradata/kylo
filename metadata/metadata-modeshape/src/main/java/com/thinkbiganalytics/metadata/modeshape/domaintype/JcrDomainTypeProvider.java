package com.thinkbiganalytics.metadata.modeshape.domaintype;

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
}
