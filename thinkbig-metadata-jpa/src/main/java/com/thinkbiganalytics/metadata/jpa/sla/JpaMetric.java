/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.sla;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.thinkbiganalytics.metadata.sla.api.Metric;

/**
 *
 * @author Sean Felten
 */
@Entity
@Table(name="SLA_METRIC")
public abstract class JpaMetric implements Metric, Serializable {
    
    private static final long serialVersionUID = 3241658544122855509L;
    
    @Id
    @GeneratedValue
    private UUID id;

    private String description;
    private String json;

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.api.Metric#getDescription()
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
