/**
 *
 */
package com.thinkbiganalytics.metadata.audit.core;

/*-
 * #%L
 * thinkbig-audit-logging-core
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

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.audit.AuditLogProvider;
import com.thinkbiganalytics.metadata.api.event.MetadataEventListener;
import com.thinkbiganalytics.metadata.api.event.MetadataEventService;
import com.thinkbiganalytics.metadata.api.event.feed.FeedChangeEvent;
import com.thinkbiganalytics.metadata.api.event.template.TemplateChangeEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * A service responsible for producing audit log entries from things like metadata events
 * and annotated methods.
 */
public class AuditLoggingService {

    private static final Logger log = LoggerFactory.getLogger(AuditLoggingService.class);

    @Inject
    private AuditLogProvider provider;

    @Inject
    private MetadataAccess metadataAccess;

    public AuditLoggingService() {
    }

    /**
     * Listen for events that may trigger an audit entry
     *
     * @param eventService event service bus
     */
    public void addListeners(MetadataEventService eventService) {
        eventService.addListener(new FeedChangeEventListener());
        eventService.addListener(new TemplateChangeEventListener());
    }

    private class FeedChangeEventListener implements MetadataEventListener<FeedChangeEvent> {

        @Override
        public void notify(FeedChangeEvent event) {
            metadataAccess.commit(() -> {
                log.debug("Audit: {} - {}", event.getData().getClass().getSimpleName(), event.getData().toString());
                provider.createEntry(event.getUserPrincipal(),
                                     event.getData().getClass().getSimpleName(),
                                     event.getData().toString(),
                                     event.getData().getFeedId().toString());
            }, MetadataAccess.SERVICE);
        }
    }

    private class TemplateChangeEventListener implements MetadataEventListener<TemplateChangeEvent> {

        @Override
        public void notify(TemplateChangeEvent event) {
            metadataAccess.commit(() -> {
                log.debug("Audit: {} - {}", event.getData().getClass().getSimpleName(), event.getData().toString());
                provider.createEntry(event.getUserPrincipal(),
                                     event.getData().getClass().getSimpleName(),
                                     event.getData().toString(),
                                     event.getData().getTemplateId().toString());
            }, MetadataAccess.SERVICE);
        }
    }
}
