/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.sla;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.thinkbiganalytics.jpa.AbstractAuditedEntity;
import com.thinkbiganalytics.jpa.JsonAttributeConverter;
import com.thinkbiganalytics.metadata.sla.api.Metric;

/**
 *
 * @author Sean Felten
 */
@Entity
@Table(name = "SLA_METRIC")
public class JpaMetricWrapper extends AbstractAuditedEntity implements Serializable {

    private static final long serialVersionUID = 3241658544122855509L;

    @Id
    @GeneratedValue
    private UUID id;

    @Convert(converter = JsonAttributeConverter.class)
    private Object metric;

    public JpaMetricWrapper() {
    }

    public JpaMetricWrapper(Metric metric) {
        super();
        this.metric = metric;
    }

    public Metric getMetric() {
        return (Metric) metric;
    }

    public void setMetric(Metric metric) {
        this.metric = metric;
    }

}
