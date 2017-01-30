/**
 *
 */
package com.thinkbiganalytics.nifi.v2.metadata;

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

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;

/**
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
