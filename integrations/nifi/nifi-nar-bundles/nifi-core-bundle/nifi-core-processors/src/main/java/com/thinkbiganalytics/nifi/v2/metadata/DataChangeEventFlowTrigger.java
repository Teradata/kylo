/**
 *
 */
package com.thinkbiganalytics.nifi.v2.metadata;

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;

/**
 * @author Sean Felten
 */
public class DataChangeEventFlowTrigger extends AbstractFeedProcessor {

    public static final PropertyDescriptor SLA_NAME = new PropertyDescriptor.Builder()
            .name("SLA name")
            .displayName("SLA name")
            .description("The name of the service level agreement used as a precondition to start")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final Relationship PRECONDITION_FAILURE = new Relationship.Builder()
            .name("precondition-failure")
            .description("Relationship followed when the required preconditions to proceed were not met.")
            .build();

    @Override
    public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {
        // TODO Auto-generated method stub

    }

}
