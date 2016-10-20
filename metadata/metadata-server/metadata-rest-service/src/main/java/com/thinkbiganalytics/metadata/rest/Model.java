/**
 * 
 */
package com.thinkbiganalytics.metadata.rest;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.datasource.filesys.FileList;
import com.thinkbiganalytics.metadata.api.datasource.hive.HivePartitionUpdate;
import com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableUpdate;
import com.thinkbiganalytics.metadata.api.feed.Feed.State;
import com.thinkbiganalytics.metadata.api.op.ChangeSet;
import com.thinkbiganalytics.metadata.rest.model.Formatters;
import com.thinkbiganalytics.metadata.rest.model.data.Datasource;
import com.thinkbiganalytics.metadata.rest.model.data.DirectoryDatasource;
import com.thinkbiganalytics.metadata.rest.model.data.HiveTableDatasource;
import com.thinkbiganalytics.metadata.rest.model.data.HiveTablePartition;
import com.thinkbiganalytics.metadata.rest.model.feed.Feed;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedCategory;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedDestination;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedPrecondition;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedSource;
import com.thinkbiganalytics.metadata.rest.model.feed.InitializationStatus;
import com.thinkbiganalytics.metadata.rest.model.op.DataOperation;
import com.thinkbiganalytics.metadata.rest.model.op.Dataset;
import com.thinkbiganalytics.metadata.rest.model.op.FeedOperation;
import com.thinkbiganalytics.metadata.rest.model.op.HiveTablePartitions;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Convenience functions and methods to transform between the metadata domain model and the REST model. 
 * @author Sean Felten
 */
public class Model {

    private Model() { }
    

    
    public static final Function<com.thinkbiganalytics.metadata.api.feed.InitializationStatus, InitializationStatus> DOMAIN_TO_INIT_STATUS 
        = new Function<com.thinkbiganalytics.metadata.api.feed.InitializationStatus, InitializationStatus>() {
            @Override
            public InitializationStatus apply(com.thinkbiganalytics.metadata.api.feed.InitializationStatus domain) {
                InitializationStatus status = new InitializationStatus();
                status.setState(InitializationStatus.State.valueOf(domain.getState().name()));
                status.setTimestamp(domain.getTimestamp());
                return status;
            }
        };
    
    public static final Function<com.thinkbiganalytics.metadata.api.feed.Feed, Feed> DOMAIN_TO_FEED 
        = new Function<com.thinkbiganalytics.metadata.api.feed.Feed, Feed>() {
            @Override
            public Feed apply(com.thinkbiganalytics.metadata.api.feed.Feed domain) {
                Feed feed = new Feed();
                feed.setId(domain.getId().toString());
                feed.setSystemName(domain.getName());
                feed.setDisplayName(domain.getDisplayName());
                feed.setDescription(domain.getDescription());
                feed.setState(Feed.State.valueOf(domain.getState().name()));
                feed.setCreatedTime(domain.getCreatedTime());
                feed.setCurrentInitStatus(DOMAIN_TO_INIT_STATUS.apply(domain.getCurrentInitStatus()));
                if(domain.getCategory() != null){
                    feed.setCategory(DOMAIN_TO_FEED_CATEGORY.apply(domain.getCategory()));
                }

//                feed.setPrecondition();
//                feed.setOwner();
                @SuppressWarnings("unchecked")
                Collection<FeedSource> sources = Collections2.transform(domain.getSources(), DOMAIN_TO_FEED_SOURCE);
                feed.setSources(new HashSet<FeedSource>(sources));
                @SuppressWarnings("unchecked")
                Collection<FeedDestination> destinations =Collections2.transform(domain.getDestinations(), DOMAIN_TO_FEED_DESTINATION) ;
                feed.setDestinations(new HashSet<FeedDestination>(destinations));
                
                for (Entry<String, Object> entry : domain.getProperties().entrySet()) {
                    if (entry.getValue() != null) {
                        feed.getProperties().setProperty(entry.getKey(), entry.getValue().toString());
                    }
                }
                
                return feed;
            }
        };
        
