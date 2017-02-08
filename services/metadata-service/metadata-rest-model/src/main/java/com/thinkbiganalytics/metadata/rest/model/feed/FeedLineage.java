package com.thinkbiganalytics.metadata.rest.model.feed;

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
import com.thinkbiganalytics.metadata.rest.model.data.Datasource;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 */
public class FeedLineage {

    private Map<String, Feed> feedMap;

    private Feed feed;

    private Map<String, Datasource> datasourceMap = new HashMap<>();

    private Map<String, FeedLineageStyle> styles;

    public FeedLineage(Feed feed, Map<String, FeedLineageStyle> styles) {
        this.feed = feed;
        this.feedMap = new HashMap<>();
        this.styles = styles;
        serialize(this.feed);
    }

    @JsonIgnore
    private void serialize(Feed feed) {

        if (feed.getDependentFeeds() != null) {
            Set<String> ids = new HashSet<>();
            Set<Feed> dependentFeeds = new HashSet<>(feed.getDependentFeeds());
            feed.setDependentFeeds(null);
            dependentFeeds.stream().forEach(depFeed -> {
                                                feedMap.put(depFeed.getId(), depFeed);
                                                ids.add(depFeed.getId());

                                                if (depFeed.getDependentFeeds() != null) {
                                                    serialize(depFeed);
                                                }
                                            }
            );

            feed.setDependentFeedIds(ids);
        }
        if (feed.getUsedByFeeds() != null) {
            Set<String> ids = new HashSet<>();
            Set<Feed> usedByFeeds = new HashSet<>(feed.getUsedByFeeds());
            feed.getUsedByFeeds().clear();
            usedByFeeds.stream().forEach(depFeed -> {
                                             feedMap.put(depFeed.getId(), depFeed);
                                             ids.add(depFeed.getId());
                                             if (depFeed.getUsedByFeeds() != null) {
                                                 serialize(depFeed);
                                             }
                                         }
            );
            feed.setUsedByFeedIds(ids);
        }

        if (feed.getSources() != null) {
            feed.getSources().forEach(feedSource -> {
                Datasource ds = serializeDatasource(feedSource.getDatasource());
                feedSource.setDatasource(null);
                if (StringUtils.isBlank(feedSource.getDatasourceId())) {
                    feedSource.setDatasourceId(ds != null ? ds.getId() : null);
                }
            });
        }
        if (feed.getDestinations() != null) {
            feed.getDestinations().forEach(feedDestination -> {
                Datasource ds = serializeDatasource(feedDestination.getDatasource());
                feedDestination.setDatasource(null);
                if (StringUtils.isBlank(feedDestination.getDatasourceId())) {
                    feedDestination.setDatasourceId(ds != null ? ds.getId() : null);
                }
            });
        }
        feedMap.put(feed.getId(), feed);
    }

    private Datasource serializeDatasource(Datasource ds) {
        if (ds != null) {
            if (!datasourceMap.containsKey(ds.getId())) {
                datasourceMap.put(ds.getId(), ds);
                if (ds.getSourceForFeeds() != null) {
                    ds.getSourceForFeeds().forEach(sourceFeed -> {
                        Feed serializedFeed = feedMap.get(sourceFeed.getId());
                        if (serializedFeed == null) {
                            serialize(sourceFeed);
                        } else {
                            sourceFeed = serializedFeed;
                        }
                    });
                }

                if (ds.getDestinationForFeeds() != null) {
                    ds.getDestinationForFeeds().forEach(destFeed -> {
                        Feed serializedFeed = feedMap.get(destFeed.getId());
                        if (serializedFeed == null) {
                            serialize(destFeed);
                        } else {
                            destFeed = serializedFeed;
                        }
                    });
                }
            }
            return datasourceMap.get(ds.getId());

        }
        return null;

    }


    public Map<String, Feed> getFeedMap() {
        return feedMap;
    }

    public void setFeedMap(Map<String, Feed> feedMap) {
        this.feedMap = feedMap;
    }

    public Feed getFeed() {
        return feed;
    }

    public void setFeed(Feed feed) {
        this.feed = feed;
    }

    public Map<String, Datasource> getDatasourceMap() {
        return datasourceMap;
    }

    public void setDatasourceMap(Map<String, Datasource> datasourceMap) {
        this.datasourceMap = datasourceMap;
    }


    public Map<String, FeedLineageStyle> getStyles() {
        return styles;
    }

    public void setStyles(Map<String, FeedLineageStyle> styles) {
        this.styles = styles;
    }
}
