/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.nifi.v2.ingest;


import com.thinkbiganalytics.ingest.StripHeaderSupport;

import org.apache.commons.io.IOUtils;
import org.apache.nifi.annotation.behavior.EventDriven;
import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.behavior.InputRequirement.Requirement;
import org.apache.nifi.annotation.behavior.SideEffectFree;
import org.apache.nifi.annotation.behavior.SupportsBatching;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.logging.ProcessorLog;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.stream.io.ByteArrayOutputStream;
import org.apache.nifi.util.ObjectHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@EventDriven
@SideEffectFree
@SupportsBatching
@Tags({"header", "text"})
@InputRequirement(Requirement.INPUT_REQUIRED)
@CapabilityDescription("Splits a text file(s) content from its header. The content of the header is passed through a separate relationship for validation")
public class StripHeader extends AbstractProcessor {

    public static final PropertyDescriptor ENABLED = new PropertyDescriptor.Builder()
        .name("Enable processing")
        .description("Whether to strip the header")
        .required(true)
        .addValidator(StandardValidators.BOOLEAN_VALIDATOR)
        .defaultValue("false")
        .expressionLanguageSupported(true)
        .build();

    public static final PropertyDescriptor HEADER_LINE_COUNT = new PropertyDescriptor.Builder()
        .name("Header Line Count")
        .description("The number of lines that should be considered part of the header")
        .required(true)
        .addValidator(StandardValidators.NON_NEGATIVE_INTEGER_VALIDATOR)
        .defaultValue("1")
        .expressionLanguageSupported(true)
        .build();

    public static final Relationship REL_ORIGINAL = new Relationship.Builder()
        .name("original")
        .description("The original input file will be routed to this destination")
        .build();
    public static final Relationship REL_CONTENT = new Relationship.Builder()
        .name("content")
        .description("The content (stripped of header if enabled) will be routed to this destination")
        .build();
    public static final Relationship REL_HEADER = new Relationship.Builder()
        .name("header")
        .description("The header will be routed to this destination when header is stripped")
        .build();
    public static final Relationship REL_FAILURE = new Relationship.Builder()
        .name("failure")
        .description("If a file cannot be split for some reason, the original file will be routed to this destination and nothing will be routed elsewhere")
        .build();

    private List<PropertyDescriptor> properties;
    private Set<Relationship> relationships;

    @Override
    protected void init(final ProcessorInitializationContext context) {
        final List<PropertyDescriptor> properties = new ArrayList<>();
        properties.add(ENABLED);
        properties.add(HEADER_LINE_COUNT);

        this.properties = Collections.unmodifiableList(properties);

        final Set<Relationship> relationships = new HashSet<>();
        relationships.add(REL_ORIGINAL);
        relationships.add(REL_CONTENT);
        relationships.add(REL_HEADER);
        relationships.add(REL_FAILURE);
        this.relationships = Collections.unmodifiableSet(relationships);
    }

    @Override
    public Set<Relationship> getRelationships() {
        return relationships;
    }

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return properties;
    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) {
        final StripHeaderSupport headerSupport = new StripHeaderSupport();
        final FlowFile flowFile = session.get();
        if (flowFile == null) {
            return;
        }

        final ProcessorLog logger = getLogger();
        final boolean isEnabled = context.getProperty(ENABLED).evaluateAttributeExpressions(flowFile).asBoolean();
        final int headerCount = context.getProperty(HEADER_LINE_COUNT).evaluateAttributeExpressions(flowFile).asInteger();

        if (!isEnabled || headerCount == 0) {
            session.transfer(flowFile, REL_ORIGINAL);
            session.transfer(flowFile, REL_CONTENT);
            return;
        }
        final ObjectHolder<String> errorMessage = new ObjectHolder<>(null);

        final FlowFile contentFlowFile = session.create(flowFile);
        final FlowFile headerFlowFile = session.create(flowFile);

        // Read the content separating header from remainder of file
        session.read(flowFile, rawIn -> {

            final ByteArrayOutputStream headerStream = new ByteArrayOutputStream();
            session.write(contentFlowFile, rawContentOut -> {
                int rows = headerSupport.doStripHeader(headerCount, rawIn, headerStream, rawContentOut);
                if (rows < headerCount) {
                    errorMessage.set("Header Line Count is set to " + headerCount + " but file had only " + rows + " lines");
                    return;
                } else {
                    session.write(headerFlowFile, headerOS -> {
                        headerStream.writeTo(headerOS);
                        IOUtils.closeQuietly(headerStream);
                    });
                }
            });
        });

        if (errorMessage.get() != null) {
            logger.error("Unable to strip header {} due to {}; routing to failure", new Object[]{flowFile, errorMessage.get()});
            session.transfer(flowFile, REL_FAILURE);
            session.remove(contentFlowFile);
            session.remove(headerFlowFile);
            return;
        }
        session.transfer(flowFile, REL_ORIGINAL);
        session.transfer(contentFlowFile, REL_CONTENT);
        session.transfer(headerFlowFile, REL_HEADER);
        return;
    }

}