    public static final Function<com.thinkbiganalytics.metadata.api.feed.FeedSource, FeedSource> DOMAIN_TO_FEED_SOURCE
        = new Function<com.thinkbiganalytics.metadata.api.feed.FeedSource, FeedSource>() {
            @Override
            public FeedSource apply(com.thinkbiganalytics.metadata.api.feed.FeedSource domain) {
                FeedSource src = new FeedSource();
//                src.setLastLoadTime();
//                src.setDatasourceId(domain.getDataset().getId().toString());
                src.setDatasource(DOMAIN_TO_DS.apply(domain.getDatasource()));
                return src;
            }
        };
    
    public static final Function<com.thinkbiganalytics.metadata.api.feed.FeedDestination, FeedDestination> DOMAIN_TO_FEED_DESTINATION
        = new Function<com.thinkbiganalytics.metadata.api.feed.FeedDestination, FeedDestination>() {
            @Override
            public FeedDestination apply(com.thinkbiganalytics.metadata.api.feed.FeedDestination domain) {
                FeedDestination dest = new FeedDestination();
//                dest.setFieldsPolicy();
//                dest.setDatasourceId(domain.getDataset().getId().toString());
                dest.setDatasource(DOMAIN_TO_DS.apply(domain.getDatasource()));
                return dest;
            }
        };
        
        public static final Function<com.thinkbiganalytics.metadata.api.feed.FeedPrecondition, FeedPrecondition> DOMAIN_TO_FEED_PRECOND
        = new Function<com.thinkbiganalytics.metadata.api.feed.FeedPrecondition, FeedPrecondition>() {
            @Override
            public FeedPrecondition apply(com.thinkbiganalytics.metadata.api.feed.FeedPrecondition domain) {
                FeedPrecondition precond = new FeedPrecondition();
//                precond.setMetrics(DOMAIN_TO_METRICS.apply(domain.getMetrics()));
                return precond;
            }
        };
    
    public static final Function<com.thinkbiganalytics.metadata.api.datasource.Datasource, Datasource> DOMAIN_TO_DS
        = new Function<com.thinkbiganalytics.metadata.api.datasource.Datasource, Datasource>() {
            @Override
            public Datasource apply(com.thinkbiganalytics.metadata.api.datasource.Datasource domain) {
                // TODO Is there a better way?
                if (domain instanceof com.thinkbiganalytics.metadata.api.datasource.filesys.DirectoryDatasource) {
                    return DOMAIN_TO_DIR_DS.apply((com.thinkbiganalytics.metadata.api.datasource.filesys.DirectoryDatasource) domain);
                } else if (domain instanceof com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableDatasource) {
                    return DOMAIN_TO_TABLE_DS.apply((com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableDatasource) domain);
                } else {
                    Datasource ds = new Datasource();
                    ds.setId(domain.getId().toString());
                    ds.setName(domain.getName());
                    ds.setDescription(domain.getDescription());
//                    ds.setOwnder();
//                    ds.setEncrypted();
//                    ds.setCompressed();
                    addConnections(domain, ds);
                    return ds;
                }
            }
        };

    public static final Function<com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableDatasource, HiveTableDatasource> DOMAIN_TO_TABLE_DS
        = new Function<com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableDatasource, HiveTableDatasource>() {
            @Override
            public HiveTableDatasource apply(com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableDatasource domain) {
                HiveTableDatasource table = new HiveTableDatasource();
                table.setId(domain.getId().toString());
                table.setName(domain.getName());
                table.setDescription(domain.getDescription());
//                table.setOwnder();
//                table.setEncrypted();
//                table.setCompressed();
                table.setDatabase(domain.getDatabaseName());
                table.setTableName(domain.getTableName());
//                table.setFields();
//                table.setPartitions();
                addConnections(domain, table);
                
                return table;
            }
        };
    
