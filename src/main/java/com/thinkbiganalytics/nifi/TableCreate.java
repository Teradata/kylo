/*
 * Copyright (c) 2015. Teradata Inc.
 */
package com.thinkbiganalytics.nifi;


import com.thinkbiganalytics.util.TableType;
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

@EventDriven
@InputRequirement(InputRequirement.Requirement.INPUT_ALLOWED)
@Tags({"hive", "ddl", "jdbc", "thinkbig"})
@CapabilityDescription("Creates a table based on a prototype table as a reference. ")
public class TableCreate extends AbstractProcessor {

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

    public static final PropertyDescriptor PROTOTYPE_TABLE = new PropertyDescriptor.Builder()
            .name("PrototypeTableName")
            .description("Qualified name of the Hive table used to generate this temporary feed table. Essentially the prototype table is cloned with a new name and location")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .expressionLanguageSupported(true)
            .build();

    public static final PropertyDescriptor TEMP_TABLE = new PropertyDescriptor.Builder()
            .name("TempTableName")
            .description("Qualified name of the temporary table to be created.")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .expressionLanguageSupported(true)
            .build();

    public static final PropertyDescriptor LOCATION = new PropertyDescriptor.Builder()
            .name("DataLocation")
            .description("URI location of the data for this external table")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .expressionLanguageSupported(true)
            .build();


    private final List<PropertyDescriptor> propDescriptors;

    public TableCreate() {
        final Set<Relationship> r = new HashSet<>();
        r.add(REL_SUCCESS);
        r.add(REL_FAILURE);
        relationships = Collections.unmodifiableSet(r);

        final List<PropertyDescriptor> pds = new ArrayList<>();
        pds.add(DBCP_SERVICE);
        pds.add(PROTOTYPE_TABLE);
        pds.add(TEMP_TABLE);
        pds.add(LOCATION);
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

        final DBCPService dbcpService = context.getProperty(DBCP_SERVICE).asControllerService(DBCPService.class);

        final StopWatch stopWatch = new StopWatch(true);
        String ddl = "";
        try (final Connection con = dbcpService.getConnection();
             final Statement st = con.createStatement()) {
            FlowFile outgoing = (incoming == null ? session.create() : incoming);

            String prototypeTable = context.getProperty(PROTOTYPE_TABLE).getValue();
            String location = context.getProperty(LOCATION).getValue();
            String tempTable = context.getProperty(TEMP_TABLE).getValue();

            ddl = createDDL(prototypeTable, location, tempTable, true);
            st.execute(ddl);

            //session.putAttribute(outgoing, SharedProperties.FEED_TABLE, tempTable);
            session.transfer(outgoing, REL_SUCCESS);

            String source = "";
            String entity = "";

            //
            // 1. Create database if not exists (source)
            st.execute(createDatabaseDDL(source));

            // 2. Using prototype create entity, set location and format to ORC w/ compression
            // 3. Using prototype, create feed table and invalid tables, alter table columns to all strings as appropriate
            // 4. Using prototype, create valid table

            // Return result


        } catch (final ProcessException | SQLException e) {
            e.printStackTrace();
            logger.error("Unable to execute SQL DDL {} for {} due to {}; routing to failure", new Object[]{ddl, incoming, e});
            session.transfer(incoming, REL_FAILURE);
        }
    }

    protected String createDatabaseDDL(String source) {
        StringBuffer sb = new StringBuffer();
        sb.append("CREATE DATABASE IF NOT EXISTS ").append(source);
        return sb.toString();
    }

    protected String createDDL(TableType tableType, String source, String entity) {

        TableType type = TableType.PROTOTYPE;

        StringBuffer sb = new StringBuffer();
        sb.append("CREATE EXTERNAL ")
        .append("TABLE IF NOT EXISTS ")
        .append(tableType.deriveQualifiedName(source, entity))
        .append(" LIKE ")
        .append(type.deriveQualifiedName(source, entity));
       // .append(tableType.deriveLocation(source, entity));




        return sb.toString();
    }

    protected String createDDL(String prototypeTable, String location, String tempTable, boolean managedTable) {

        StringBuffer sb = new StringBuffer();
        sb.append("CREATE ");
        if (!managedTable) {
            sb.append("EXTERNAL ");
        }
        sb.append("TABLE IF NOT EXISTS ")
                .append(tempTable)
                .append(" LIKE ")
                .append(prototypeTable);
        if (!managedTable) {
            sb.append(" LOCATION '")
                    .append(location).append("'");
        }
        return sb.toString();
    }

}