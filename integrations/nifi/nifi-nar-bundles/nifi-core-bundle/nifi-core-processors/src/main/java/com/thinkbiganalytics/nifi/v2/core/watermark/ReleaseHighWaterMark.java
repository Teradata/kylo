/**
 *
 */
package com.thinkbiganalytics.nifi.v2.core.watermark;

/*-
 * #%L
 * thinkbig-nifi-core-processors
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

import com.thinkbiganalytics.nifi.core.api.metadata.ActiveWaterMarksCancelledException;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataProviderService;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataRecorder;
import com.thinkbiganalytics.nifi.provenance.KyloProcessorFlowType;
import com.thinkbiganalytics.nifi.provenance.NiFiProvenanceConstants;
import com.thinkbiganalytics.nifi.v2.common.CommonProperties;

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

import java.util.List;
import java.util.Set;

/**
 * Releases the high-water mark (commits or rolls back).
 */
@EventDriven
@InputRequirement(InputRequirement.Requirement.INPUT_ALLOWED)
@Tags({"release", "high-water", "mark", "thinkbig"})
@CapabilityDescription("Releases the watermark claim associated with a feed.")
public class ReleaseHighWaterMark extends HighWaterMarkProcessor {

    protected static final AllowableValue[] MODE_VALUES = new AllowableValue[]{
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
        .required(true)
        .build();

    Relationship CANCELLED_WATER_MARK = new Relationship.Builder()
        .name("cancelledWaterMark")
        .description("The active water mark for the flowfile was cancelled")
        .autoTerminateDefault(true)
        .build();


    /* (non-Javadoc)
     * @see org.apache.nifi.processor.AbstractProcessor#onTrigger(org.apache.nifi.processor.ProcessContext, org.apache.nifi.processor.ProcessSession)
     */
    @Override
    public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {
        MetadataRecorder recorder = context.getProperty(CommonProperties.METADATA_SERVICE).asControllerService(MetadataProviderService.class).getRecorder();
        FlowFile ff = session.get();

        if (ff != null) {
            try {
                ff = initialize(context, session, ff);
            } catch (Exception e) {
                getLog().error("Failure during initialization", e);
                session.transfer(ff, CommonProperties.REL_FAILURE);
            }

            String mode = context.getProperty(MODE).toString();

            try {
                if (mode.equals("COMMIT")) {
                    if (context.getProperty(RELEASE_ALL).asBoolean()) {
                        ff = recorder.commitAllWaterMarks(session, ff, getFeedId(context, ff));
                    } else {
                        String waterMarkName = context.getProperty(HIGH_WATER_MARK).evaluateAttributeExpressions(ff).toString();
                        ff = recorder.commitWaterMark(session, ff, getFeedId(context, ff), waterMarkName);
                    }
                } else {
                    if (context.getProperty(RELEASE_ALL).asBoolean()) {
                        ff = recorder.releaseAllWaterMarks(session, ff, getFeedId(context, ff));
                    } else {
                        String waterMarkName = context.getProperty(HIGH_WATER_MARK).evaluateAttributeExpressions(ff).toString();
                        ff = recorder.releaseWaterMark(session, ff, getFeedId(context, ff), waterMarkName);
                    }
                }

                session.transfer(ff, CommonProperties.REL_SUCCESS);
            } catch (ActiveWaterMarksCancelledException e) {
                transferForCancelledWaterMarks(context, session, ff, e);
            } catch (Exception e) {
                getLog().warn("Failure during release of high-water mark(s)", e);
                session.transfer(ff, CommonProperties.REL_FAILURE);
            }
        }

    }

    private void transferForCancelledWaterMarks(ProcessContext context, ProcessSession session, FlowFile ff, ActiveWaterMarksCancelledException ex) {
        FlowFile resultFF = session.putAttribute(ff, NiFiProvenanceConstants.NiFiKyloJobExecutionState, KyloProcessorFlowType.WARNING.toString());
        resultFF = session.putAttribute(ff, "kylo.waterMarksCancelled", ex.getWaterMarkNames().toString());
        
        if (context.hasConnection(CANCELLED_WATER_MARK)) {
            getLog().info("Active high-water mark(s) were canceled for feed: {}, water mark name(s): {}", new Object[]{ex.getFeedId(), ex.getWaterMarkNames()});
            session.transfer(resultFF, CANCELLED_WATER_MARK);
        } else {
            getLog().warn("Active high-water mark(s) were canceled for feed: {}, water mark name(s): {}", new Object[]{ex.getFeedId(), ex.getWaterMarkNames()});
            session.transfer(resultFF, CommonProperties.REL_FAILURE);
        }
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
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.nifi.v2.core.watermark.HighWaterMarkProcessor#addRelationships(java.util.Set)
     */
    @Override
    protected void addRelationships(Set<Relationship> set) {
        super.addRelationships(set);
        set.add(CANCELLED_WATER_MARK);
    }
}