    public static final Function<com.thinkbiganalytics.metadata.api.datasource.filesys.DirectoryDatasource, DirectoryDatasource> DOMAIN_TO_DIR_DS
        = new Function<com.thinkbiganalytics.metadata.api.datasource.filesys.DirectoryDatasource, DirectoryDatasource>() {
            @Override
            public DirectoryDatasource apply(com.thinkbiganalytics.metadata.api.datasource.filesys.DirectoryDatasource domain) {
                DirectoryDatasource dir = new DirectoryDatasource();
                dir.setId(domain.getId().toString());
                dir.setName(domain.getName());
                dir.setDescription(domain.getDescription());
//                dir.setOwnder();
//                dir.setEncrypted();
//                dir.setCompressed();
                dir.setPath(domain.getDirectory().toString());
                addConnections(domain, dir);
                
                return dir;
            }
        };
        
    public static final Function<com.thinkbiganalytics.metadata.api.op.FeedOperation, FeedOperation> DOMAIN_TO_FEED_OP
        = new Function<com.thinkbiganalytics.metadata.api.op.FeedOperation, FeedOperation>() {
            @Override
            public FeedOperation apply(com.thinkbiganalytics.metadata.api.op.FeedOperation domain) {
                FeedOperation op = new FeedOperation();
                op.setOperationId(domain.getId().toString());
                op.setStartTime(domain.getStartTime());
                op.setStopTime(domain.getStopTime());
                op.setState(FeedOperation.State.valueOf(domain.getState().name()));
                op.setStatus(domain.getStatus());
                op.setResults(domain.getResults().entrySet().stream()
                                  .collect(Collectors.toMap(Map.Entry::getKey,
                                                            e -> e.getValue().toString())));
                
                return op;
            }
        };

    public static final Function<com.thinkbiganalytics.metadata.api.op.DataOperation, DataOperation> DOMAIN_TO_DS_OP
        = new Function<com.thinkbiganalytics.metadata.api.op.DataOperation, DataOperation>() {
            @Override
            public DataOperation apply(com.thinkbiganalytics.metadata.api.op.DataOperation domain) {
                DataOperation op = new DataOperation();
                op.setId(domain.getId().toString());
                op.setStartTime(Formatters.print(domain.getStartTime()));
                op.setStopTiime(Formatters.print(domain.getStopTime()));
                op.setState(DataOperation.State.valueOf(domain.getState().name()));
                op.setStatus(domain.getStatus());
                if (domain.getDataset() != null) op.setDataset(DOMAIN_TO_DATASET.apply(domain.getDataset()));
                
                return op;
            }
        };
    

    public static final Function<com.thinkbiganalytics.metadata.api.op.Dataset<com.thinkbiganalytics.metadata.api.datasource.Datasource, ChangeSet>, Dataset> DOMAIN_TO_DATASET
        = new Function<com.thinkbiganalytics.metadata.api.op.Dataset<com.thinkbiganalytics.metadata.api.datasource.Datasource, ChangeSet>, Dataset>() {
            @Override
            public Dataset apply(com.thinkbiganalytics.metadata.api.op.Dataset<com.thinkbiganalytics.metadata.api.datasource.Datasource, ChangeSet> domain) {
                Datasource src = DOMAIN_TO_DS.apply(domain.getDatasource());
                com.thinkbiganalytics.metadata.rest.model.op.Dataset ds = new com.thinkbiganalytics.metadata.rest.model.op.Dataset();
                List<com.thinkbiganalytics.metadata.rest.model.op.ChangeSet> changeSets 
                    = new ArrayList<>(Collections2.transform(domain.getChanges(), DOMAIN_TO_CHANGESET));
                ds.setChangeSets(changeSets); 
                ds.setDatasource(src);
                return ds;
            }
        };

