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

import com.thinkbiganalytics.util.TableType;
import org.apache.nifi.components.ValidationResult;
import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.util.MockProcessContext;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collection;

public class RegisterFeedTablesTest {

    /**
     * Identifier for thrift service
     */
    private static final String THRIFT_SERVICE_IDENTIFIER = "MockThriftService";

    /**
     * Test runner
     */
    private final TestRunner runner = TestRunners.newTestRunner(RegisterFeedTables.class);

    /**
     * Mock thrift service
     */
    private MockThriftService thriftService;

    /**
     * Initialize instance variables
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
     * Verify no properties are required.
     */
    @Test
    public void testValidators() {
        runner.enqueue(new byte[0]);
        Collection<ValidationResult> results = ((MockProcessContext) runner.getProcessContext()).validate();
        Assert.assertEquals(0, results.size());
    }

    /**
     * Verify registering tables.
     */
    @Test
    public void testRegisterTables() throws Exception {
        // Test with only required properties
        runner.setProperty(IngestProperties.FIELD_SPECIFICATION, "id|int\nfirst_name|string\nlast_name|string");
        runner.enqueue(new byte[0], ImmutableMap.of("metadata.category.systemName", "movies", "metadata.systemFeedName", "artists"));
        runner.run();

        Assert.assertEquals(0, runner.getFlowFilesForRelationship(IngestProperties.REL_FAILURE).size());
        Assert.assertEquals(1, runner.getFlowFilesForRelationship(IngestProperties.REL_SUCCESS).size());

        final InOrder inOrder = Mockito.inOrder(thriftService.statement);
        inOrder.verify(thriftService.statement).execute("CREATE DATABASE IF NOT EXISTS `movies`");
        inOrder.verify(thriftService.statement).close();
        inOrder.verify(thriftService.statement).execute("use `movies`");
        inOrder.verify(thriftService.statement).executeQuery("show tables like 'artists*'");
        inOrder.verify(thriftService.statement).close();
        inOrder.verify(thriftService.statement).execute("CREATE EXTERNAL TABLE IF NOT EXISTS `movies`.`artists_feed` (`id` string, `first_name` string, `last_name` string)   "
                                                        + "PARTITIONED BY (`processing_dttm` string)  ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' LINES TERMINATED BY '\n' STORED AS TEXTFILE "
                                                        + "LOCATION '/model.db/movies/artists/feed'");
        inOrder.verify(thriftService.statement).close();
        inOrder.verify(thriftService.statement).execute("CREATE TABLE IF NOT EXISTS `movies`.`artists_valid` (`id` int, `first_name` string, `last_name` string)   "
                + "PARTITIONED BY (`processing_dttm` string)  STORED AS ORC LOCATION '/model.db/movies/artists/valid'");
        inOrder.verify(thriftService.statement).close();
        inOrder.verify(thriftService.statement).execute("CREATE TABLE IF NOT EXISTS `movies`.`artists_invalid` (`id` string, `first_name` string, `last_name` string, dlp_reject_reason string "
                                                        + ")   PARTITIONED BY (`processing_dttm` string)  STORED AS ORC LOCATION '/model.db/movies/artists/invalid'");
        inOrder.verify(thriftService.statement).close();
        inOrder.verify(thriftService.statement).execute("CREATE TABLE IF NOT EXISTS `movies`.`artists` (`id` int, `first_name` string, `last_name` string, processing_dttm string)  STORED AS ORC "
                                                        + "LOCATION '/app/warehouse/movies/artists'");
        inOrder.verify(thriftService.statement).close();
        inOrder.verify(thriftService.statement).execute("CREATE TABLE IF NOT EXISTS `movies`.`artists_profile` ( `columnname` string,`metrictype` string,`metricvalue` string)   "
                                                        + "PARTITIONED BY (`processing_dttm` string)  STORED AS ORC LOCATION '/model.db/movies/artists/profile'");
        inOrder.verify(thriftService.statement).close();
        inOrder.verifyNoMoreInteractions();

        // Test with all properties
        runner.setProperty(IngestProperties.PARTITION_SPECS, "year|int");
        runner.setProperty(IngestProperties.FEED_FORMAT_SPECS, "ROW FORMAT DELIMITED LINES TERMINATED BY '\\n' STORED AS TEXTFILE");
        runner.setProperty(IngestProperties.TARGET_FORMAT_SPECS, "STORED AS PARQUET");
        runner.setProperty(IngestProperties.TARGET_TBLPROPERTIES, "TBLPROPERTIES (\"comment\"=\"Movie Actors\")");
        runner.enqueue(new byte[0], ImmutableMap.of("metadata.category.systemName", "movies", "metadata.systemFeedName", "artists"));
        runner.run();

        Assert.assertEquals(0, runner.getFlowFilesForRelationship(IngestProperties.REL_FAILURE).size());
        Assert.assertEquals(2, runner.getFlowFilesForRelationship(IngestProperties.REL_SUCCESS).size());

        inOrder.verify(thriftService.statement).execute("CREATE DATABASE IF NOT EXISTS `movies`");
        inOrder.verify(thriftService.statement).close();
        inOrder.verify(thriftService.statement).execute("use `movies`");
        inOrder.verify(thriftService.statement).executeQuery("show tables like 'artists*'");
        inOrder.verify(thriftService.statement).close();
        inOrder.verify(thriftService.statement).execute("CREATE EXTERNAL TABLE IF NOT EXISTS `movies`.`artists_feed` (`id` string, `first_name` string, `last_name` string)   "
                                                        + "PARTITIONED BY (`processing_dttm` string)  ROW FORMAT DELIMITED LINES TERMINATED BY '\\n' STORED AS TEXTFILE "
                                                        + "LOCATION '/model.db/movies/artists/feed'");
        inOrder.verify(thriftService.statement).close();
        inOrder.verify(thriftService.statement).execute("CREATE TABLE IF NOT EXISTS `movies`.`artists_valid` (`id` int, `first_name` string, `last_name` string)   "
                + "PARTITIONED BY (`processing_dttm` string)  STORED AS PARQUET LOCATION '/model.db/movies/artists/valid' "
                + "TBLPROPERTIES (\"comment\"=\"Movie Actors\")");
        inOrder.verify(thriftService.statement).close();
        inOrder.verify(thriftService.statement).execute("CREATE TABLE IF NOT EXISTS `movies`.`artists_invalid` (`id` string, `first_name` string, `last_name` string, dlp_reject_reason string "
                                                        + ")   PARTITIONED BY (`processing_dttm` string)  STORED AS PARQUET LOCATION '/model.db/movies/artists/invalid' "
                                                        + "TBLPROPERTIES (\"comment\"=\"Movie Actors\")");
        inOrder.verify(thriftService.statement).close();
        inOrder.verify(thriftService.statement)
            .execute("CREATE TABLE IF NOT EXISTS `movies`.`artists` (`id` int, `first_name` string, `last_name` string, processing_dttm string)   PARTITIONED BY (`year` int)  "
                     + "STORED AS PARQUET LOCATION '/app/warehouse/movies/artists' TBLPROPERTIES (\"comment\"=\"Movie Actors\")");
        inOrder.verify(thriftService.statement).close();
        inOrder.verify(thriftService.statement).execute("CREATE TABLE IF NOT EXISTS `movies`.`artists_profile` ( `columnname` string,`metrictype` string,`metricvalue` string)   "
                                                        + "PARTITIONED BY (`processing_dttm` string)  STORED AS PARQUET LOCATION '/model.db/movies/artists/profile'");
        inOrder.verify(thriftService.statement).close();
        inOrder.verifyNoMoreInteractions();
    }

