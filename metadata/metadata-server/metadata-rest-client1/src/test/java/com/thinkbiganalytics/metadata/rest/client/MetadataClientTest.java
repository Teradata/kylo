package com.thinkbiganalytics.metadata.rest.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.thinkbiganalytics.metadata.rest.model.data.Datasource;
import com.thinkbiganalytics.metadata.rest.model.data.DirectoryDatasource;
import com.thinkbiganalytics.metadata.rest.model.data.HiveTableDatasource;
import com.thinkbiganalytics.metadata.rest.model.data.HiveTablePartition;
import com.thinkbiganalytics.metadata.rest.model.feed.Feed;
import com.thinkbiganalytics.metadata.rest.model.op.DataOperation;
import com.thinkbiganalytics.metadata.rest.model.op.DataOperation.State;
import com.thinkbiganalytics.metadata.rest.model.op.Dataset;
import com.thinkbiganalytics.metadata.rest.model.op.Dataset.ChangeType;
import com.thinkbiganalytics.metadata.rest.model.op.Dataset.ContentType;
import com.thinkbiganalytics.metadata.rest.model.op.HiveTablePartitions;
import com.thinkbiganalytics.metadata.rest.model.sla.FeedExecutedSinceFeedMetric;

@Ignore  // Requires a running metadata server
public class MetadataClientTest {
    
    private static MetadataClient client;

    @BeforeClass
    public static void connect() {
        client = new MetadataClient(URI.create("http://localhost:8080/api/metadata/"));
    }
    
    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testBuildFeed() throws ParseException {
        Feed feed = buildFeed("feed1").post();
        
        assertThat(feed).isNotNull();
    }
    
    @Test
    public void testAddFeedSource() throws ParseException {
        Feed feed = buildFeed("feed1").post();
        HiveTableDatasource ds = buildHiveTableDatasource("test-table").post();
        
        Feed result = client.addSource(feed.getId(), ds.getId());
        
        assertThat(result).isNotNull();
    }
    
    @Test 
    public void testAddFeedDestination() throws ParseException {
        Feed feed = buildFeed("feed1").post();
        HiveTableDatasource ds = buildHiveTableDatasource("test-table").post();
        
        Feed result = client.addDestination(feed.getId(), ds.getId());
        
        assertThat(result).isNotNull();
    }

    @Test 
    public void testBuildHiveTableDatasource() {
        HiveTableDatasource ds = buildHiveTableDatasource("test-table").post();
        
        assertThat(ds).isNotNull();
    }
    
    @Test
    public void testBuildDirectoryDatasource() {
        DirectoryDatasource ds = buildDirectoryDatasource("test-dir").post();
        
        assertThat(ds).isNotNull();
    }
    
    @Test
    public void testListDatasources() {
        buildDirectoryDatasource("ds1").post();
        buildHiveTableDatasource("ds2").post();
        buildDirectoryDatasource("ds3").post();
        
        List<Datasource> list = client.getDatasources();
        
        assertThat(list)
            .isNotNull()
            .hasSize(3);
    }
    
    @Test
    public void testBeginOperation() throws ParseException {
        Feed feed = buildFeed("feed1").post();
        HiveTableDatasource ds = buildHiveTableDatasource("test-table").post();
        feed = client.addDestination(feed.getId(), ds.getId());
        String destId = feed.getDestinations().iterator().next().getId();

        DataOperation op = client.beginOperation(destId, "");
        
        assertThat(op).isNotNull();
    }
    
    @Test
    public void testCompleteOperation() throws ParseException {
        Feed feedA = buildFeed("feedA").post();
        HiveTableDatasource dsA = buildHiveTableDatasource("test-table").post();
        feedA = client.addDestination(feedA.getId(), dsA.getId());
        
        Feed feedB = buildFeed("feedB", "feedA").post();
        feedB = client.addSource(feedB.getId(), dsA.getId());
        String destA = feedA.getDestinations().iterator().next().getId();
        
        DataOperation op = client.beginOperation(destA, "");
        op.setState(State.SUCCESS);
        
        HiveTablePartitions changeSet = new HiveTablePartitions();
        changeSet.setPartitions(Arrays.asList(new HiveTablePartition("month", null, "Jan", "Feb"),
                                           new HiveTablePartition("year", null, "2015", "2016")));
        Dataset dataset = new Dataset(new DateTime(), dsA, ChangeType.UPDATE, ContentType.PARTITIONS, changeSet);
        op.setDataset(dataset);

        op = client.updateDataOperation(op);
        
        assertThat(op).isNotNull();
    }
    
    private FeedBuilder buildFeed(String name) throws ParseException {
        return client.buildFeed(name)
                .description(name + " feed")
                .owner("ownder")
                .systemName(name);
//                .preconditionMetric(FeedExecutedSinceScheduleMetric.named(name, "0 0 6 * * ? *"));
    }
    
    private FeedBuilder buildFeed(String name, String dependent) throws ParseException {
        return client.buildFeed(name)
                .description(name + " feed")
                .owner("ownder")
                .systemName(name)
                .preconditionMetric(FeedExecutedSinceFeedMetric.named(dependent, name));
    }
    
    private DirectoryDatasourceBuilder buildDirectoryDatasource(String name) {
        return client.buildDirectoryDatasource(name)
                .description(name + " datasource")
                .compressed(true)
                .ownder("owner")
                .path("/tmp/test")
                .globPattern("*.txt");
    }
    
    private HiveTableDatasourceBuilder buildHiveTableDatasource(String name) {
        return client.buildHiveTableDatasource(name)
                .description(name + " datasource")
                .encrypted(true)
                .ownder("owner")
                .database("testdb")
                .tableName("test_table")
                .field("key", "INT")
                .field("value", "VARCHAR")
                .partition("month", null, "jan", "feb", "mar")
                .partition("year", null, "2016");
    }
}
