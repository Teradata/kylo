package com.thinkbiganalytics.feedmgr.service.feed;

import com.thinkbiganalytics.feedmgr.nifi.PropertyExpressionResolver;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.NifiFeed;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.rest.model.ReusableTemplateConnectionInfo;
import com.thinkbiganalytics.feedmgr.security.FeedsAccessControl;
import com.thinkbiganalytics.nifi.feedmgr.CreateFeedBuilder;
import com.thinkbiganalytics.nifi.feedmgr.FeedRollbackException;
import com.thinkbiganalytics.nifi.feedmgr.InputOutputPort;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClient;
import com.thinkbiganalytics.nifi.rest.model.NifiProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.nifi.rest.support.NifiPropertyUtil;
import com.thinkbiganalytics.security.AccessController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    protected abstract RegisteredTemplate getRegisteredTemplateWithAllProperties(String templateId);


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
            feedBuilder = nifiRestClient.newFeedBuilder(registeredTemplate.getNifiTemplateId(), feedMetadata.getCategory().getSystemName(), feedMetadata.getSystemFeedName());

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

}
