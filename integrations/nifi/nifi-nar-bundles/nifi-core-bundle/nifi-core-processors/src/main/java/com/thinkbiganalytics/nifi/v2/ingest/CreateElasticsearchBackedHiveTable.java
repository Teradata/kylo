package com.thinkbiganalytics.nifi.v2.ingest;

/*-
 * #%L
 * thinkbig-nifi-elasticsearch-processors
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

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.util.StopWatch;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.thinkbiganalytics.nifi.v2.thrift.ThriftService;
import com.thinkbiganalytics.util.ColumnSpec;
import com.thinkbiganalytics.util.TableType;

import static com.thinkbiganalytics.nifi.v2.ingest.IngestProperties.FIELD_SPECIFICATION;
import static com.thinkbiganalytics.nifi.v2.ingest.IngestProperties.PARTITION_SPECS;
import static com.thinkbiganalytics.nifi.v2.ingest.IngestProperties.FEED_CATEGORY;
import static com.thinkbiganalytics.nifi.v2.ingest.IngestProperties.FEED_NAME;
import static com.thinkbiganalytics.nifi.v2.ingest.IngestProperties.THRIFT_SERVICE;


/**
 * This processor indexes json data in elasticsearch
 */
@InputRequirement(InputRequirement.Requirement.INPUT_REQUIRED)
@Tags({"elasticsearch", "thinkbig", "hive"})
@CapabilityDescription("Creates a table in Hive that is backed by data in Elasticsearch")
public class CreateElasticsearchBackedHiveTable extends AbstractNiFiProcessor {


    public static final Relationship REL_SUCCESS = new Relationship.Builder()
        .name("success")
        .description("Successfully created Elasticsearch backed Hive table.")
        .build();


    public static final Relationship REL_FAILURE = new Relationship.Builder()
        .name("failure")
        .description("Failed to create Elasticsearch backed Hive table")
        .build();