    /**
     * Verify XML feed table with custom table properties
     */
    @Test
    public void testXMLTable() throws Exception {
        // Test with all properties
        runner.setProperty(RegisterFeedTables.TABLE_TYPE, TableType.FEED.toString());
        runner.setProperty(IngestProperties.FIELD_SPECIFICATION, "id_|string\nauthor|string\nprice|string");
        runner.setProperty(IngestProperties.FEED_FORMAT_SPECS, "row format serde 'com.ibm.spss.hive.serde2.xml.XmlSerDe' with serdeproperties (\"column.xpath.price\" = \"/book/price/text()\", "
                                                               + "     \"column.xpath.id\" = \"/book/@id\",\"column.xpath.author\" = \"/book/author/text()\") stored as inputformat 'com.ibm.spss.hive.serde2.xml.XmlInputFormat' outputformat 'org.apache.hadoop.hive.ql.io.IgnoreKeyTextOutputFormat'");
        runner.setProperty(IngestProperties.FEED_TBLPROPERTIES, "tblproperties ( \"xmlinput.start\" = \"<book \",\"xmlinput.end\"   = \"</book>\")");
        runner.enqueue(new byte[0], ImmutableMap.of("metadata.category.systemName", "demo", "metadata.systemFeedName", "xml_test_001"));
        runner.run();

        Assert.assertEquals(0, runner.getFlowFilesForRelationship(IngestProperties.REL_FAILURE).size());
        Assert.assertEquals(1, runner.getFlowFilesForRelationship(IngestProperties.REL_SUCCESS).size());
    }

