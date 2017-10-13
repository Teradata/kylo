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

import com.thinkbiganalytics.nifi.v2.thrift.ExecuteHQLStatement;
import com.thinkbiganalytics.nifi.v2.thrift.ThriftService;
import com.thinkbiganalytics.util.ColumnSpec;
import com.thinkbiganalytics.util.TableType;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.thinkbiganalytics.nifi.v2.ingest.IngestProperties.FEED_CATEGORY;
import static com.thinkbiganalytics.nifi.v2.ingest.IngestProperties.FEED_NAME;
import static com.thinkbiganalytics.nifi.v2.ingest.IngestProperties.FIELD_SPECIFICATION;


/**
 * This processor indexes json data in elasticsearch
 */
@InputRequirement(InputRequirement.Requirement.INPUT_REQUIRED)
@Tags({"elasticsearch", "thinkbig", "hive"})
@CapabilityDescription("Creates a table in Hive that is backed by data in Elasticsearch")
public class CreateElasticsearchBackedHiveTable extends ExecuteHQLStatement {

    /**
     * List of Elasticsearch nodes to connect to
     */
    public static final PropertyDescriptor NODES = new PropertyDescriptor.Builder()
        .name("Nodes")
        .description("A comma separated list of one or more Elasticsearch nodes to be used.")
        .required(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    public static final PropertyDescriptor FEED_ROOT = new PropertyDescriptor.Builder()
        .name("Feed Root Path")
        .description("Specify the full HDFS or S3 root path for the index table.")
        .required(true)
        .defaultValue("${s3ingest.s3.protocol}://${s3ingest.hiveBucket}/${hive.profile.root}")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    /**
     * Location of Jar file
     */
    public static final PropertyDescriptor JAR_URL = new PropertyDescriptor.Builder()
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
        .description(
            "Whether the connector is used against an Elasticsearch instance in a cloud/restricted environment over the WAN, such as Amazon Web Services. "
            + "In this mode, the connector disables discovery and only connects through the declared es.nodes during all operations, including reads and writes."
            + " Note that in this mode, performance is highly affected.")
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

    public static final PropertyDescriptor FIELD_INDEX_STRING = new PropertyDescriptor.Builder()
        .name("Field Index String")
        .description("Comma separated list of fields to be indexed.")
        .required(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .defaultValue("${metadata.table.fieldIndexString}")
        .build();
    private final static String KYLO_DATA_INDEX_NAME = "kylo-data";
    private final static String KYLO_DATA_INDEX_TYPE = "hive-data";
    private final Set<Relationship> relationships;
    private final List<PropertyDescriptor> propDescriptors;

    /**
     * default constructor constructs the relationship and property collections
     */
    public CreateElasticsearchBackedHiveTable() {
        final Set<Relationship> r = new HashSet<>();
        r.add(IngestProperties.REL_SUCCESS);
        r.add(IngestProperties.REL_FAILURE);
        relationships = Collections.unmodifiableSet(r);

        final List<PropertyDescriptor> pds = new ArrayList<>();
        pds.add(NODES);
        pds.add(FEED_ROOT);
        pds.add(ID_FIELD);
        pds.add(FIELD_SPECIFICATION);
        pds.add(FEED_NAME);
        pds.add(FEED_CATEGORY);
        pds.add(THRIFT_SERVICE);
        pds.add(JAR_URL);
        pds.add(USE_WAN);
        pds.add(AUTO_CREATE_INDEX);
        pds.add(FIELD_INDEX_STRING);
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
        FlowFile flowFile = session.get();
        if (flowFile == null) {
            return;
        }

        String jarUrl = context.getProperty(JAR_URL).evaluateAttributeExpressions(flowFile).getValue();
        String useWan = context.getProperty(USE_WAN).getValue();
        String autoIndex = context.getProperty(AUTO_CREATE_INDEX).getValue();
        String idField = context.getProperty(ID_FIELD).evaluateAttributeExpressions(flowFile).getValue();

        final ColumnSpec[] columnSpecs = Optional.ofNullable(context.getProperty(FIELD_SPECIFICATION).evaluateAttributeExpressions(flowFile).getValue())
            .filter(StringUtils::isNotEmpty)
            .map(ColumnSpec::createFromString)
            .orElse(new ColumnSpec[0]);
        validateArrayProperty(FIELD_SPECIFICATION.getDisplayName(), columnSpecs, session, flowFile);

        final String feedName = context.getProperty(IngestProperties.FEED_NAME).evaluateAttributeExpressions(flowFile).getValue();
        validateStringProperty(FEED_NAME.getDisplayName(), feedName, session, flowFile);

        final String categoryName = context.getProperty(IngestProperties.FEED_CATEGORY).evaluateAttributeExpressions(flowFile).getValue();
        validateStringProperty(FEED_CATEGORY.getDisplayName(), categoryName, session, flowFile);

        final String nodes = context.getProperty(NODES).evaluateAttributeExpressions(flowFile).getValue();
        validateStringProperty(NODES.getDisplayName(), nodes, session, flowFile);

        final String indexString = context.getProperty(FIELD_INDEX_STRING).evaluateAttributeExpressions(flowFile).getValue();
        validateStringProperty(FIELD_INDEX_STRING.getDisplayName(), indexString, session, flowFile);

        final String feedRoot = context.getProperty(FEED_ROOT).evaluateAttributeExpressions(flowFile).getValue();
        validateStringProperty(FEED_ROOT.getDisplayName(), indexString, session, flowFile);

        List<String> hiveStatements = getHQLStatements(columnSpecs, nodes, feedRoot, feedName, categoryName, useWan, autoIndex, idField, jarUrl, indexString);
        final ThriftService thriftService = context.getProperty(THRIFT_SERVICE).asControllerService(ThriftService.class);

        executeStatements(context,session, flowFile, hiveStatements.toArray(new String[hiveStatements.size()]), thriftService);

    }

    private void validateArrayProperty(String propertyName, Object[] values, ProcessSession session, FlowFile flowFile) {
        if (values == null || values.length == 0) {
            getLog().error("Missing " + propertyName + " specification");
            session.transfer(flowFile, IngestProperties.REL_FAILURE);
            return;
        }
    }

    private void validateStringProperty(String propertyName, String propertyValue, ProcessSession session, FlowFile flowFile) {
        if (propertyValue == null || propertyValue.isEmpty()) {
            getLog().error("Missing " + propertyName + " parameter");
            session.transfer(flowFile, IngestProperties.REL_FAILURE);
            return;
        }
    }

    public String generateHQL(String columnsSQL, String nodes, String locationRoot, String feedName, String categoryName, String useWan, String autoIndex, String idField) {
        // elastic search records for the kylo-data index require the kylo_schema and kylo_table
        columnsSQL = columnsSQL + ", kylo_schema string, kylo_table string";

        // Construct location
        Path path = Paths.get(locationRoot, categoryName, feedName, "index" );
        String location = path.toString().replace(":/", "://");

        StringBuilder sb = new StringBuilder();
        sb.append("CREATE EXTERNAL TABLE IF NOT EXISTS ")
            .append(categoryName)
            .append(".")
            .append(feedName)
            .append("_index")
            .append(" (")
            .append(columnsSQL)
            .append(") ")
            .append("STORED BY 'org.elasticsearch.hadoop.hive.EsStorageHandler' ")
            .append("LOCATION '")
            .append(location)
            .append( "' ")
            .append("TBLPROPERTIES('es.resource' = '")
            .append(KYLO_DATA_INDEX_NAME)
            .append("/")
            .append(KYLO_DATA_INDEX_TYPE)
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

    public List<String> getHQLStatements(ColumnSpec[] columnSpecs, String nodes, String locationRoot, String feedName, String categoryName, String useWan, String autoIndex, String idField,
                                         String jarUrl, String indexFieldString) {

        final ColumnSpec[] partitions = {};

        TableType tableType = TableType.MASTER;

        List<String> indexFields = Arrays.asList(indexFieldString.toLowerCase().split(","));

        List<ColumnSpec> indexCols = Arrays.asList(columnSpecs)
            .stream().filter(p -> indexFields.contains(p.getName().toLowerCase()))
            .collect(Collectors.toList());

        String columnsSQL = tableType.deriveColumnSpecification(indexCols.toArray(new ColumnSpec[indexCols.size()]), partitions, "");
        String hql = generateHQL(columnsSQL, nodes, locationRoot, feedName, categoryName, useWan, autoIndex, idField);

        List<String> hiveStatements = new ArrayList<>();

        if (jarUrl != null && !jarUrl.isEmpty()) {
            String addJar = "ADD JAR " + jarUrl;
            hiveStatements.add(addJar);
        }

        hiveStatements.add(hql);
        return hiveStatements;
    }

}
