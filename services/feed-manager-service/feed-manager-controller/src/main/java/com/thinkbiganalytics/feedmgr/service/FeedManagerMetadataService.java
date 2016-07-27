package com.thinkbiganalytics.feedmgr.service;

import com.thinkbiganalytics.feedmgr.InvalidOperationException;
import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.FeedSummary;
import com.thinkbiganalytics.feedmgr.rest.model.NifiFeed;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.rest.model.UIFeed;
import com.thinkbiganalytics.feedmgr.service.category.FeedManagerCategoryService;
import com.thinkbiganalytics.feedmgr.service.feed.FeedManagerFeedService;
import com.thinkbiganalytics.feedmgr.service.template.FeedManagerTemplateService;
import com.thinkbiganalytics.metadata.api.Command;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.event.MetadataEventListener;
import com.thinkbiganalytics.metadata.api.event.MetadataEventService;
import com.thinkbiganalytics.metadata.api.event.feed.CleanupTriggerEvent;
import com.thinkbiganalytics.metadata.api.event.feed.FeedOperationStatusEvent;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.op.FeedOperation;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClient;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.nifi.rest.support.NifiConnectionUtil;
import com.thinkbiganalytics.nifi.rest.support.NifiConstants;

import org.apache.nifi.web.api.dto.ConnectionDTO;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.entity.ConnectionsEntity;
import org.apache.nifi.web.api.entity.ProcessGroupEntity;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Created by sr186054 on 1/13/16.
 */
public class FeedManagerMetadataService implements MetadataService {

    @Inject
    FeedManagerCategoryService categoryProvider;

    @Inject
    FeedManagerTemplateService templateProvider;

    @Inject
    FeedManagerFeedService feedProvider;

    @Inject
    NifiRestClient nifiRestClient;

    @Inject
    MetadataAccess metadataAccess;

    // Metadata event service
    @Inject
    private MetadataEventService eventService;


    public FeedManagerMetadataService() {

    }

    @Override
    public void registerTemplate(RegisteredTemplate registeredTemplate) {
        templateProvider.registerTemplate(registeredTemplate);
    }

    @Override
    public List<NifiProperty> getTemplateProperties(String templateId) {
        return templateProvider.getTemplateProperties(templateId);
    }

    @Override
    public RegisteredTemplate getRegisteredTemplate(String templateId) {
        return templateProvider.getRegisteredTemplate(templateId);
    }

    @Override
    public RegisteredTemplate getRegisteredTemplateByName(String templateName) {
        return templateProvider.getRegisteredTemplateByName(templateName);
    }

    @Override
    //@Transactional(transactionManager = "metadataTransactionManager")
    public RegisteredTemplate getRegisteredTemplateWithAllProperties(final String templateId) {
        return metadataAccess.read(new Command<RegisteredTemplate>() {
            @Override
            public RegisteredTemplate execute() {
                return templateProvider.getRegisteredTemplateWithAllProperties(templateId);
            }
        });

    }
    @Override
    public RegisteredTemplate getRegisteredTemplateForNifiProperties(final String nifiTemplateId, final String nifiTemplateName) {
        return metadataAccess.read(new Command<RegisteredTemplate>() {
            public RegisteredTemplate execute() {
                return templateProvider.getRegisteredTemplateForNifiProperties(nifiTemplateId, nifiTemplateName);
            }
        });
       }

    public void deleteRegisteredTemplate(String templateId) {
        templateProvider.deleteRegisteredTemplate(templateId);
    }


    @Override
    public List<RegisteredTemplate> getRegisteredTemplates() {
        return templateProvider.getRegisteredTemplates();
    }

    @Override
    public NifiFeed createFeed(FeedMetadata feedMetadata) {
        NifiFeed feed = feedProvider.createFeed(feedMetadata);
        if (feed.isSuccess()) {
            //requery to get the latest version
            FeedMetadata updatedFeed = getFeedById(feed.getFeedMetadata().getId());
            feed.setFeedMetadata(updatedFeed);
        }
        return feed;

    }

    @Override
    public void saveFeed(FeedMetadata feed) {
        feedProvider.saveFeed(feed);
    }

