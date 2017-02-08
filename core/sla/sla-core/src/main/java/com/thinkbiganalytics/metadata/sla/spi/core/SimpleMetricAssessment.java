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
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.MetricAssessment;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 */
public class SimpleMetricAssessment<D extends Serializable> implements MetricAssessment<D> {

    private static final long serialVersionUID = -209788646749034842L;

    private Metric metric;
    private String message = "";
    private AssessmentResult result = AssessmentResult.SUCCESS;
    private D data;
    private Comparator<MetricAssessment<D>> comparator = new DefaultComparator();
    private List<Comparable<? extends Serializable>> comparables = Collections.emptyList();

    /**
     *
     */
    protected SimpleMetricAssessment() {
        super();
    }

    public SimpleMetricAssessment(Metric metric) {
        this();
        this.metric = metric;
    }

    public SimpleMetricAssessment(Metric metric, String message, AssessmentResult result) {
        this();
        this.metric = metric;
        this.message = message;
        this.result = result;
    }

    @Override
    public Metric getMetric() {
        return this.metric;
    }

    protected void setMetric(Metric metric) {
        this.metric = metric;
    }

    @Override
    public String getMetricDescription() {
        return getMetric() != null ? getMetric().getDescription() : null;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    protected void setMessage(String message) {
        this.message = message;
    }

    @Override
    public AssessmentResult getResult() {
        return this.result;
    }

    protected void setResult(AssessmentResult result) {
        this.result = result;
    }

    @Override
    public D getData() {
        return this.data;
    }

    public void setData(D data) {
        this.data = data;
    }

    @Override
    public int compareTo(MetricAssessment<D> metric) {
        return this.comparator.compare(this, metric);
    }

    protected void setComparator(Comparator<MetricAssessment<D>> comparator) {
        this.comparator = comparator;
    }

    protected void setComparables(List<Comparable<? extends Serializable>> comparables) {
        this.comparables = comparables;
    }

    protected class DefaultComparator implements Comparator<MetricAssessment<D>> {

        @Override
        public int compare(MetricAssessment<D> o1, MetricAssessment<D> o2) {
            ComparisonChain chain = ComparisonChain
                .start()
                .compare(o1.getResult(), o2.getResult());

            if (o1 instanceof SimpleMetricAssessment<?> && o2 instanceof SimpleMetricAssessment<?>) {
                SimpleMetricAssessment<?> s1 = (SimpleMetricAssessment<?>) o1;
                SimpleMetricAssessment<?> s2 = (SimpleMetricAssessment<?>) o2;

                for (int idx = 0; idx < s1.comparables.size(); idx++) {
                    chain = chain.compare(s1.comparables.get(idx), s2.comparables.get(idx));
                }
            }

            return chain.result();
        }
    }

}
