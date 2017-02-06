package com.thinkbiganalytics.nifi;

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

import com.thinkbiganalytics.nifi.v2.elasticsearch.IndexElasticSearch;

import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Integration test so we can validate the processor sends data to elasticsearch without having to deploy it
 */
public class IndexElasticSearchTest {

    private static final String TEST_HOST = "ec2-54-152-98-43.compute-1.amazonaws.com";
    private static final String TEST_INDEX = "integration-test";
    private static final String TEST_TYPE = "userdatatest";
    private static final String TEST_CLUSTER = "demo-cluster";
    private static final String TEST_ID = "id";

    private InputStream insertDocument;
    private InputStream updateDocument;
    private InputStream metadataDocument;
    private Client esClient = null;

    @Before
    public void setUp() throws IOException {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        insertDocument = classloader.getResourceAsStream("elasticsearch/insert.json");
        updateDocument = classloader.getResourceAsStream("elasticsearch/update.json");
        metadataDocument = classloader.getResourceAsStream("elasticsearch/metadata.json");

        Settings settings = Settings.settingsBuilder()
            .put("cluster.name", TEST_CLUSTER).build();
        esClient = TransportClient.builder().settings(settings).build()
            .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(TEST_HOST), 9300));
    }

    @After
    public void cleanUp() {
        DeleteResponse response = esClient.prepareDelete().setIndex(TEST_INDEX).setType(TEST_TYPE).setId("*").get();
        esClient.close();
    }

    //@Test
    public void tester() {
        runProcessor(insertDocument, TEST_INDEX, TEST_TYPE, TEST_ID);
        Map searchResult = queryElasticSearch("2");
        assertTrue("The first name did not match as expected", searchResult.get("first_name").equals("Albert"));
        assertTrue("The last name did not match as expected", searchResult.get("last_name").equals("Freeman"));

        runProcessor(updateDocument, TEST_INDEX, TEST_TYPE, TEST_ID);
        searchResult = queryElasticSearch("2");
        assertTrue("The first name did not match as expected", searchResult.get("first_name").equals("Jacob"));
        assertTrue("The last name did not match as expected", searchResult.get("last_name").equals("Thomas"));

    }

    //@Test
    public void testMetadata() {
        runProcessor(metadataDocument, "metadata2", "hive-tables", null);
    }

    private void runProcessor(InputStream testDocument, String index, String type, String idField) {
        TestRunner nifiTestRunner = TestRunners.newTestRunner(new IndexElasticSearch());
        nifiTestRunner.setValidateExpressionUsage(true);
        nifiTestRunner.setProperty(IndexElasticSearch.HOST_NAME, TEST_HOST);
        nifiTestRunner.setProperty(IndexElasticSearch.INDEX_NAME, index);
        nifiTestRunner.setProperty(IndexElasticSearch.TYPE, type);
        nifiTestRunner.setProperty(IndexElasticSearch.CLUSTER_NAME, TEST_CLUSTER);
        if (idField != null) {
            nifiTestRunner.setProperty(IndexElasticSearch.ID_FIELD, TEST_ID);
        }
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
        GetResponse response = esClient.prepareGet(TEST_INDEX, TEST_TYPE, id)
            .execute()
            .actionGet();
        return response.getSource();
    }
}
