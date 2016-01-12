/*
 * Copyright (c) 2015. Teradata Inc.
 */

package com.thinkbiganalytics.nifi;

import org.apache.nifi.annotation.behavior.EventDriven;
import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.dbcp.DBCPService;
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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.TimeUnit;


@EventDriven
@InputRequirement(InputRequirement.Requirement.INPUT_ALLOWED)
@Tags({"validate", "thinkbig"})
@CapabilityDescription("Validate data and separates into Valid, Invalid, and Errors related to parse errors.")
public class ValidateData extends AbstractProcessor {

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

    public static final PropertyDescriptor DBCP_SERVICE = new PropertyDescriptor.Builder()
            .name("Database Connection Pooling Service")
            .description("The Controller Service that is used to obtain connection to database")
            .required(true)
            .identifiesControllerService(DBCPService.class)
            .build();

    public static final PropertyDescriptor SQL_DDL_STATEMENT = new PropertyDescriptor.Builder()
            .name("DDL Statement")
            .description("DDL statement")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .expressionLanguageSupported(true)
            .build();

    private final List<PropertyDescriptor> propDescriptors;

    public ValidateData() {
        final Set<Relationship> r = new HashSet<>();
        r.add(REL_SUCCESS);
        r.add(REL_FAILURE);
        relationships = Collections.unmodifiableSet(r);

        final List<PropertyDescriptor> pds = new ArrayList<>();
        pds.add(DBCP_SERVICE);
        pds.add(SQL_DDL_STATEMENT);
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

        getLogger().info("Incoming: " + (incoming == null ? " NULL!!!" : "SET"));
        final DBCPService dbcpService = context.getProperty(DBCP_SERVICE).asControllerService(DBCPService.class);
        final String ddlQuery = (incoming == null ? context.getProperty(SQL_DDL_STATEMENT).getValue() : context.getProperty(SQL_DDL_STATEMENT).evaluateAttributeExpressions(incoming).getValue());
        getLogger().info("DDL QUERY got " + ddlQuery);
        getLogger().info("EXPRESSION " + context.getProperty(SQL_DDL_STATEMENT).evaluateAttributeExpressions(incoming).getValue());

        final StopWatch stopWatch = new StopWatch(true);

        try (final Connection con = dbcpService.getConnection();
             final Statement st = con.createStatement()) {
            FlowFile outgoing = (incoming == null ? session.create() : incoming);

            boolean result = st.execute(ddlQuery);
            session.getProvenanceReporter().modifyContent(outgoing, "Execution result " + result, stopWatch.getElapsed(TimeUnit.MILLISECONDS));

            session.transfer(outgoing, REL_SUCCESS);
        } catch (final ProcessException | SQLException e) {
            e.printStackTrace();
            logger.error("Unable to execute SQL DDL {} for {} due to {}; routing to failure", new Object[]{ddlQuery, incoming, e});
            session.transfer(incoming, REL_FAILURE);
        }
    }


}