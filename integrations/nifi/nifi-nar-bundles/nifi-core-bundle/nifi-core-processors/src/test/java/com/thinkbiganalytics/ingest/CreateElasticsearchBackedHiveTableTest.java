package com.thinkbiganalytics.ingest;

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

import com.google.common.collect.ImmutableMap;
import com.thinkbiganalytics.nifi.v2.ingest.CreateElasticsearchBackedHiveTable;
import com.thinkbiganalytics.nifi.v2.ingest.IngestProperties;
import com.thinkbiganalytics.nifi.v2.thrift.ThriftService;
import com.thinkbiganalytics.util.ColumnSpec;

import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CreateElasticsearchBackedHiveTableTest {

    public static final String CATEGORY = "test_category";
    public static final String FEED = "test_feed";
    public static final String FEED_ROOT = "s3a://hive-bucket/model.db";
    public static final String LOCATION = FEED_ROOT + "/" + CATEGORY + "/" + FEED + "/index";
    public static final String NODES = "localhost:8300";
    public static final String FIELD_STRING = "Name,Phone";
    public static final String JAR_URL = "jar://url";
    public static final String FIELD_SPEC = "name|string\nid|int\nphone|string";
    public static final String ID_FIELD = "id";
    private static final String THRIFT_SERVICE_IDENTIFIER = "MockThriftService";
    private final TestRunner runner = TestRunners.newTestRunner(CreateElasticsearchBackedHiveTable.class);
    private MockThriftService thriftService;
    private CreateElasticsearchBackedHiveTable table = new CreateElasticsearchBackedHiveTable();

    @Before
    public void setUp() throws Exception {
        // Setup thrift service
        thriftService = new MockThriftService();

        // Setup test runner
        runner.addControllerService(THRIFT_SERVICE_IDENTIFIER, thriftService);
        runner.enableControllerService(thriftService);
        runner.setProperty(IngestProperties.THRIFT_SERVICE, THRIFT_SERVICE_IDENTIFIER);
    }

    @Test
    public void testGetHQLStatements() throws Exception {
        ColumnSpec spec = new ColumnSpec("name", "string", "");
        ColumnSpec spec2 = new ColumnSpec("iD", "int", "");
        ColumnSpec spec3 = new ColumnSpec("PHONE", "string", "");
        ColumnSpec[] specs = {spec, spec2, spec3};
        List<String> statements = table.getHQLStatements(specs, NODES, FEED_ROOT, FEED, CATEGORY, "true", "true", "", JAR_URL, FIELD_STRING);
        assertEquals("ADD JAR " + JAR_URL, statements.get(0));
        assertEquals("CREATE EXTERNAL TABLE IF NOT EXISTS " + CATEGORY + "." + FEED
                     + "_index (`name` string, `phone` string, processing_dttm string, kylo_schema string, kylo_table string) "
                     + "STORED BY 'org.elasticsearch.hadoop.hive.EsStorageHandler' "
                     + "LOCATION '" + LOCATION + "' "
                     + "TBLPROPERTIES('es.resource' = 'kylo-data/hive-data', 'es.nodes' = '"
                     + NODES + "', 'es.nodes.wan.only' = 'true', 'es.index.auto.create' = 'true')", statements.get(1));
    }

    @Test
    public void testGeneratingHQL() throws Exception {
        String hql = table.generateHQL("id int, count int, name string", NODES, FEED_ROOT, FEED, CATEGORY, "true", "true", "");
        assertEquals("CREATE EXTERNAL TABLE IF NOT EXISTS " + CATEGORY + "." + FEED
                     + "_index (id int, count int, name string, kylo_schema string, kylo_table string) STORED BY "
                     + "'org.elasticsearch.hadoop.hive.EsStorageHandler' "
                     + "LOCATION '" + LOCATION + "' "
                     + "TBLPROPERTIES('es.resource' = 'kylo-data/hive-data', 'es.nodes' = '"
                     + NODES + "', 'es.nodes.wan.only' = 'true', 'es.index.auto.create' = 'true')", hql);
    }

    @Test
    public void CreateTableRequiredProperties() throws Exception {
        // Test with only required properties
        runner.setProperty(IngestProperties.FIELD_SPECIFICATION, FIELD_SPEC);
        runner.setProperty(CreateElasticsearchBackedHiveTable.NODES, NODES);
        runner.setProperty(CreateElasticsearchBackedHiveTable.FEED_ROOT, FEED_ROOT);
        runner.setProperty(CreateElasticsearchBackedHiveTable.FIELD_INDEX_STRING, FIELD_STRING);
        runner.enqueue(new byte[0], ImmutableMap.of("metadata.category.systemName", CATEGORY, "metadata.systemFeedName", FEED));
        runner.run();

        assertEquals(0, runner.getFlowFilesForRelationship(IngestProperties.REL_FAILURE).size());
        assertEquals(1, runner.getFlowFilesForRelationship(IngestProperties.REL_SUCCESS).size());

        final InOrder inOrder = Mockito.inOrder(thriftService.statement);
        inOrder.verify(thriftService.statement).execute("CREATE EXTERNAL TABLE IF NOT EXISTS " + CATEGORY + "." + FEED
                                                        + "_index (`name` string, `phone` string, processing_dttm string, kylo_schema string, kylo_table string) "
                                                        + "STORED BY 'org.elasticsearch.hadoop.hive.EsStorageHandler' "
                                                        + "LOCATION '" + LOCATION + "' "
                                                        + "TBLPROPERTIES('es.resource' = 'kylo-data/hive-data', 'es.nodes' = '"
                                                        + NODES + "', 'es.nodes.wan.only' = 'true', 'es.index.auto.create' = 'true')");
        inOrder.verify(thriftService.statement).close();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void CreateTableAllProperties() throws Exception {
        // Test with all properties
        runner.setProperty(IngestProperties.FIELD_SPECIFICATION, FIELD_SPEC);
        runner.setProperty(CreateElasticsearchBackedHiveTable.NODES, NODES);
        runner.setProperty(CreateElasticsearchBackedHiveTable.FEED_ROOT, FEED_ROOT);
        runner.setProperty(CreateElasticsearchBackedHiveTable.FIELD_INDEX_STRING, FIELD_STRING);
        runner.setProperty(CreateElasticsearchBackedHiveTable.JAR_URL, JAR_URL);
        runner.setProperty(CreateElasticsearchBackedHiveTable.ID_FIELD, ID_FIELD);
        runner.enqueue(new byte[0], ImmutableMap.of("metadata.category.systemName", CATEGORY, "metadata.systemFeedName", FEED));
        runner.run();

        assertEquals(0, runner.getFlowFilesForRelationship(IngestProperties.REL_FAILURE).size());
        assertEquals(1, runner.getFlowFilesForRelationship(IngestProperties.REL_SUCCESS).size());

        final InOrder inOrder = Mockito.inOrder(thriftService.statement);
        inOrder.verify(thriftService.statement).execute("CREATE EXTERNAL TABLE IF NOT EXISTS " + CATEGORY + "." + FEED
                                                        + "_index (`name` string, `phone` string, processing_dttm string, kylo_schema string, kylo_table string) "
                                                        + "STORED BY 'org.elasticsearch.hadoop.hive.EsStorageHandler' "
                                                        + "LOCATION '" + LOCATION + "' "
                                                        + "TBLPROPERTIES('es.resource' = 'kylo-data/hive-data', 'es.nodes' = '"
                                                        + NODES + "', 'es.nodes.wan.only' = 'true', 'es.index.auto.create' = 'true', 'es.mapping.id' = '" + ID_FIELD + "')");
        inOrder.verify(thriftService.statement).close();
        inOrder.verifyNoMoreInteractions();
    }

    /**
     * A mock implementation of {@link ThriftService} for unit testing.
     */
    private class MockThriftService extends AbstractControllerService implements ThriftService {

        /**
         * Mock connection for unit testing
         */
        public final Connection connection = Mockito.mock(Connection.class);

        /**
         * Mock statement for unit testing
         */
        public final Statement statement = Mockito.mock(Statement.class);

        /**
         * Constructs a {@code MockThriftService}.
         *
         * @throws Exception never
         */
        public MockThriftService() throws Exception {
            Mockito.when(connection.createStatement()).thenReturn(statement);
        }

        @Override
        public Connection getConnection() throws ProcessException {
            return connection;
        }
    }

}
