/**
 *
 */
package com.thinkbiganalytics.feedmgr.service;

/*-
 * #%L
 * thinkbig-metadata-rest-controller
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
import com.thinkbiganalytics.metadata.api.datasource.DerivedDatasource;
import com.thinkbiganalytics.metadata.api.feed.Feed.State;
import com.thinkbiganalytics.metadata.rest.model.data.Datasource;
import com.thinkbiganalytics.metadata.rest.model.data.DatasourceDefinition;
import com.thinkbiganalytics.metadata.rest.model.data.DirectoryDatasource;
import com.thinkbiganalytics.metadata.rest.model.data.HiveTableDatasource;
import com.thinkbiganalytics.metadata.rest.model.feed.Feed;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedCategory;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedDestination;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedPrecondition;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedSource;
import com.thinkbiganalytics.metadata.rest.model.feed.InitializationStatus;
import com.thinkbiganalytics.metadata.rest.model.op.FeedOperation;
import com.thinkbiganalytics.security.rest.controller.SecurityModelTransform;
import com.thinkbiganalytics.security.rest.model.ActionGroup;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

/**
 * Convenience functions and methods to transform between the metadata domain model and the REST model.
 */
public class MetadataModelTransform {
    
    @Inject
    private SecurityModelTransform actionsTransform;

    public Function<com.thinkbiganalytics.metadata.api.feed.InitializationStatus, InitializationStatus> domainToInitStatus() {
        return (domain) -> {
            InitializationStatus status = new InitializationStatus();
            status.setState(InitializationStatus.State.valueOf(domain.getState().name()));
            status.setTimestamp(domain.getTimestamp());
            return status;
        };
    }

    // @formatter:off
    public Function<com.thinkbiganalytics.metadata.api.feed.FeedPrecondition, FeedPrecondition> domainToFeedPrecond() {
        return (domain) -> {
            FeedPrecondition precond = new FeedPrecondition();
            return precond;
        };
    }
    
    /**
     * Convert a Domin DatasourceDefinition to the Rest Model
     */
    public Function<com.thinkbiganalytics.metadata.api.datasource.DatasourceDefinition, DatasourceDefinition> domainToDsDefinition() {
        return (domain) -> {
            DatasourceDefinition dsDef = new DatasourceDefinition();
            dsDef.setDatasourceType(domain.getDatasourceType());
            dsDef.setProcessorType(domain.getProcessorType());
            if (domain.getConnectionType() != null) {
                dsDef.setConnectionType(DatasourceDefinition.ConnectionType.valueOf(domain.getConnectionType().name()));
            }
            dsDef.setIdentityString(domain.getIdentityString());
            dsDef.setDatasourcePropertyKeys(domain.getDatasourcePropertyKeys());
            dsDef.setTitle(domain.getTitle());
            dsDef.setDescription(domain.getDescription());
            return dsDef;
        };
    }
    
    public Function<DerivedDatasource, com.thinkbiganalytics.metadata.rest.model.data.DerivedDatasource> domainToDerivedDs() {
        return (domain) -> {
            com.thinkbiganalytics.metadata.rest.model.data.DerivedDatasource ds = new com.thinkbiganalytics.metadata.rest.model.data.DerivedDatasource();
            ds.setId(domain.getId().toString());
            ds.setName(domain.getName());
            ds.setDescription(domain.getDescription());
            ds.setProperties(domain.getProperties());
            ds.setDatasourceType(domain.getDatasourceType());
            return ds;
        };
    }
    
    public Function<com.thinkbiganalytics.metadata.api.feed.FeedSource, FeedSource> domainToFeedSource() {
        return (domain) -> {
            FeedSource src = new FeedSource();
            src.setDatasource(domainToDs().apply(domain.getDatasource()));
            return src;
        };
    }
    
    public Function<com.thinkbiganalytics.metadata.api.feed.FeedDestination, FeedDestination> domainToFeedDestination() {
        return (domain) -> {
            FeedDestination dest = new FeedDestination();
            dest.setDatasource(domainToDs().apply(domain.getDatasource()));
            return dest;
        };
    }
    
    public Function<com.thinkbiganalytics.metadata.api.op.FeedOperation, FeedOperation> domainToFeedOp() {
        return (domain) -> {
            FeedOperation op = new FeedOperation();
            op.setOperationId(domain.getId().toString());
            op.setStartTime(domain.getStartTime());
            op.setStopTime(domain.getStopTime());
            op.setState(FeedOperation.State.valueOf(domain.getState().name()));
            op.setStatus(domain.getStatus());
            op.setResults(domain.getResults().entrySet().stream()
                              .collect(Collectors.toMap(Map.Entry::getKey,
                                                        e -> e.getValue().toString())));

            return op;
        };
    }
    
