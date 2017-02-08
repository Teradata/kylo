/**
 *
 */
package com.thinkbiganalytics.metadata.sla.spi.core;

/*-
 * #%L
 * thinkbig-sla-core
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.common.collect.ComparisonChain;
import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.MetricAssessment;
import com.thinkbiganalytics.metadata.sla.api.Obligation;
import com.thinkbiganalytics.metadata.sla.api.ObligationAssessment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class SimpleObligationAssessment implements ObligationAssessment {

    public static final Comparator<ObligationAssessment> DEF_COMPARATOR = new DefaultComparator();
    private static final long serialVersionUID = -6209570471757886664L;
    private Obligation obligation;
    private String message = "";
    private AssessmentResult result = AssessmentResult.SUCCESS;
    private Set<MetricAssessment> metricAssessments;
    private Comparator<ObligationAssessment> comparator = DEF_COMPARATOR;
    private List<Comparable<? extends Serializable>> comparables = Collections.emptyList();

    /**
     *
     */
    protected SimpleObligationAssessment() {
        this.metricAssessments = new HashSet<MetricAssessment>();
    }

    public SimpleObligationAssessment(Obligation obligation) {
        this();
        this.obligation = obligation;
    }

    public SimpleObligationAssessment(Obligation obligation, String message, AssessmentResult result) {
        this();
        this.obligation = obligation;
        this.message = message;
        this.result = result;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.api.ObligationAssessment#getObligation()
     */
    @Override
    public Obligation getObligation() {
        return this.obligation;
    }

    protected void setObligation(Obligation obligation) {
        this.obligation = obligation;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.api.ObligationAssessment#getMessage()
     */
    @Override
    public String getMessage() {
        return this.message;
    }

    protected void setMessage(String message) {
        this.message = message;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.api.ObligationAssessment#getResult()
     */
    @Override
    public AssessmentResult getResult() {
        return this.result;
    }

    protected void setResult(AssessmentResult result) {
        this.result = result;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.api.ObligationAssessment#getMetricAssessments()
     */
    @Override
    public Set<MetricAssessment> getMetricAssessments() {
        return new HashSet<MetricAssessment>(this.metricAssessments);
    }

    protected void setMetricAssessments(Set<MetricAssessment> metricAssessments) {
        this.metricAssessments = metricAssessments;
    }

    @Override
    public int compareTo(ObligationAssessment obAssessment) {
        return this.comparator.compare(this, obAssessment);
    }

    protected boolean add(MetricAssessment assessment) {
        return this.metricAssessments.add(assessment);
    }

    protected boolean addAll(Collection<? extends MetricAssessment> assessments) {
        return this.metricAssessments.addAll(assessments);
    }

    protected void setComparator(Comparator<ObligationAssessment> comparator) {
        this.comparator = comparator;
    }

    protected void setComparables(List<Comparable<? extends Serializable>> comparables) {
        this.comparables = comparables;
    }

    protected static class DefaultComparator implements Comparator<ObligationAssessment> {

        @Override
        public int compare(ObligationAssessment o1, ObligationAssessment o2) {
            ComparisonChain chain = ComparisonChain
                .start()
                .compare(o1.getResult(), o2.getResult());

            if (o1 instanceof SimpleObligationAssessment && o2 instanceof SimpleObligationAssessment) {
                SimpleObligationAssessment s1 = (SimpleObligationAssessment) o1;
                SimpleObligationAssessment s2 = (SimpleObligationAssessment) o2;

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
