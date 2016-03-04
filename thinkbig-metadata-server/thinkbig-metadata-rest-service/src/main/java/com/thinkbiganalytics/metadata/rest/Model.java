/**
 * 
 */
package com.thinkbiganalytics.metadata.rest;

import java.util.HashSet;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.api.dataset.filesys.DirectoryDataset;
import com.thinkbiganalytics.metadata.api.dataset.hive.HiveTableDataset;
import com.thinkbiganalytics.metadata.rest.model.data.Datasource;
import com.thinkbiganalytics.metadata.rest.model.data.DirectoryDatasource;
import com.thinkbiganalytics.metadata.rest.model.data.HiveTableDatasource;
import com.thinkbiganalytics.metadata.rest.model.feed.Feed;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedDestination;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedSource;

/**
 *
 * @author Sean Felten
 */
public class Model {

    private Model() { }
    
    public static Function<com.thinkbiganalytics.metadata.api.feed.Feed, Feed> DOMAIN_TO_FEED 
        = new Function<com.thinkbiganalytics.metadata.api.feed.Feed, Feed>() {
            @Override
            public Feed apply(com.thinkbiganalytics.metadata.api.feed.Feed domain) {
                Feed feed = new Feed();
                feed.setId(domain.getId().toString());
                feed.setDescription(domain.getDescription());
                feed.setDisplayName(domain.getName());
//                feed.setOwner();
//                feed.setTrigger();
                feed.setSources(new HashSet<>(Collections2.transform(domain.getSources(), DOMAIN_TO_FEED_SOURCE)));
                feed.setDestinations(new HashSet<>(Collections2.transform(domain.getDestinations(), DOMAIN_TO_FEED_DESTINATION)));
                
                return feed;
            }
        };
        
    public static Function<com.thinkbiganalytics.metadata.api.feed.FeedSource, FeedSource> DOMAIN_TO_FEED_SOURCE
        = new Function<com.thinkbiganalytics.metadata.api.feed.FeedSource, FeedSource>() {
            @Override
            public FeedSource apply(com.thinkbiganalytics.metadata.api.feed.FeedSource domain) {
                FeedSource src = new FeedSource();
                src.setId(domain.getId().toString());
//                src.setLastLoadTime();
                src.setDatasourceId(domain.getDataset().getId().toString());
                return src;
            }
        };
    
    public static Function<com.thinkbiganalytics.metadata.api.feed.FeedDestination, FeedDestination> DOMAIN_TO_FEED_DESTINATION
        = new Function<com.thinkbiganalytics.metadata.api.feed.FeedDestination, FeedDestination>() {
            @Override
            public FeedDestination apply(com.thinkbiganalytics.metadata.api.feed.FeedDestination domain) {
                FeedDestination src = new FeedDestination();
                src.setId(domain.getId().toString());
//                dest.setFieldsPolicy();
                src.setDatasourceId(domain.getDataset().getId().toString());
                return src;
            }
        };
    
    public static Function<Dataset, Datasource> DOMAIN_TO_DS
        = new Function<Dataset, Datasource>() {
            @Override
            public Datasource apply(Dataset domain) {
                // TODO Is there a better way?
                if (domain instanceof DirectoryDataset) {
                    return DOMAIN_TO_DIR_DS.apply((DirectoryDataset) domain);
                } else if (domain instanceof HiveTableDataset) {
                    return DOMAIN_TO_TABLE_DS.apply((HiveTableDataset) domain);
                } else {
                    Datasource ds = new Datasource();
                    ds.setName(domain.getName());
                    ds.setDescription(domain.getDescription());
//                    ds.setOwnder();
//                    ds.setEncrypted();
//                    ds.setCompressed();
                    return ds;
                }
            }
        };

    public static Function<HiveTableDataset, HiveTableDatasource> DOMAIN_TO_TABLE_DS
        = new Function<HiveTableDataset, HiveTableDatasource>() {
            @Override
            public HiveTableDatasource apply(HiveTableDataset domain) {
                HiveTableDatasource table = new HiveTableDatasource();
                table.setName(domain.getName());
                table.setDescription(domain.getDescription());
//                table.setOwnder();
//                table.setEncrypted();
//                table.setCompressed();
                table.setDatabase(domain.getTableName());
                table.setTableName(domain.getTableName());
//                table.setFields();
//                table.setPartitions();
                
                return table;
            }
        };
    
    public static Function<DirectoryDataset, DirectoryDatasource> DOMAIN_TO_DIR_DS
        = new Function<DirectoryDataset, DirectoryDatasource>() {
            @Override
            public DirectoryDatasource apply(DirectoryDataset domain) {
                DirectoryDatasource dir = new DirectoryDatasource();
                dir.setName(domain.getName());
                dir.setDescription(domain.getDescription());
//                dir.setOwnder();
//                dir.setEncrypted();
//                dir.setCompressed();
                dir.setPath(domain.getDirectory().toString());
                
                return dir;
            }
        };
        
    public static void validateCreate(Feed feed) {
        // TODO Auto-generated method stub
        
    }


    public static void validateCreate(String fid, FeedDestination dest) {
        // TODO Auto-generated method stub
        
    }


    public static void validateCreate(HiveTableDatasource ds) {
        // TODO Auto-generated method stub
        
    }


    public static void validateCreate(DirectoryDatasource ds) {
        // TODO Auto-generated method stub
        
    }
}
