package com.thinkbiganalytics.feedmgr.rest;

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

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.thinkbiganalytics.feedmgr.service.datasource.DatasourceModelTransform;
import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.feed.Feed.State;
import com.thinkbiganalytics.metadata.rest.model.data.Datasource;
import com.thinkbiganalytics.metadata.rest.model.data.DatasourceDefinition;
import com.thinkbiganalytics.metadata.rest.model.feed.Feed;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedCategory;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedDestination;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedPrecondition;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedSource;
import com.thinkbiganalytics.metadata.rest.model.feed.InitializationStatus;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

/**
 * Convenience functions and methods to transform between the metadata domain model and the REST model.
 */
public class Model {

    public static final Function<com.thinkbiganalytics.metadata.api.feed.InitializationStatus, InitializationStatus> DOMAIN_TO_INIT_STATUS
        = new Function<com.thinkbiganalytics.metadata.api.feed.InitializationStatus, InitializationStatus>() {
        @Override
        public InitializationStatus apply(com.thinkbiganalytics.metadata.api.feed.InitializationStatus domain) {
            InitializationStatus status = new InitializationStatus();
            status.setState(InitializationStatus.State.valueOf(domain.getState().name()));
            status.setTimestamp(domain.getTimestamp());
            return status;
        }
    };

    public static final Function<com.thinkbiganalytics.metadata.api.feed.FeedPrecondition, FeedPrecondition> DOMAIN_TO_FEED_PRECOND
        = new Function<com.thinkbiganalytics.metadata.api.feed.FeedPrecondition, FeedPrecondition>() {
        @Override
        public FeedPrecondition apply(com.thinkbiganalytics.metadata.api.feed.FeedPrecondition domain) {
            FeedPrecondition precond = new FeedPrecondition();
            return precond;
        }
    };

    /**
     * Convert a Domin DatasourceDefinition to the Rest Model
     */
    public static final Function<com.thinkbiganalytics.metadata.api.datasource.DatasourceDefinition, DatasourceDefinition> DOMAIN_TO_DS_DEFINITION
        = new Function<com.thinkbiganalytics.metadata.api.datasource.DatasourceDefinition, DatasourceDefinition>() {
        @Override
        public DatasourceDefinition apply(com.thinkbiganalytics.metadata.api.datasource.DatasourceDefinition domain) {
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
        }
    };

    public static final Function<Category, FeedCategory> DOMAIN_TO_FEED_CATEGORY = new Function<Category, FeedCategory>() {
        @Override
        public FeedCategory apply(Category category) {
            FeedCategory feedCategory = new FeedCategory();
            feedCategory.setId(category.getId().toString());
            feedCategory.setSystemName(category.getSystemName());
            feedCategory.setDisplayName(category.getDisplayName());
            feedCategory.setDescription(category.getDescription());
            return feedCategory;
        }
    };


    public static com.thinkbiganalytics.metadata.api.feed.Feed updateDomain(Feed feed, com.thinkbiganalytics.metadata.api.feed.Feed domain) {
        domain.setDisplayName(feed.getDisplayName());
        domain.setDescription(feed.getDescription());
        domain.setState(State.valueOf(feed.getState().name()));
        return domain;
    }

    public static void validateCreate(Feed feed) {
        // ignored
    }

    /**
     * The {@code Datasource} transformer.
     */
    @Nonnull
    private final DatasourceModelTransform datasourceTransform;

    /**
     * Constructs a {@code Model}.
     *
     * @param datasourceTransform the datasource transformer
     */
    public Model(@Nonnull final DatasourceModelTransform datasourceTransform) {
        this.datasourceTransform = datasourceTransform;
    }

    /**
     * Transforms the specified domain object to a REST object.
     *
     * @param domain the domain object
     * @return the REST object
     */
    public Feed domainToFeed(@Nonnull final com.thinkbiganalytics.metadata.api.feed.Feed domain) {
        return domainToFeed(domain, true);
    }

    /**
     * Transforms the specified domain object to a REST object.
     *
     * @param domain     the domain object
     * @param addSources {@code true} to include sources in the REST object, or {@code false} otherwise
     * @return the REST object
     */
    public Feed domainToFeed(@Nonnull final com.thinkbiganalytics.metadata.api.feed.Feed domain, final boolean addSources) {
        Feed feed = new Feed();
        feed.setId(domain.getId().toString());
        feed.setSystemName(domain.getName());
        feed.setDisplayName(domain.getDisplayName());
        feed.setDescription(domain.getDescription());
        feed.setState(Feed.State.valueOf(domain.getState().name()));
        feed.setCreatedTime(domain.getCreatedTime());
        feed.setCurrentInitStatus(DOMAIN_TO_INIT_STATUS.apply(domain.getCurrentInitStatus()));
        if (domain.getCategory() != null) {
            feed.setCategory(DOMAIN_TO_FEED_CATEGORY.apply(domain.getCategory()));
        }

        if (addSources) {
            @SuppressWarnings("unchecked")
            Collection<FeedSource> sources = Collections2.transform(domain.getSources(), this::domainToFeedSource);
            feed.setSources(new HashSet<>(sources));

            @SuppressWarnings("unchecked")
            Collection<FeedDestination> destinations = Collections2.transform(domain.getDestinations(), this::domainToFeedDestination);
            feed.setDestinations(new HashSet<>(destinations));
        }

        for (Entry<String, Object> entry : domain.getProperties().entrySet()) {
            if (entry.getValue() != null) {
                feed.getProperties().setProperty(entry.getKey(), entry.getValue().toString());
            }
        }

        return feed;
    }

    /**
     * Transforms the specified domain object to a REST object.
     *
     * @param domain the domain object
     * @return the REST object
     */
    public FeedSource domainToFeedSource(@Nonnull final com.thinkbiganalytics.metadata.api.feed.FeedSource domain) {
        FeedSource src = new FeedSource();
        src.setDatasource(domainToDs(domain.getDatasource()));
        return src;
    }

    /**
     * Transforms the specified domain object to a REST object.
     *
     * @param domain the domain object
     * @return the REST object
     */
    public FeedDestination domainToFeedDestination(@Nonnull final com.thinkbiganalytics.metadata.api.feed.FeedDestination domain) {
        FeedDestination dest = new FeedDestination();
        dest.setDatasource(domainToDs(domain.getDatasource()));
        return dest;
    }

    /**
     * Transforms the specified domain object to a REST object.
     *
     * @param domain the domain object
     * @return the REST object
     */
    private Datasource domainToDs(@Nonnull final com.thinkbiganalytics.metadata.api.datasource.Datasource domain) {
        return datasourceTransform.toDatasource(domain, DatasourceModelTransform.Level.CONNECTIONS);
    }
}