    /**
     * List of Elasticsearch nodes to connect to
     */
    public static final PropertyDescriptor NODES = new PropertyDescriptor.Builder()
        .name("Nodes")
        .description("Elasticsearch nodes")
        .required(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    /**
     * Location of Jar file
     */
    public static final PropertyDescriptor JARURL = new PropertyDescriptor.Builder()
        .name("Jar URL")
        .description("Location of Jar file to be added before table creation")
        .required(false)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    /**
     * Property for the id to use for indexing into elasticsearch.
     */
    public static final PropertyDescriptor ID_FIELD = new PropertyDescriptor.Builder()
        .name("IdField")
        .description("ID that you want to use for indexing into elasticsearch. If it is empty then elasticsearch will use its own ID.")
        .required(false)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    public static final PropertyDescriptor USE_WAN = new PropertyDescriptor.Builder()
        .name("Use WAN")
        .description("Whether the connector is used against an Elasticsearch instance in a cloud/restricted environment over the WAN, such as Amazon Web Services. In this mode, the connector disables discovery and only connects through the declared es.nodes during all operations, including reads and writes. Note that in this mode, performance is highly affected.")
        .allowableValues("true", "false")
        .required(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(false)
        .defaultValue("true")
        .build();

    public static final PropertyDescriptor AUTO_CREATE_INDEX = new PropertyDescriptor.Builder()
        .name("Auto create index")
        .description("Whether or not the Elasticsearch index can be created if id does not already exist.")
        .allowableValues("true", "false")
        .required(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(false)
        .defaultValue("true")
        .build();

    private final Set<Relationship> relationships;
    private final List<PropertyDescriptor> propDescriptors;

    /**
     * default constructor constructs the relationship and property collections
     */
    public CreateElasticsearchBackedHiveTable() {
        final Set<Relationship> r = new HashSet<>();
        r.add(REL_SUCCESS);
        r.add(REL_FAILURE);
        relationships = Collections.unmodifiableSet(r);

        final List<PropertyDescriptor> pds = new ArrayList<>();
        pds.add(NODES);
        pds.add(ID_FIELD);
        pds.add(FIELD_SPECIFICATION);
        pds.add(PARTITION_SPECS);
        pds.add(FEED_NAME);
        pds.add(FEED_CATEGORY);
        pds.add(THRIFT_SERVICE);
        pds.add(JARURL);
        pds.add(USE_WAN);
        pds.add(AUTO_CREATE_INDEX);
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
        final ComponentLog logger = getLog();
        FlowFile flowFile = session.get();
        if (flowFile == null) {
            return;
        }

        String jarUrl = context.getProperty(JARURL).evaluateAttributeExpressions(flowFile).getValue();
        String useWan = context.getProperty(USE_WAN).getValue();
        String autoIndex = context.getProperty(AUTO_CREATE_INDEX).getValue();
        String idField = context.getProperty(ID_FIELD).evaluateAttributeExpressions(flowFile).getValue();

        final ColumnSpec[] partitions = Optional.ofNullable(context.getProperty(PARTITION_SPECS).evaluateAttributeExpressions(flowFile).getValue())
            .filter(StringUtils::isNotEmpty)
            .map(ColumnSpec::createFromString)
            .orElse(new ColumnSpec[0]);

        final ColumnSpec[] columnSpecs = Optional.ofNullable(context.getProperty(FIELD_SPECIFICATION).evaluateAttributeExpressions(flowFile).getValue())
            .filter(StringUtils::isNotEmpty)
            .map(ColumnSpec::createFromString)
            .orElse(new ColumnSpec[0]);
        if (columnSpecs == null || columnSpecs.length == 0) {
            getLog().error("Missing field specification");
            session.transfer(flowFile, IngestProperties.REL_FAILURE);
            return;
        }

        final String feedName = context.getProperty(IngestProperties.FEED_NAME).evaluateAttributeExpressions(flowFile).getValue();
        if (feedName == null || feedName.isEmpty()) {
            getLog().error("Missing feed name");
            session.transfer(flowFile, IngestProperties.REL_FAILURE);
            return;
        }

        final String categoryName = context.getProperty(IngestProperties.FEED_CATEGORY).evaluateAttributeExpressions(flowFile).getValue();
        if (categoryName == null || categoryName.isEmpty()) {
            getLog().error("Missing category name");
            session.transfer(flowFile, IngestProperties.REL_FAILURE);
            return;
        }

        final String nodes = context.getProperty(NODES).evaluateAttributeExpressions(flowFile).getValue();
        if (nodes == null || nodes.isEmpty()) {
            getLog().error("Missing node parameter");
            session.transfer(flowFile, IngestProperties.REL_FAILURE);
            return;
        }

        TableType tableType = TableType.MASTER;
        String columnsSQL = tableType.deriveColumnSpecification(columnSpecs, partitions,"");
        String hql = generateHQL(columnsSQL, nodes, feedName, categoryName, useWan, autoIndex, idField);

        List<String> hiveStatements = new ArrayList<>();

        if (jarUrl != null || jarUrl.isEmpty()) {
            String addJar = "ADD JAR " + jarUrl;
            hiveStatements.add(addJar);
        }

        hiveStatements.add(hql);

        final ThriftService thriftService = context.getProperty(THRIFT_SERVICE).asControllerService(ThriftService.class);
        final StopWatch stopWatch = new StopWatch(true);


        try (final Connection con = thriftService.getConnection();
             final Statement st = con.createStatement()) {
            boolean result = false;

            for (String statement:hiveStatements
                ) {
                result = st.execute(statement);
            }

            session.getProvenanceReporter().modifyContent(flowFile, "Execution result " + result, stopWatch.getElapsed(TimeUnit.MILLISECONDS));
            session.transfer(flowFile, REL_SUCCESS);
        } catch (final Exception e) {
            logger.error("Unable to execute SQL DDL {} for {} due to {}; routing to failure", new Object[]{hql, flowFile, e});
            session.transfer(flowFile, REL_FAILURE);
        }

    }

    public String generateHQL(String columnsSQL, String nodes, String feedName, String categoryName, String useWan, String autoIndex, String idField){
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE EXTERNAL TABLE IF NOT EXISTS ")
            .append(categoryName)
            .append(".index_")
            .append(feedName)
            .append(" (").append(columnsSQL).append(") ")
            .append("STORED BY 'org.elasticsearch.hadoop.hive.EsStorageHandler' TBLPROPERTIES('es.resource' = '")
            .append(categoryName)
            .append("/")
            .append(feedName)
            .append("', 'es.nodes' = '")
            .append(nodes)
            .append("', 'es.nodes.wan.only' = '")
            .append(useWan)
            .append("', 'es.index.auto.create' = '")
            .append(autoIndex);

        if (idField != null && !idField.isEmpty()) {
            sb.append("', 'es.mapping.id' = '")
                .append(idField);
        }

        sb.append("')");

        return sb.toString();
    }

}
