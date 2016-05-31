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
    private ObligationGroup defaultGroup = new ObligationGroup("REQUIRED");
    private List<ObligationGroup> groups = new ArrayList<>();
    
    public ServiceLevelAgreement() {
    }
    
    public ServiceLevelAgreement(String id, String name, String description) {
        super();
        this.id = id;
        this.name = name;
        this.description = description;
        this.defaultGroup = null;
        this.groups = null;
    }

    public ServiceLevelAgreement(String id, String name, String description, Obligation... obligations) {
        super();
        this.id = id;
        this.name = name;
        this.description = description;
        this.groups = null;
        

    }

    public ServiceLevelAgreement(String id, String name, String description, ObligationGroup... groups) {
        super();
        this.id = id;
        this.name = name;
        this.description = description;
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
        if (! this.defaultGroup.getObligations().isEmpty() && this.groups.isEmpty()) {
            return this.defaultGroup.getObligations();
        } else {
            return null;
        }
    }
    
    public void addObligation(Obligation ob) {
        this.defaultGroup.addObligation(ob);
    }

    public List<ObligationGroup> getGroups() {
        List<ObligationGroup> all = new ArrayList<>();
        
        if (! this.defaultGroup.getObligations().isEmpty()) {
            all.add(defaultGroup);
        }
        
        all.addAll(this.groups);
        
        return all;
    }

    public void setGroups(List<ObligationGroup> groups) {
        this.groups = new ArrayList<>(groups);
        this.defaultGroup.getObligations().clear();
    }
    
    public void addGroup(ObligationGroup group) {
        this.groups.add(group);
    }
    
    protected ObligationGroup getDefaultGroup() {
        return defaultGroup;
    }
    
    protected void setDefaultGroup(ObligationGroup defaultGroup) {
        this.defaultGroup = defaultGroup;
    }

}
