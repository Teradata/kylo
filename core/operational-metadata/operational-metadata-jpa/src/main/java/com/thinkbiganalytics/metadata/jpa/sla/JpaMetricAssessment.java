package com.thinkbiganalytics.metadata.jpa.sla;

/*-
 * #%L
 * thinkbig-operational-metadata-jpa
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
import com.thinkbiganalytics.jpa.AbstractAuditedEntity;
import com.thinkbiganalytics.jpa.JsonAttributeConverter;
import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.MetricAssessment;
import com.thinkbiganalytics.metadata.sla.api.ObligationAssessment;

import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Entity representing metric assessment results for Service Level Agreement (SLA).
 * SLA's are defined in Modeshape, but their assessments are stored here
 * Metric assessments are attached to {@link JpaObligationAssessment} which are attached to the overall {@link JpaServiceLevelAssessment}
 */
@Entity
@Table(name = "SLA_METRIC_ASSESSMENT")
public class JpaMetricAssessment<D extends Serializable> extends AbstractAuditedEntity implements MetricAssessment<D> {


    @Transient
    private Comparator<MetricAssessment<D>> comparator = new DefaultComparator();

    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @Transient
    private Metric metric;

    @Column(name = "MESSAGE")
    @Type(type = "com.thinkbiganalytics.jpa.TruncateStringUserType", parameters = {@Parameter(name = "length", value = "255")})
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "RESULT")
    private AssessmentResult result;

    @ManyToOne(optional = true, targetEntity = JpaObligationAssessment.class)
    @JoinColumn(name = "SLA_OBLIGATION_ASSESSMENT_ID", nullable = true, insertable = true, updatable = true)
    private ObligationAssessment obligationAssessment;

    @Convert(converter = ComparablesAttributeConverter.class)
    @Column(name = "COMPARABLES")
    private List<Comparable<? extends Serializable>> comparables = Collections.emptyList();

    @Column(name = "DATA")
    private String serializedData;

    @Column(name = "METRIC_TYPE")
    @Type(type = "com.thinkbiganalytics.jpa.TruncateStringUserType", parameters = {@Parameter(name = "length", value = "255")})
    private String metricType;

    @Column(name = "METRIC_DESCRIPTION")
    @Type(type = "com.thinkbiganalytics.jpa.TruncateStringUserType", parameters = {@Parameter(name = "length", value = "255")})
    private String metricDescription;


    public JpaMetricAssessment() {

    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Metric getMetric() {
        return metric;
    }

    public void setMetric(Metric metric) {
        this.metric = metric;
        if (metric != null) {
            this.metricType = metric.getClass().getName();
        }
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public AssessmentResult getResult() {
        return result;
    }

    public void setResult(AssessmentResult result) {
        this.result = result;
    }

    public ObligationAssessment getObligationAssessment() {
        return obligationAssessment;
    }

    public void setObligationAssessment(ObligationAssessment obligationAssessment) {
        this.obligationAssessment = obligationAssessment;
    }

    public String getSerializedData() {
        return serializedData;
    }

    @Override
    public D getData() {
        if (this.serializedData != null) {
            JsonAttributeConverter c = new JsonAttributeConverter();
            return (D) c.convertToEntityAttribute(this.serializedData);
        } else {
            return null;
        }
    }

    public void setData(D data) {
        if (data != null) {
            JsonAttributeConverter c = new JsonAttributeConverter();
            String stringData = c.convertToDatabaseColumn(data);
            this.serializedData = stringData;
        } else {
            this.serializedData = null;
        }
    }

    protected void setComparator(Comparator<MetricAssessment<D>> comparator) {
        this.comparator = comparator;
    }

    protected void setComparables(List<Comparable<? extends Serializable>> comparables) {
        this.comparables = comparables;
    }

    public String getMetricDescription() {
        return metricDescription;
    }

    public void setMetricDescription(String metricDescription) {
        this.metricDescription = metricDescription;
    }

    @Override
    public int compareTo(MetricAssessment<D> metric) {
        return this.comparator.compare(this, metric);
    }

    public static class ComparablesAttributeConverter extends JsonAttributeConverter<List<Comparable<? extends Serializable>>> {

    }

    protected class DefaultComparator implements Comparator<MetricAssessment<D>> {

        @Override
        public int compare(MetricAssessment<D> o1, MetricAssessment<D> o2) {
            ComparisonChain chain = ComparisonChain
                .start()
                .compare(o1.getResult(), o2.getResult());

            if (o1 instanceof JpaMetricAssessment<?> && o2 instanceof JpaMetricAssessment<?>) {
                JpaMetricAssessment<?> s1 = (JpaMetricAssessment<?>) o1;
                JpaMetricAssessment<?> s2 = (JpaMetricAssessment<?>) o2;

                for (int idx = 0; idx < s1.comparables.size(); idx++) {
                    Comparable comparables1 = s1.comparables.get(idx);
                    Comparable comparables2 = s2.comparables.get(idx);
                    chain = chain.compare(comparables1, comparables2);
                }
            }

            return chain.result();
        }
    }
}
