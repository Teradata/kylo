package com.thinkbiganalytics.metadata.modeshape.sla;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.jcr.Node;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Sets;
import com.thinkbiganalytics.metadata.modeshape.common.AbstractJcrAuditableSystemEntity;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.MetricAssessment;
import com.thinkbiganalytics.metadata.sla.api.Obligation;
import com.thinkbiganalytics.metadata.sla.api.ObligationAssessment;

/**
 * Created by sr186054 on 7/23/16.
 */
public class JcrObligationAssessment extends AbstractJcrAuditableSystemEntity implements ObligationAssessment {

    public static String OBLIGATION = "tba:obligation";
    public static String MESSAGE = "tba:message";
    public static String RESULT = "tba:result";
    public static String METRIC_ASSESSMENTS = "tba:metricAssessments";
    public static String NODE_TYPE = "tba:obligationAssessment";

    public static final Comparator<ObligationAssessment> DEF_COMPARATOR = new DefaultComparator();
    private Comparator<ObligationAssessment> comparator = DEF_COMPARATOR;
    private List<Comparable<? extends Serializable>> comparables = Collections.emptyList();

    public JcrObligationAssessment(Node node) {
        super(node);
    }

    public JcrObligationAssessment(Node node, Obligation o) {
        this(node);
        setObligation(o);
    }

    @Override
    public Obligation getObligation() {

        return (Obligation) JcrPropertyUtil.getProperty(this.node, OBLIGATION, true);
    }

    public void setObligation(Obligation obligation) {
        JcrPropertyUtil.setWeakReferenceProperty(this.node, OBLIGATION, ((JcrObligation) obligation).getNode());
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
    public Set<MetricAssessment> getMetricAssessments() {
        return Sets.newHashSet(JcrUtil.getNodes(this.node, METRIC_ASSESSMENTS, JcrMetricAssessment.class));
    }


    protected void setComparator(Comparator<ObligationAssessment> comparator) {
        this.comparator = comparator;
    }

    protected void setComparables(List<Comparable<? extends Serializable>> comparables) {
        this.comparables = comparables;
    }

    @Override
    public int compareTo(ObligationAssessment obAssessment) {
        return this.comparator.compare(this, obAssessment);
    }


    protected static class DefaultComparator implements Comparator<ObligationAssessment> {

        @Override
        public int compare(ObligationAssessment o1, ObligationAssessment o2) {
            ComparisonChain chain = ComparisonChain
                .start()
                .compare(o1.getResult(), o2.getResult());

            if (o1 instanceof JcrObligationAssessment && o2 instanceof JcrObligationAssessment) {
                JcrObligationAssessment s1 = (JcrObligationAssessment) o1;
                JcrObligationAssessment s2 = (JcrObligationAssessment) o2;

                for (int idx = 0; idx < s1.comparables.size(); idx++) {
                    chain = chain.compare(s1.comparables.get(idx), s2.comparables.get(idx));
                }
            }

            if (chain.result() != 0) {
                return chain.result();
            }

            List<MetricAssessment<Serializable>> list1 = new ArrayList<>(o1.getMetricAssessments());
            List<MetricAssessment<Serializable>> list2 = new ArrayList<>(o2.getMetricAssessments());

            chain = chain.compare(list1.size(), list2.size());

            Collections.sort(list1);
            Collections.sort(list2);

            for (int idx = 0; idx < list1.size(); idx++) {
                chain = chain.compare(list1.get(idx), list2.get(idx));
            }

            return chain.result();
        }

    }


}
