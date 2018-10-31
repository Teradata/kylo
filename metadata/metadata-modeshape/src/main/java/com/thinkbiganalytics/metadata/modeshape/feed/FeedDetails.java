/**
 *
 */
package com.thinkbiganalytics.metadata.modeshape.feed;

import com.thinkbiganalytics.metadata.api.catalog.DataSet;

/*-
 * #%L
 * kylo-metadata-modeshape
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

import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.extension.UserFieldDescriptor;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedDestination;
import com.thinkbiganalytics.metadata.api.feed.FeedPrecondition;
import com.thinkbiganalytics.metadata.api.feed.FeedSource;
import com.thinkbiganalytics.metadata.api.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.catalog.dataset.JcrDataSet;
import com.thinkbiganalytics.metadata.modeshape.common.JcrObject;
import com.thinkbiganalytics.metadata.modeshape.common.mixin.AuditableMixin;
import com.thinkbiganalytics.metadata.modeshape.common.mixin.PropertiedMixin;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrDatasource;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeed.FeedId;
import com.thinkbiganalytics.metadata.modeshape.sla.JcrServiceLevelAgreement;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrVersionUtil;
import com.thinkbiganalytics.metadata.modeshape.support.NodeModificationInvocationHandler;
import com.thinkbiganalytics.metadata.modeshape.template.JcrFeedTemplate;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

/**
 *
 */
public class FeedDetails extends JcrObject implements PropertiedMixin, AuditableMixin {

    private static final Logger log = LoggerFactory.getLogger(FeedDetails.class);

    public static final String NODE_TYPE = "tba:feedDetails";

    public static final String FEED_JSON = "tba:json";
    public static final String PROCESS_GROUP_ID = "tba:processGroupId";
    public static final String FEED_TEMPLATE = "tba:feedTemplate";

    public static final String PRECONDITION = "tba:precondition";
    public static final String DEPENDENTS = "tba:dependentFeeds";
    public static final String USED_BY_FEEDS = "tba:usedByFeeds";
    public static final String SOURCE_NAME = "tba:sources";
    public static final String DESTINATION_NAME = "tba:destinations";

    public static final String TEMPLATE = "tba:feedTemplate";
    public static final String SLA = "tba:slas";

    private FeedSummary summary;

    /**
     * @param node
     */
    public FeedDetails(Node node, FeedSummary summary) {
        super(node);
        this.summary = summary;
    }

    protected JcrFeed getParentFeed() {
        return this.summary.getParentFeed();
    }

    protected FeedSummary getParentSummary() {
        return this.summary;
    }
    
    @Override
    public DateTime getModifiedTime() {
        DateTime thisTime = AuditableMixin.super.getModifiedTime();
        
        return getPrecondition()
            .map(JcrFeedPrecondition.class::cast)
            .map(JcrFeedPrecondition::getModifiedTime)
            .filter(time -> time.compareTo(thisTime) > 0)
            .orElse(thisTime);
    }
    
    @Override
    public String getModifiedBy() {
        String thisModifier = getModifiedBy();
        DateTime thisTime = AuditableMixin.super.getModifiedTime();
        
        return getPrecondition()
            .map(JcrFeedPrecondition.class::cast)
            .map(JcrFeedPrecondition::getModifiedTime)
            .filter(time -> time.compareTo(thisTime) > 0)
            .map(time -> thisModifier)
            .orElse(thisModifier);
    }

    public List<? extends FeedSource> getSources() {
        return JcrUtil.getJcrObjects(getNode(), SOURCE_NAME, JcrFeedSource.class);
    }

    public List<? extends FeedDestination> getDestinations() {
        return JcrUtil.getJcrObjects(getNode(), DESTINATION_NAME, JcrFeedDestination.class);
    }

    public <C extends Category> List<Feed> getDependentFeeds() {
        List<Feed> deps = new ArrayList<>();
        Set<Node> depNodes = JcrPropertyUtil.getSetProperty(getNode(), DEPENDENTS);

        for (Node depNode : depNodes) {
            deps.add(new JcrFeed(depNode, this.summary.getParentFeed().getOpsAccessProvider().orElse(null)));
        }

        return deps;
    }

    public boolean addDependentFeed(Feed feed) {
        JcrFeed dependent = (JcrFeed) feed;
        Node depNode = dependent.getNode();
        feed.addUsedByFeed(getParentFeed());

        return JcrPropertyUtil.addToSetProperty(getNode(), DEPENDENTS, depNode, true);
    }

    public boolean removeDependentFeed(Feed feed) {
        JcrFeed dependent = (JcrFeed) feed;
        Node depNode = dependent.getNode();
        feed.removeUsedByFeed(getParentFeed());

        boolean weakRef = false;
        Optional<Property> prop = JcrPropertyUtil.findProperty(getNode(), DEPENDENTS);
        if (prop.isPresent()) {
            try {
                weakRef = PropertyType.WEAKREFERENCE == prop.get().getType();
            } catch (RepositoryException e) {
                log.error("Error removeDependentFeed for {}.  Unable to identify if the property is a Weak Reference or not {} ", feed.getName(), e.getMessage(), e);
            }
        }
        return JcrPropertyUtil.removeFromSetProperty(getNode(), DEPENDENTS, depNode, weakRef);
    }

