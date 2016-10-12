package com.thinkbiganalytics.feedmgr.service.feed;

import com.thinkbiganalytics.datalake.authorization.HadoopAuthorizationService;
import com.thinkbiganalytics.datalake.authorization.model.HadoopAuthorizationGroup;
import com.thinkbiganalytics.feedmgr.nifi.CreateFeedBuilder;
import com.thinkbiganalytics.feedmgr.nifi.PropertyExpressionResolver;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.NifiFeed;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.rest.model.ReusableTemplateConnectionInfo;
import com.thinkbiganalytics.feedmgr.security.FeedsAccessControl;
import com.thinkbiganalytics.metadata.api.event.MetadataEventListener;
import com.thinkbiganalytics.metadata.api.event.MetadataEventService;
import com.thinkbiganalytics.metadata.api.event.feed.FeedPropertyChangeEvent;
import com.thinkbiganalytics.nifi.feedmgr.FeedRollbackException;
import com.thinkbiganalytics.nifi.feedmgr.InputOutputPort;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClient;
import com.thinkbiganalytics.nifi.rest.model.NifiProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.nifi.rest.support.NifiPropertyUtil;
import com.thinkbiganalytics.security.AccessController;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

/**
 * Created by sr186054 on 5/4/16.
 */
public abstract class AbstractFeedManagerFeedService implements FeedManagerFeedService {

    private static final Logger log = LoggerFactory.getLogger(AbstractFeedManagerFeedService.class);

    @Autowired
    private NifiRestClient nifiRestClient;

    @Autowired
    PropertyExpressionResolver propertyExpressionResolver;

    @Inject
    private AccessController accessController;

    @Inject
    private MetadataEventService metadataEventService;

    // I had to use autowired instead of Inject to allow null values.
    @Autowired(required = false)
    @Qualifier("hadoopAuthorizationService")
    private HadoopAuthorizationService hadoopAuthorizationService;

    /** Event listener for precondition events */
    private final MetadataEventListener<FeedPropertyChangeEvent> feedPropertyChangeListener = new FeedPropertyChangeDispatcher();

    protected abstract RegisteredTemplate getRegisteredTemplateWithAllProperties(String templateId);

    /**
     * Adds listeners for transferring events.
     */
    @PostConstruct
    public void addEventListener() {
        metadataEventService.addListener(feedPropertyChangeListener);
    }

    /**
     * Removes listeners and stops transferring events.
     */
    @PreDestroy
    public void removeEventListener() {
        metadataEventService.removeListener(feedPropertyChangeListener);
    }

    public NifiFeed createFeed(FeedMetadata feedMetadata) {
        this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.EDIT_FEEDS);

        NifiFeed feed = null;
        //replace expressions with values
        if (feedMetadata.getTable() != null) {
            feedMetadata.getTable().updateMetadataFieldValues();
        }
        if (feedMetadata.getSchedule() != null) {
            //     feedMetadata.getSchedule().updateDependentFeedNamesString();
        }

        if (feedMetadata.getProperties() == null) {
            feedMetadata.setProperties(new ArrayList<NifiProperty>());
        }

        feedMetadata.updateHadoopSecurityGroups();

        if (hadoopAuthorizationService == null) {
            feedMetadata.setHadoopAuthorizationType(hadoopAuthorizationService.HADOOP_AUTHORIZATION_TYPE_NONE);
        }
        else {
            feedMetadata.setHadoopAuthorizationType(hadoopAuthorizationService.getType());
        }

        //get all the properties for the metadata
        RegisteredTemplate
            registeredTemplate = getRegisteredTemplateWithAllProperties(feedMetadata.getTemplateId());

        List<NifiProperty> matchedProperties = NifiPropertyUtil
            .matchAndSetPropertyByIdKey(registeredTemplate.getProperties(), feedMetadata.getProperties(),NifiPropertyUtil.PROPERTY_MATCH_AND_UPDATE_MODE.UPDATE_ALL_PROPERTIES);
        if (matchedProperties.size() == 0) {
            matchedProperties =
                NifiPropertyUtil
                    .matchAndSetPropertyByProcessorName(registeredTemplate.getProperties(), feedMetadata.getProperties(), NifiPropertyUtil.PROPERTY_MATCH_AND_UPDATE_MODE.UPDATE_ALL_PROPERTIES);
        }
        feedMetadata.setProperties(registeredTemplate.getProperties());
        //resolve any ${metadata.} properties
        List<NifiProperty> resolvedProperties = propertyExpressionResolver.resolvePropertyExpressions(feedMetadata);

