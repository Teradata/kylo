/**
 *
 */
package com.thinkbiganalytics.metadata.modeshape.sla;

import com.thinkbiganalytics.metadata.modeshape.BaseJcrProvider;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAssessmentProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;

public class JcrServiceLevelAssessmentProvider extends BaseJcrProvider<ServiceLevelAssessment, ServiceLevelAssessment.ID> implements ServiceLevelAssessmentProvider {

    private static final Logger log = LoggerFactory.getLogger(JcrServiceLevelAssessmentProvider.class);

    public static final String SLA_ASSESSMENT_PATH = "/metadata/sla/slaAssessments";

    @Override
    public Class<? extends ServiceLevelAssessment> getEntityClass() {
        return JcrServiceLevelAssessment.class;
    }

    @Override
    public Class<? extends JcrEntity> getJcrEntityClass() {
        return JcrServiceLevelAssessment.class;
    }


    @Override
    public String getNodeType() {
        return "tba:serviceLevelAssessment";
    }


    @Override
    public ServiceLevelAssessment.ID resolveId(Serializable ser) {
        if (ser instanceof JcrServiceLevelAssessment.AssessmentId) {
            return (JcrServiceLevelAssessment.AssessmentId) ser;
        } else {
            return new JcrServiceLevelAssessment.AssessmentId(ser);
        }
    }

    @Override
    public List<ServiceLevelAssessment> getAssessments() {
        return findAll();
    }


    //find last assessment
    public ServiceLevelAssessment findLatestAssessment(ServiceLevelAgreement.ID slaId) {
        String query = "SELECT * FROM [" + getNodeType() + "] as assessment WHERE assessment.[" + JcrServiceLevelAssessment.SLA + "] = '" + slaId + "'"
                       + " ORDER BY assessment.[jcr:createTime] desc ";
        return findFirst(query);
    }

    @Override
    public ServiceLevelAssessment findServiceLevelAssessment(ServiceLevelAssessment.ID id) {
        return super.findById(id);
    }
}
