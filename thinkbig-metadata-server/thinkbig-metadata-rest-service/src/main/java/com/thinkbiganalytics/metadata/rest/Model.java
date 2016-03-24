/**
 * 
 */
package com.thinkbiganalytics.metadata.rest;

import java.io.Serializable;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.joda.time.Period;
import org.quartz.CronExpression;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.api.dataset.filesys.DirectoryDataset;
import com.thinkbiganalytics.metadata.api.dataset.filesys.FileList;
import com.thinkbiganalytics.metadata.api.dataset.hive.HivePartitionUpdate;
import com.thinkbiganalytics.metadata.api.dataset.hive.HiveTableDataset;
import com.thinkbiganalytics.metadata.api.dataset.hive.HiveTableUpdate;
import com.thinkbiganalytics.metadata.api.feed.precond.DatasetUpdatedSinceMetric;
import com.thinkbiganalytics.metadata.api.op.ChangeSet;
import com.thinkbiganalytics.metadata.api.op.ChangedContent;
import com.thinkbiganalytics.metadata.rest.model.Formatters;
import com.thinkbiganalytics.metadata.rest.model.data.Datasource;
import com.thinkbiganalytics.metadata.rest.model.data.DirectoryDatasource;
import com.thinkbiganalytics.metadata.rest.model.data.HiveTableDatasource;
import com.thinkbiganalytics.metadata.rest.model.data.HiveTablePartition;
import com.thinkbiganalytics.metadata.rest.model.feed.Feed;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedDestination;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedPrecondition;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedSource;
import com.thinkbiganalytics.metadata.rest.model.op.DataOperation;
import com.thinkbiganalytics.metadata.rest.model.op.HiveTablePartitions;
import com.thinkbiganalytics.metadata.rest.model.sla.DatasourceUpdatedSinceFeedExecutedMetric;
import com.thinkbiganalytics.metadata.rest.model.sla.DatasourceUpdatedSinceScheduleMetric;
import com.thinkbiganalytics.metadata.rest.model.sla.FeedExecutedSinceFeedMetric;
import com.thinkbiganalytics.metadata.rest.model.sla.FeedExecutedSinceScheduleMetric;
import com.thinkbiganalytics.metadata.rest.model.sla.Metric;
import com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAssessment.Result;
import com.thinkbiganalytics.metadata.rest.model.sla.WithinSchedule;
import com.thinkbiganalytics.metadata.sla.api.MetricAssessment;
import com.thinkbiganalytics.metadata.sla.api.Obligation;
import com.thinkbiganalytics.metadata.sla.api.ObligationAssessment;
import com.thinkbiganalytics.metadata.sla.api.ObligationGroup;
import com.thinkbiganalytics.metadata.sla.api.ObligationGroup.Condition;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment;

/**
 * Convenience functions and methods to transform between the metadata domain model and the REST model. 
 * @author Sean Felten
 */
public class Model {

    private Model() { }
    
