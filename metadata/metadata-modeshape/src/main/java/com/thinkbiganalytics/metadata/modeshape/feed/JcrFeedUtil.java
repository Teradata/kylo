/**
 *
 */
package com.thinkbiganalytics.metadata.modeshape.feed;

/*-
 * #%L
 * thinkbig-metadata-modeshape
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

import com.thinkbiganalytics.metadata.api.event.MetadataChange;
import com.thinkbiganalytics.metadata.api.event.MetadataEventService;
import com.thinkbiganalytics.metadata.api.event.feed.FeedChange;
import com.thinkbiganalytics.metadata.api.event.feed.FeedChangeEvent;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;

import org.joda.time.DateTime;
import org.springframework.security.core.context.SecurityContextHolder;

import java.security.Principal;
import java.util.function.Consumer;

import javax.inject.Inject;


public class JcrFeedUtil {

    @Inject
    private MetadataEventService metadataEventService;


    /**
     * Registers an action that produces a feed change event upon a successful transaction commit.
     *
     * @param feed the feed to being created
     */
    public  void addPostFeedChangeAction(Feed<?> feed, MetadataChange.ChangeType changeType) {
        Feed.State state = feed.getState();
        Feed.ID id = feed.getId();
        String descr = feed.getQualifiedName();
        final Principal principal = SecurityContextHolder.getContext().getAuthentication() != null
                                    ? SecurityContextHolder.getContext().getAuthentication()
                                    : null;

        Consumer<Boolean> action = (success) -> {
            if (success) {
                FeedChange change = new FeedChange(changeType, descr, id, state);
                FeedChangeEvent event = new FeedChangeEvent(change, DateTime.now(), principal);
                metadataEventService.notify(event);
            }
        };

        JcrMetadataAccess.addPostTransactionAction(action);
    }

}
