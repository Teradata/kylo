package com.thinkbiganalytics.nifi.v2.elasticsearch;

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

import com.thinkbiganalytics.hashing.HashingUtil;
import com.thinkbiganalytics.nifi.processor.AbstractNiFiProcessor;

import org.apache.commons.io.IOUtils;
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
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * This processor indexes json data in elasticsearch
 */
@InputRequirement(InputRequirement.Requirement.INPUT_REQUIRED)
@Tags({"elasticsearch", "thinkbig"})
@CapabilityDescription("Write FlowFile from a JSON array to Elasticsearch (V2)")
public class IndexElasticSearch extends AbstractNiFiProcessor {

    /**
     * Success Relationship for JSON objects that are successfully indexed in elasticsearch
     */
    public static final Relationship REL_SUCCESS = new Relationship.Builder()
        .name("success")
        .description("Json objects that are successfully indexed in elasticsearch are transferred to this relationship")
        .build();

    /**
     * Failure Relationship for JSON objects that are fail to index in elasticsearch
     */
    public static final Relationship REL_FAILURE = new Relationship.Builder()
        .name("failure")
        .description(
            "Json objects that are un-successfully indexed in elasticsearch are transferred to this relationship")
        .build();

    /**
     * Property for the name of the index
     */
    public static final PropertyDescriptor INDEX_NAME = new PropertyDescriptor.Builder()
        .name("IndexName")
        .description("The name of the index")
        .required(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    /**
     * Property for the index type
     */
    public static final PropertyDescriptor TYPE = new PropertyDescriptor.Builder()
        .name("Type")
        .description("Elasticsearch type")
        .required(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    /**
     * Property for the elastic search host name
     */
    public static final PropertyDescriptor HOST_NAME = new PropertyDescriptor.Builder()
        .name("HostName")
        .description("Elasticsearch host")
        .required(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    /**
     * Property for the elastic search cluster name
     */
    public static final PropertyDescriptor CLUSTER_NAME = new PropertyDescriptor.Builder()
        .name("ClusterName")
        .description("Elasticsearch cluster")
        .required(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    /**
     * Property for the id to use for indexing into elasticsearch.
     */
    public static final PropertyDescriptor ID_FIELD = new PropertyDescriptor.Builder()
        .name("IdField")
        .description("Id that you want to use for indexing into elasticsearch. If it is empty then a UUID will be generated")
        .required(false)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    /**
     * Property for Kylo category name
     */
    public static final PropertyDescriptor CATEGORY_NAME = new PropertyDescriptor.Builder()
        .name("KyloCategory")
        .description("Kylo category system name for data to be indexed")
        .required(true)
        .defaultValue("${category}")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    /**
     * Property for Kylo feed name
     */
    public static final PropertyDescriptor FEED_NAME = new PropertyDescriptor.Builder()
        .name("KyloFeed")
        .description("Kylo feed system name for data to be indexed")
        .required(true)
        .defaultValue("${feed}")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    private final Set<Relationship> relationships;
    private final List<PropertyDescriptor> propDescriptors;

    /**
     * default constructor constructs the relationship and property collections
     */
    public IndexElasticSearch() {
        final Set<Relationship> r = new HashSet<>();
        r.add(REL_SUCCESS);
        r.add(REL_FAILURE);
        relationships = Collections.unmodifiableSet(r);

        final List<PropertyDescriptor> pds = new ArrayList<>();
        pds.add(INDEX_NAME);
        pds.add(TYPE);
        pds.add(HOST_NAME);
        pds.add(CLUSTER_NAME);
        pds.add(ID_FIELD);
        pds.add(CATEGORY_NAME);
        pds.add(FEED_NAME);
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
        try {
            String indexName = context.getProperty(INDEX_NAME).evaluateAttributeExpressions(flowFile).getValue();
            String type = context.getProperty(TYPE).evaluateAttributeExpressions(flowFile).getValue();
            String hostName = context.getProperty(HOST_NAME).evaluateAttributeExpressions(flowFile).getValue();
            String clusterName = context.getProperty(CLUSTER_NAME).evaluateAttributeExpressions(flowFile).getValue();
            String idField = context.getProperty(ID_FIELD).evaluateAttributeExpressions(flowFile).getValue();
            String categoryName = context.getProperty(CATEGORY_NAME).evaluateAttributeExpressions(flowFile).getValue();
            String feedName = context.getProperty(FEED_NAME).evaluateAttributeExpressions(flowFile).getValue();

            final StringBuffer sb = new StringBuffer();
            session.read(flowFile, in -> sb.append(IOUtils.toString(in, Charset.defaultCharset())));

            logger.debug("The json that was received is: " + sb.toString());

            boolean success = sendToElasticSearch(sb.toString(),
                                                  hostName,
                                                  indexName,
                                                  type,
                                                  clusterName,
                                                  idField,
                                                  categoryName,
                                                  feedName);

            if (!success) {
                logger.info("*** Completed with failed status");
                session.transfer(flowFile, REL_FAILURE);
            } else {
                logger.info("*** Completed with success status");
                session.transfer(flowFile, REL_SUCCESS);
            }
        } catch (final Exception e) {
            logger.error("Unable to execute Elasticsearch job", new Object[]{flowFile, e});
            session.transfer(flowFile, REL_FAILURE);
        }

    }

    private boolean sendToElasticSearch(String json,
                                        String hostName,
                                        String index,
                                        String type,
                                        String clusterName,
                                        String idField,
                                        String categoryName,
                                        String feedName
                                        ) throws Exception {
        final ComponentLog logger = getLog();
        Settings settings = Settings.settingsBuilder()
            .put("cluster.name", clusterName).build();
        Client client = TransportClient.builder().settings(settings).build()
            .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(hostName), 9300));

        JSONArray array = new JSONArray(json);
        BulkRequestBuilder bulkRequest = client.prepareBulk();

        for (int i = 0; i < array.length(); i++) {
            JSONObject jsonObj = array.getJSONObject(i);
            String id = null;

            if (idField != null && idField.length() > 0) {
                id = jsonObj.getString(idField);
                logger.debug("Document index id using field " + idField + ": " + id);
            } else if (StringUtils.isNotEmpty(categoryName) && (StringUtils.isNotEmpty(feedName))) {
                String hash = HashingUtil.getHashMD5(jsonObj.toString());
                if (StringUtils.isNotEmpty(hash)) {
                    id = categoryName + "::" + feedName + "::" + hash;
                    logger.debug("Document index id using hash: " + id);
                }
            }

            if (StringUtils.isEmpty(id)) {
                id = UUID.randomUUID().toString();
                logger.debug("Document index id auto-generated + " + id);
            }

            jsonObj.put("post_date", String.valueOf(System.currentTimeMillis()));
            bulkRequest.add(client.prepareIndex(index, type, id)
                                .setSource(jsonObj.toString())
            );
        }
        BulkResponse bulkResponse = bulkRequest.get();
        if (bulkResponse.hasFailures()) {
            logger.error("Error occurred while batch updating" + bulkResponse.buildFailureMessage());
            return false;
        }
        return true;
    }
}
