package com.thinkbiganalytics.metadata.rest.model.sla;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thinkbiganalytics.metadata.rest.model.feed.Feed;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by sr186054 on 8/5/16.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeedServiceLevelAgreement extends ServiceLevelAgreement {

    private ServiceLevelAgreement serviceLevelAgreement;


    public FeedServiceLevelAgreement(com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAgreement restModel) {
        this.serviceLevelAgreement = restModel;
    }


    private Set<Feed> feeds;

    public Set<Feed> getFeeds() {
        return feeds;
    }

    public void setFeeds(Set<Feed> feeds) {
        this.feeds = feeds;
    }


    @JsonProperty("feedNames")
    public String getFeedNames() {
        String feedNames = "";
        if (feeds != null && !feeds.isEmpty()) {
            feedNames = feeds.stream().map(feed -> feed.getCategory().getSystemName() + "." + feed.getSystemName()).collect(Collectors.joining(","));
        }
        return feedNames;
    }

    @Override
    public String getId() {
        return serviceLevelAgreement.getId();
    }

    @Override
    public void setId(String id) {
        serviceLevelAgreement.setId(id);
    }

    @Override
    public String getName() {
        return serviceLevelAgreement.getName();
    }

    @Override
    public void setName(String name) {
        serviceLevelAgreement.setName(name);
    }

    @Override
    public String getDescription() {
        return serviceLevelAgreement.getDescription();
    }

    @Override
    public void setDescription(String description) {
        serviceLevelAgreement.setDescription(description);
    }

    @Override
    @JsonIgnore
    public List<Obligation> getObligations() {
        return serviceLevelAgreement.getObligations();
    }

    @Override
    public void addObligation(Obligation ob) {
        serviceLevelAgreement.addObligation(ob);
    }

    @Override
    public List<ObligationGroup> getGroups() {
        return serviceLevelAgreement.getGroups();
    }

    @Override
    public void setGroups(List<ObligationGroup> groups) {
        serviceLevelAgreement.setGroups(groups);
    }

    @Override
    public void addGroup(ObligationGroup group) {
        serviceLevelAgreement.addGroup(group);
    }

    @Override
    public ObligationGroup getDefaultGroup() {
        return serviceLevelAgreement.getDefaultGroup();
    }

    @Override
    public void setDefaultGroup(ObligationGroup defaultGroup) {
        serviceLevelAgreement.setDefaultGroup(defaultGroup);
    }

    @Override
    public List<ServiceLevelAgreementCheck> getSlaChecks() {
        return serviceLevelAgreement.getSlaChecks();
    }

    @Override
    public void setSlaChecks(List<ServiceLevelAgreementCheck> slaChecks) {
        serviceLevelAgreement.setSlaChecks(slaChecks);
    }
}
