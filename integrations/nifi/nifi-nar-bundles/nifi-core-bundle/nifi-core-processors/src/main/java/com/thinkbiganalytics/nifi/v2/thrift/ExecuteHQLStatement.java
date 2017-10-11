package com.thinkbiganalytics.nifi.v2.thrift;

/*-
 * #%L
 * thinkbig-nifi-hadoop-processors
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
import com.thinkbiganalytics.nifi.v2.ingest.IngestProperties;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.annotation.behavior.EventDriven;
import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.util.StopWatch;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;


@EventDriven
@InputRequirement(InputRequirement.Requirement.INPUT_REQUIRED)
@Tags({"hive", "ddl", "dml", "jdbc", "thinkbig"})
@CapabilityDescription("Execute provided HIVE or Spark statement. This can be any HQL DML or DDL statement that results in no results."
)
public class ExecuteHQLStatement extends AbstractNiFiProcessor {

    public static final PropertyDescriptor THRIFT_SERVICE = new PropertyDescriptor.Builder()
        .name("Database Connection Pooling Service")
        .description("The Controller Service that is used to obtain connection to database")
        .required(true)
        .identifiesControllerService(ThriftService.class)
        .build();
    public static final PropertyDescriptor SQL_DDL_STATEMENT = new PropertyDescriptor.Builder()
        .name("Statement")
        .description("Provide the DDL or DML statement. Return values will be ignored.")
        .required(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();
    private final Set<Relationship> relationships;
    private final List<PropertyDescriptor> propDescriptors;

    public ExecuteHQLStatement() {
        final Set<Relationship> r = new HashSet<>();
        r.add(IngestProperties.REL_SUCCESS);
        r.add(IngestProperties.REL_FAILURE);
        relationships = Collections.unmodifiableSet(r);

        final List<PropertyDescriptor> pds = new ArrayList<>();
        pds.add(THRIFT_SERVICE);
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
    public void onTrigger(final ProcessContext context, final ProcessSession session) {
        FlowFile flowFile = session.get();
        if (flowFile == null) {
            return;
        }

        final String ddlQuery = context.getProperty(SQL_DDL_STATEMENT).evaluateAttributeExpressions(flowFile).getValue();
        String[] hiveStatements = StringUtils.split(ddlQuery, ';');
        final ThriftService thriftService = context.getProperty(THRIFT_SERVICE).asControllerService(ThriftService.class);

        executeStatements(context,session, flowFile, hiveStatements, thriftService);
    }

    public void executeStatements(ProcessContext context,ProcessSession session, FlowFile flowFile, String[] hiveStatements, ThriftService thriftService) {
        final ComponentLog logger = getLog();

        String EXCEPTION_STATUS_KEY = "HQLStmt Status ";

        final StopWatch stopWatch = new StopWatch(true);

        try (final Connection con = thriftService.getConnection();
             final Statement st = con.createStatement()) {
            boolean result = false;
            EXCEPTION_STATUS_KEY = context.getName() + EXCEPTION_STATUS_KEY;

            for (String statement : hiveStatements) {
                //  leading whitespace will cause Hive statement to fail
                statement = statement.trim();
                logger.debug("Executing statement: '{}'", new Object[] {statement} );
                result = st.execute(statement);
            }

            session.getProvenanceReporter().modifyContent(flowFile, "Execution result " + result, stopWatch.getElapsed(TimeUnit.MILLISECONDS));
            session.transfer(flowFile, IngestProperties.REL_SUCCESS);
        } catch (final Exception e) {
            logger.error("Unable to execute SQL DDL {} for {} due to {}; routing to failure", new Object[]{hiveStatements, flowFile, e});
            logger.error(e.getMessage());
            //add the exception to the flow file
            flowFile = session.putAttribute(flowFile, EXCEPTION_STATUS_KEY, "Failed With Exception: "+(e.getMessage().length() > 300 ? e.getMessage().substring(0,300) : e.getMessage()));
            session.transfer(flowFile, IngestProperties.REL_FAILURE);
        }
    }

}