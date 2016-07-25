package com.thinkbiganalytics.metadata.modeshape.sla;

import com.google.common.collect.ComparisonChain;
import com.thinkbiganalytics.metadata.modeshape.common.AbstractJcrAuditableSystemEntity;
import com.thinkbiganalytics.metadata.modeshape.common.JcrPropertyConstants;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.MetricAssessment;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.jcr.Node;

/**
 * Created by sr186054 on 7/23/16.
 */
public class JcrMetricAssessment<D extends Serializable> extends AbstractJcrAuditableSystemEntity implements MetricAssessment<D> {

    public static String METRIC = "tba:metric";
    public static String MESSAGE = "tba:message";
    public static String RESULT = "tba:result";
    public static String RESOURCE = "nt:resource";
    public static String DATA = "tba:json";

    public static String NODE_TYPE = "tba:metricAssessment";

    private Comparator<MetricAssessment<D>> comparator = new DefaultComparator();
    private List<Comparable<? extends Serializable>> comparables = Collections.emptyList();


    public JcrMetricAssessment(Node node) {
        super(node);
    }

    public JcrMetricAssessment(Node node, Node metricNode) {
        super(node);
        //ENSURE ITS SET AS WEAK REF
        JcrPropertyUtil.setWeakReferenceProperty(this.node, METRIC, metricNode);
    }

    @Override
    public Metric getMetric() {
        Node metricNode = (Node) JcrPropertyUtil.getProperty(this.node, METRIC);
        return JcrUtil.getGenericJson(metricNode, JcrPropertyConstants.JSON);

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
    public D getData() {
        return (D) JcrPropertyUtil.getProperty(this.node, RESOURCE);
    }


    public void setData(D data) {

    }

    protected void setComparator(Comparator<MetricAssessment<D>> comparator) {
        this.comparator = comparator;
    }

    protected void setComparables(List<Comparable<? extends Serializable>> comparables) {
        this.comparables = comparables;
    }


    @Override
    public int compareTo(MetricAssessment<D> metric) {
        return this.comparator.compare(this, metric);
    }

    protected class DefaultComparator implements Comparator<MetricAssessment<D>> {

        @Override
        public int compare(MetricAssessment<D> o1, MetricAssessment<D> o2) {
            ComparisonChain chain = ComparisonChain
                .start()
                .compare(o1.getResult(), o2.getResult());

            if (o1 instanceof JcrMetricAssessment<?> && o2 instanceof JcrMetricAssessment<?>) {
                JcrMetricAssessment<?> s1 = (JcrMetricAssessment<?>) o1;
                JcrMetricAssessment<?> s2 = (JcrMetricAssessment<?>) o2;

                for (int idx = 0; idx < s1.comparables.size(); idx++) {
                    chain = chain.compare(s1.comparables.get(idx), s2.comparables.get(idx));
                }
            }

            return chain.result();
        }
    }


}
