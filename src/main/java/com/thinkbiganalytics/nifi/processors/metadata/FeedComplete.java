/**
 * 
 */
package com.thinkbiganalytics.nifi.processors.metadata;

import java.util.List;
import java.util.Set;

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;

import com.thinkbiganalytics.metadata.api.op.DataOperation;
import com.thinkbiganalytics.metadata.api.op.DataOperation.State;
import com.thinkbiganalytics.metadata.api.op.DataOperationsProvider;

/**
 *
 * @author Sean Felten
 */
public class FeedComplete extends FeedProcessor {
    
    public static final PropertyDescriptor COMPLETION_RESULT = new PropertyDescriptor.Builder()
            .name("completion_result")
            .displayName("Completion result")
            .description("The completion result of the feed")
            .allowableValues("SUCCESS", "FAILURE")
            .required(true)
            .build();
    public static final Relationship PROCEED = new Relationship.Builder()
            .name("Proceed")
            .description("Proceed with flow processing after metadata capture.")
            .build();

    /* (non-Javadoc)
     * @see org.apache.nifi.processor.AbstractProcessor#onTrigger(org.apache.nifi.processor.ProcessContext, org.apache.nifi.processor.ProcessSession)
     */
    @Override
    public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {
        try {
            FlowFile flowFile = session.get();
            DataOperationsProvider operationProvider = getProviderService(context).getDataOperationsProvider();
            String opIdStr = flowFile.getAttribute(OPERATON_ID_PROP);
            
            if (opIdStr != null) {
                DataOperation.ID opId = operationProvider.asOperationId(opIdStr);
                
                operationProvider.updateOperation(opId, "", getOperationState(context));
            }
            
            session.transfer(flowFile, PROCEED);
        } catch (Exception e) {
            context.yield();
            session.rollback();
            getLogger().error("Unexpected error processing feed completion", e);
            throw new ProcessException(e);
        }
    }
    
    @Override
    protected void addProperties(List<PropertyDescriptor> props) {
        super.addProperties(props);
        props.add(COMPLETION_RESULT);
    }
    
    @Override
    protected void addRelationships(Set<Relationship> rels) {
        super.addRelationships(rels);
        rels.add(PROCEED);
    }
    
    private State getOperationState(ProcessContext context) {
        String result = context.getProperty(COMPLETION_RESULT).getValue();
        
        if (result.equals("FAILURE")) {
            return State.FAILURE;
        } else {
            return State.SUCCESS;
        }
    }

}
