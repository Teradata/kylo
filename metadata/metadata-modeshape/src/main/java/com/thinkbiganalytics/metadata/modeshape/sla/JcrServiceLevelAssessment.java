package com.thinkbiganalytics.metadata.modeshape.sla;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.joda.time.DateTime;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Sets;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.AbstractJcrAuditableSystemEntity;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.ObligationAssessment;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment;

/**
 * Created by sr186054 on 7/23/16.
 */
public class JcrServiceLevelAssessment extends AbstractJcrAuditableSystemEntity implements ServiceLevelAssessment {

    public static final String SLA = "tba:sla";
    public static final String NODE_TYPE = "tba:serviceLevelAssessment";

    public static String MESSAGE = "tba:message";
    public static String RESULT = "tba:result";
    public static String OBLIGATION_ASSESSMENTS = "tba:obligationAssessments";


    public JcrServiceLevelAssessment(Node node) {
        super(node);
    }


    public JcrServiceLevelAssessment(Node node, JcrServiceLevelAgreement sla) {
        super(node);
      //  JcrPropertyUtil.setWeakReferenceProperty(this.node, SLA, sla.getNode());
    }

    public AssessmentId getId() {
        try {
            return new AssessmentId(getObjectId());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the Assessment ID", e);
        }
    }

    @Override
    public DateTime getTime() {
        return null;
    }

    @Override
    public ServiceLevelAgreement getAgreement() {
        try {
            Node slaNode = this.node.getParent();
            return new JcrServiceLevelAgreement(slaNode);
        }
        catch(RepositoryException e){
            throw new MetadataRepositoryException("Unable to get SLA for Assessment ",e);
        }
    }

    @Override
    public String getMessage() {
        return JcrPropertyUtil.getString(this.node, MESSAGE);
    }


    public void setMessage(String message) {
        JcrPropertyUtil.setProperty(this.node, MESSAGE, message);
    }

    @Override
    public AssessmentResult getResult() {
        return JcrPropertyUtil.getEnum(this.node, RESULT, AssessmentResult.class, AssessmentResult.FAILURE);
    }

    public void setResult(AssessmentResult result) {
        JcrPropertyUtil.setProperty(this.node, RESULT, result);
    }

    @Override
    public Set<ObligationAssessment> getObligationAssessments() {
        return Sets.newHashSet(JcrUtil.getNodes(this.node, OBLIGATION_ASSESSMENTS, JcrObligationAssessment.class));
    }


    public void add(ObligationAssessment obligationAssessment) {
        JcrPropertyUtil.addToSetProperty(this.node, OBLIGATION_ASSESSMENTS, obligationAssessment);
    }

    public static class AssessmentId extends JcrEntity.EntityId implements ServiceLevelAssessment.ID {

        public AssessmentId(Serializable ser) {
            super(ser);
        }
    }


    @Override
    public int compareTo(ServiceLevelAssessment sla) {

        ComparisonChain chain = ComparisonChain
            .start()
            .compare(this.getResult(), sla.getResult())
            .compare(this.getAgreement().getName(), sla.getAgreement().getName());

        if (chain.result() != 0) {
            return chain.result();
        }

        List<ObligationAssessment> list1 = new ArrayList<>(this.getObligationAssessments());
        List<ObligationAssessment> list2 = new ArrayList<>(sla.getObligationAssessments());

        chain = chain.compare(list1.size(), list2.size());

        Collections.sort(list1);
        Collections.sort(list2);

        for (int idx = 0; idx < list1.size(); idx++) {
            chain = chain.compare(list1.get(idx), list2.get(idx));
        }

        return chain.result();
    }
}
