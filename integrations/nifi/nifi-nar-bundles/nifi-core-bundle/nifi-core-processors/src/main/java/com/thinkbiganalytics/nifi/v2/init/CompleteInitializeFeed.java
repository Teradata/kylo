/**
 * 
 */
package com.thinkbiganalytics.nifi.v2.init;

import java.util.List;
import java.util.Set;

import org.apache.nifi.annotation.behavior.EventDriven;
import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.AllowableValue;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;

import com.thinkbiganalytics.nifi.v2.common.CommonProperties;
import com.thinkbiganalytics.nifi.v2.common.FeedProcessor;

/**
 *
 * @author Sean Felten
 */
@EventDriven
@InputRequirement(InputRequirement.Requirement.INPUT_ALLOWED)
@Tags({ "feed", "initialize", "initialization", "thinkbig"})
@CapabilityDescription("Indicates that a feed initialization flow has completed; either successfully or a failed")
public class CompleteInitializeFeed extends FeedProcessor {

    protected static final AllowableValue[] MODE_VALUES = new AllowableValue[] { 
                             new AllowableValue("SUCCESSFUL", "Successful", "The mode indicating feed initialization was successful."),
                             new AllowableValue("FAILURE", "Failure", "The mode indicating feed initialization failed.")
                          };

    protected static final PropertyDescriptor FAILURE_STRATEGY = new PropertyDescriptor.Builder()
                    .name("Initialization Failure Strategy")
                    .description("Indicates how this processor should behave when a flow file arrives after feed initialization has failed.")
                    .allowableValues(MODE_VALUES)
                    .required(true)
                    .build();

    /* (non-Javadoc)
     * @see org.apache.nifi.processor.AbstractProcessor#onTrigger(org.apache.nifi.processor.ProcessContext, org.apache.nifi.processor.ProcessSession)
     */
    @Override
    public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {
        FlowFile inputFF = session.get();
        
        if (inputFF != null) {
            String mode = context.getProperty(FAILURE_STRATEGY).getValue();
            
            if ("SUCCESSFUL".equals(mode)) {
                getMetadataRecorder().completeFeedInitialization(getFeedId());
            } else {
                getMetadataRecorder().failFeedInitialization(getFeedId());
            } 
            
            session.remove(inputFF);
        }
    }
    
    @Override
    protected void addProperties(List<PropertyDescriptor> list) {
        super.addProperties(list);
        list.add(FAILURE_STRATEGY);
    }
    
    @Override
    protected void addRelationships(Set<Relationship> set) {
        super.addRelationships(set);
        set.add(CommonProperties.REL_SUCCESS);
        set.add(CommonProperties.REL_FAILURE);
    }

}