    @Override
    public void deleteFeed(@Nonnull final String feedId) {
        // Step 1: Fetch feed metadata
        final FeedMetadata feed = feedProvider.getFeedById(feedId);
        if (feed == null) {
            throw new IllegalArgumentException("Unknown feed: " + feedId);
        }

        // Step 2: Enable NiFi cleanup flow
        boolean needsCleanup = false;
        ProcessGroupDTO nifiFeed;
        final ProcessGroupDTO nifiGroup = nifiRestClient.getProcessGroupByName("root", feed.getSystemCategoryName());

        if (nifiGroup != null) {
            nifiFeed = nifiRestClient.getProcessGroupByName(nifiGroup.getId(), feed.getSystemFeedName());
            if (nifiFeed != null) {
                needsCleanup = nifiRestClient.setInputAsRunningByProcessorMatchingType(nifiFeed.getId(), "com.thinkbiganalytics.nifi.v2.metadata.TriggerCleanup");
            }
        }

        // Step 3: Run NiFi cleanup flow
        if (needsCleanup) {
            final FeedCompletionListener listener = new FeedCompletionListener(feed, Thread.currentThread());
            eventService.addListener(listener);

            feedProvider.enableFeedCleanup(feedId);
            eventService.notify(new CleanupTriggerEvent(feedProvider.resolveFeed(feedId)));

            long remaining = 60000L;
            while (remaining > 0 && (listener.getState() == null || listener.getState() == FeedOperation.State.STARTED)) {
                final long start = System.currentTimeMillis();
                try {
                    Thread.sleep(remaining);
                } catch (InterruptedException e) {
                    // ignored
                }
                remaining -= System.currentTimeMillis() - start;
            }

            eventService.removeListener(listener);

            if (listener.getState() != FeedOperation.State.SUCCESS) {
                throw new IllegalStateException("Cleanup feed failed with state: " + listener.getState());
            }
        }

        // Step 4: Remove feed from NiFi
        final ConnectionsEntity connections;
        final List<ProcessGroupDTO> processGroups;

        if (nifiGroup != null) {
            ProcessGroupEntity parentProcessGroup = nifiRestClient.getProcessGroup(nifiGroup.getId(), true, true);
            connections = nifiRestClient.getProcessGroupConnections(parentProcessGroup.getProcessGroup().getId());
            processGroups = NifiConnectionUtil.findProcessGroupsMatchingFeedName(parentProcessGroup.getProcessGroup().getContents().getProcessGroups(), feed.getSystemFeedName());
        } else {
            connections = null;
            processGroups = Collections.emptyList();
        }

        for (ProcessGroupDTO processGroup : processGroups) {
            nifiRestClient.disableAllInputProcessors(processGroup.getId());
            nifiRestClient.stopInputs(processGroup.getId());

            if (connections != null) {
                for (ConnectionDTO connection : NifiConnectionUtil.findConnectionsMatchingDestinationGroupId(connections.getConnections(), processGroup.getId())) {
                    String type = connection.getSource().getType();
                    if (NifiConstants.NIFI_PORT_TYPE.INPUT_PORT.name().equalsIgnoreCase(type)) {
                        nifiRestClient.stopInputPort(connection.getSource().getGroupId(), connection.getSource().getId());
                        nifiRestClient.deleteConnection(connection, false);
                    }
                }
                for (ConnectionDTO connection : NifiConnectionUtil.findConnectionsMatchingSourceGroupId(connections.getConnections(), processGroup.getId())) {
                    nifiRestClient.deleteConnection(connection, false);
                }
            }

            nifiRestClient.deleteProcessGroup(processGroup);
        }

        // Step 5: Delete database entries
        feedProvider.deleteFeed(feedId);
    }

    private boolean updateNifiFeedRunningStatus(FeedSummary feedSummary, Feed.State state) {
        boolean updatedNifi = false;
        if (feedSummary != null && feedSummary.getState().equals(state.name())) {

            ProcessGroupDTO group = nifiRestClient.getProcessGroupByName("root", feedSummary.getSystemCategoryName());
            if (group != null) {
                ProcessGroupDTO feed = nifiRestClient.getProcessGroupByName(group.getId(), feedSummary.getSystemFeedName());
                if (feed != null) {
                    ProcessGroupEntity entity = null;
                    if (state.equals(Feed.State.ENABLED)) {
                        entity = nifiRestClient.startAll(feed.getId(), feed.getParentGroupId());
                    } else if (state.equals(Feed.State.DISABLED)) {
                        entity = nifiRestClient.stopInputs(feed.getId());
                    }

                    if (entity != null) {
                        updatedNifi = true;
                    }
                }
            }
        }
        return updatedNifi;
    }

