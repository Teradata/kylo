package com.thinkbiganalytics.metadata.rest.client;

import static org.assertj.core.api.Assertions.*;

import java.net.URI;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.thinkbiganalytics.metadata.rest.model.data.Datasource;
import com.thinkbiganalytics.metadata.rest.model.data.DirectoryDatasource;
import com.thinkbiganalytics.metadata.rest.model.data.HiveTableDatasource;
import com.thinkbiganalytics.metadata.rest.model.feed.Feed;

public class MetadataClientTest {
    
    private static MetadataClient client;

    @BeforeClass
    public static void connect() {
        client = new MetadataClient(URI.create("http://localhost:8080/api/metadata/"));
    }
    
    @Before
    public void setUp() throws Exception {
    }

//    @Test
    public void testBuildFeed() {
        Feed feed = client.buildFeed("Feed 1")
                .description("Test feed 1")
                .systemName("feed1")
                .owner("owner")
                .post();
        
        assertThat(feed).isNotNull();
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
