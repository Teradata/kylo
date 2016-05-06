package com.thinkbiganalytics.feedmgr.service.feed;

import com.thinkbiganalytics.feedmgr.nifi.PropertyExpressionResolver;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.NifiFeed;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.rest.model.ReusableTemplateConnectionInfo;
import com.thinkbiganalytics.nifi.feedmgr.CreateFeedBuilder;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClient;
import com.thinkbiganalytics.nifi.rest.model.NifiProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.nifi.rest.support.NifiPropertyUtil;
import com.thinkbiganalytics.rest.JerseyClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Created by sr186054 on 5/4/16.
 */
public abstract class AbstractFeedManagerFeedService implements FeedManagerFeedService {

    @Autowired
    private NifiRestClient nifiRestClient;

    protected abstract RegisteredTemplate getRegisteredTemplateWithAllProperties(String templateId) throws JerseyClientException;


    @Transactional(transactionManager = "metadataTransactionManager")
    public NifiFeed createFeed(FeedMetadata feedMetadata) throws JerseyClientException {
        NifiFeed feed = null;
        //replace expressions with values
        if(feedMetadata.getTable() != null) {
            feedMetadata.getTable().updateMetadataFieldValues();
        }
        if(feedMetadata.getSchedule() != null) {
            feedMetadata.getSchedule().updateDependentFeedNamesString();
        }

        if(feedMetadata.getProperties() == null) {
            feedMetadata.setProperties(new ArrayList<NifiProperty>());
        }
        //get all the properties for the metadata
        RegisteredTemplate
                registeredTemplate = getRegisteredTemplateWithAllProperties(feedMetadata.getTemplateId());
        List<NifiProperty> matchedProperties =  NifiPropertyUtil
                .matchAndSetPropertyByIdKey(registeredTemplate.getProperties(), feedMetadata.getProperties());
        feedMetadata.setProperties(registeredTemplate.getProperties());
        //resolve any ${metadata.} properties
        List<NifiProperty> resolvedProperties = PropertyExpressionResolver.resolvePropertyExpressions(feedMetadata);

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
                feedBuilder = nifiRestClient.newFeedBuilder(registeredTemplate.getNifiTemplateId(), feedMetadata.getCategory().getSystemName(), feedMetadata.getFeedName());

        if(registeredTemplate.isReusableTemplate()){
            feedBuilder.setReusableTemplate(true);
            feedMetadata.setIsReusableFeed(true);
        }
        else {
            feedBuilder.inputProcessorType(feedMetadata.getInputProcessorType())
                    .feedSchedule(feedMetadata.getSchedule()).properties( feedMetadata.getProperties());
            if(registeredTemplate.usesReusableTemplate())
            {
                ReusableTemplateConnectionInfo reusableInfo = registeredTemplate.getReusableTemplateConnections().get(0);
                //TODO change FeedBuilder to accept a List of ReusableTemplateConnectionInfo objects
                feedBuilder.reusableTemplateInputPortName(reusableInfo.getReusableTemplateInputPortName()).feedOutputPortName(reusableInfo.getFeedOutputPortName());
            }
        }
        NifiProcessGroup
                entity = feedBuilder.build();


        if (entity.isSuccess()) {
            feedMetadata.setNifiProcessGroupId(entity.getProcessGroupEntity().getProcessGroup().getId());
            // feedMetadata.setNifiProcessGroup(entity);
            Date createDate = new Date();
            feedMetadata.setCreateDate(createDate);
            feedMetadata.setUpdateDate(createDate);

            saveFeed(feedMetadata);
        }
        else {
            //rollback feed
        }
        feed = new NifiFeed(feedMetadata, entity);
        return feed;
    }

}
