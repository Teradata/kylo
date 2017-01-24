package com.thinkbiganalytics.nifi.v2.ingest;


import com.thinkbiganalytics.nifi.core.api.metadata.MetadataProviderService;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataRecorder;
import com.thinkbiganalytics.nifi.processor.AbstractNiFiProcessor;

import org.apache.nifi.annotation.behavior.EventDriven;
import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.behavior.InputRequirement.Requirement;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.thinkbiganalytics.nifi.v2.ingest.IngestProperties.FEED_CATEGORY;
import static com.thinkbiganalytics.nifi.v2.ingest.IngestProperties.FEED_NAME;
import static com.thinkbiganalytics.nifi.v2.ingest.IngestProperties.METADATA_SERVICE;
import static com.thinkbiganalytics.nifi.v2.ingest.IngestProperties.REL_SUCCESS;

@EventDriven
@InputRequirement(Requirement.INPUT_REQUIRED)
@Tags({"thinkbig", "registration", "route"})
@CapabilityDescription("Routes depending on whether a registration is required.  Registration is typically one-time setup such as creating permanent tables.")

public class RouteOnRegistration extends AbstractNiFiProcessor {

    private final Set<Relationship> relationships;

    // Relationships

    public static final Relationship REL_REGISTRATION_REQ = new Relationship.Builder()
        .name("registration_required")
        .description("Registration is required.")
        .build();

    private final List<PropertyDescriptor> propDescriptors;

    public RouteOnRegistration() {
        HashSet r = new HashSet();
        r.add(REL_SUCCESS);
        r.add(REL_REGISTRATION_REQ);
        this.relationships = Collections.unmodifiableSet(r);
        ArrayList pds = new ArrayList();
        pds.add(METADATA_SERVICE);
        pds.add(FEED_CATEGORY);
        pds.add(FEED_NAME);
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
        final ComponentLog logger = getLog();

        try {
            final MetadataProviderService metadataService = context.getProperty(METADATA_SERVICE).asControllerService(MetadataProviderService.class);
            final String categoryName = context.getProperty(FEED_CATEGORY).evaluateAttributeExpressions(flowFile).getValue();
            final String feedName = context.getProperty(FEED_NAME).evaluateAttributeExpressions(flowFile).getValue();

            final MetadataRecorder recorder = metadataService.getRecorder();

            // TODO: restore workaround
            //boolean required = recorder.isFeedInitialized(flowFile);

            // TODO: remove this workaround
            boolean isInitialized = recorder.isFeedInitialized(categoryName, feedName);
            if (isInitialized) {
                session.transfer(flowFile, REL_SUCCESS);
                return;
            }

        } catch (final Exception e) {
            logger.warn("Routing to registration required. Unable to determine registration status. Failed to route on registration due to {}", new Object[]{flowFile, e});
        }
        session.transfer(flowFile, REL_REGISTRATION_REQ);
    }

}