    /*
    com.thinkbiganalytics.metadata.rest.model.sla.DatasourceUpdatedSinceScheduleMetric
    com.thinkbiganalytics.metadata.rest.model.sla.FeedExecutedSinceFeedMetric
    com.thinkbiganalytics.metadata.rest.model.sla.FeedExecutedSinceScheduleMetric
    com.thinkbiganalytics.metadata.rest.model.sla.WithinSchedule
    
    com.thinkbiganalytics.metadata.api.feed.precond.DatasetUpdatedSinceMetric
    com.thinkbiganalytics.metadata.api.feed.precond.FeedExecutedSinceFeedMetric
    com.thinkbiganalytics.metadata.api.feed.precond.FeedExecutedSinceScheduleMetric
    com.thinkbiganalytics.metadata.api.feed.precond.WithinSchedule
     */
    public static final Map<Class<? extends Metric>, Function<Metric, com.thinkbiganalytics.metadata.sla.api.Metric>> METRIC_TO_DOMAIN_MAP;
    static {
        Map<Class<? extends Metric>, Function<Metric, com.thinkbiganalytics.metadata.sla.api.Metric>> map = new HashMap<>();
        map.put(WithinSchedule.class, new Function<Metric, com.thinkbiganalytics.metadata.sla.api.Metric>() {
            @Override
            public com.thinkbiganalytics.metadata.sla.api.Metric apply(Metric model) {
                WithinSchedule cast = (WithinSchedule) model;
                try {
                    CronExpression cronExpression = new CronExpression(cast.getCronSchedule());
                    Period period = Formatters.PERIOD_FORMATTER.parsePeriod(cast.getPeriod()); 
                    
                    return new com.thinkbiganalytics.metadata.api.feed.precond.WithinSchedule(cronExpression, period);
                } catch (ParseException e) {
                    throw new WebApplicationException("Invalid cron and/or period expression provided for schedule: " + 
                            cast.getCronSchedule() + " / " + cast.getPeriod(), Status.BAD_REQUEST);
            }
            }
        });
        map.put(FeedExecutedSinceFeedMetric.class, new Function<Metric, com.thinkbiganalytics.metadata.sla.api.Metric>() {
            @Override
            public com.thinkbiganalytics.metadata.sla.api.Metric apply(Metric model) {
                FeedExecutedSinceFeedMetric cast = (FeedExecutedSinceFeedMetric) model;
                return new com.thinkbiganalytics.metadata.api.feed.precond.FeedExecutedSinceFeedMetric(cast.getDependentFeedName(), 
                                                                                                       cast.getSinceFeedName());
            }
        });
        map.put(FeedExecutedSinceScheduleMetric.class, new Function<Metric, com.thinkbiganalytics.metadata.sla.api.Metric>() {
            @Override
            public com.thinkbiganalytics.metadata.sla.api.Metric apply(Metric model) {
                FeedExecutedSinceScheduleMetric cast = (FeedExecutedSinceScheduleMetric) model;
                try {
                    return new com.thinkbiganalytics.metadata.api.feed.precond.FeedExecutedSinceScheduleMetric(cast.getDependentFeedName(), 
                                                                                                               cast.getCronSchedule());
                } catch (ParseException e) {
                    throw new WebApplicationException("Invalid cron expression provided for feed execution schedule: " + 
                            cast.getCronSchedule(), Status.BAD_REQUEST);
            }
            }
        });
        map.put(DatasourceUpdatedSinceFeedExecutedMetric.class, new Function<Metric, com.thinkbiganalytics.metadata.sla.api.Metric>() {
            @Override
            public com.thinkbiganalytics.metadata.sla.api.Metric apply(Metric model) {
                DatasourceUpdatedSinceFeedExecutedMetric cast = (DatasourceUpdatedSinceFeedExecutedMetric) model;
                return new com.thinkbiganalytics.metadata.api.feed.precond.DatasourceUpdatedSinceFeedExecutedMetric(cast.getDatasourceName(),
                        cast.getFeedName());
            }
        });
        map.put(DatasourceUpdatedSinceScheduleMetric.class, new Function<Metric, com.thinkbiganalytics.metadata.sla.api.Metric>() {
            @Override
            public com.thinkbiganalytics.metadata.sla.api.Metric apply(Metric model) {
                DatasourceUpdatedSinceScheduleMetric cast = (DatasourceUpdatedSinceScheduleMetric) model;
                try {
                    return new DatasetUpdatedSinceMetric(cast.getDatasourceName(), cast.getCronSchedule());
                } catch (ParseException e) {
                    throw new WebApplicationException("Invalid cron expression provided for datasource update schedule: " + 
                                cast.getCronSchedule(), Status.BAD_REQUEST);
                }
            }
        });
        
        METRIC_TO_DOMAIN_MAP = map;
    }
    
