/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.model.sla;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.collect.Lists;
import com.thinkbiganalytics.metadata.sla.api.Metric;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
    private ObligationGroup defaultGroup;
    private List<ObligationGroup> groups;

    private List<ServiceLevelAgreementCheck> slaChecks;
    
    public ServiceLevelAgreement() {
        this.defaultGroup = new ObligationGroup("REQUIRED");
        this.groups = Lists.newArrayList(this.defaultGroup);
    }
    
    public ServiceLevelAgreement(String id, String name, String description) {
        this();
        this.id = id;
        this.name = name;
        this.description = description;
    }
    
    public ServiceLevelAgreement(String name, Metric... metrics) {
        this(null, name, "", metrics);
    }
    
    public ServiceLevelAgreement(String id, String name, String description, Metric... metrics) {
        this(id, name, description, new Obligation("", metrics));
    }

    public ServiceLevelAgreement(String id, String name, String description, Obligation... obligations) {
        this(id, name, description);
        
        for (Obligation ob : obligations) {
            this.defaultGroup.addObligation(ob);
        }
    }

    public ServiceLevelAgreement(String id, String name, String description, ObligationGroup... groups) {
        this(id, name, description);

        this.groups.addAll(Arrays.asList(groups));
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

    @JsonIgnore
    public List<Obligation> getObligations() {
        return this.groups.stream()
                        .flatMap((grp) -> grp.getObligations().stream())
                        .collect(Collectors.toList());
    }
    
    public void addObligation(Obligation ob) {
        this.defaultGroup.addObligation(ob);
    }

    public List<ObligationGroup> getGroups() {
        return this.groups.stream().filter(grp -> ! grp.getObligations().isEmpty()).collect(Collectors.toList());
    }

    public void setGroups(List<ObligationGroup> groups) {
        this.defaultGroup.getObligations().clear();
        this.groups = Lists.asList(this.defaultGroup, groups.toArray(new ObligationGroup[groups.size()]));
    }
    
    public void addGroup(ObligationGroup group) {
        this.groups.add(group);
    }

    public ObligationGroup getDefaultGroup() {
        return defaultGroup;
    }

    public void setDefaultGroup(ObligationGroup defaultGroup) {
        this.defaultGroup = defaultGroup;
    }

    public List<ServiceLevelAgreementCheck> getSlaChecks() {
        return slaChecks;
    }

    public void setSlaChecks(List<ServiceLevelAgreementCheck> slaChecks) {
        this.slaChecks = slaChecks;
    }
}
