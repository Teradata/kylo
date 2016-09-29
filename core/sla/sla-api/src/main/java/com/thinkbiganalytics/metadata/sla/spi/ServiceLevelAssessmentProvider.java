package com.thinkbiganalytics.metadata.sla.spi;

import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment;

import java.util.List;

/**
 * Created by sr186054 on 7/23/16.
 */
public interface ServiceLevelAssessmentProvider {


    List<? extends ServiceLevelAssessment> getAssessments();

    ServiceLevelAssessment findServiceLevelAssessment(ServiceLevelAssessment.ID id);

    ServiceLevelAssessment findLatestAssessment(ServiceLevelAgreement.ID slaId);

    ServiceLevelAssessment findLatestAssessmentNotEqualTo(ServiceLevelAgreement.ID slaId, ServiceLevelAssessment.ID assessmentId);


    /**
     * Ensure the assessment.getAgreement() is not null and if it is it will query and get the correct agreement according to the slaId on the assessment
     */
    boolean ensureServiceLevelAgreementOnAssessment(ServiceLevelAssessment assessment);


}
