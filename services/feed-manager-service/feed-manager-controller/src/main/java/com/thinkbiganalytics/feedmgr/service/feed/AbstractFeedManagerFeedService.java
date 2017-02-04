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

import com.thinkbiganalytics.feedmgr.nifi.CreateFeedBuilder;
import com.thinkbiganalytics.feedmgr.nifi.NifiFlowCache;
import com.thinkbiganalytics.feedmgr.nifi.PropertyExpressionResolver;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.NifiFeed;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.rest.model.ReusableTemplateConnectionInfo;
import com.thinkbiganalytics.feedmgr.security.FeedsAccessControl;
import com.thinkbiganalytics.nifi.feedmgr.FeedRollbackException;
import com.thinkbiganalytics.nifi.feedmgr.InputOutputPort;
import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;
import com.thinkbiganalytics.nifi.rest.model.NiFiPropertyDescriptorTransform;
import com.thinkbiganalytics.nifi.rest.model.NifiProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.nifi.rest.support.NifiPropertyUtil;
import com.thinkbiganalytics.security.AccessController;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

/**
 * Common Feed Manager methods used by the {@link DefaultFeedManagerFeedService} and {@link InMemoryFeedManagerFeedService}
 */
public abstract class AbstractFeedManagerFeedService implements FeedManagerFeedService {

    private static final Logger log = LoggerFactory.getLogger(AbstractFeedManagerFeedService.class);

    @Inject
    private LegacyNifiRestClient nifiRestClient;

    @Inject
    PropertyExpressionResolver propertyExpressionResolver;

    @Inject
    NifiFlowCache nifiFlowCache;

    @Inject
    private AccessController accessController;

    @Inject
    private NiFiPropertyDescriptorTransform propertyDescriptorTransform;


    protected abstract RegisteredTemplate getRegisteredTemplateWithAllProperties(String templateId);

    /**
     * Create/Update a Feed in NiFi
     *
     * @param feedMetadata the feed metadata
     * @return an object indicating if the feed creation was successful or not
     */
    public NifiFeed createFeed(FeedMetadata feedMetadata) {
        this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.EDIT_FEEDS);

        NifiFeed feed = null;
        if (StringUtils.isBlank(feedMetadata.getId())) {
            feedMetadata.setIsNew(true);
        }
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
        feedMetadata.setRegisteredTemplate(registeredTemplate);
        //resolve any ${metadata.} properties
        List<NifiProperty> resolvedProperties = propertyExpressionResolver.resolvePropertyExpressions(feedMetadata);

        //store all input related properties as well
        List<NifiProperty> inputProperties = NifiPropertyUtil
            .findInputProperties(registeredTemplate.getProperties());

        ///store only those matched and resolved in the final metadata store
        Set<NifiProperty> updatedProperties = new HashSet<>();
        //first get all those selected properties where the value differs from the template value

        List<NifiProperty> modifiedProperties = registeredTemplate.findModifiedDefaultProperties();
        if (modifiedProperties != null) {
            updatedProperties.addAll(modifiedProperties);
        }
        updatedProperties.addAll(matchedProperties);
        updatedProperties.addAll(resolvedProperties);
        updatedProperties.addAll(inputProperties);
        feedMetadata.setProperties(new ArrayList<NifiProperty>(updatedProperties));

        FeedMetadata.STATE state = FeedMetadata.STATE.NEW;
        try {
            state = FeedMetadata.STATE.valueOf(feedMetadata.getState());
        }catch (Exception e) {
            //if the string isnt valid, disregard as it will end up disabling the feed.
        }

        boolean enabled = (FeedMetadata.STATE.NEW.equals(state) && feedMetadata.isActive()) || FeedMetadata.STATE.ENABLED.equals(state);

        // flag to indicate to enable the feed later
        //if this is the first time for this feed and it is set to be enabled, mark it to be enabled after we commit to the JCR store
        boolean enableLater = false;
        if (enabled && feedMetadata.isNew()) {
            enableLater = true;
            enabled = false;
            feedMetadata.setState(FeedMetadata.STATE.DISABLED.name());
        }

        CreateFeedBuilder
            feedBuilder = CreateFeedBuilder.newFeed(nifiRestClient,nifiFlowCache, feedMetadata, registeredTemplate.getNifiTemplateId(), propertyExpressionResolver, propertyDescriptorTransform).enabled(enabled)
            .versionProcessGroup(false);

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
            feedMetadata.setNifiProcessGroupId(entity.getProcessGroupEntity().getId());

            try {

                saveFeed(feedMetadata);
                feed.setEnableAfterSave(enableLater);
                feed.setSuccess(true);
                feedBuilder.checkAndRemoveVersionedProcessGroup();
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