    public static final Function<Category,FeedCategory> DOMAIN_TO_FEED_CATEGORY = new Function<Category, FeedCategory>() {
        @Override
        public FeedCategory apply(Category category) {
           FeedCategory feedCategory = new FeedCategory();
            feedCategory.setId(category.getId().toString());
            feedCategory.setSystemName(category.getName());
            feedCategory.setDisplayName(category.getDisplayName());
            feedCategory.setDescription(category.getDescription());
            return feedCategory;
        }
    };


        
    public static final Function<ChangeSet, com.thinkbiganalytics.metadata.rest.model.op.ChangeSet> DOMAIN_TO_CHANGESET
        = new Function<ChangeSet, com.thinkbiganalytics.metadata.rest.model.op.ChangeSet>() {
            @Override
            public com.thinkbiganalytics.metadata.rest.model.op.ChangeSet apply(ChangeSet domain) {
                com.thinkbiganalytics.metadata.rest.model.op.ChangeSet cs;// = new com.thinkbiganalytics.metadata.rest.model.op.ChangeSet();
                
                if (domain instanceof FileList) {
                    FileList domainFl = (FileList) domain;
                    com.thinkbiganalytics.metadata.rest.model.op.FileList fl = new com.thinkbiganalytics.metadata.rest.model.op.FileList();
                    for (Path path : domainFl.getFilePaths()) {
                        fl.addPath(path.toString());
                    }
                    cs = fl;
                } else if (domain instanceof HiveTableUpdate) {
                    HiveTableUpdate domainHt = (HiveTableUpdate) domain;
                    HiveTablePartitions parts = new HiveTablePartitions();
                    List<HiveTablePartition> partList = new ArrayList<>(Collections2.transform(domainHt.getPartitions(), DOMAIN_TO_PARTITION));
                    parts.setPartitions(partList);
                    cs = parts;
                } else {
                    cs = new com.thinkbiganalytics.metadata.rest.model.op.ChangeSet();
                }
                
                cs.setIncompletenessFactor(domain.getCompletenessFactor());
                cs.setIntrinsicTime(domain.getIntrinsicTime());
                
                if (domain.getIntrinsicPeriod() != null) {
                    cs.setIntrinsicPeriod(Formatters.PERIOD_FORMATTER.print(domain.getIntrinsicPeriod()));
                }
                
                return cs;
            }
        };
        
    public static final Function<HivePartitionUpdate, HiveTablePartition> DOMAIN_TO_PARTITION
        = new Function<HivePartitionUpdate, HiveTablePartition>() {
            @Override
            public HiveTablePartition apply(HivePartitionUpdate domain) {
                HiveTablePartition part = new HiveTablePartition();
                part.setName(domain.getColumnName());
                for (Serializable ser : domain.getValues()) {
                    part.addValue(ser.toString());
                }
                return part;
            }
        };


    public static final Function<DataOperation.State, com.thinkbiganalytics.metadata.api.op.DataOperation.State> OP_STATE_TO_DOMAIN
        = new Function<DataOperation.State, com.thinkbiganalytics.metadata.api.op.DataOperation.State>() {
        @Override
        public com.thinkbiganalytics.metadata.api.op.DataOperation.State apply(DataOperation.State input) {
            return com.thinkbiganalytics.metadata.api.op.DataOperation.State.valueOf(input.name());
        }
    };

    protected static void addConnections(com.thinkbiganalytics.metadata.api.datasource.Datasource domain, Datasource datasource) {
        for (com.thinkbiganalytics.metadata.api.feed.FeedSource domainSrc : domain.getFeedSources()) {
            Feed feed = new Feed();
            feed.setId(domainSrc.getFeed().getId().toString());
            feed.setSystemName(domainSrc.getFeed().getName());

            datasource.getSourceForFeeds().add(feed);
        }
        for (com.thinkbiganalytics.metadata.api.feed.FeedDestination domainDest : domain.getFeedDestinations()) {
            Feed feed = new Feed();
            feed.setId(domainDest.getFeed().getId().toString());
            feed.setSystemName(domainDest.getFeed().getName());

            datasource.getDestinationForFeeds().add(feed);
        }
    }


    public static com.thinkbiganalytics.metadata.api.feed.Feed updateDomain(Feed feed, com.thinkbiganalytics.metadata.api.feed.Feed domain) {
        domain.setDisplayName(feed.getDisplayName());
        domain.setDescription(feed.getDescription());
        domain.setState(State.valueOf(feed.getState().name()));
        return domain;
    }
        
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
