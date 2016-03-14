package com.thinkbiganalytics.controller.metadata;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Paths;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.thinkbiganalytics.metadata.rest.model.data.Datasource;
import com.thinkbiganalytics.metadata.rest.model.data.DirectoryDatasource;
import com.thinkbiganalytics.metadata.rest.model.data.HiveTableDatasource;
import com.thinkbiganalytics.metadata.rest.model.feed.Feed;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedDestination;
import com.thinkbiganalytics.metadata.rest.model.op.DataOperation;
import com.thinkbiganalytics.metadata.rest.model.op.DataOperation.State;
import com.thinkbiganalytics.metadata.rest.model.op.Dataset;
import com.thinkbiganalytics.metadata.rest.model.sla.DatasourceUpdatedSinceFeedExecutedMetric;
import com.thinkbiganalytics.metadata.rest.model.sla.DatasourceUpdatedSinceScheduleMetric;
import com.thinkbiganalytics.metadata.rest.model.sla.FeedExecutedSinceFeedMetric;
import com.thinkbiganalytics.metadata.rest.model.sla.FeedExecutedSinceScheduleMetric;
import com.thinkbiganalytics.metadata.rest.model.sla.WithinSchedule;

//@Ignore  // TODO Requires the metadata server running.  Add support for embedded test server.
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
        
        String feedId = feed.getId();
        feed = this.provider.ensureFeed("test1", "");
        
        assertThat(feed).isNotNull();
        assertThat(feed.getId()).isEqualTo(feedId);
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
        
        feed = this.provider.ensureFeedSource(feed.getId(), ds.getId());
        
        assertThat(feed.getSources()).hasSize(1);
    }

    @Test
    public void testEnsureFeedDestination() {
        Feed feed = this.provider.ensureFeed("test4", "");
        Datasource ds = this.provider.ensureDirectoryDatasource("test4", "", Paths.get("aaa", "bbb"));
        feed = this.provider.ensureFeedDestination(feed.getId(), ds.getId());
        
        assertThat(feed.getDestinations()).hasSize(1);
        
        feed = this.provider.ensureFeedDestination(feed.getId(), ds.getId());
        
        assertThat(feed.getDestinations()).hasSize(1);
    }

    @Test
    public void testEnsurePrecondition() {
        Feed feed = this.provider.ensureFeed("test5", "");
        feed = this.provider.ensurePrecondition(feed.getId(), 
                                                DatasourceUpdatedSinceFeedExecutedMetric.named("ds5", "test5"),
                                                DatasourceUpdatedSinceScheduleMetric.named("ds5", "0 0 6 * * ? *"),
                                                FeedExecutedSinceFeedMetric.named("dep5", "test5"),
                                                FeedExecutedSinceScheduleMetric.named("test5", "0 0 6 * * ? *"),
                                                new WithinSchedule("0 0 6 * * ? *", "2 hours"));
        
        assertThat(feed).isNotNull();
    }

    @Test
    public void testEnsureDirectoryDatasource() {
        this.provider.ensureDirectoryDatasource("test6", "", Paths.get("aaa", "bbb"));
        Datasource ds = this.provider.getDatasourceByName("test6");
        
        assertThat(ds).isNotNull();
        assertThat(ds).isInstanceOf(DirectoryDatasource.class);
        
        String dsId = ds.getId();
        DirectoryDatasource dds = (DirectoryDatasource) ds;
        
        assertThat(dds.getPath()).contains("aaa/bbb");
        
        ds = this.provider.ensureDirectoryDatasource("test6", "", Paths.get("aaa", "bbb"));
        
        assertThat(ds).isNotNull();
        assertThat(ds.getId()).isEqualTo(dsId);
    }

    @Test
    public void testEnsureHiveTableDatasource() {
        this.provider.ensureHiveTableDatasource("test7", "", "testdb", "test_table");
        Datasource ds = this.provider.getDatasourceByName("test7");
        
        assertThat(ds).isNotNull();
        assertThat(ds).isInstanceOf(HiveTableDatasource.class);
        
        String dsId = ds.getId();
        HiveTableDatasource dds = (HiveTableDatasource) ds;
        
        assertThat(dds.getTableName()).contains("test_table");
        
        ds = this.provider.ensureHiveTableDatasource("test7", "", "testdb", "test_table");
        
        assertThat(ds).isNotNull();
        assertThat(ds.getId()).isEqualTo(dsId);
    }

    @Test
    public void testBeginOperation() {
        Feed feed = this.provider.ensureFeed("test9", "");
        Datasource ds = this.provider.ensureDirectoryDatasource("test9", "", Paths.get("aaa", "bbb"));
        feed = this.provider.ensureFeedDestination(feed.getId(), ds.getId());
        FeedDestination dest = feed.getDestination(ds.getId());
        
        DataOperation op = this.provider.beginOperation(dest, new DateTime());
        
        assertThat(op).isNotNull();
        assertThat(op.getState()).isEqualTo(State.IN_PROGRESS);
    }

    @Test
    public void testCompleteOperation() {
        Feed feed = this.provider.ensureFeed("test10", "");
        DirectoryDatasource ds = this.provider.ensureDirectoryDatasource("test10", "", Paths.get("aaa", "bbb"));
        feed = this.provider.ensureFeedDestination(feed.getId(), ds.getId());
        FeedDestination dest = feed.getDestination(ds.getId());
        DataOperation op = this.provider.beginOperation(dest, new DateTime());
        
        Dataset set = this.provider.createDataset(ds, Paths.get("a.txt"), Paths.get("b.txt"));
        op = this.provider.completeOperation(op.getId(), "", set);

        assertThat(op).isNotNull();
        assertThat(op.getState()).isEqualTo(State.SUCCESS);
    }

}