    public static final Map<Class<? extends com.thinkbiganalytics.metadata.sla.api.Metric>, Function<com.thinkbiganalytics.metadata.sla.api.Metric, Metric>> DOMAIN_TO_METRIC_MAP;
    static {
        Map<Class<? extends com.thinkbiganalytics.metadata.sla.api.Metric>, Function<com.thinkbiganalytics.metadata.sla.api.Metric, Metric>> map = new HashMap<>();
        map.put(com.thinkbiganalytics.metadata.api.feed.precond.WithinSchedule.class, new Function<com.thinkbiganalytics.metadata.sla.api.Metric, Metric>() {
            @Override
            public Metric apply(com.thinkbiganalytics.metadata.sla.api.Metric domain) {
                com.thinkbiganalytics.metadata.api.feed.precond.WithinSchedule cast = (com.thinkbiganalytics.metadata.api.feed.precond.WithinSchedule) domain;
                return new WithinSchedule(cast.getCronExpression().toString(), Formatters.PERIOD_FORMATTER.print(cast.getPeriod()));
            }
        });
        map.put(com.thinkbiganalytics.metadata.api.feed.precond.FeedExecutedSinceFeedMetric.class, new Function<com.thinkbiganalytics.metadata.sla.api.Metric, Metric>() {
            @Override
            public Metric apply(com.thinkbiganalytics.metadata.sla.api.Metric domain) {
                com.thinkbiganalytics.metadata.api.feed.precond.FeedExecutedSinceFeedMetric cast 
                    = (com.thinkbiganalytics.metadata.api.feed.precond.FeedExecutedSinceFeedMetric) domain;
                return FeedExecutedSinceFeedMetric.named(cast.getFeedName(), cast.getSinceName());
            }
        });
        map.put(com.thinkbiganalytics.metadata.api.feed.precond.FeedExecutedSinceScheduleMetric.class, new Function<com.thinkbiganalytics.metadata.sla.api.Metric, Metric>() {
            @Override
            public Metric apply(com.thinkbiganalytics.metadata.sla.api.Metric domain) {
                com.thinkbiganalytics.metadata.api.feed.precond.FeedExecutedSinceScheduleMetric cast 
                    = (com.thinkbiganalytics.metadata.api.feed.precond.FeedExecutedSinceScheduleMetric) domain;
                return FeedExecutedSinceScheduleMetric.named(cast.getFeedName(), cast.getCronExpression().toString());
            }
        });
        map.put(com.thinkbiganalytics.metadata.api.feed.precond.DatasourceUpdatedSinceFeedExecutedMetric.class, new Function<com.thinkbiganalytics.metadata.sla.api.Metric, Metric>() {
            @Override
            public Metric apply(com.thinkbiganalytics.metadata.sla.api.Metric domain) {
                com.thinkbiganalytics.metadata.api.feed.precond.DatasourceUpdatedSinceFeedExecutedMetric cast 
                    = (com.thinkbiganalytics.metadata.api.feed.precond.DatasourceUpdatedSinceFeedExecutedMetric) domain;
                return DatasourceUpdatedSinceFeedExecutedMetric.named(cast.getDatasetName(), cast.getFeedName());
            }
        });
        map.put(DatasetUpdatedSinceMetric.class, new Function<com.thinkbiganalytics.metadata.sla.api.Metric, Metric>() {
            @Override
            public Metric apply(com.thinkbiganalytics.metadata.sla.api.Metric domain) {
                DatasetUpdatedSinceMetric cast = (DatasetUpdatedSinceMetric) domain;
                return DatasourceUpdatedSinceScheduleMetric.named(cast.getDatasetName(), cast.getCronExpression().toString());
            }
        });
        
        DOMAIN_TO_METRIC_MAP = map;
    }
    