        //store all input related properties as well
        List<NifiProperty> inputProperties = NifiPropertyUtil
            .findInputProperties(registeredTemplate.getProperties());

        ///store only those matched and resolved in the final metadata store
        Set<NifiProperty> updatedProperties = new HashSet<>();
        updatedProperties.addAll(matchedProperties);
        updatedProperties.addAll(resolvedProperties);
        updatedProperties.addAll(inputProperties);
        feedMetadata.setProperties(new ArrayList<NifiProperty>(updatedProperties));

        CreateFeedBuilder
            feedBuilder = CreateFeedBuilder.newFeed(nifiRestClient, feedMetadata, registeredTemplate.getNifiTemplateId(), propertyExpressionResolver);

        if (registeredTemplate.isReusableTemplate()) {
            feedBuilder.setReusableTemplate(true);
            feedMetadata.setIsReusableFeed(true);
        } else {
            feedBuilder.inputProcessorType(feedMetadata.getInputProcessorType())
                .feedSchedule(feedMetadata.getSchedule()).properties(feedMetadata.getProperties());
            if (registeredTemplate.usesReusableTemplate()) {
                for (ReusableTemplateConnectionInfo connection : registeredTemplate.getReusableTemplateConnections()) {
                    feedBuilder.addInputOutputPort(new InputOutputPort(connection.getReusableTemplateInputPortName(), connection.getFeedOutputPortName()));
                }
            }
        }
        NifiProcessGroup
            entity = feedBuilder.build();

        feed = new NifiFeed(feedMetadata, entity);
        if (entity.isSuccess()) {
            feedMetadata.setNifiProcessGroupId(entity.getProcessGroupEntity().getProcessGroup().getId());

            try {
                saveFeed(feedMetadata);
                feed.setSuccess(true);
            } catch (Exception e) {
                feed.setSuccess(false);
                feed.addErrorMessage(e);
            }

        } else {
            feed.setSuccess(false);
        }
        if (!feed.isSuccess()) {
            if (!entity.isRolledBack()) {
                try {
                    feedBuilder.rollback();
                } catch (FeedRollbackException rollbackException) {
                    log.error("Error rolling back feed {}. {} ", feedMetadata.getCategoryAndFeedName(), rollbackException.getMessage());
                    feed.addErrorMessage("Error occurred in rolling back the Feed.");
                }
                entity.setRolledBack(true);
            }
        }
        return feed;
    }

    private class FeedPropertyChangeDispatcher implements MetadataEventListener<FeedPropertyChangeEvent> {

        @Override
        public void notify(@Nonnull final FeedPropertyChangeEvent metadataEvent) {
            // TODO Remove metadata properties if they were removed from NiFI

            // TODO Only update if the nifi metadata properties that we care about changed
            if (hadoopAuthorizationService != null && metadataEvent.getHadoopSecurityGroupNames() != null) {
                String hdfsFoldersWithCommas = metadataEvent.getNewProperties().getProperty("nifi:metadata:securityGroupHdfsFolders").replace("\n", ",");
                Stream<String> hdfsFolders = Stream.of(hdfsFoldersWithCommas);

                String hiveTablesWithCommas = metadataEvent.getNewProperties().getProperty("nifi:metadata:securityGroupHiveTableNames").replace("\n", ",");
                Stream<String> hiveTables = Stream.of(hiveTablesWithCommas);
                String hiveSchema = metadataEvent.getNewProperties().getProperty("nifi:metadata:securityGroupHiveSchema");

                //List<String> securityGroups =  metadataEvent.getHadoopSecurityGroups().stream().map(group -> group.getName()).collect(Collectors.toList());

                 hadoopAuthorizationService.createReadOnlyPolicy("kylo_" + metadataEvent.getFeedCategory() ,metadataEvent.getFeedName()
                    , metadataEvent.getHadoopSecurityGroupNames()
                    , hdfsFolders.collect(Collectors.toList())
                    , hiveSchema
                    , hiveTables.collect(Collectors.toList()));
            }
        }
    }

}
