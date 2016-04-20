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
public class ServiceLevelAgreement {

    private String id;
    private String name;
    private String description;
    private List<Obligation> obligations; // either a list of obligations a list of groups, but not both.
    private List<ObligationGroup> groups;
    
    public ServiceLevelAgreement() {
    }
    
    public ServiceLevelAgreement(String id, String name, String description) {
        super();
        this.id = id;
        this.name = name;
        this.description = description;
        this.obligations = null;
        this.groups = null;
    }

    public ServiceLevelAgreement(String id, String name, String description, Obligation... obligations) {
        super();
        this.id = id;
        this.name = name;
        this.description = description;
        this.obligations = Arrays.asList(obligations);
        this.groups = null;
    }

    public ServiceLevelAgreement(String id, String name, String description, ObligationGroup... groups) {
        super();
        this.id = id;
        this.name = name;
        this.description = description;
        this.obligations = null;
        this.groups = Arrays.asList(groups);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Obligation> getObligations() {
        return obligations;
    }

    public void setObligations(List<Obligation> obligations) {
        this.obligations = obligations;
        this.groups = null;
    }
    
    public void addObligation(Obligation ob) {
        if (this.obligations == null) {
            this.obligations = new ArrayList<>();
        }

        this.obligations.add(ob);
    }

    public List<ObligationGroup> getGroups() {
        return groups;
    }

    public void setGroups(List<ObligationGroup> groups) {
        this.groups = groups;
        this.obligations = null;
    }
    
    public void addGroup(ObligationGroup group) {
        if (this.groups == null) {
            this.groups = new ArrayList<>();
        }

        this.groups.add(group);
    }
    
}
