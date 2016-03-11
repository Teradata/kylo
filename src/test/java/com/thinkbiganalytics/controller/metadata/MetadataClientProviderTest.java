package com.thinkbiganalytics.controller.metadata;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Paths;

import org.apache.hadoop.fs.Path;
import org.junit.Before;
import org.junit.Test;

import com.thinkbiganalytics.metadata.rest.model.data.Datasource;
import com.thinkbiganalytics.metadata.rest.model.feed.Feed;

public class MetadataClientProviderTest {

    private MetadataClientProvider provider;
    
    @Before
    public void setUp() throws Exception {
        this.provider = new MetadataClientProvider();
    }

    @Test
    public void testEnsureFeed() {
        Feed feed = this.provider.ensureFeed("test1", "");
        
        assertThat(feed).isNotNull();
    }

    @Test
    public void testGetDatasourceByName() {
        this.provider.ensureDirectoryDatasource("test2", "", Paths.get("aaa", "bbb"));
        Datasource ds = this.provider.getDatasourceByName("test2");
        
        assertThat(ds).isNotNull();
    }

    @Test
    public void testEnsureFeedSource() {
        Feed feed = this.provider.ensureFeed("test3", "");
        Datasource ds = this.provider.ensureDirectoryDatasource("test3", "", Paths.get("aaa", "bbb"));
        feed = this.provider.ensureFeedSource(feed.getId(), ds.getId());
        
        assertThat(feed.getSources()).hasSize(1);
    }

    @Test
    public void testEnsureFeedDestination() {
        Feed feed = this.provider.ensureFeed("test4", "");
        Datasource ds = this.provider.ensureDirectoryDatasource("test4", "", Paths.get("aaa", "bbb"));
        feed = this.provider.ensureFeedDestination(feed.getId(), ds.getId());
        
        assertThat(feed.getDestinations()).hasSize(1);
    }

    @Test
    public void testEnsurePrecondition() {

    }

    @Test
    public void testEnsureDirectoryDatasource() {

    }

    @Test
    public void testEnsureHiveTableDatasource() {

    }

    @Test
    public void testCreateDataset() {

    }

    @Test
    public void testCreateChangeSet() {

    }

    @Test
    public void testBeginOperation() {

    }

    @Test
    public void testCompleteOperation() {

    }

}
