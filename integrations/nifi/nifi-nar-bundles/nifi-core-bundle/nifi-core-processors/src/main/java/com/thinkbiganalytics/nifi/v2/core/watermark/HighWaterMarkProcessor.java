/**
 * 
 */
package com.thinkbiganalytics.nifi.v2.core.watermark;

import java.util.List;
import java.util.Set;

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.util.StandardValidators;

import com.thinkbiganalytics.nifi.v2.common.CommonProperties;
import com.thinkbiganalytics.nifi.v2.common.FeedProcessor;

/**
 * Base abstract processor for high-water mark processors.
 * 
 * @author Sean Felten
 */
public abstract class HighWaterMarkProcessor extends FeedProcessor {

    protected static final PropertyDescriptor HIGH_WATER_MARK = new PropertyDescriptor.Builder()
                    .name("High-Water Mark")
                    .description("Name of the high-water mark managed for this feed")
                    .defaultValue("highWaterMark")
                    .required(true)
                    .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
                    .expressionLanguageSupported(true)
                    .build();
    
    protected static final PropertyDescriptor PROPERTY_NAME = new PropertyDescriptor.Builder()
                    .name("High-Water Mark Value Property Name")
                    .description("Name of the property that should be set to the current high-water mark value")
                    .defaultValue("water.mark")
                    .required(true)
                    .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
                    .expressionLanguageSupported(true)
                    .build();
    
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
}
