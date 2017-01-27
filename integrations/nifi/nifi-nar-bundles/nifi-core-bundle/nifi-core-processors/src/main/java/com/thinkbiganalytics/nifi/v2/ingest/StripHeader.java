package com.thinkbiganalytics.nifi.v2.ingest;

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


import com.thinkbiganalytics.ingest.StripHeaderSupport;
import com.thinkbiganalytics.nifi.processor.AbstractNiFiProcessor;

import org.apache.commons.lang3.mutable.MutableLong;
import org.apache.nifi.annotation.behavior.EventDriven;
import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.behavior.InputRequirement.Requirement;
import org.apache.nifi.annotation.behavior.SideEffectFree;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.util.StandardValidators;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@EventDriven
@SideEffectFree
@Tags({"header", "text"})
@InputRequirement(Requirement.INPUT_REQUIRED)
@CapabilityDescription("Splits a text file(s) content from its header. The content of the header is passed through a separate relationship for validation")
public class StripHeader extends AbstractNiFiProcessor {

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
        super.init(context);

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

        final boolean isEnabled = context.getProperty(ENABLED).evaluateAttributeExpressions(flowFile).asBoolean();
        final int headerCount = context.getProperty(HEADER_LINE_COUNT).evaluateAttributeExpressions(flowFile).asInteger();

        // Empty files and no work to do will simply pass along content
        if (!isEnabled || headerCount == 0 || flowFile.getSize() == 0L) {
            final FlowFile contentFlowFile = session.clone(flowFile);
            session.transfer(contentFlowFile, REL_CONTENT);
            session.transfer(flowFile, REL_ORIGINAL);
            return;
        }

        final MutableLong headerBoundaryInBytes = new MutableLong(-1);

        session.read(flowFile, false, rawIn -> {
            try {
                // Identify the byte boundary of the header
                long bytes = headerSupport.findHeaderBoundary(headerCount, rawIn);
                headerBoundaryInBytes.setValue(bytes);

                if (bytes < 0) {
                    getLog().error("Unable to strip header {} expecting at least {} lines in file", new Object[]{flowFile, headerCount});
                }

            } catch (IOException e) {
                getLog().error("Unable to strip header {} due to {}; routing to failure", new Object[]{flowFile, e.getLocalizedMessage()}, e);
            }

        });

        long headerBytes = headerBoundaryInBytes.getValue();
        if (headerBytes < 0) {
            session.transfer(flowFile, REL_FAILURE);
        } else {
            // Transfer header
            final FlowFile headerFlowFile = session.clone(flowFile, 0, headerBytes);
            session.transfer(headerFlowFile, REL_HEADER);

            // Transfer content
            long contentBytes = flowFile.getSize() - headerBytes;
            final FlowFile contentFlowFile = session.clone(flowFile, headerBytes, contentBytes);
            session.transfer(contentFlowFile, REL_CONTENT);

            session.transfer(flowFile, REL_ORIGINAL);
        }
    }
}
