package com.thinkbiganalytics.controller.metadata;

/*-
 * #%L
 * thinkbig-nifi-core-service
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

import com.thinkbiganalytics.metadata.api.sla.DatasourceUpdatedSinceFeedExecuted;
import com.thinkbiganalytics.metadata.api.sla.DatasourceUpdatedSinceSchedule;
import com.thinkbiganalytics.metadata.api.sla.FeedExecutedSinceFeed;
import com.thinkbiganalytics.metadata.api.sla.FeedExecutedSinceSchedule;
import com.thinkbiganalytics.metadata.rest.model.data.Datasource;
import com.thinkbiganalytics.metadata.rest.model.data.DirectoryDatasource;
import com.thinkbiganalytics.metadata.rest.model.data.HiveTableDatasource;
import com.thinkbiganalytics.metadata.rest.model.feed.Feed;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedDestination;
import com.thinkbiganalytics.metadata.rest.model.op.DataOperation;
import com.thinkbiganalytics.metadata.rest.model.op.DataOperation.State;
import com.thinkbiganalytics.metadata.rest.model.op.Dataset;
import com.thinkbiganalytics.nifi.v2.core.metadata.MetadataClientProvider;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.file.Paths;
import java.text.ParseException;

import static org.assertj.core.api.Assertions.assertThat;

@Ignore  // TODO Requires the metadata server running.  Add support for embedded test server.
public class MetadataClientProviderTest {

    private MetadataClientProvider provider;

    @Before
    public void setUp() throws Exception {
        this.provider = new MetadataClientProvider();
    }

    @Test
    public void testEnsureFeed() {
        Feed feed = this.provider.ensureFeed("category", "test1", "");

        assertThat(feed).isNotNull();

        String feedId = feed.getId();
        feed = this.provider.ensureFeed("category", "test1", "");

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
        Feed feed = this.provider.ensureFeed("category", "test3", "");
        Datasource ds = this.provider.ensureDirectoryDatasource("test3", "", Paths.get("aaa", "bbb"));
        feed = this.provider.ensureFeedSource(feed.getId(), ds.getId());

        assertThat(feed.getSources()).hasSize(1);

        feed = this.provider.ensureFeedSource(feed.getId(), ds.getId());

        assertThat(feed.getSources()).hasSize(1);
    }

    @Test
    public void testEnsureFeedDestination() {
        Feed feed = this.provider.ensureFeed("category", "test4", "");
        Datasource ds = this.provider.ensureDirectoryDatasource("test4", "", Paths.get("aaa", "bbb"));
        feed = this.provider.ensureFeedDestination(feed.getId(), ds.getId());

        assertThat(feed.getDestinations()).hasSize(1);

        feed = this.provider.ensureFeedDestination(feed.getId(), ds.getId());

        assertThat(feed.getDestinations()).hasSize(1);
    }

    @Test
    public void testEnsurePrecondition() {
        Feed feed = this.provider.ensureFeed("category", "test5", "");
        try {
            feed = this.provider.ensurePrecondition(feed.getId(),
                                                    new DatasourceUpdatedSinceFeedExecuted("ds5", "test5"),
                                                    new DatasourceUpdatedSinceSchedule("ds5", "0 0 6 * * ? *"),
                                                    new FeedExecutedSinceFeed("category", "dep5", "category", "test5"),
                                                    new FeedExecutedSinceSchedule("category", "test5", "0 0 6 * * ? *"),
                                                    new com.thinkbiganalytics.metadata.api.sla.WithinSchedule("0 0 6 * * ? *", "2 hours"));
        } catch (ParseException e) {
            e.printStackTrace();
            ;
        }

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
        Feed feed = this.provider.ensureFeed("category", "test9", "");
        Datasource ds = this.provider.ensureDirectoryDatasource("test9", "", Paths.get("aaa", "bbb"));
        feed = this.provider.ensureFeedDestination(feed.getId(), ds.getId());
        FeedDestination dest = feed.getDestination(ds.getId());

        DataOperation op = this.provider.beginOperation(dest, new DateTime());

        assertThat(op).isNotNull();
        assertThat(op.getState()).isEqualTo(State.IN_PROGRESS);
    }

    @Test
    public void testCompleteOperation() {
        Feed feed = this.provider.ensureFeed("category", "test10", "");
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
