package com.thinkbiganalytics.feedmgr.service.domaintype;

import com.thinkbiganalytics.json.ObjectMapperSerializer;
import com.thinkbiganalytics.metadata.api.domaintype.DomainTypeProvider;
import com.thinkbiganalytics.policy.rest.model.FieldPolicy;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * Transform domain types objects between REST and domain models.
 */
public class DomainTypeTransform {

    /**
     * Provides access to domain type objects.
     */
    @Inject
    private DomainTypeProvider domainTypeProvider;

    /**
     * Transforms the specified REST model to a domain model.
     */
    @Nonnull
    public com.thinkbiganalytics.metadata.api.domaintype.DomainType toDomainModel(@Nonnull final com.thinkbiganalytics.feedmgr.rest.model.DomainType restModel) {
        com.thinkbiganalytics.metadata.api.domaintype.DomainType domainModel = null;
        if (restModel.getId() != null) {
            domainModel = domainTypeProvider.findById(domainTypeProvider.resolveId(restModel.getId()));
        }
        if (domainModel == null) {
            domainModel = domainTypeProvider.create();
        }

        domainModel.setDescription(restModel.getDescription());
        domainModel.setFieldPolicyJson(ObjectMapperSerializer.serialize(restModel.getFieldPolicy()));
        domainModel.setIcon(restModel.getIcon());
        domainModel.setIconColor(restModel.getIconColor());
        domainModel.setRegexFlags(restModel.getRegexFlags());
        domainModel.setRegexPattern(restModel.getRegexPattern());
        domainModel.setTitle(restModel.getTitle());
        return domainModel;
    }

    /**
     * Transforms the specified domain model to a REST model.
     */
    @Nonnull
    public com.thinkbiganalytics.feedmgr.rest.model.DomainType toRestModel(@Nonnull final com.thinkbiganalytics.metadata.api.domaintype.DomainType domainModel) {
        final com.thinkbiganalytics.feedmgr.rest.model.DomainType restModel = new com.thinkbiganalytics.feedmgr.rest.model.DomainType();
        restModel.setDescription(domainModel.getDescription());
        restModel.setFieldPolicy(ObjectMapperSerializer.deserialize(domainModel.getFieldPolicyJson(), FieldPolicy.class));
        restModel.setIcon(domainModel.getIcon());
        restModel.setIconColor(domainModel.getIconColor());
        restModel.setId(domainModel.getId().toString());
        restModel.setRegexFlags(domainModel.getRegexFlags());
        restModel.setRegexPattern(domainModel.getRegexPattern());
        restModel.setTitle(domainModel.getTitle());
        return restModel;
    }
}
