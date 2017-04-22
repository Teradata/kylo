package com.thinkbiganalytics.nifi.v2.spark;

/*-
 * #%L
 * thinkbig-nifi-spark-processors
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

import com.thinkbiganalytics.nifi.processor.AbstractNiFiProcessor;

import org.apache.nifi.annotation.behavior.EventDriven;
import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

@EventDriven
@InputRequirement(InputRequirement.Requirement.INPUT_REQUIRED)
@Tags({"spark", "thinkbig"})
@CapabilityDescription("Checks to see if a spark context is running")
public class CheckSparkContextExists extends AbstractNiFiProcessor {

    // Relationships
    public static final Relationship REL_CONTEXT_EXISTS = new Relationship.Builder()
        .name("Exists")
        .description("Context Exists")
        .build();

    public static final Relationship REL_CONTEXT_NOT_EXISTS = new Relationship.Builder()
        .name("Does Not Exist")
        .description("Context Does Not Exist")
        .build();

    public static final Relationship REL_FAILURE = new Relationship.Builder()
        .name("failure")
        .description("Spark context failed. Incoming FlowFile will be penalized and routed to this relationship")
        .build();

    public static final PropertyDescriptor CONTEXT_NAME = new PropertyDescriptor.Builder()
        .name("Context Name")
        .description("Name of the Long Running Spark Context")
        .required(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    public static final PropertyDescriptor JOB_SERVICE = new PropertyDescriptor.Builder()
        .name("Spark Job Service")
        .description("The Controller Service that is used to manage long running spark contexts.")
        .required(true)
        .identifiesControllerService(JobService.class)
        .build();

    private final Set<Relationship> relationships;

    private List<PropertyDescriptor> propDescriptors;

    public CheckSparkContextExists() {
        final Set<Relationship> r = new HashSet<>();
        r.add(REL_CONTEXT_EXISTS);
        r.add(REL_CONTEXT_NOT_EXISTS);
        r.add(REL_FAILURE);
        relationships = Collections.unmodifiableSet(r);
    }

    @Override
    protected void init(@Nonnull final ProcessorInitializationContext context) {
        super.init(context);

        // Create list of properties
        final List<PropertyDescriptor> pds = new ArrayList<>();
        pds.add(CONTEXT_NAME);
        pds.add(JOB_SERVICE);

        propDescriptors = Collections.unmodifiableList(pds);
    }

    @Override
    public Set<Relationship> getRelationships() {
        return relationships;
    }

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return propDescriptors;
    }


    @Override
    public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {

        final ComponentLog logger = getLog();
        FlowFile flowFile = null;

        try {
            if (context.hasIncomingConnection()) {
                flowFile = session.get();
                if (flowFile == null) {
                    return;
                }
            }
        } catch (NoSuchMethodError e) {
            logger.error("Failed to get incoming", e);
            session.transfer(flowFile, REL_FAILURE);
        }
        FlowFile outgoing = (flowFile == null ? session.create() : flowFile);

        final String contextName = context.getProperty(CONTEXT_NAME).evaluateAttributeExpressions(flowFile).getValue().trim();
        final JobService jobService = context.getProperty(JOB_SERVICE).asControllerService(JobService.class);

        boolean contextExists = jobService.checkIfContextExists(contextName);

        if (contextExists) {
            session.transfer(outgoing, REL_CONTEXT_EXISTS);
        } else {
            session.transfer(outgoing, REL_CONTEXT_NOT_EXISTS);
        }
    }
}