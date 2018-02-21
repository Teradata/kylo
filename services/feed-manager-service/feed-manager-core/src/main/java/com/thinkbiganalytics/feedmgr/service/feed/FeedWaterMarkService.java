package com.thinkbiganalytics.feedmgr.service.feed;

/*-
 * #%L
 * thinkbig-feed-manager-controller
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

import com.thinkbiganalytics.feedmgr.security.FeedServicesAccessControl;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedNotFoundException;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.event.jms.MetadataTopics;
import com.thinkbiganalytics.metadata.rest.model.event.FeedWaterMarkCancelEvent;
import com.thinkbiganalytics.security.AccessController;

import org.springframework.jms.core.JmsMessagingTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jms.Topic;

/**
 * Manages feed water marks.
 */
public class FeedWaterMarkService {

    @Inject
    private FeedProvider feedProvider;

    @Inject
    private MetadataAccess metadata;

    @Inject
    private AccessController accessController;
    
    @Inject
    @Named(MetadataTopics.CANCEL_ACTIVE_WATER_MARK)
    private Topic cancelActiveTopic;
    
    @Inject
    private JmsMessagingTemplate jmsMessagingTemplate;

    
    public List<String> getWaterMarks(String feedId) {
        return this.metadata.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_FEEDS);

            com.thinkbiganalytics.metadata.api.feed.Feed.ID id = feedProvider.resolveFeed(feedId);
            com.thinkbiganalytics.metadata.api.feed.Feed feed = feedProvider.getFeed(id);

            if (feed != null) {
                List<String> list = feed.getWaterMarkNames().stream().collect(Collectors.toList());
                Collections.sort(list);
                return list;
            } else {
                throw new FeedNotFoundException(id);
            }
        });
    }
    
    public Optional<String> getWaterMark(String feedId, String waterMarkName) {
        return this.metadata.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_FEEDS);

            com.thinkbiganalytics.metadata.api.feed.Feed.ID id = feedProvider.resolveFeed(feedId);
            com.thinkbiganalytics.metadata.api.feed.Feed feed = feedProvider.getFeed(id);

            if (feed != null) {
                return feed.getWaterMarkValue(waterMarkName);
            } else {
                throw new FeedNotFoundException(id);
            }
        });
    }
    
    public String updateWaterMark(String feedId, String waterMarkName, String value, boolean cancelActive) {
        com.thinkbiganalytics.metadata.api.feed.Feed.ID updId = this.metadata.commit(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.EDIT_FEEDS);

            com.thinkbiganalytics.metadata.api.feed.Feed.ID id = feedProvider.resolveFeed(feedId);
            com.thinkbiganalytics.metadata.api.feed.Feed feed = feedProvider.getFeed(id);

            if (feed != null) {
                feed.setWaterMarkValue(waterMarkName, value);
                return id;
            } else {
                throw new FeedNotFoundException(id);
            }
        });
        
        if (cancelActive) {
            cancelActiveWaterMark(updId, waterMarkName);
        }
        
        return this.metadata.commit(() -> {
            com.thinkbiganalytics.metadata.api.feed.Feed feed = feedProvider.getFeed(updId);
            
            if (feed != null) {
                return feed.getWaterMarkValue(waterMarkName)
                    .orElseThrow(() -> new WaterMarkNotFoundExcepton(updId, waterMarkName));
            } else {
                throw new FeedNotFoundException(updId);
            }
        });

    }
    
    public void cancelActiveWaterMark(String feedId, String waterMarkName) {
        com.thinkbiganalytics.metadata.api.feed.Feed.ID updId = this.metadata.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.EDIT_FEEDS);
            
            com.thinkbiganalytics.metadata.api.feed.Feed.ID id = feedProvider.resolveFeed(feedId);
            com.thinkbiganalytics.metadata.api.feed.Feed feed = feedProvider.getFeed(id);
            
            if (feed != null) {
                return feed.getId();
            } else {
                throw new FeedNotFoundException(id);
            }
        });

        cancelActiveWaterMark(updId, waterMarkName);
    }

    private void cancelActiveWaterMark(Feed.ID feed, String waterMarkName) {
        FeedWaterMarkCancelEvent event = new FeedWaterMarkCancelEvent(feed.toString(), waterMarkName);
        this.jmsMessagingTemplate.convertAndSend(this.cancelActiveTopic, event);
    }
}