    /**
     * Verify XML feed table with custom table properties
     */
    @Test
    public void testFeedOverride() throws Exception {
        // Test with all properties
        final String ddl = "CREATE TABLE IF NOT EXISTS `movies`.`artists_feed` ( `id` string)";
        runner.setProperty(RegisterFeedTables.TABLE_TYPE, TableType.FEED.toString());
        runner.setProperty(RegisterFeedTables.FEED_TABLE_OVERRIDE, ddl);
        runner.enqueue(new byte[0], ImmutableMap.of("metadata.category.systemName", "movies", "metadata.systemFeedName", "artists"));
        runner.run();

        Assert.assertEquals(0, runner.getFlowFilesForRelationship(IngestProperties.REL_FAILURE).size());
        Assert.assertEquals(1, runner.getFlowFilesForRelationship(IngestProperties.REL_SUCCESS).size());

        final InOrder inOrder = Mockito.inOrder(thriftService.statement);
        inOrder.verify(thriftService.statement).execute("CREATE DATABASE IF NOT EXISTS `movies`");
        inOrder.verify(thriftService.statement).close();
        inOrder.verify(thriftService.statement).execute(ddl);
        inOrder.verify(thriftService.statement).close();

    }

    /**
     * Verify registering tables with some pre-existing.
     */
    @Test
    public void testRegisterTablesWithExisting() throws Exception {
        // Mock 'show table' results
        Mockito.when(thriftService.artistsTablesResults.next()).thenReturn(true);
        Mockito.when(thriftService.artistsTablesResults.getString(1)).thenReturn("artists");
        Mockito.when(thriftService.artistsTablesResults.next()).thenReturn(true);
        Mockito.when(thriftService.artistsTablesResults.getString(1)).thenReturn("artists_valid");
        Mockito.when(thriftService.artistsTablesResults.next()).thenReturn(true);
        Mockito.when(thriftService.artistsTablesResults.getString(1)).thenReturn("artists_invalid");
        Mockito.when(thriftService.artistsTablesResults.next()).thenReturn(false);

        // Run flow
        runner.setProperty(IngestProperties.FIELD_SPECIFICATION, "id|int\nfirst_name|string\nlast_name|string");
        runner.enqueue(new byte[0], ImmutableMap.of("metadata.category.systemName", "movies", "metadata.systemFeedName", "artists"));
        runner.run();

        Assert.assertEquals(0, runner.getFlowFilesForRelationship(IngestProperties.REL_FAILURE).size());
        Assert.assertEquals(1, runner.getFlowFilesForRelationship(IngestProperties.REL_SUCCESS).size());

        // Verify SQL
        final InOrder inOrder = Mockito.inOrder(thriftService.statement);
        inOrder.verify(thriftService.statement).execute("CREATE DATABASE IF NOT EXISTS `movies`");
        inOrder.verify(thriftService.statement).close();
        inOrder.verify(thriftService.statement).execute("use `movies`");
        inOrder.verify(thriftService.statement).executeQuery("show tables like 'artists*'");
        inOrder.verify(thriftService.statement).close();
        inOrder.verify(thriftService.statement).execute("CREATE EXTERNAL TABLE IF NOT EXISTS `movies`.`artists_feed` (`id` string, `first_name` string, `last_name` string)   "
                                                        + "PARTITIONED BY (`processing_dttm` string)  ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' LINES TERMINATED BY '\n' STORED AS TEXTFILE "
                                                        + "LOCATION '/model.db/movies/artists/feed'");
        inOrder.verify(thriftService.statement).close();
        inOrder.verify(thriftService.statement).execute("CREATE TABLE IF NOT EXISTS `movies`.`artists_profile` ( `columnname` string,`metrictype` string,`metricvalue` string)   "
                                                        + "PARTITIONED BY (`processing_dttm` string)  STORED AS ORC LOCATION '/model.db/movies/artists/profile'");
        inOrder.verify(thriftService.statement).close();
        inOrder.verifyNoMoreInteractions();
    }

