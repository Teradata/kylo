package com.thinkbiganalytics.metadata.sla.spi;

import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment;

import java.util.List;

/**
 * Created by sr186054 on 7/23/16.
 */
public interface ServiceLevelAssessmentProvider {


    List<ServiceLevelAssessment> getAssessments();

    ServiceLevelAssessment findServiceLevelAssessment(ServiceLevelAssessment.ID id);

    ServiceLevelAssessment findLatestAssessment(ServiceLevelAgreement.ID slaId);


}
