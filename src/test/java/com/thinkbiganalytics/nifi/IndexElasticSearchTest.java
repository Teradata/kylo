package com.thinkbiganalytics.nifi;

import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.junit.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by Jeremy Merrifield on 2/14/16.
 *
 * Integration test so we can validate the processor sends data to elasticsearch without having to deploy it
 */
public class IndexElasticSearchTest {
    private static final String TEST_HOST = "ec2-54-152-98-43.compute-1.amazonaws.com";
    private static final String TEST_INDEX = "integration-test";
    private static final String TEST_TYPE = "userdatatest";
    private InputStream insertDocument;
    private InputStream updateDocument;
    private TestRunner nifiTestRunner;
    private Client esClient = null;

    @Before
    public void setUp() throws IOException {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        insertDocument = classloader.getResourceAsStream("elasticsearch/insert.json");
        updateDocument = classloader.getResourceAsStream("elasticsearch/update.json");

        Settings settings = Settings.settingsBuilder()
                .put("cluster.name", "demo-cluster").build();
        esClient = TransportClient.builder().settings(settings).build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(TEST_HOST), 9300));
    }

    @After
    public void cleanUp() {
        DeleteResponse response = esClient.prepareDelete().setIndex(TEST_INDEX).setType(TEST_TYPE).setId("*").get();
        esClient.close();
    }

    @Test
    public void tester() {
        runProcessor(insertDocument);
        Map searchResult = queryElasticSearch("2");
        assertTrue("The first name did not match as expected", ((String) searchResult.get("first_name")).equals( "Albert"));
        assertTrue("The last name did not match as expected", ((String) searchResult.get("last_name")).equals( "Freeman"));

        runProcessor(updateDocument);
        searchResult = queryElasticSearch("2");
        assertTrue("The first name did not match as expected", ((String) searchResult.get("first_name")).equals( "Jacob"));
        assertTrue("The last name did not match as expected", ((String) searchResult.get("last_name")).equals( "Thomas"));

    }

    private void runProcessor(InputStream testDocument) {
        nifiTestRunner = TestRunners.newTestRunner(new IndexElasticSearch()); // no failures
        nifiTestRunner.setValidateExpressionUsage(true);
        nifiTestRunner.setProperty(IndexElasticSearch.HOST_NAME, TEST_HOST);
        nifiTestRunner.setProperty(IndexElasticSearch.INDEX_NAME, TEST_INDEX);
        nifiTestRunner.setProperty(IndexElasticSearch.TYPE, TEST_TYPE);
        nifiTestRunner.assertValid();

        nifiTestRunner.enqueue(testDocument, new HashMap<String, String>() {{
            put("doc_id", "8736522777");
        }});
        nifiTestRunner.run(1, true, true);

        nifiTestRunner.assertAllFlowFilesTransferred(IndexElasticSearch.REL_SUCCESS, 1);
        final MockFlowFile out = nifiTestRunner.getFlowFilesForRelationship(IndexElasticSearch.REL_SUCCESS).get(0);
        assertNotNull(out);
        out.assertAttributeEquals("doc_id", "8736522777");
    }

    private Map queryElasticSearch(String id) {
        Map result = null;
        GetResponse response = esClient.prepareGet(TEST_INDEX, TEST_TYPE, id)
                .execute()
                .actionGet();
        result = response.getSource();
        return result;
    }
}
