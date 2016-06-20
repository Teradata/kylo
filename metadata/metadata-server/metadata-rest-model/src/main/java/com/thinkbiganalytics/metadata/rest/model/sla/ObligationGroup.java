/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.model.sla;

import java.util.ArrayList;
import java.util.Arrays;
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

    private String condition = "REQUIRED";
    private List<Obligation> obligations = new ArrayList<>();

    public ObligationGroup() {
        super();
    }
    
    public ObligationGroup(String condition) {
        this();
        this.condition = condition;
    }
    
    public ObligationGroup(Obligation... obligations) {
        this("REQUIRED", Arrays.asList(obligations));
    }
    
    public ObligationGroup(String condition, Obligation... obligations) {
        this(condition, Arrays.asList(obligations));
    }

    public ObligationGroup(String condition, List<Obligation> obligations) {
        this(condition);
        this.obligations.addAll(obligations);
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
