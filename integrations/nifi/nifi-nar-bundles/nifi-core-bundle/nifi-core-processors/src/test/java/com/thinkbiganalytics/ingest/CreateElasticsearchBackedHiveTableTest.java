package com.thinkbiganalytics.ingest;

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

import com.thinkbiganalytics.nifi.v2.ingest.CreateElasticsearchBackedHiveTable;
import com.thinkbiganalytics.nifi.v2.ingest.IngestProperties;
import com.thinkbiganalytics.nifi.v2.ingest.RegisterFeedTablesTest;

import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CreateElasticsearchBackedHiveTableTest {

    private final TestRunner runner = TestRunners.newTestRunner(CreateElasticsearchBackedHiveTable.class);
    CreateElasticsearchBackedHiveTable table = new CreateElasticsearchBackedHiveTable();

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testGeneratingHQL() throws Exception {
        String hql = table.generateHQL("id int, count int, name string", "localhost:8300", "test_feed", "test_category", "true", "true", "");
        assertEquals(hql, "CREATE EXTERNAL TABLE IF NOT EXISTS test_category.index_test_feed (id int, count int, name string) STORED BY 'org.elasticsearch.hadoop.hive.EsStorageHandler' TBLPROPERTIES('es.resource' = 'test_category/test_feed', 'es.nodes' = 'localhost:8300', 'es.nodes.wan.only' = 'true', 'es.index.auto.create' = 'true')");
    }

}