    public static final Function<com.thinkbiganalytics.metadata.sla.api.Metric, Metric> DOMAIN_TO_METRIC
        =  new Function<com.thinkbiganalytics.metadata.sla.api.Metric, Metric>() {
            @Override
            public Metric apply(com.thinkbiganalytics.metadata.sla.api.Metric domain) {
                Function<com.thinkbiganalytics.metadata.sla.api.Metric, Metric> func = DOMAIN_TO_METRIC_MAP.get(domain.getClass());
                return func.apply(domain);
            }
        };
    
    public static final Function<Metric, com.thinkbiganalytics.metadata.sla.api.Metric> METRIC_TO_DOMAIN
        =  new Function<Metric, com.thinkbiganalytics.metadata.sla.api.Metric>() {
            @Override
            public com.thinkbiganalytics.metadata.sla.api.Metric apply(Metric model) {
                Function<Metric, com.thinkbiganalytics.metadata.sla.api.Metric> func = METRIC_TO_DOMAIN_MAP.get(model.getClass());
                return func.apply(model);
            }
        };
    
    public static final Function<com.thinkbiganalytics.metadata.api.feed.Feed, Feed> DOMAIN_TO_FEED 
        = new Function<com.thinkbiganalytics.metadata.api.feed.Feed, Feed>() {
            @Override
            public Feed apply(com.thinkbiganalytics.metadata.api.feed.Feed domain) {
                Feed feed = new Feed();
                feed.setId(domain.getId().toString());
                feed.setSystemName(domain.getName());
                feed.setDisplayName(domain.getName());
                feed.setDescription(domain.getDescription());
                feed.setDisplayName(domain.getName());
//                feed.setOwner();
//                feed.setTrigger();
                feed.setSources(new HashSet<>(Collections2.transform(domain.getSources(), DOMAIN_TO_FEED_SOURCE)));
                feed.setDestinations(new HashSet<>(Collections2.transform(domain.getDestinations(), DOMAIN_TO_FEED_DESTINATION)));
                
                return feed;
            }
        };
        
    public static final Function<com.thinkbiganalytics.metadata.api.feed.FeedSource, FeedSource> DOMAIN_TO_FEED_SOURCE
        = new Function<com.thinkbiganalytics.metadata.api.feed.FeedSource, FeedSource>() {
            @Override
            public FeedSource apply(com.thinkbiganalytics.metadata.api.feed.FeedSource domain) {
                FeedSource src = new FeedSource();
                src.setId(domain.getId().toString());
//                src.setLastLoadTime();
//                src.setDatasourceId(domain.getDataset().getId().toString());
                src.setDatasource(DOMAIN_TO_DS.apply(domain.getDataset()));
                return src;
            }
        };
    
