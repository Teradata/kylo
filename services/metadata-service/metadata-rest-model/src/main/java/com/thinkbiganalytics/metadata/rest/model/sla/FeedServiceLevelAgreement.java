package com.thinkbiganalytics.metadata.rest.model.sla;

/*-
 * #%L
 * thinkbig-metadata-rest-model
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thinkbiganalytics.metadata.rest.model.feed.Feed;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeedServiceLevelAgreement extends ServiceLevelAgreement {

    private ServiceLevelAgreement serviceLevelAgreement;
    private Set<Feed> feeds;


    public FeedServiceLevelAgreement(com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAgreement restModel) {
        this.serviceLevelAgreement = restModel;
    }

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


    @Override
    public void addSlaCheckError(String error) {
        serviceLevelAgreement.addSlaCheckError(error);
    }

    @Override
    public void addObligationError(String error) {
        serviceLevelAgreement.addObligationError(error);
    }

    @Override
    public List<String> getObligationErrors() {
        return serviceLevelAgreement.getObligationErrors();
    }

    @Override
    public void setObligationErrors(List<String> obligationErrors) {
        serviceLevelAgreement.setObligationErrors(obligationErrors);
    }

    @Override
    public List<String> getSlaCheckErrors() {
        return serviceLevelAgreement.getSlaCheckErrors();
    }

    @Override
    public void setSlaCheckErrors(List<String> slaCheckErrors) {
        serviceLevelAgreement.setSlaCheckErrors(slaCheckErrors);
    }

    @JsonIgnore
    public int getFeedsCount() {
        return feeds != null ? feeds.size() : 0;
    }
}
