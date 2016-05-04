/*
 * Copyright (c) 2016. Teradata Inc.
 */
package com.thinkbiganalytics.nifi.v2.core.metadata;

import com.thinkbiganalytics.metadata.rest.client.MetadataClient;
import com.thinkbiganalytics.metadata.rest.model.data.Datasource;
import com.thinkbiganalytics.metadata.rest.model.data.DatasourceCriteria;
import com.thinkbiganalytics.metadata.rest.model.data.DirectoryDatasource;
import com.thinkbiganalytics.metadata.rest.model.data.HiveTableDatasource;
import com.thinkbiganalytics.metadata.rest.model.feed.Feed;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedDestination;
import com.thinkbiganalytics.metadata.rest.model.op.DataOperation;
import com.thinkbiganalytics.metadata.rest.model.op.DataOperation.State;
import com.thinkbiganalytics.metadata.rest.model.op.Dataset;
import com.thinkbiganalytics.metadata.rest.model.op.Dataset.ChangeType;
import com.thinkbiganalytics.metadata.rest.model.op.Dataset.ContentType;
import com.thinkbiganalytics.metadata.rest.model.op.FileList;
import com.thinkbiganalytics.metadata.rest.model.op.HiveTablePartitions;
import com.thinkbiganalytics.metadata.rest.model.sla.Metric;
import com.thinkbiganalytics.nifi.core.api.metadata.BatchLoadStatus;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataProvider;
import org.joda.time.DateTime;

import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Sean Felten
 */
public class MetadataClientProvider implements MetadataProvider {

    // TODO: Sean to remove after refactor
    private InMemoryLegacyMetadataProvider legacy = new InMemoryLegacyMetadataProvider();

    private MetadataClient client;

    public MetadataClientProvider() {
        this(URI.create("http://localhost:8077/api/metadata"));
    }

    public MetadataClientProvider(URI baseUri) {
        this.client = new MetadataClient(baseUri);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.controller.metadata.MetadataProvider#ensureFeed(java.lang.String, java.lang.String)
     */
    @Override
    public Feed ensureFeed(String feedName, String descr) {
        return this.client
                .buildFeed(feedName)
                .description(descr)
                .post();
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.controller.metadata.MetadataProvider#getDatasourceByName(java.lang.String)
     */
    @Override
    public Datasource getDatasourceByName(String dsName) {
        DatasourceCriteria criteria = this.client.datasourceCriteria().name(dsName);
        List<Datasource> list = this.client.getDatasources(criteria);

        if (list.isEmpty()) {
            return null;
        } else {
            return list.get(0);
        }
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.controller.metadata.MetadataProvider#ensureFeedSource(java.lang.String, java.lang.String)
     */
    @Override
    public Feed ensureFeedSource(String feedId, String datasourceId) {
        return this.client.addSource(feedId, datasourceId);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.controller.metadata.MetadataProvider#ensureFeedDestination(java.lang.String, java.lang.String)
     */
    @Override
    public Feed ensureFeedDestination(String feedId, String datasourceId) {
        return this.client.addDestination(feedId, datasourceId);
    }

    @Override
    public Feed ensurePrecondition(String feedId, Metric... metrics) {
        return this.client.setPrecondition(feedId, metrics);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.controller.metadata.MetadataProvider#ensureDirectoryDatasource(java.lang.String, java.lang.String, java.nio.file.Path)
     */
    @Override
    public DirectoryDatasource ensureDirectoryDatasource(String datasetName, String descr, Path path) {
        return this.client.buildDirectoryDatasource(datasetName)
                .description(descr)
                .path(path.toString())
                .post();
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.controller.metadata.MetadataProvider#ensureHiveTableDatasource(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public HiveTableDatasource ensureHiveTableDatasource(String dsName, String descr, String databaseName, String tableName) {
        return this.client.buildHiveTableDatasource(dsName)
                .description(descr)
                .database(databaseName)
                .tableName(tableName)
                .post();
    }

    @Override
    public Dataset createDataset(DirectoryDatasource dds, Path... paths) {
        return createDataset(dds, new ArrayList<>(Arrays.asList(paths)));
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.controller.metadata.MetadataProvider#createDatasourceSet(com.thinkbiganalytics.metadata.rest.model.data.DirectoryDatasource, java.util.ArrayList)
     */
    @Override
    public Dataset createDataset(DirectoryDatasource dds, ArrayList<Path> paths) {
        FileList files = new FileList(paths);
        return new Dataset(dds, ChangeType.UPDATE, ContentType.FILES, files);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.controller.metadata.MetadataProvider#createChangeSet(com.thinkbiganalytics.metadata.rest.model.data.HiveTableDatasource, int)
     */
    @Override
    public Dataset createDataset(HiveTableDatasource hds, HiveTablePartitions partitions) {
        return new Dataset(hds, ChangeType.UPDATE, ContentType.PARTITIONS, partitions);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.controller.metadata.MetadataProvider#beginOperation(com.thinkbiganalytics.metadata.rest.model.feed.FeedDestination, org.joda.time.DateTime)
     */
    @Override
    public DataOperation beginOperation(FeedDestination feedDestination, DateTime opStart) {
        return this.client.beginOperation(feedDestination.getId(), "");
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.controller.metadata.MetadataProvider#updateOperation(java.lang.String, java.lang.String, com.thinkbiganalytics.metadata.rest.model.data.Datasource)
     */
    @Override
    public DataOperation completeOperation(String id, String status, Dataset dataset) {
        DataOperation op = this.client.getDataOperation(id);
        op.setStatus(status);
        op.setDataset(dataset);
        op.setState(State.SUCCESS);

        return this.client.updateDataOperation(op);
    }

    @Override
    public DataOperation completeOperation(String id, String string, State state) {
        DataOperation op = this.client.getDataOperation(id);
        op.setState(state);

        return this.client.updateDataOperation(op);
    }

//    @Override
//    // TODO: Sean to remove after refactor
//    public void recordLastSuccessfulLoad(String categoryName, String feedName, BatchLoadStatus newStatus) {
//        legacy.recordLastSuccessfulLoad(categoryName, feedName, newStatus);
//    }
//
//    @Override
//    // TODO: Sean to remove after refactor
//    public BatchLoadStatus getLastLoad(String categoryName, String feedName) {
//        return legacy.getLastLoad(categoryName, feedName);
//    }
//
//    @Override
//    // TODO: Sean to remove after refactor
//    public boolean isRegistrationRequired(String categoryName, String feedName) {
//        return legacy.isRegistrationRequired(categoryName, feedName);
//    }
//
//    @Override
//    // TODO: Sean to remove after refactor
//    public void recordRegistration(String category, String feed, boolean result) {
//        legacy.recordRegistration(category, feed, result);
//    }
}