    public boolean addUsedByFeed(Feed feed) {
        JcrFeed dependent = (JcrFeed) feed;
        Node depNode = dependent.getNode();

        return JcrPropertyUtil.addToSetProperty(getNode(), USED_BY_FEEDS, depNode, true);
    }

    public List<Feed> getUsedByFeeds() {
        List<Feed> deps = new ArrayList<>();
        Set<Node> depNodes = JcrPropertyUtil.getSetProperty(getNode(), USED_BY_FEEDS);

        for (Node depNode : depNodes) {
            deps.add(new JcrFeed(depNode, this.summary.getParentFeed().getOpsAccessProvider().orElse(null)));
        }

        return deps;
    }

    public boolean removeUsedByFeed(Feed feed) {
        JcrFeed dependent = (JcrFeed) feed;
        Node depNode = dependent.getNode();
        boolean weakRef = false;
        Optional<Property> prop = JcrPropertyUtil.findProperty(getNode(), USED_BY_FEEDS);
        if (prop.isPresent()) {
            try {
                weakRef = PropertyType.WEAKREFERENCE == prop.get().getType();
            } catch (RepositoryException e) {
                log.error("Error removeUsedByFeed for {}.  Unable to identify if the property is a Weak Reference or not {} ", feed.getName(), e.getMessage(), e);
            }
        }
        return JcrPropertyUtil.removeFromSetProperty(getNode(), USED_BY_FEEDS, depNode, weakRef);
    }

    public FeedSource getSource(final Datasource.ID id) {
        List<? extends FeedSource> sources = getSources();
        if (sources != null) {
            return sources.stream()
                .filter(feedSource -> feedSource.getDatasource().isPresent())
                .filter(feedSource -> feedSource.getDatasource().get().getId().equals(id))
                .findFirst()
                .orElse(null);
        }
        return null;
    }

    public FeedDestination getDestination(final Datasource.ID id) {
        List<? extends FeedDestination> destinations = getDestinations();
        if (destinations != null) {
            return destinations.stream()
                .filter(feedDestination -> feedDestination.getDatasource().isPresent())
                .filter(feedDestination -> feedDestination.getDatasource().get().getId().equals(id))
                .findFirst()
                .orElse(null);
        }
        return null;
    }
    
    public FeedSource getSource(final DataSet.ID id) {
        List<? extends FeedSource> sources = getSources();
        if (sources != null) {
            return sources.stream()
                .filter(feedSource -> feedSource.getDataSet().isPresent())
                .filter(feedSource -> feedSource.getDataSet().get().getId().equals(id))
                .findFirst()
                .orElse(null);
        }
        return null;
    }
    
    public FeedDestination getDestination(final DataSet.ID id) {
        List<? extends FeedDestination> destinations = getDestinations();
        if (destinations != null) {
            return destinations.stream()
                .filter(feedDestination -> feedDestination.getDataSet().isPresent())
                .filter(feedDestination -> feedDestination.getDataSet().get().getId().equals(id))
                .findFirst()
                .orElse(null);
        }
        return null;
    }

    public Optional<FeedPrecondition> getPrecondition() {
        return Optional.ofNullable(JcrUtil.getJcrObject(getNode(), PRECONDITION, JcrFeedPrecondition.class, getParentFeed()));
    }

    public FeedManagerTemplate getTemplate() {
        return getProperty(TEMPLATE, JcrFeedTemplate.class);
    }

    public void setTemplate(FeedManagerTemplate template) {
        setProperty(TEMPLATE, template);
        template.addFeed(getParentFeed());
    }

    public List<ServiceLevelAgreement> getServiceLevelAgreements() {
        Set<Node> list = JcrPropertyUtil.getReferencedNodeSet(getNode(), SLA);
        List<ServiceLevelAgreement> serviceLevelAgreements = new ArrayList<>();
        if (list != null) {
            for (Node n : list) {
                serviceLevelAgreements.add(JcrUtil.createJcrObject(n, JcrServiceLevelAgreement.class));
            }
        }
        return serviceLevelAgreements;
    }

    public void setServiceLevelAgreements(List<? extends ServiceLevelAgreement> serviceLevelAgreements) {
        setProperty(SLA, serviceLevelAgreements);
    }

    public void removeServiceLevelAgreement(ServiceLevelAgreement.ID id) {
        try {
            Set<Node> nodes = JcrPropertyUtil.getSetProperty(getNode(), SLA);
            Set<Value> updatedSet = new HashSet<>();
            for (Node node : nodes) {
                if (!node.getIdentifier().equalsIgnoreCase(id.toString())) {
                    Value value = JcrPropertyUtil.createValue(getNode().getSession(), node, true); 
                    updatedSet.add(value);
                }
            }
            getNode().setProperty(SLA, (Value[]) updatedSet.stream().toArray(size -> new Value[size]));
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to remove reference to SLA " + id + "from feed " + getParentFeed().getId());
        }

    }