    public static final Function<com.thinkbiganalytics.metadata.api.feed.FeedDestination, FeedDestination> DOMAIN_TO_FEED_DESTINATION
        = new Function<com.thinkbiganalytics.metadata.api.feed.FeedDestination, FeedDestination>() {
            @Override
            public FeedDestination apply(com.thinkbiganalytics.metadata.api.feed.FeedDestination domain) {
                FeedDestination dest = new FeedDestination();
                dest.setId(domain.getId().toString());
//                dest.setFieldsPolicy();
//                dest.setDatasourceId(domain.getDataset().getId().toString());
                dest.setDatasource(DOMAIN_TO_DS.apply(domain.getDataset()));
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
    
    public static final Function<Dataset, Datasource> DOMAIN_TO_DS
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

    public static final Function<HiveTableDataset, HiveTableDatasource> DOMAIN_TO_TABLE_DS
        = new Function<HiveTableDataset, HiveTableDatasource>() {
            @Override
            public HiveTableDatasource apply(HiveTableDataset domain) {
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
                
                return table;
            }
        };
    
    public static final Function<DirectoryDataset, DirectoryDatasource> DOMAIN_TO_DIR_DS
        = new Function<DirectoryDataset, DirectoryDatasource>() {
            @Override
            public DirectoryDatasource apply(DirectoryDataset domain) {
                DirectoryDatasource dir = new DirectoryDatasource();
                dir.setId(domain.getId().toString());
                dir.setName(domain.getName());
                dir.setDescription(domain.getDescription());
//                dir.setOwnder();
//                dir.setEncrypted();
//                dir.setCompressed();
                dir.setPath(domain.getDirectory().toString());
                
                return dir;
            }
        };

    public static final Function<com.thinkbiganalytics.metadata.api.op.DataOperation, DataOperation> DOMAIN_TO_OP
        = new Function<com.thinkbiganalytics.metadata.api.op.DataOperation, DataOperation>() {
            @Override
            public DataOperation apply(com.thinkbiganalytics.metadata.api.op.DataOperation domain) {
                DataOperation op = new DataOperation();
                op.setId(domain.getId().toString());
                op.setFeedDestinationId(domain.getProducer().getId().toString());
                op.setStartTime(Formatters.TIME_FORMATTER.print(domain.getStartTime()));
                op.setStopTiime(Formatters.TIME_FORMATTER.print(domain.getStopTime()));
                op.setState(DataOperation.State.valueOf(domain.getState().name()));
                op.setStatus(domain.getStatus());
                if (domain.getChangeSet() != null) op.setDataset(DOMAIN_TO_DATASET.apply(domain.getChangeSet()));
                
                return op;
            }
        };
    

    public static final Function<ChangeSet<Dataset, ChangedContent>, com.thinkbiganalytics.metadata.rest.model.op.Dataset> DOMAIN_TO_DATASET
        = new Function<ChangeSet<Dataset, ChangedContent>, com.thinkbiganalytics.metadata.rest.model.op.Dataset>() {
            @Override
            public com.thinkbiganalytics.metadata.rest.model.op.Dataset apply(ChangeSet<Dataset, ChangedContent> domain) {
                Datasource src = DOMAIN_TO_DS.apply(domain.getDataset());
                com.thinkbiganalytics.metadata.rest.model.op.Dataset ds = new com.thinkbiganalytics.metadata.rest.model.op.Dataset();
                List<com.thinkbiganalytics.metadata.rest.model.op.ChangeSet> changeSets 
                    = new ArrayList<>(Collections2.transform(domain.getChanges(), DOMAIN_TO_CHANGESET));
                ds.setChangeSets(changeSets); 
                ds.setDatasource(src);
                return ds;
            }
        };
        
    public static final Function<ChangedContent, com.thinkbiganalytics.metadata.rest.model.op.ChangeSet> DOMAIN_TO_CHANGESET
        = new Function<ChangedContent, com.thinkbiganalytics.metadata.rest.model.op.ChangeSet>() {
            @Override
            public com.thinkbiganalytics.metadata.rest.model.op.ChangeSet apply(ChangedContent domain) {
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
        
    public static final Function<ServiceLevelAgreement, com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAgreement> DOMAIN_TO_SLA
        = new Function<ServiceLevelAgreement, com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAgreement>() {
            @Override
            public com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAgreement apply(ServiceLevelAgreement domain) {
                return toModel(domain, true);
            }
    };
    
    
    
    
    public static final Function<ServiceLevelAssessment, com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAssessment> DOMAIN_TO_SLA_ASSMT
        = new Function<ServiceLevelAssessment, com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAssessment>() {
            @Override
            public com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAssessment apply(ServiceLevelAssessment domain) {
                com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAgreement sla = toModel(domain.getAgreement(), false);
                
                com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAssessment slAssmt 
                    = new com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAssessment(sla,
                                                                                               domain.getTime(), 
//                                                                                               null,
                                                                                               domain.getMessage(), 
                                                                                               Result.valueOf(domain.getResult().name()));
                for (ObligationAssessment domainObAssmt : domain.getObligationAssessments()) {
                    com.thinkbiganalytics.metadata.rest.model.sla.ObligationAssessment obAssmt 
                        = new com.thinkbiganalytics.metadata.rest.model.sla.ObligationAssessment(toModel(domainObAssmt.getObligation(), false), 
                                                                                                 Result.valueOf(domain.getResult().name()), 
                                                                                                 domainObAssmt.getMessage());
                    for (MetricAssessment<?> domainMetAssmt : domainObAssmt.getMetricAssessments()) {
                        com.thinkbiganalytics.metadata.rest.model.sla.MetricAssessment metricAssmnt
                            = new com.thinkbiganalytics.metadata.rest.model.sla.MetricAssessment(DOMAIN_TO_METRIC.apply(domainMetAssmt.getMetric()), 
                                                                                                 Result.valueOf(domain.getResult().name()), 
                                                                                                 domainMetAssmt.getMessage());
                        obAssmt.addMetricAssessment(metricAssmnt);
                    }
                    
                    slAssmt.addObligationAssessment(obAssmt);
                }
                
                return slAssmt;
            }
    };
 
    public static final Function<DataOperation.State, com.thinkbiganalytics.metadata.api.op.DataOperation.State> OP_STATE_TO_DOMAIN
        = new Function<DataOperation.State, com.thinkbiganalytics.metadata.api.op.DataOperation.State>() {
            @Override
            public com.thinkbiganalytics.metadata.api.op.DataOperation.State apply(DataOperation.State input) {
                return com.thinkbiganalytics.metadata.api.op.DataOperation.State.valueOf(input.name());
            }
        };

    
        
    public static List<Metric> toModelMetrics(Collection<com.thinkbiganalytics.metadata.sla.api.Metric> metrics) {
        return new ArrayList<>(Collections2.transform(metrics, DOMAIN_TO_METRIC));
    }
    
    public static Set<com.thinkbiganalytics.metadata.sla.api.Metric> toDomainMetrics(List<Metric> metrics) {
        return new HashSet<>(Collections2.transform(metrics, METRIC_TO_DOMAIN));
    }
    
    public static <D extends com.thinkbiganalytics.metadata.sla.api.Metric> D toDomain(Metric model) {
        @SuppressWarnings("unchecked")
        D result = (D) METRIC_TO_DOMAIN.apply(model);
        return result;
    }
    
    public static com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAgreement toModel(ServiceLevelAgreement domain, boolean deep) {
        com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAgreement sla 
            = new com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAgreement(domain.getId().toString(), 
                                                                                      domain.getName(), 
                                                                                      domain.getDescription());
        if (deep) {
            if (domain.getObligationGroups().size() == 1 && domain.getObligationGroups().get(0).getCondition() == Condition.REQUIRED) {
                for (Obligation domainOb : domain.getObligations()) {
                    com.thinkbiganalytics.metadata.rest.model.sla.Obligation ob = toModel(domainOb, true);
                    sla.addObligation(ob);
                }
            } else {
                for (ObligationGroup domainGroup : domain.getObligationGroups()) {
                    com.thinkbiganalytics.metadata.rest.model.sla.ObligationGroup group 
                        = new com.thinkbiganalytics.metadata.rest.model.sla.ObligationGroup(domainGroup.getCondition().toString());
                    for (Obligation domainOb : domainGroup.getObligations()) {
                        com.thinkbiganalytics.metadata.rest.model.sla.Obligation ob = toModel(domainOb, true);
                        group.addObligation(ob);
                    }
                    
                    sla.addGroup(group);
                }
            }
        }
        
        return sla;
    }

    public static com.thinkbiganalytics.metadata.rest.model.sla.Obligation toModel(Obligation domainOb, boolean deep) {
        com.thinkbiganalytics.metadata.rest.model.sla.Obligation ob 
            = new com.thinkbiganalytics.metadata.rest.model.sla.Obligation();
        ob.setDescription(domainOb.getDescription());
        if (deep) ob.setMetrics(toModelMetrics(domainOb.getMetrics()));
        return ob;
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