    /**
     * Verify error for missing category name.
     */
    @Test
    public void testRegisterTablesWithMissingCategory() {
        runner.setProperty(IngestProperties.FIELD_SPECIFICATION, "data|string");
        runner.enqueue(new byte[0], ImmutableMap.of("metadata.systemFeedName", "artists"));
        runner.run();

        Assert.assertEquals(1, runner.getFlowFilesForRelationship(IngestProperties.REL_FAILURE).size());
        Assert.assertEquals(0, runner.getFlowFilesForRelationship(IngestProperties.REL_SUCCESS).size());
    }

    @Test
    public void testStructFieldEscaping() throws Exception {

        String fieldSpec = "contributors|string||0|0|0|contributors\ncoordinates|string||0|0|0|coordinates\ncreated_at|string||0|0|0|created_at\ndisplay_text_range|array<bigint>||0|0|0|display_text_range\nentities|struct<hashtags:array<string>,symbols:array<string>,urls:array<string>,user_mentions:array<struct<id:bigint,id_str:string,indices:array<bigint>,name:string,screen_name:string>>>||0|0|0|entities\nfavorite_count|bigint||0|0|0|favorite_count\nfavorited|boolean||0|0|0|favorited\nfilter_level|string||0|0|0|filter_level\ngeo|string||0|0|0|geo\nid|bigint||0|0|0|id\nid_str|string||0|0|0|id_str\nin_reply_to_screen_name|string||0|0|0|in_reply_to_screen_name\nin_reply_to_status_id|bigint||0|0|0|in_reply_to_status_id\nin_reply_to_status_id_str|string||0|0|0|in_reply_to_status_id_str\nin_reply_to_user_id|bigint||0|0|0|in_reply_to_user_id\nin_reply_to_user_id_str|string||0|0|0|in_reply_to_user_id_str\nis_quote_status|boolean||0|0|0|is_quote_status\nlang|string||0|0|0|lang\nplace|string||0|0|0|place\nquote_count|bigint||0|0|0|quote_count\nreply_count|bigint||0|0|0|reply_count\nretweet_count|bigint||0|0|0|retweet_count\nretweeted|boolean||0|0|0|retweeted\nsource|string||0|0|0|source\ntext|string||0|0|0|text\ntimestamp_ms|string||0|0|0|timestamp_ms\ntruncated|boolean||0|0|0|truncated\nuser|struct<contributors_enabled:boolean,created_at:string,default_profile:boolean,default_profile_image:boolean,description:string,favourites_count:bigint,follow_request_sent:string,followers_count:bigint,following:string,friends_count:bigint,geo_enabled:boolean,id:bigint,id_str:string,is_translator:boolean,lang:string,listed_count:bigint,location:string,name:string,notifications:string,profile_background_color:string,profile_background_image_url:string,profile_background_image_url_https:string,profile_background_tile:boolean,profile_image_url:string,profile_image_url_https:string,profile_link_color:string,profile_sidebar_border_color:string,profile_sidebar_fill_color:string,profile_text_color:string,profile_use_background_image:boolean,protected:boolean,screen_name:string,statuses_count:bigint,time_zone:string,translator_type:string,url:string,utc_offset:string,verified:boolean>||0|0|0|user";
        runner.setProperty(IngestProperties.FIELD_SPECIFICATION, fieldSpec);
        runner.setProperty(RegisterFeedTables.TABLE_TYPE, "MASTER");
        runner.enqueue(new byte[0], ImmutableMap.of("metadata.category.systemName", "movies", "metadata.systemFeedName", "artists"));
        runner.run();

        Assert.assertEquals(0, runner.getFlowFilesForRelationship(IngestProperties.REL_FAILURE).size());
        Assert.assertEquals(1, runner.getFlowFilesForRelationship(IngestProperties.REL_SUCCESS).size());

    }

