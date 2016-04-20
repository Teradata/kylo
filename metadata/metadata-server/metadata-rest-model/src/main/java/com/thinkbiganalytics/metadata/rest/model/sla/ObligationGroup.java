/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.model.sla;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 *
 * @author Sean Felten
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ObligationGroup {

    private String condition;
    private List<Obligation> obligations;

    public ObligationGroup() {
        this.obligations = new ArrayList<>();
    }
    
    public ObligationGroup(String condition) {
        this();
        this.condition = condition;
    }

    public ObligationGroup(String condition, List<Obligation> obligations) {
        super();
        this.condition = condition;
        this.obligations = obligations;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public List<Obligation> getObligations() {
        return obligations;
    }

    public void setObligations(List<Obligation> obligations) {
        this.obligations = obligations;
    }
    
    public void addObligation(Obligation ob) {
        this.obligations.add(ob);
    }

}
