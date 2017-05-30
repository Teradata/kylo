package com.thinkbiganalytics.ingest;

import com.thinkbiganalytics.nifi.v2.ingest.CreateElasticsearchBackedHiveTable;
import com.thinkbiganalytics.nifi.v2.ingest.IngestProperties;
import com.thinkbiganalytics.nifi.v2.ingest.RegisterFeedTablesTest;

import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by gp186022 on 5/30/17.
 */
public class CreateElasticsearchBackedHiveTableTest {

    private final TestRunner runner = TestRunners.newTestRunner(CreateElasticsearchBackedHiveTable.class);
    CreateElasticsearchBackedHiveTable table = new CreateElasticsearchBackedHiveTable();

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testGeneratingHQL() throws Exception {
        String hql = table.generateHQL("id int, count int, name string", "localhost:8300", "test_feed", "test_category");
        assertEquals(hql, "CREATE EXTERNAL TABLE IF NOT EXISTS test_category.indextest_feed (id int, count int, name string) STORED BY 'org.elasticsearch.hadoop.hive.EsStorageHandler' TBLPROPERTIES('es.resource' = 'test_category/test_feed', 'es.nodes' = 'localhost:8300', 'es.nodes.wan.only' = 'true', 'es.index.auto.create' = 'true')");
    }

}
