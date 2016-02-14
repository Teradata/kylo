package com.thinkbiganalytics.nifi;

import org.apache.commons.io.IOUtils;
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
import org.apache.nifi.processor.io.InputStreamCallback;
import org.apache.nifi.processor.util.StandardValidators;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.*;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * This processor copies FlowFiles to HDFS.
 */
@InputRequirement(InputRequirement.Requirement.INPUT_REQUIRED)
@Tags({"elasticsearch", "thinkbig"})
@CapabilityDescription("Write FlowFile from a JSON array to Elasticsearch")
public class IndexElasticSearch extends AbstractProcessor {

    // relationships
    public static final Relationship REL_SUCCESS = new Relationship.Builder()
            .name("success")
            .description("Json objects that are successfully indexed in elasticsearch are transferred to this relationship")
            .build();

    public static final Relationship REL_FAILURE = new Relationship.Builder()
            .name("failure")
            .description(
                    "Json objects that are successfully indexed in elasticsearch are transferred to this relationship")
            .build();
    private final Set<Relationship> relationships;

    // properties
    public static final PropertyDescriptor INDEX_NAME = new PropertyDescriptor.Builder()
            .name("IndexName")
            .description("The name of the index")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .expressionLanguageSupported(true)
            .build();
    public static final PropertyDescriptor TYPE = new PropertyDescriptor.Builder()
            .name("Type")
            .description("Elasticsearch type")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .expressionLanguageSupported(true)
            .build();
    public static final PropertyDescriptor HOST_NAME = new PropertyDescriptor.Builder()
            .name("HostName")
            .description("Elasticsearch host")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .expressionLanguageSupported(true)
            .build();

    private final List<PropertyDescriptor> propDescriptors;

    public IndexElasticSearch() {
        final Set<Relationship> r = new HashSet<>();
        r.add(REL_SUCCESS);
        r.add(REL_FAILURE);
        relationships = Collections.unmodifiableSet(r);

        final List<PropertyDescriptor> pds = new ArrayList<>();
        pds.add(INDEX_NAME);
        pds.add(TYPE);
        pds.add(HOST_NAME);
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
        try {
              /* Configuration parameters for spark launcher */
            String indexName = context.getProperty(INDEX_NAME).evaluateAttributeExpressions(outgoing).getValue();
            String type = context.getProperty(TYPE).evaluateAttributeExpressions(outgoing).getValue();
            String hostName = context.getProperty(HOST_NAME).evaluateAttributeExpressions(outgoing).getValue();

            final StringBuffer sb = new StringBuffer();
            session.read(incoming, new InputStreamCallback() {

                @Override
                public void process(InputStream in) throws IOException {
                    sb.append(IOUtils.toString(in));
                }

            });

            logger.info("The json that was received is: " + sb.toString());

            sendToElasticSearch(sb.toString(), hostName, indexName, type);

             /* Wait for job completion */
            boolean success = true;
            if (!success) {
                logger.info("*** Completed with failed status ");
                session.transfer(outgoing, REL_FAILURE);
            }else{
                logger.info("*** Completed with status ");
                session.transfer(outgoing, REL_SUCCESS);
            }
        } catch (final Exception e ) {
            e.printStackTrace();
            logger.error("Unable to execute Elasticsearch job", new Object[]{incoming, e});
            session.transfer(incoming, REL_FAILURE);
        }

    }

    private boolean sendToElasticSearch(String json, String hostName, String index, String type) throws Exception {
        final ProcessorLog logger = getLogger();
        Settings settings = Settings.settingsBuilder()
                .put("cluster.name", "demo-cluster").build();
        Client client = TransportClient.builder().settings(settings).build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(hostName), 9300));

        JSONArray array = new JSONArray(json);
        BulkRequestBuilder bulkRequest = client.prepareBulk();

        for (int i = 0; i < array.length(); i++) {
            JSONObject jsonObj = array.getJSONObject(i);
            bulkRequest.add(client.prepareIndex(index, type, jsonObj.getString("id"))
                            .setSource(jsonBuilder()
                                            .startObject()
                                            .field("id", jsonObj.getString("id"))
                                            .field("first_name", jsonObj.getString("first_name"))
                                            .field("last_name", jsonObj.getString("last_name"))
                                            .field("post_date", new Date())
                                            .endObject()
                            )
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
