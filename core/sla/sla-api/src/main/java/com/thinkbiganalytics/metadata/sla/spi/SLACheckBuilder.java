package com.thinkbiganalytics.metadata.sla.spi;

import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementActionConfiguration;

import java.util.List;

/**
 * Created by sr186054 on 7/22/16.
 */
public interface SLACheckBuilder {

    SLACheckBuilder actionConfiguration(ServiceLevelAgreementActionConfiguration configuration);

    SLACheckBuilder actionConfigurations(List<ServiceLevelAgreementActionConfiguration> configurations);

    SLACheckBuilder cronExpression(String cronExpression);

    /**
     * Removes all slaCheck objects under this SLA
     */
    SLACheckBuilder removeSlaChecks();

    ServiceLevelAgreementCheck build();
}
