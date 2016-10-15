package com.thinkbiganalytics.metadata.sla.spi;

import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;

/**
 * Created by sr186054 on 7/22/16.
 */
public interface ServiceLevelAgreementScheduler {

    void scheduleServiceLevelAgreement(ServiceLevelAgreement sla);

    boolean unscheduleServiceLevelAgreement(ServiceLevelAgreement sla);

    boolean unscheduleServiceLevelAgreement(ServiceLevelAgreement.ID slaId);

    void enableServiceLevelAgreement(ServiceLevelAgreement sla);

    void disableServiceLevelAgreement(ServiceLevelAgreement sla);





}
