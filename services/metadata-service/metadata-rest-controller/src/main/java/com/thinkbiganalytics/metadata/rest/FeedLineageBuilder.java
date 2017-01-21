package com.thinkbiganalytics.metadata.rest;

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

/**
 * Created by sr186054 on 11/12/16.
 */
public class FeedLineageBuilder {

    Map<String, Feed> processedDomainFeeds = new HashMap<>();
    Map<String, com.thinkbiganalytics.metadata.rest.model.feed.Feed> restFeeds = new HashMap<>();

    Map<String, Datasource> restDatasources = new HashMap<>();


    private Feed domainFeed;

    public FeedLineageBuilder(Feed domainFeed) {
        this.domainFeed = domainFeed;
        build(this.domainFeed);
    }


    public com.thinkbiganalytics.metadata.rest.model.feed.Feed build() {
        return build(this.domainFeed);
    }

    private Datasource buildDatasource(com.thinkbiganalytics.metadata.api.datasource.Datasource domainDatasource) {
        Datasource ds = restDatasources.get(domainDatasource.getId().toString());
        if (ds == null) {
            // build the data source
            ds = Model.DOMAIN_TO_DS(false).apply(domainDatasource);

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
            restFeeds.containsKey(domainFeed.getId().toString()) ? restFeeds.get(domainFeed.getId().toString()) : Model.DOMAIN_TO_FEED.apply(domainFeed);
        restFeeds.put(feed.getId(), feed);

        @SuppressWarnings("unchecked")
        List<com.thinkbiganalytics.metadata.api.feed.FeedSource> sources = domainFeed.getSources();
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
        List<com.thinkbiganalytics.metadata.api.feed.FeedDestination> destinations = domainFeed.getDestinations();
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
                    com.thinkbiganalytics.metadata.rest.model.feed.Feed depRestFeed = Model.DOMAIN_TO_FEED(false).apply(depFeed);
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
                    com.thinkbiganalytics.metadata.rest.model.feed.Feed usedByRestFeed = Model.DOMAIN_TO_FEED(false).apply(usedByFeed);
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
