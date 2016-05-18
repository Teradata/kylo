/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.nifi.v2.ingest;

import com.thinkbiganalytics.nifi.core.api.metadata.MetadataProviderService;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataRecorder;

import org.apache.nifi.annotation.behavior.EventDriven;
import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.logging.ProcessorLog;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@EventDriven
@InputRequirement(InputRequirement.Requirement.INPUT_REQUIRED)
@Tags({"thinkbig", "registration", "put"})
@CapabilityDescription("Saves the outcome of registration.")

public class UpdateRegistration extends AbstractProcessor {

    public static final String SUCCESS = "success";
    public static final String FAIL = "fail";

    // Relationships

    public static final Relationship REL_SUCCESS = new Relationship.Builder()
        .name("success")
        .description("Registration succeeded.")
        .build();

    private final Set<Relationship> relationships;

    public static final PropertyDescriptor METADATA_SERVICE = new PropertyDescriptor.Builder()
        .name("Metadata Service")
        .description("The Think Big metadata service")
        .required(true)
        .identifiesControllerService(MetadataProviderService.class)
        .build();

    public static final PropertyDescriptor FEED_CATEGORY = new PropertyDescriptor.Builder()
        .name("System Feed Category")
        .description("System category of feed this processor supports")
        .required(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    public static final PropertyDescriptor FEED_NAME = new PropertyDescriptor.Builder()
        .name("System Feed Name")
        .description("System name of feed this processor supports")
        .required(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    public static final PropertyDescriptor RESULT = new PropertyDescriptor.Builder()
        .name("Result")
        .description("Indicates what should happen when a file with the same name already exists in the output directory")
        .required(true)
        .defaultValue(SUCCESS)
        .allowableValues(SUCCESS, FAIL)
        .build();

    private final List<PropertyDescriptor> propDescriptors;

    public UpdateRegistration() {
        HashSet r = new HashSet();
        r.add(REL_SUCCESS);

        this.relationships = Collections.unmodifiableSet(r);
        ArrayList pds = new ArrayList();
        pds.add(METADATA_SERVICE);
        pds.add(FEED_CATEGORY);
        pds.add(FEED_NAME);
        pds.add(RESULT);
        this.propDescriptors = Collections.unmodifiableList(pds);
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
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
        FlowFile flowFile = session.get();
        if (flowFile == null) {
            return;
        }
        ProcessorLog logger = getLogger();
        try {
            final MetadataProviderService metadataService = context.getProperty(METADATA_SERVICE).asControllerService(MetadataProviderService.class);
            final String categoryName = context.getProperty(FEED_CATEGORY).evaluateAttributeExpressions(flowFile).getValue();
            final String feedName = context.getProperty(FEED_NAME).evaluateAttributeExpressions(flowFile).getValue();
            //final String result = context.getProperty(RESULT).getValue();

            final MetadataRecorder client = metadataService.getRecorder();

            // TODO: Restore when working
            //client.recordFeedInitialization(session, incoming, true);

            //TODO: Remove workaround
            client.recordFeedInitialization(categoryName, feedName);

        } catch (final Exception e) {
            logger.warn("Failed to update registration due to {}. Will proceed anyway resulting in new files going through registration.", new Object[]{flowFile, e});
        }
        session.transfer(flowFile, REL_SUCCESS);
    }

}