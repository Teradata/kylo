package com.thinkbiganalytics.nifi.core.api.metadata;

/*-
 * #%L
 * thinkbig-nifi-core-service-api
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

import com.thinkbiganalytics.metadata.api.op.FeedDependencyDeltaResults;
import com.thinkbiganalytics.metadata.rest.model.data.Datasource;
import com.thinkbiganalytics.metadata.rest.model.data.DirectoryDatasource;
import com.thinkbiganalytics.metadata.rest.model.data.HiveTableDatasource;
import com.thinkbiganalytics.metadata.rest.model.feed.Feed;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedDestination;
import com.thinkbiganalytics.metadata.rest.model.op.DataOperation;
import com.thinkbiganalytics.metadata.rest.model.op.DataOperation.State;
import com.thinkbiganalytics.metadata.rest.model.op.Dataset;
import com.thinkbiganalytics.metadata.rest.model.op.HiveTablePartitions;
import com.thinkbiganalytics.metadata.sla.api.Metric;

import org.joda.time.DateTime;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Properties;

import javax.annotation.Nonnull;

/**
 * Provides services for working with feeds and data sources
 */
public interface MetadataProvider {

    /**
     * gets the id for the feed given
     *
     * @param category the category containing the feed
     * @param feedName the name of the feed
     * @return a feed id
     */
    String getFeedId(String category, String feedName);

    /**
     * returns the api service class used to obtain information about feed deltas
     *
     * @param feedId the feed in question
     * @return a service object to get more information about feed deltas
     */
    FeedDependencyDeltaResults getFeedDependentResultDeltas(String feedId);

    /**
     * Ensures the feed exists in the metadata store.  Finds an existing feed of if none exists then one is created.
     *
     * @param categoryName The category for the feed
     * @param feedName     The name of the feed
     * @param description  A description to associate with the feed
     * @return the feed
     */
    Feed ensureFeed(String categoryName, String feedName, String description);

    /**
     * gets the data source service by the given name
     *
     * @param dsName the data source name
     * @return the data source description
     */
    Datasource getDatasourceByName(String dsName);

    /**
     * ensures the feed source given by the feedId and datasourceId
     *
     * @param feedId       the feed id
     * @param datasourceId the datasource id
     * @return the feed
     */
    Feed ensureFeedSource(String feedId, String datasourceId);

    /**
     * ensures the feed destination given by the feedId and datasourceId
     *
     * @param feedId       the feed id
     * @param datasourceId the datasource id
     * @return the feed
     */
    Feed ensureFeedDestination(String feedId, String datasourceId);

    /**
     * gets, or creates a feed, based on certain preconditions given as Metric's
     *
     * @param feedId  the feed id
     * @param metrics the conditions to be met
     * @return the feed
     */
    Feed ensurePrecondition(String feedId, Metric... metrics);

    /**
     * merge the given properties to the current feed properties
     *
     * @param feedId the feed id
     * @param props  the datasource id
     * @return the updated properties
     */
    Properties updateFeedProperties(String feedId, Properties props);

    /**
     * gets, or creates, a datasource
     *
     * @param datasetName the dataset name
     * @param descr       A description for the datasource
     * @param path        the path for the data
     * @return a directory data source that can be used to fetch or modify metadata
     */
    DirectoryDatasource ensureDirectoryDatasource(String datasetName, String descr, Path path);

    /**
     * gets, or creates, a table in hive to use a data source
     *
     * @param datasetName  the dataset name
     * @param descr        a description for the data source
     * @param databaseName the database name where the table (will) reside(s)
     * @param tableName    the name of the table
     * @return a HiveTableDatasource that can be used to fetch or modify table metadata
     */
    HiveTableDatasource ensureHiveTableDatasource(String datasetName, String descr, String databaseName, String tableName);

    /**
     * creates a file system Dataset at one or more paths
     *
     * @param dds   the directory datasource which describes the locations of the data
     * @param paths one or more paths
     * @return a dataset object that is used to fetch or modify meta data
     */
    Dataset createDataset(DirectoryDatasource dds, Path... paths);

    /**
     * create a data set that can be used to describe the metadata for a directory data source and it's paths
     *
     * @param dds   the directory datasource which describes the dataset
     * @param paths one or more paths
     * @return a dataset object that is used to fetch or modify meta data
     */
    Dataset createDataset(DirectoryDatasource dds, ArrayList<Path> paths);

    /**
     * create a data set that can be used to describe the metadata for a hive data source and it's partitions
     *
     * @param hds        the datasource that describes the hive table
     * @param partitions the partitions of the table
     * @return a dataset object that is used to fetch or modify meta data
     */
    Dataset createDataset(HiveTableDatasource hds, HiveTablePartitions partitions);

    /**
     * Begin tracking the run time of the operation on the feedDestination
     *
     * @param feedDestination the feed destination
     * @param opStart         the time it starts
     * @return a data operation
     */
    DataOperation beginOperation(FeedDestination feedDestination, DateTime opStart);

    /**
     * complete the operation begun by beginOperation
     *
     * @param id        the id of the operation
     * @param status    the status of the operation
     * @param changeSet the changeset resulting from the operation
     * @return a data operation object which is used to track the metadata of the operation
     */
    DataOperation completeOperation(String id, String status, Dataset changeSet);

    /**
     * complete the operation begun by beginOperation
     *
     * @param id     the id of the operation
     * @param status the status of the operation
     * @param state  the state of the operation
     * @return a data operation object which is used to track the metadata of the operation
     */
    DataOperation completeOperation(String id, String status, State state);

    /**
     * Gets the properties of the specified feed.
     *
     * @param id the feed id
     * @return the properties
     */
    Properties getFeedProperties(@Nonnull final String id);

    /**
     * Gets the feed for given category and feed names
     *
     * @param category category system name
     * @param feed feed system name
     * @return the feed definition
     */
    Feed getFeed(@Nonnull final String category, @Nonnull final String feed);

    /**
     * Merges the specified properties into the feed's properties.
     *
     * @param id    the feed id
     * @param props the new properties
     * @return the merged properties
     */
    Properties mergeFeedProperties(@Nonnull final String id, @Nonnull final Properties props);

    /**
     * Gets the data source with the specified id.
     *
     * @param id the data source id
     * @return the data source, if found
     */
    Optional<Datasource> getDatasource(@Nonnull String id);
}
