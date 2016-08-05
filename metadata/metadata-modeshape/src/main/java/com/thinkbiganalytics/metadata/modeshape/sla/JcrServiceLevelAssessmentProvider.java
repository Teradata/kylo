/**
 *
 */
package com.thinkbiganalytics.metadata.modeshape.sla;

import com.thinkbiganalytics.metadata.modeshape.BaseJcrProvider;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.support.JcrQueryUtil;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAssessmentProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

public class JcrServiceLevelAssessmentProvider extends BaseJcrProvider<ServiceLevelAssessment, ServiceLevelAssessment.ID> implements ServiceLevelAssessmentProvider {

    private static final Logger log = LoggerFactory.getLogger(JcrServiceLevelAssessmentProvider.class);

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
        String query = "SELECT * FROM [" + JcrServiceLevelAssessment.SLA + "] AS sla  "
                       + "INNER JOIN  [" + getNodeType() + "] as assessment ON ISCHILDNODE(assessment,sla) "
                       + "WHERE sla.[jcr:uuid] = '" + slaId + "'"
                       + "ORDER BY assessment.[jcr:created] DESC ";
try {
    ServiceLevelAssessment entity = null;
    QueryResult result = JcrQueryUtil.query(getSession(), query);
        if (result != null) {
            RowIterator rowIterator = result.getRows();
            if (rowIterator.hasNext()) {
                Row row = rowIterator.nextRow();
                Node node = row.getNode("assessment");
               entity = constructEntity(node);
            }
        }
    return entity;
    } catch (RepositoryException e) {
        throw new MetadataRepositoryException("Unable to findAll for Type : " + getNodeType(), e);
    }

    }

    @Override
    public ServiceLevelAssessment findServiceLevelAssessment(ServiceLevelAssessment.ID id) {
        return super.findById(id);
    }




}
