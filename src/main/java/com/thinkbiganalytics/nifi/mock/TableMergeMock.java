package com.thinkbiganalytics.nifi.mock;

import com.thinkbiganalytics.components.TableMergeSupport;
import com.thinkbiganalytics.controller.ThriftService;
import com.thinkbiganalytics.nifi.TableMerge;
import com.thinkbiganalytics.util.PartitionSpec;
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
import org.apache.nifi.util.StopWatch;

import java.sql.Connection;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by Jeremy Merrifield on 3/25/16.
 */
@EventDriven
@InputRequirement(InputRequirement.Requirement.INPUT_ALLOWED)
@Tags({"hive", "ddl", "merge", "thinkbig"})
@CapabilityDescription("Mock integration test for table merge. Processor gets a connection and sleeps for 5 seconds")
public class TableMergeMock extends AbstractProcessor {
    // Relationships
    public static final Relationship REL_SUCCESS = new Relationship.Builder()
            .name("success")
            .description("Successfully created FlowFile from .")
            .build();
    public static final Relationship REL_FAILURE = new Relationship.Builder()
            .name("failure")
            .description("SQL query execution failed. Incoming FlowFile will be penalized and routed to this relationship")
            .build();
    private final Set<Relationship> relationships;

    public static final PropertyDescriptor THRIFT_SERVICE = new PropertyDescriptor.Builder()
            .name("Database Connection Pooling Service")
            .description("The Controller Service that is used to obtain connection to database")
            .required(true)
            .identifiesControllerService(ThriftService.class)
            .build();

    private final List<PropertyDescriptor> propDescriptors;

    public TableMergeMock() {
        final Set<Relationship> r = new HashSet<>();
        r.add(REL_SUCCESS);
        r.add(REL_FAILURE);
        relationships = Collections.unmodifiableSet(r);

        final List<PropertyDescriptor> pds = new ArrayList<>();
        pds.add(THRIFT_SERVICE);
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
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
        final ProcessorLog logger = getLogger();
        FlowFile incoming = session.get();
        FlowFile outgoing = (incoming == null ? session.create() : incoming);

        ThriftService thriftService = context.getProperty(THRIFT_SERVICE).asControllerService(ThriftService.class);

        final StopWatch stopWatch = new StopWatch(true);

        try (final Connection conn = thriftService.getConnection()) {
            Thread.currentThread().sleep(5000);
            stopWatch.stop();
            session.getProvenanceReporter().modifyContent(outgoing, "Execution completed", stopWatch.getElapsed(TimeUnit.MILLISECONDS));
            session.transfer(outgoing, REL_SUCCESS);

        } catch (final Exception e) {
            logger.error("Unable to execute mock table merge for {} due to {}; routing to failure", new Object[]{incoming, e});
            session.transfer(incoming, REL_FAILURE);
        }
    }
}