    //@Transactional(transactionManager = "metadataTransactionManager")
    public FeedSummary enableFeed(String feedId) {
        return metadataAccess.commit(new Command<FeedSummary>() {
            @Override
            public FeedSummary execute() {
                FeedMetadata feedMetadata = feedProvider.getFeedById(feedId);
                if (!feedMetadata.getState().equals(Feed.State.ENABLED.name())) {
                    FeedSummary feedSummary = feedProvider.enableFeed(feedId);

                    boolean updatedNifi = updateNifiFeedRunningStatus(feedSummary, Feed.State.ENABLED);
                    if (!updatedNifi) {
                        //rollback
                        throw new RuntimeException("Unable to enable Feed " + feedId);
                    }
                    return feedSummary;
                }
                return new FeedSummary(feedMetadata);

            }
        });

    }

    //@Transactional(transactionManager = "metadataTransactionManager")
    public FeedSummary disableFeed( final String feedId) {
        return metadataAccess.commit(new Command<FeedSummary>() {

            @Override
            public FeedSummary execute() {
                FeedMetadata feedMetadata = feedProvider.getFeedById(feedId);
                if (!feedMetadata.getState().equals(Feed.State.DISABLED.name())) {
                    FeedSummary feedSummary = feedProvider.disableFeed(feedId);
                    boolean updatedNifi = updateNifiFeedRunningStatus(feedSummary, Feed.State.DISABLED);
                    if (!updatedNifi) {
                        //rollback
                        throw new RuntimeException("Unable to disable Feed " + feedId);
                    }
                    return feedSummary;
                }
                return new FeedSummary(feedMetadata);
            }


        });
    }

    @Override
    public Collection<FeedMetadata> getFeeds() {
        return feedProvider.getFeeds();
    }

    @Override
    public Collection<? extends UIFeed> getFeeds(boolean verbose) {
        return feedProvider.getFeeds(verbose);
    }

    @Override
    public List<FeedSummary> getFeedSummaryData() {
        return feedProvider.getFeedSummaryData();
    }

    @Override
    public List<FeedSummary> getFeedSummaryForCategory(String categoryId) {
        return feedProvider.getFeedSummaryForCategory(categoryId);
    }

    @Override
    public FeedMetadata getFeedByName(String categoryName, String feedName) {
        return feedProvider.getFeedByName(categoryName, feedName);
    }

    @Override
    public FeedMetadata getFeedById(String feedId) {
        return feedProvider.getFeedById(feedId);
    }

    @Override
    public FeedMetadata getFeedById(String feedId, boolean refreshTargetTableSchema) {
        return feedProvider.getFeedById(feedId, refreshTargetTableSchema);
    }

    @Override
    public List<FeedMetadata> getReusableFeeds() {
        return feedProvider.getReusableFeeds();
    }

    @Override
    public Collection<FeedCategory> getCategories() {
        return categoryProvider.getCategories();
    }



    @Override
    public FeedCategory getCategoryBySystemName(String name) {
        return categoryProvider.getCategoryBySystemName(name);
    }

    @Override
    public void saveCategory(FeedCategory category) {
        categoryProvider.saveCategory(category);
    }

    @Override
    public boolean deleteCategory(String categoryId) throws InvalidOperationException {
        return categoryProvider.deleteCategory(categoryId);
    }

    /**
     * Listens for a feed completion then interrupts a target thread.
     */
    private static class FeedCompletionListener implements MetadataEventListener<FeedOperationStatusEvent> {

        /** Name of the feed to watch for */
        @Nonnull
        private final String feedName;

        /** Current state of the feed */
        @Nullable
        private FeedOperation.State state;

        /** Thread to interrupt */
        @Nonnull
        private final Thread target;

        /**
         * Constructs a {@code FeedCompletionListener} that listens for events for the specified feed then interrupts the specified thread.
         *
         * @param feed the feed to watch far
         * @param target the thread to interrupt
         */
        FeedCompletionListener(@Nonnull final FeedMetadata feed, @Nonnull final Thread target) {
            this.feedName = feed.getCategoryAndFeedName();
            this.target = target;
        }

        /**
         * Gets the current state of the feed.
         *
         * @return the feed state
         */
        @Nullable
        public FeedOperation.State getState() {
            return state;
        }

        @Override
        public void notify(@Nonnull final FeedOperationStatusEvent event) {
            if (event.getFeedName().equals(feedName)) {
                state = event.getState();
                target.interrupt();
            }
        }

    }
}
