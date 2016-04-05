/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.sla;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Table;

import com.thinkbiganalytics.metadata.sla.api.Metric;

/**
 *
 * @author Sean Felten
 */
@Entity
@Table(name="SLA_METRIC")
public class JpaMetric implements Metric, Serializable {
    
    private static final long serialVersionUID = 3241658544122855509L;
    
    private String description;

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.api.Metric#getDescription()
     */
    @Override
    public String getDescription() {
        return this.description;
    }

}