    public Function<Category, FeedCategory> domainToFeedCategory() {
        return (category) -> {
            FeedCategory feedCategory = new FeedCategory();
            feedCategory.setId(category.getId().toString());
            feedCategory.setSystemName(category.getSystemName());
            feedCategory.setDisplayName(category.getDisplayName());
            feedCategory.setDescription(category.getDescription());
            feedCategory.setUserProperties(category.getUserProperties());
            return feedCategory;
        };
    }
    
    public Function<com.thinkbiganalytics.metadata.api.feed.Feed, Feed> domainToFeed() {
        return domainToFeed(true);
    }
    
    public Function<com.thinkbiganalytics.metadata.api.feed.Feed, Feed> domainToFeed(boolean addSources) {
        return (domain) -> {
            Feed feed = new Feed();
            feed.setId(domain.getId().toString());
            feed.setSystemName(domain.getName());
            feed.setDisplayName(domain.getDisplayName());
            feed.setDescription(domain.getDescription());
            feed.setUserProperties(domain.getUserProperties());
            if (domain.getState() != null) feed.setState(Feed.State.valueOf(domain.getState().name()));
            if (domain.getCreatedTime() != null) feed.setCreatedTime(domain.getCreatedTime());
            if (domain.getCurrentInitStatus() != null) feed.setCurrentInitStatus(domainToInitStatus().apply(domain.getCurrentInitStatus()));
            
            if (domain.getCategory() != null) {
                feed.setCategory(domainToFeedCategory().apply(domain.getCategory()));
            }

            if (addSources) {
                Collection<FeedSource> sources = domain.getSources().stream().map(domainToFeedSource()).collect(Collectors.toList());
                feed.setSources(new HashSet<FeedSource>(sources));

                Collection<FeedDestination> destinations = domain.getDestinations().stream().map(domainToFeedDestination()).collect(Collectors.toList());
                feed.setDestinations(new HashSet<FeedDestination>(destinations));
            }

            for (Entry<String, Object> entry : domain.getProperties().entrySet()) {
                if (entry.getValue() != null) {
                    feed.getProperties().setProperty(entry.getKey(), entry.getValue().toString());
                }
            }
            
            ActionGroup allowed = actionsTransform.toActionGroup(null).apply(domain.getAllowedActions());
            feed.setAllowedActions(allowed);

            return feed;
        };
    }

    
    public Function<com.thinkbiganalytics.metadata.api.datasource.Datasource, Datasource> domainToDs() {
        return domainToDs(true);
    }

    public Function<com.thinkbiganalytics.metadata.api.datasource.Datasource, Datasource> domainToDs(boolean addConnections) {
        return (domain) -> {
            Datasource ds;
            if (domain instanceof DerivedDatasource) {
                ds = domainToDerivedDs().apply((DerivedDatasource) domain);
            } else {
                ds = new Datasource();
                ds.setId(domain.getId().toString());
                ds.setName(domain.getName());
                ds.setDescription(domain.getDescription());
            }
            if (addConnections) {
                addConnections(domain, ds);
            }
            return ds;
        };
    }

    protected void addConnections(com.thinkbiganalytics.metadata.api.datasource.Datasource domain, Datasource datasource) {
        for (com.thinkbiganalytics.metadata.api.feed.FeedSource domainSrc : domain.getFeedSources()) {
            Feed feed = new Feed();
            feed.setId(domainSrc.getFeed().getId().toString());
            feed.setSystemName(domainSrc.getFeed().getName());

            datasource.getSourceForFeeds().add(feed);
        }
        for (com.thinkbiganalytics.metadata.api.feed.FeedDestination domainDest : domain.getFeedDestinations()) {
            Feed feed = new Feed();
            feed.setId(domainDest.getFeed().getId().toString());
            feed.setSystemName(domainDest.getFeed().getName());

            datasource.getDestinationForFeeds().add(feed);
        }
    }


    public com.thinkbiganalytics.metadata.api.feed.Feed updateDomain(Feed feed, com.thinkbiganalytics.metadata.api.feed.Feed domain) {
        domain.setDisplayName(feed.getDisplayName());
        domain.setDescription(feed.getDescription());
        domain.setState(State.valueOf(feed.getState().name()));
        return domain;
    }

    public void validateCreate(Feed feed) {
        // TODO Auto-generated method stub

    }


    public void validateCreate(String fid, FeedDestination dest) {
        // TODO Auto-generated method stub

    }


    public void validateCreate(HiveTableDatasource ds) {
        // TODO Auto-generated method stub

    }


    public void validateCreate(DirectoryDatasource ds) {
        // TODO Auto-generated method stub

    }

    // @formatter:on

}
