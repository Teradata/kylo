/**
 * 
 */
package com.thinkbiganalytics.nifi.v2.core.watermark;

import java.util.List;
import java.util.Set;

import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.util.StandardValidators;

import com.thinkbiganalytics.nifi.core.api.metadata.MetadataProviderService;
import com.thinkbiganalytics.nifi.v2.common.BaseProcessor;
import com.thinkbiganalytics.nifi.v2.common.CommonProperties;

/**
 *
 * @author Sean Felten
 */
public abstract class HighWaterMarkProcessor extends BaseProcessor {

    protected static final PropertyDescriptor HIGH_WATER_MARK = new PropertyDescriptor.Builder()
                    .name("High-Water Mark")
                    .description("Name of the high-water mark managed for this feed")
                    .required(true)
                    .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
                    .expressionLanguageSupported(true)
                    .build();
    
    protected static final PropertyDescriptor PROPERTY_NAME = new PropertyDescriptor.Builder()
                    .name("High-Water Mark Value Property Name")
                    .description("Name of the property that should be set to the current high-water mark value")
                    .defaultValue("highWaterMark")
                    .required(true)
                    .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
                    .expressionLanguageSupported(true)
                    .build();

    private transient String feedId;

    @OnScheduled
    public void scheduled(ProcessContext context) {
        String category = context.getProperty(CommonProperties.FEED_CATEGORY).getValue();
        String feedName = context.getProperty(CommonProperties.FEED_NAME).getValue();
        MetadataProviderService metadataController = context.getProperty(CommonProperties.METADATA_SERVICE).asControllerService(MetadataProviderService.class);
        
        try {
            this.feedId = metadataController.getProvider().getFeedId(category, feedName);
        } catch (Exception e) {
            getLogger().warn("Failure retrieving feed metadata" + category + "/" + feedName, e);
            // TODO Swallowing for now until metadata client is working again
        }
    }
    
    @Override
    protected void addProperties(List<PropertyDescriptor> list) {
        super.addProperties(list);
        list.add(HIGH_WATER_MARK);
        list.add(PROPERTY_NAME);
    }
    
    @Override
    protected void addRelationships(Set<Relationship> set) {
        super.addRelationships(set);
        set.add(CommonProperties.REL_SUCCESS);
        set.add(CommonProperties.REL_FAILURE);
    }
    
    protected String getFeedId() {
        return feedId;
    }
}
