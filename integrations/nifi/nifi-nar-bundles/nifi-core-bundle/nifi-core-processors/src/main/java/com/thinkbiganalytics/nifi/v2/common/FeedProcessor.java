/**
 * 
 */
package com.thinkbiganalytics.nifi.v2.common;

import java.util.List;

import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.ProcessContext;

import com.thinkbiganalytics.nifi.core.api.metadata.MetadataProvider;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataProviderService;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataRecorder;

/**
 * An abstract processor that can be configured with the feed canteory and name and
 * which will look up the feed's ID.
 * 
 * @author Sean Felten
 */
public abstract class FeedProcessor extends BaseProcessor {

    private transient String feedId;
    private transient MetadataProviderService providerService;

    @OnScheduled
    public void scheduled(ProcessContext context) {
        String category = context.getProperty(CommonProperties.FEED_CATEGORY).getValue();
        String feedName = context.getProperty(CommonProperties.FEED_NAME).getValue();
        MetadataProviderService metadataController = context.getProperty(CommonProperties.METADATA_SERVICE).asControllerService(MetadataProviderService.class);
        this.feedId = metadataController.getProvider().getFeedId(category, feedName);
        this.providerService = context.getProperty(CommonProperties.METADATA_SERVICE).asControllerService(MetadataProviderService.class);
    }
    
    @Override
    protected void addProperties(List<PropertyDescriptor> list) {
        super.addProperties(list);
        list.add(CommonProperties.METADATA_SERVICE);
        list.add(CommonProperties.FEED_CATEGORY);
        list.add(CommonProperties.FEED_NAME);
    }
    
    protected MetadataProvider getMetadataProvider() {
        return this.providerService.getProvider();
    }
    
    protected MetadataRecorder getMetadataRecorder() {
        return this.providerService.getRecorder();
    }
    
    protected String getFeedId() {
        return feedId;
    }

}
