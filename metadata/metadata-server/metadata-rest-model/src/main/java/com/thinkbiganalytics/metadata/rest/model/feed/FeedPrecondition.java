/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.model.feed;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.thinkbiganalytics.metadata.rest.model.sla.Metric;
import com.thinkbiganalytics.metadata.rest.model.sla.Obligation;
import com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAgreement;

/**
 *
 * @author Sean Felten
 */
@SuppressWarnings("serial")
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeedPrecondition implements Serializable {

    private ServiceLevelAgreement sla;
    
    public FeedPrecondition() {
        this("Feed preconditon");
    }
    
    public FeedPrecondition(String name) {
        super();
        this.sla = new ServiceLevelAgreement("Feed " + name + " precondition");
    }
    
    public FeedPrecondition(String name, String description, List<Metric> metrics) {
        this(name);
        this.addMetrics(description, metrics);
    }
    
    public void addMetrics(String description, Metric... metrics) {
        addMetrics(description, Arrays.asList(metrics));
    }
    
    public void addMetrics(String description, List<Metric> metrics) {
        Obligation ob = new Obligation(description, metrics);
        this.sla.addObligation(ob);
    }
    
    public ServiceLevelAgreement getSla() {
        return sla;
    }
    
    public void setSla(ServiceLevelAgreement sla) {
        this.sla = sla;
    }
}
