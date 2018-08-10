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

import com.google.common.collect.ImmutableMap;
import com.thinkbiganalytics.nifi.v2.thrift.ThriftService;

import org.apache.nifi.components.ValidationResult;
import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.util.MockProcessContext;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Collection;

public class DropFeedTablesTest {

    /**
     * Identifier for thrift service
     */
    private static final String THRIFT_SERVICE_IDENTIFIER = "MockThriftService";

    /**
     * Test runner
     */
    private final TestRunner runner = TestRunners.newTestRunner(DropFeedTables.class);

    /**
     * Mock thrift service
     */
    private MockThriftService thriftService;

    /**
     * Initialize instance variables.
     */
    @Before
    public void setUp() throws Exception {
        // Setup thrift service
        thriftService = new MockThriftService();

        // Setup test runner
        runner.addControllerService(THRIFT_SERVICE_IDENTIFIER, thriftService);
        runner.enableControllerService(thriftService);
        runner.setProperty(IngestProperties.THRIFT_SERVICE, THRIFT_SERVICE_IDENTIFIER);
    }

    /**
     * Verify property validators.
     */
    @Test
    public void testValidators() {
        // Test with no properties
        runner.enqueue(new byte[0]);
        Collection<ValidationResult> results = ((MockProcessContext) runner.getProcessContext()).validate();
        Assert.assertEquals(1, results.size());
        results.forEach((ValidationResult result) -> Assert.assertEquals("'Table Type' is invalid because Table Type is required", result.toString()));

        // Test with valid properties
        runner.setProperty(DropFeedTables.TABLE_TYPE, "ALL");
        runner.enqueue(new byte[0]);
        results = ((MockProcessContext) runner.getProcessContext()).validate();
        Assert.assertEquals(0, results.size());
    }

    /**
     * Verify dropping tables.
     */
    @Test
    public void testDropTables() throws Exception {
        // Test dropping tables
        runner.setProperty(DropFeedTables.TABLE_TYPE, "ALL");
        runner.enqueue(new byte[0], ImmutableMap.of("metadata.category.systemName", "movies", "metadata.systemFeedName", "artists"));
        runner.run();

        Assert.assertEquals(0, runner.getFlowFilesForRelationship(IngestProperties.REL_FAILURE).size());
        Assert.assertEquals(1, runner.getFlowFilesForRelationship(IngestProperties.REL_SUCCESS).size());

        Mockito.verify(thriftService.statement).execute("DROP TABLE IF EXISTS `movies`.`artists_feed`");
        Mockito.verify(thriftService.statement).execute("DROP TABLE IF EXISTS `movies`.`artists_valid`");
        Mockito.verify(thriftService.statement).execute("DROP TABLE IF EXISTS `movies`.`artists_invalid`");
        Mockito.verify(thriftService.statement).execute("DROP TABLE IF EXISTS `movies`.`artists`");
        Mockito.verify(thriftService.statement).execute("DROP TABLE IF EXISTS `movies`.`artists_profile`");
        Mockito.verify(thriftService.statement, Mockito.times(5)).close();
        Mockito.verifyNoMoreInteractions(thriftService.statement);
    }

    /**
     * Verify dropping tables with additional tables.
     */
    @Test
    public void testDropTablesWithAdditionalTables() throws Exception {
        // Test dropping tables
        runner.setProperty(DropFeedTables.ADDITIONAL_TABLES, "test.sample_07,test.sample_08");
        runner.setProperty(DropFeedTables.TABLE_TYPE, "MASTER");
        runner.enqueue(new byte[0], ImmutableMap.of("metadata.category.systemName", "movies", "metadata.systemFeedName", "artists"));
        runner.run();

        Assert.assertEquals(0, runner.getFlowFilesForRelationship(IngestProperties.REL_FAILURE).size());
        Assert.assertEquals(1, runner.getFlowFilesForRelationship(IngestProperties.REL_SUCCESS).size());

        Mockito.verify(thriftService.statement).execute("DROP TABLE IF EXISTS `movies`.`artists`");
        Mockito.verify(thriftService.statement).execute("DROP TABLE IF EXISTS test.sample_07");
        Mockito.verify(thriftService.statement).execute("DROP TABLE IF EXISTS test.sample_08");
        Mockito.verify(thriftService.statement, Mockito.times(3)).close();
        Mockito.verifyNoMoreInteractions(thriftService.statement);
    }

    /**
     * Verify exception for missing category name.
     */
    @Test
    public void testDropTablesWithMissingCategory() {
        runner.setProperty(DropFeedTables.TABLE_TYPE, "ALL");
        runner.enqueue(new byte[0], ImmutableMap.of("feed", "artists"));
        runner.run();

        Assert.assertEquals(1, runner.getFlowFilesForRelationship(IngestProperties.REL_FAILURE).size());
        Assert.assertEquals(0, runner.getFlowFilesForRelationship(IngestProperties.REL_SUCCESS).size());
    }

    /**
     * Verify exception for missing feed name.
     */
    @Test
    public void testDropTablesWithMissingFeed() {
        runner.setProperty(DropFeedTables.TABLE_TYPE, "ALL");
        runner.enqueue(new byte[0], ImmutableMap.of("category", "movies"));
        runner.run();

        Assert.assertEquals(1, runner.getFlowFilesForRelationship(IngestProperties.REL_FAILURE).size());
        Assert.assertEquals(0, runner.getFlowFilesForRelationship(IngestProperties.REL_SUCCESS).size());
    }

    /**
     * Verify dropping tables with a single table type.
     */
    @Test
    public void testDropTablesWithTableType() throws Exception {
        // Test dropping tables
        runner.setProperty(DropFeedTables.TABLE_TYPE, "MASTER");
        runner.enqueue(new byte[0], ImmutableMap.of("metadata.category.systemName", "movies", "metadata.systemFeedName", "artists"));
        runner.run();

        Assert.assertEquals(0, runner.getFlowFilesForRelationship(IngestProperties.REL_FAILURE).size());
        Assert.assertEquals(1, runner.getFlowFilesForRelationship(IngestProperties.REL_SUCCESS).size());

        Mockito.verify(thriftService.statement).execute("DROP TABLE IF EXISTS `movies`.`artists`");
        Mockito.verify(thriftService.statement).close();
        Mockito.verifyNoMoreInteractions(thriftService.statement);
    }

    public class MockThriftService extends AbstractControllerService implements ThriftService {

        public final Connection connection = Mockito.mock(Connection.class);

        public final Statement statement = Mockito.mock(Statement.class);

        public MockThriftService() throws Exception {
            Mockito.when(connection.createStatement()).thenReturn(statement);
        }

        @Override
        public Connection getConnection() throws ProcessException {
            return connection;
        }
    }
}
