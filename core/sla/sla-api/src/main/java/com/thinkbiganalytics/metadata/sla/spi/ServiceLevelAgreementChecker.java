package com.thinkbiganalytics.metadata.sla.spi;

import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;

/**
 * Check SLAs to see if they are violated Created by sr186054 on 7/22/16.
 */
public interface ServiceLevelAgreementChecker {

    void checkAgreements();

    void checkAgreement(ServiceLevelAgreement agreement);

}
