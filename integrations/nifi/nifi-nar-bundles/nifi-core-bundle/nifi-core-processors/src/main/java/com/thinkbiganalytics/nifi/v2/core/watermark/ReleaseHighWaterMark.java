/**
 * 
 */
package com.thinkbiganalytics.nifi.v2.core.watermark;

import java.util.List;

import org.apache.nifi.annotation.behavior.EventDriven;
import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.AllowableValue;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.exception.ProcessException;

import com.thinkbiganalytics.nifi.v2.common.CommonProperties;

/**
 *
 * @author Sean Felten
 */
@EventDriven
@InputRequirement(InputRequirement.Requirement.INPUT_ALLOWED)
@Tags({"release", "high-water", "mark", "thinkbig"})
@CapabilityDescription("Loadeds and makes active a watermark associated with a feed.")
public class ReleaseHighWaterMark extends HighWaterMarkProcessor {

    protected static final AllowableValue[] MODE_VALUES = new AllowableValue[] { 
                             new AllowableValue("COMMIT", "Commit", "Commits the updates to the high-water mark(s)"),
                             new AllowableValue("REJECT", "Reject", "Rejects any updates to the high-water mark(s)")
                          };
    
    protected static final PropertyDescriptor MODE = new PropertyDescriptor.Builder()
                    .name("Mode")
                    .description("Indicates whether this processor should commit or reject high-water mark updates")
                    .allowableValues(MODE_VALUES)
                    .defaultValue("COMMIT")
                    .required(true)
                    .build();
    
    protected static final PropertyDescriptor RELEASE_ALL = new PropertyDescriptor.Builder()
                    .name("Release All")
                    .description("If true, commits or rolls back all pending high-water marks.  "
                                    + "Otherwise, commits/rolls back only the named water mark property.")
                    .allowableValues(CommonProperties.BOOLEANS)
                    .defaultValue("true")
                    .build();

    /* (non-Javadoc)
     * @see org.apache.nifi.processor.AbstractProcessor#onTrigger(org.apache.nifi.processor.ProcessContext, org.apache.nifi.processor.ProcessSession)
     */
    @Override
    public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.nifi.v2.common.BaseProcessor#addProperties(java.util.List)
     */
    @Override
    protected void addProperties(List<PropertyDescriptor> list) {
        super.addProperties(list);
        list.add(MODE);
        list.add(RELEASE_ALL);
    }
}
