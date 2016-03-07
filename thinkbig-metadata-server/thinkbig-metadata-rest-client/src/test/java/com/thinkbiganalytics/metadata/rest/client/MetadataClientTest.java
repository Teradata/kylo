package com.thinkbiganalytics.metadata.rest.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.text.ParseException;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.thinkbiganalytics.metadata.rest.model.data.Datasource;
import com.thinkbiganalytics.metadata.rest.model.data.DirectoryDatasource;
import com.thinkbiganalytics.metadata.rest.model.data.HiveTableDatasource;
import com.thinkbiganalytics.metadata.rest.model.feed.Feed;
import com.thinkbiganalytics.metadata.rest.model.sla.FeedExecutedSinceFeedMetric;
import com.thinkbiganalytics.metadata.rest.model.sla.FeedExecutedSinceScheduleMetric;

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
    
//    @Test
    public void testAddFeedSource() throws ParseException {
        Feed feed = buildFeed("feed1").post();
        HiveTableDatasource ds = buildHiveTableDatasource("test-table").post();
        
        Feed result = client.addSource(feed.getId(), ds.getId());
        
        assertThat(result).isNotNull();
    }
    
//    @Test 
    public void testAddFeedDestination() throws ParseException {
        Feed feed = buildFeed("feed1").post();
        HiveTableDatasource ds = buildHiveTableDatasource("test-table").post();
        
        Feed result = client.addDestination(feed.getId(), ds.getId());
        
        assertThat(result).isNotNull();
    }

//    @Test 
    public void testBuildHiveTableDatasource() {
        HiveTableDatasource ds = buildHiveTableDatasource("test-table").post();
        
        assertThat(ds).isNotNull();
    }
    
//    @Test
    public void testBuildDirectoryDatasource() {
        DirectoryDatasource ds = buildDirectoryDatasource("test-dir").post();
        
        assertThat(ds).isNotNull();
    }
    
//    @Test
    public void testListDatasources() {
        buildDirectoryDatasource("ds1").post();
        buildHiveTableDatasource("ds2").post();
        buildDirectoryDatasource("ds3").post();
        
        List<Datasource> list = client.getDatasources();
        
        assertThat(list)
            .isNotNull()
            .hasSize(3);
    }
    
    private FeedBuilder buildFeed(String name) throws ParseException {
        return client.buildFeed(name)
                .description(name + " feed")
                .owner("ownder")
                .systemName(name)
                .preconditionMetric(FeedExecutedSinceScheduleMetric.named(name, "0 0 6 * * ? *"));
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