    /**
     * Verify error for missing feed name.
     */
    @Test
    public void testRegisterTablesWithMissingFeed() {
        runner.setProperty(IngestProperties.FIELD_SPECIFICATION, "data|string");
        runner.enqueue(new byte[0], ImmutableMap.of("metadata.category.systemName", "movies"));
        runner.enqueue(new byte[0], ImmutableMap.of("feed", "artists"));
        runner.run();

        Assert.assertEquals(1, runner.getFlowFilesForRelationship(IngestProperties.REL_FAILURE).size());
        Assert.assertEquals(0, runner.getFlowFilesForRelationship(IngestProperties.REL_SUCCESS).size());
    }

    /**
     * Verify error for missing field specification.
     */
    @Test
    public void testRegisterTablesWithMissingFieldSpecification() {
        runner.enqueue(new byte[0], ImmutableMap.of("metadata.category.systemName", "movies", "metadata.systemFeedName", "artists"));
        runner.run();

        Assert.assertEquals(1, runner.getFlowFilesForRelationship(IngestProperties.REL_FAILURE).size());
        Assert.assertEquals(0, runner.getFlowFilesForRelationship(IngestProperties.REL_SUCCESS).size());
    }

    /**
     * Verify registering a single table.
     */
    @Test
    public void testRegisterTablesWithTableType() throws Exception {
        runner.setProperty(IngestProperties.FIELD_SPECIFICATION, "id|int\nfirst_name|string\nlast_name|string");
        runner.setProperty(RegisterFeedTables.TABLE_TYPE, "MASTER");
        runner.enqueue(new byte[0], ImmutableMap.of("metadata.category.systemName", "movies", "metadata.systemFeedName", "artists"));
        runner.run();

        Assert.assertEquals(0, runner.getFlowFilesForRelationship(IngestProperties.REL_FAILURE).size());
        Assert.assertEquals(1, runner.getFlowFilesForRelationship(IngestProperties.REL_SUCCESS).size());

        final InOrder inOrder = Mockito.inOrder(thriftService.statement);
        inOrder.verify(thriftService.statement).execute("CREATE DATABASE IF NOT EXISTS `movies`");
        inOrder.verify(thriftService.statement).close();
        inOrder.verify(thriftService.statement).execute("CREATE TABLE IF NOT EXISTS `movies`.`artists` (`id` int, `first_name` string, `last_name` string, processing_dttm string)  STORED AS ORC "
                                                        + "LOCATION '/app/warehouse/movies/artists'");
        inOrder.verify(thriftService.statement).close();
        inOrder.verifyNoMoreInteractions();
    }