    public boolean addServiceLevelAgreement(ServiceLevelAgreement sla) {
        JcrServiceLevelAgreement jcrServiceLevelAgreement = (JcrServiceLevelAgreement) sla;
        Node node = jcrServiceLevelAgreement.getNode();
        //add a ref to this node
        return JcrPropertyUtil.addToSetProperty(getNode(), SLA, node, true);
    }

    public String getJson() {
        return getProperty(FeedDetails.FEED_JSON, String.class);
    }

    public void setJson(String json) {
        setProperty(FeedDetails.FEED_JSON, json);
    }

    public String getNifiProcessGroupId() {
        return getProperty(FeedDetails.PROCESS_GROUP_ID, String.class);
    }

    public void setNifiProcessGroupId(String id) {
        setProperty(FeedDetails.PROCESS_GROUP_ID, id);
    }

    protected JcrFeedSource ensureFeedSource(JcrDatasource datasource) {
        Node feedSrcNode = JcrUtil.addNode(getNode(), FeedDetails.SOURCE_NAME, JcrFeedSource.NODE_TYPE);
        return new JcrFeedSource(feedSrcNode, datasource);
    }
    
    protected JcrFeedSource ensureFeedSource(JcrDataSet dataSource, boolean isSample) {
        Node feedSrcNode = JcrUtil.addNode(getNode(), FeedDetails.SOURCE_NAME, JcrFeedSource.NODE_TYPE);
        JcrFeedSource feedSource = new JcrFeedSource(feedSrcNode, dataSource);
        feedSource.setSample(isSample);
        return feedSource;
    }

    protected JcrFeedDestination ensureFeedDestination(JcrDatasource datasource) {
        Node feedDestNode = JcrUtil.addNode(getNode(), FeedDetails.DESTINATION_NAME, JcrFeedDestination.NODE_TYPE);
        return new JcrFeedDestination(feedDestNode, datasource);
    }
    
    protected JcrFeedDestination ensureFeedDestination(JcrDataSet datasource) {
        Node feedDestNode = JcrUtil.addNode(getNode(), FeedDetails.DESTINATION_NAME, JcrFeedDestination.NODE_TYPE);
        return new JcrFeedDestination(feedDestNode, datasource);
    }

    protected void removeFeedSource(JcrFeedSource source) {
        source.remove();
    }

    protected void removeFeedDestination(JcrFeedDestination dest) {
        dest.remove();
    }

    protected void removeFeedSources() {
        List<? extends FeedSource> sources = getSources();
        if (sources != null && !sources.isEmpty()) {
            //checkout the feed
            sources.stream()
                .map(JcrFeedSource.class::cast)
                .forEach(source -> {
                    removeFeedSource(source);
                });
        }
    }

    protected void removeFeedDestinations() {
        List<? extends FeedDestination> destinations = getDestinations();
        
        if (destinations != null && !destinations.isEmpty()) {
            destinations.stream()
                .map(JcrFeedDestination.class::cast)
                .forEach(dest -> {
                    Optional<Datasource> datasource = dest.getDatasource();
                    
                    removeFeedDestination(dest);
                    // Remove the datasource if there are no referencing feeds
                    datasource
                        .filter(dsrc -> dsrc.getFeedSources().isEmpty())
                        .filter(dsrc -> dsrc.getFeedDestinations().isEmpty())
                        .map(JcrDatasource.class::cast)
                        .ifPresent(dsrc -> dsrc.remove());
                });
        }
    }

    protected Node createNewPrecondition() {
        try {
            Node feedNode = getNode();
            Node precondNode = JcrUtil.getOrCreateNode(feedNode, FeedDetails.PRECONDITION, JcrFeed.PRECONDITION_TYPE);

            if (precondNode.hasProperty(JcrFeedPrecondition.SLA_REF)) {
                precondNode.getProperty(JcrFeedPrecondition.SLA_REF).remove();
            }
            if (precondNode.hasNode(JcrFeedPrecondition.SLA)) {
                precondNode.getNode(JcrFeedPrecondition.SLA).remove();
            }

            return precondNode.addNode(JcrFeedPrecondition.SLA, JcrFeedPrecondition.SLA_TYPE);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to create the precondition for feed " + getParentFeed().getId(), e);
        }
    }

    @Nonnull
    public Map<String, String> getUserProperties() {
        return JcrPropertyUtil.getUserProperties(getNode());
    }

    public void setUserProperties(@Nonnull final Map<String, String> userProperties, @Nonnull final Set<UserFieldDescriptor> userFields) {
        JcrPropertyUtil.setUserProperties(getNode(), userFields, userProperties);
    }

    /**
     * is this feed missing any user properties that are required
     * @param userFields
     * @return true if missing properties, false if not
     */
   public boolean isMissingRequiredProperties(@Nonnull final Set<UserFieldDescriptor> userFields){
        return JcrPropertyUtil.isMissingRequiredUserProperties(getNode(),userFields);
    }

}
