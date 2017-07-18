package com.thinkbiganalytics.metadata.api.domaintype;

import com.thinkbiganalytics.metadata.api.BaseProvider;

import javax.annotation.Nonnull;

/**
 * Provides access to {@link DomainType} objects.
 */
public interface DomainTypeProvider extends BaseProvider<DomainType, DomainType.ID> {

    /**
     * Creates a new domain type.
     */
    @Nonnull
    DomainType create();
}