    /**
     * Verify registering a single table.
     */
    @Test
    public void testRegisterTablesWithConfig() throws Exception {
        runner.setProperty(IngestProperties.FIELD_SPECIFICATION, "id|int\nfirst_name|string\nlast_name|string");

        runner.setProperty(RegisterFeedTables.TABLE_TYPE, "ALL");
        runner.enqueue(new byte[0], ImmutableMap
            .of("metadata.category.systemName", "movies", "metadata.systemFeedName", "artists", "hive.ingest.root", "/var/ingest", "hive.profile.root", "/var/profile/",
                "hive.master.root", "/master"));
        runner.run();

        Assert.assertEquals(0, runner.getFlowFilesForRelationship(IngestProperties.REL_FAILURE).size());
        Assert.assertEquals(1, runner.getFlowFilesForRelationship(IngestProperties.REL_SUCCESS).size());

        final InOrder inOrder = Mockito.inOrder(thriftService.statement);
        inOrder.verify(thriftService.statement).execute(
            "CREATE EXTERNAL TABLE IF NOT EXISTS `movies`.`artists_feed` (`id` string, `first_name` string, `last_name` string)   PARTITIONED BY (`processing_dttm` string)  "
            + "ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' LINES TERMINATED BY '\n' STORED AS TEXTFILE LOCATION '/var/ingest/movies/artists/feed'");
        inOrder.verify(thriftService.statement).close();

        inOrder.verify(thriftService.statement).execute("CREATE TABLE IF NOT EXISTS `movies`.`artists_valid` (`id` int, `first_name` string, `last_name` string)   "
                + "PARTITIONED BY (`processing_dttm` string)  STORED AS ORC LOCATION '/var/ingest/movies/artists/valid'");
        inOrder.verify(thriftService.statement).close();

        inOrder.verify(thriftService.statement).execute("CREATE TABLE IF NOT EXISTS `movies`.`artists_invalid` (`id` string, `first_name` string, `last_name` string, dlp_reject_reason string"
                                                        + " )   PARTITIONED BY (`processing_dttm` string)  STORED AS ORC LOCATION '/var/ingest/movies/artists/invalid'");
        inOrder.verify(thriftService.statement).close();

        inOrder.verify(thriftService.statement).execute("CREATE TABLE IF NOT EXISTS `movies`.`artists` (`id` int, `first_name` string, `last_name` string, processing_dttm string)  STORED AS ORC "
                                                        + "LOCATION '/master/movies/artists'");
        inOrder.verify(thriftService.statement).close();

        inOrder.verify(thriftService.statement).execute("CREATE TABLE IF NOT EXISTS `movies`.`artists_profile` ( `columnname` string,`metrictype` string,`metricvalue` string)   "
                                                        + "PARTITIONED BY (`processing_dttm` string)  STORED AS ORC LOCATION '/var/profile/movies/artists/profile'");
        inOrder.verify(thriftService.statement).close();
        inOrder.verifyNoMoreInteractions();
    }

    /**
     * Verify registering only a profile table.
     */
    @Test
    public void testRegisterOnlyProfileTableWithConfig() throws Exception {
        runner.setProperty(IngestProperties.FIELD_SPECIFICATION, "id|int\nfirst_name|string\nlast_name|string");

        runner.setProperty(RegisterFeedTables.TABLE_TYPE, TableType.PROFILE.toString());
        runner.enqueue(new byte[0], ImmutableMap
                .of("metadata.category.systemName", "movies", "metadata.systemFeedName", "artists", "hive.ingest.root", "/var/ingest", "hive.profile.root", "/var/profile/",
                        "hive.master.root", "/master"));
        runner.run();

        Assert.assertEquals(0, runner.getFlowFilesForRelationship(IngestProperties.REL_FAILURE).size());
        Assert.assertEquals(1, runner.getFlowFilesForRelationship(IngestProperties.REL_SUCCESS).size());

        final InOrder inOrder = Mockito.inOrder(thriftService.statement);
        inOrder.verify(thriftService.statement).execute("CREATE DATABASE IF NOT EXISTS `movies`");
        inOrder.verify(thriftService.statement).close();
        inOrder.verify(thriftService.statement).execute("CREATE TABLE IF NOT EXISTS `movies`.`artists_profile` ( `columnname` string,`metrictype` string,`metricvalue` string)   "
                + "PARTITIONED BY (`processing_dttm` string)  STORED AS ORC LOCATION '/var/profile/movies/artists/profile'");
        inOrder.verify(thriftService.statement).close();
        inOrder.verifyNoMoreInteractions();
    }

    /**
     * A mock implementation of {@link ThriftService} for unit testing.
     */
    private class MockThriftService extends AbstractControllerService implements ThriftService {

        /**
         * Query results for {@code SHOW TABLES}
         */
        public final ResultSet artistsTablesResults = Mockito.mock(ResultSet.class);

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
            Mockito.when(statement.executeQuery("show tables like 'artists*'")).thenReturn(artistsTablesResults);
        }

        @Override
        public Connection getConnection() throws ProcessException {
            return connection;
        }
    }
}
