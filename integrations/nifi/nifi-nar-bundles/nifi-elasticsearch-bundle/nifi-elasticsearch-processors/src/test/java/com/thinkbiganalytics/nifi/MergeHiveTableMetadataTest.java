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
import com.thinkbiganalytics.nifi.v2.elasticsearch.MergeHiveTableMetadata;

import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Before;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MergeHiveTableMetadataTest {

    private InputStream testDocument;

    @Before
    public void setUp() throws IOException {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        testDocument = classloader.getResourceAsStream("tableMetadata/test.json");
    }

    //@Test
    public void tester() {
        runProcessor(testDocument);
        assertTrue(true);

    }

    private void runProcessor(InputStream testDocument) {
        TestRunner nifiTestRunner = TestRunners.newTestRunner(new MergeHiveTableMetadata());
        nifiTestRunner.setValidateExpressionUsage(true);
        nifiTestRunner.setProperty(MergeHiveTableMetadata.DATABASE_NAME, "NAME");
        nifiTestRunner.setProperty(MergeHiveTableMetadata.DATABASE_OWNER, "OWNER_NAME");
        nifiTestRunner.setProperty(MergeHiveTableMetadata.TABLE_CREATE_TIME, "CREATE_TIME");
        nifiTestRunner.setProperty(MergeHiveTableMetadata.TABLE_NAME, "TBL_NAME");
        nifiTestRunner.setProperty(MergeHiveTableMetadata.TABLE_TYPE, "TBL_TYPE");
        nifiTestRunner.setProperty(MergeHiveTableMetadata.COLUMN_NAME, "COLUMN_NAME");
        nifiTestRunner.setProperty(MergeHiveTableMetadata.COLUMN_TYPE, "TYPE_NAME");
        nifiTestRunner.assertValid();

        nifiTestRunner.enqueue(testDocument, new HashMap<String, String>() {{
            put("doc_id", "8736522777");
        }});
        nifiTestRunner.run(1, true, true);

        nifiTestRunner.assertAllFlowFilesTransferred(IndexElasticSearch.REL_SUCCESS, 1);
        final MockFlowFile out = nifiTestRunner.getFlowFilesForRelationship(IndexElasticSearch.REL_SUCCESS).get(0);
        String outgoingJson = new String(out.toByteArray());
        assertNotNull(out);
        out.assertAttributeEquals("doc_id", "8736522777");
    }
}
