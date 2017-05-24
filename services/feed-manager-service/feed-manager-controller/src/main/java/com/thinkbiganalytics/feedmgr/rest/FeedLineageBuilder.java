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

import com.thinkbiganalytics.feedmgr.service.datasource.DatasourceModelTransform;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.rest.model.data.Datasource;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedDestination;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

public class FeedLineageBuilder {

    Map<String, Feed> processedDomainFeeds = new HashMap<>();
    Map<String, com.thinkbiganalytics.metadata.rest.model.feed.Feed> restFeeds = new HashMap<>();

    Map<String, Datasource> restDatasources = new HashMap<>();

    /**
     * The {@code Datasource} transformer
     */
    @Nonnull
    private final DatasourceModelTransform datasourceTransform;

    private Feed domainFeed;

    /**
     * The feed model transformer
     */
    @Nonnull
    private final Model model;

    /**
     * Constructs a {@code FeedLineageBuilder} for the specified feed.
     *
     * @param domainFeed          the feed
     * @param model               the feed model transformer
     * @param datasourceTransform the datasource transformer
     */
    public FeedLineageBuilder(Feed domainFeed, @Nonnull final Model model, @Nonnull final DatasourceModelTransform datasourceTransform) {
        this.domainFeed = domainFeed;
        this.model = model;
        this.datasourceTransform = datasourceTransform;
        build(this.domainFeed);
    }


    public com.thinkbiganalytics.metadata.rest.model.feed.Feed build() {
        return build(this.domainFeed);
    }

    private Datasource buildDatasource(com.thinkbiganalytics.metadata.api.datasource.Datasource domainDatasource) {
        Datasource ds = restDatasources.get(domainDatasource.getId().toString());
        if (ds == null) {
            // build the data source
            ds = datasourceTransform.toDatasource(domainDatasource, DatasourceModelTransform.Level.BASIC);

            restDatasources.put(ds.getId(), ds);
            //populate the Feed relationships
            if (domainDatasource.getFeedSources() != null) {

                List<com.thinkbiganalytics.metadata.rest.model.feed.Feed> feedList = new ArrayList<>();
                for (com.thinkbiganalytics.metadata.api.feed.FeedSource domainSrc : domainDatasource.getFeedSources()) {
                    com.thinkbiganalytics.metadata.rest.model.feed.Feed feed = build(domainSrc.getFeed());
                    feedList.add(feed);
                }
                ds.getSourceForFeeds().addAll(feedList);
            }
            if (domainDatasource.getFeedDestinations() != null) {
                List<com.thinkbiganalytics.metadata.rest.model.feed.Feed> feedList = new ArrayList<>();
                for (com.thinkbiganalytics.metadata.api.feed.FeedDestination domainDest : domainDatasource.getFeedDestinations()) {
                    com.thinkbiganalytics.metadata.rest.model.feed.Feed feed = build(domainDest.getFeed());
                    feedList.add(feed);
                }
                ds.getDestinationForFeeds().addAll(feedList);
            }
        }
        return ds;

    }

    private com.thinkbiganalytics.metadata.rest.model.feed.Feed build(Feed domainFeed) {
        com.thinkbiganalytics.metadata.rest.model.feed.Feed
            feed =
            restFeeds.containsKey(domainFeed.getId().toString()) ? restFeeds.get(domainFeed.getId().toString()) : model.domainToFeed(domainFeed);
        restFeeds.put(feed.getId(), feed);

        @SuppressWarnings("unchecked")
        List<? extends com.thinkbiganalytics.metadata.api.feed.FeedSource> sources = domainFeed.getSources();
        Set<FeedSource> feedSources = new HashSet<FeedSource>();
        if (sources != null) {

            sources.stream().forEach(feedSource -> {
                FeedSource src = new FeedSource();
                Datasource ds = buildDatasource(feedSource.getDatasource());
                src.setDatasource(ds);
                feedSources.add(src);
            });
        }
        feed.setSources(feedSources);
        Set<FeedDestination> feedDestinations = new HashSet<FeedDestination>();
        List<? extends com.thinkbiganalytics.metadata.api.feed.FeedDestination> destinations = domainFeed.getDestinations();
        if (destinations != null) {
            destinations.stream().forEach(feedDestination -> {
                FeedDestination dest = new FeedDestination();
                Datasource ds = buildDatasource(feedDestination.getDatasource());
                dest.setDatasource(ds);
                feedDestinations.add(dest);
            });
        }
        feed.setDestinations(feedDestinations);

        if (domainFeed.getDependentFeeds() != null) {
            List<Feed> depFeeds = domainFeed.getDependentFeeds();
            depFeeds.stream().forEach(depFeed -> {
                com.thinkbiganalytics.metadata.rest.model.feed.Feed restFeed = restFeeds.get(depFeed.getId().toString());
                if (restFeed == null) {
                    com.thinkbiganalytics.metadata.rest.model.feed.Feed depRestFeed = model.domainToFeed(depFeed);
                    restFeeds.put(depRestFeed.getId(), depRestFeed);
                    feed.getDependentFeeds().add(depRestFeed);
                    build(depFeed);
                } else {
                    feed.getDependentFeeds().add(restFeed);
                }

            });
        }
        if (domainFeed.getUsedByFeeds() != null) {
            List<Feed> usedByFeeds = domainFeed.getUsedByFeeds();
            usedByFeeds.stream().forEach(usedByFeed -> {
                com.thinkbiganalytics.metadata.rest.model.feed.Feed restFeed = restFeeds.get(usedByFeed.getId().toString());
                if (restFeed == null) {
                    com.thinkbiganalytics.metadata.rest.model.feed.Feed usedByRestFeed = model.domainToFeed(usedByFeed);
                    restFeeds.put(usedByRestFeed.getId(), usedByRestFeed);
                    feed.getUsedByFeeds().add(usedByRestFeed);
                    build(usedByFeed);
                } else {
                    feed.getUsedByFeeds().add(restFeed);
                }

            });
        }
        return feed;
    }
}
