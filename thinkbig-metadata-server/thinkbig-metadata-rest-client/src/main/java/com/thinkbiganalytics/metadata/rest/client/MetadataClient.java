/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.client;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.joda.time.DateTime;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.thinkbiganalytics.metadata.rest.model.data.Datasource;
import com.thinkbiganalytics.metadata.rest.model.data.DirectoryDatasource;
import com.thinkbiganalytics.metadata.rest.model.data.HiveTableDatasource;
import com.thinkbiganalytics.metadata.rest.model.data.HiveTableField;
import com.thinkbiganalytics.metadata.rest.model.data.HiveTablePartition;
import com.thinkbiganalytics.metadata.rest.model.feed.Feed;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedDestination;
import com.thinkbiganalytics.metadata.rest.model.feed.PreconditonTrigger;
import com.thinkbiganalytics.metadata.rest.model.sla.Metric;

/**
 *
 * @author Sean Felten
 */
public class MetadataClient extends JerseyClient {
    
    public static final GenericType<List<Feed>> FEED_LIST = new GenericType<List<Feed>>() { };
    public static final GenericType<List<Datasource>> DATASOURCE_LIST = new GenericType<List<Datasource>>() { };
    
    private static final Function<WebTarget, WebTarget> EVERYTHING = new TargetDatasourceCriteria();
    
    private WebTarget baseTarget;
    
    public MetadataClient(URI base) {
        super();
        this.baseTarget = target(base);
        
        register(JacksonFeature.class);
    }
    
    public FeedBuilder buildFeed(String name) {
        return new FeedBuilderImpl(name);
    }
    
    public FeedDestination addDestination(Feed feed, String datasourceId) {
        
    }

    public FeedCriteria feedCriteria() {
        return new TargetFeedCriteria();
    }
    
    public List<Feed> getFeeds() {
        return get("feed", EVERYTHING, FEED_LIST);
    }
    
    public List<Feed> getFeeds(FeedCriteria criteria) {
        try {
            return get("feed", (TargetFeedCriteria) criteria, FEED_LIST);
        } catch (ClassCastException e) {
            throw new IllegalThreadStateException("Unknown criteria type: " + criteria.getClass());
        }
    }

    public DirectoryDatasourceBuilder buildDirectoryDatasource(String name) {
        return new DirectoryDatasourceBuilderImpl(name);
    }
    
    public HiveTableDatasourceBuilder buildHiveTableDatasource(String name) {
        return new HiveTableDatasourceBuilderImpl(name);
    }
    
    public DatasourceCriteria datasourceCriteria() {
        return new TargetDatasourceCriteria();
    }

    public List<Datasource> getDatasources() {
        return get("datasource", EVERYTHING, DATASOURCE_LIST);
    }
    
    public List<Datasource> getDatasources(DatasourceCriteria criteria) {
        try {
            return get("datasource", (TargetDatasourceCriteria) criteria, DATASOURCE_LIST);
        } catch (ClassCastException e) {
            throw new IllegalThreadStateException("Unknown criteria type: " + criteria.getClass());
        }
    }

    private PreconditonTrigger createTrigger(List<Metric> metrics) {
        PreconditonTrigger trigger = new PreconditonTrigger();
        trigger.setMetrics(metrics);
        return null;
    }

    private Feed postFeed(Feed feed) {
        return post("feed", feed, Feed.class);
    }
    
    private HiveTableDatasource postDatasource(HiveTableDatasource ds) {
        return post("datasource/hivetable", ds, HiveTableDatasource.class);
    }
    
    private DirectoryDatasource postDatasource(DirectoryDatasource ds) {
        return post("datasource/directory", ds, DirectoryDatasource.class);
    }
    
    private <R> R get(String path, Function<WebTarget, WebTarget> funct, Class<R> resultType) {
        return funct.apply(this.baseTarget)
                .path(path)
                .request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(resultType);
    }
    
    private <R> R get(String path, Function<WebTarget, WebTarget> funct, GenericType<R> resultType) {
        return funct.apply(this.baseTarget)
                .path(path)
                .request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(resultType);
    }
    
    private <R> R post(String path, Object body, Class<R> resultType) {
        return this.baseTarget
                .path(path)
                .request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .buildPost(Entity.entity(body, MediaType.APPLICATION_JSON_TYPE))
                .invoke(resultType);
    }

    private class FeedBuilderImpl implements FeedBuilder {
        private String feedName;
        private String systemName;
        private String description;
        private String owner;
        private List<Metric> preconditionMetrics = new ArrayList<Metric>();
    
        public FeedBuilderImpl(String name) {
            this.feedName = name;
        }
    
        @Override
        public FeedBuilder systemName(String name) {
            this.systemName = name;
            return this;
        }
    
        @Override
        public FeedBuilder description(String descr) {
            this.description = descr;
            return this;
        }
    
        @Override
        public FeedBuilder owner(String owner) {
            this.owner = owner;
            return this;
        }
    
        @Override
        public FeedBuilder preconditionMetric(Metric... metrics) {
            for (Metric m : metrics) {
                this.preconditionMetrics.add(m);
            }
            return this;
        }
    
        @Override
        public Feed build() {
            Feed feed = new Feed();
            feed.setDisplayName(this.feedName);
            feed.setSystemName(this.systemName);
            feed.setDescription(this.description);
            feed.setOwner(this.owner);
            feed.setTrigger(createTrigger(this.preconditionMetrics));
            
            return feed;
        }
        
        @Override
        public Feed post() {
            Feed feed = build();
            return postFeed(feed);
        }
    
    }

    private abstract class DatasourceBuilderImpl<B extends DatasourceBuilder<B, D>, D extends Datasource> implements DatasourceBuilder<B, D> {
        protected String name;
        protected String description;
        protected String owner;
        protected boolean encrypted;
        protected boolean compressed;
        
        public DatasourceBuilderImpl(String name) {
            this.name = name;
        }
        
        @Override
        public B description(String descr) {
            this.description = descr;
            return self();
        }

        @Override
        public B ownder(String owner) {
            this.owner = owner;
            return self();
        }

        @Override
        public B encrypted(boolean flag) {
            this.encrypted = flag;
            return self();
        }

        @Override
        public B compressed(boolean flag) {
            this.compressed = flag;
            return self();
        }

        @SuppressWarnings("unchecked")
        private B self() {
            return (B) this;
        }
    }
    
    private class HiveTableDatasourceBuilderImpl
            extends DatasourceBuilderImpl<HiveTableDatasourceBuilder, HiveTableDatasource>
            implements HiveTableDatasourceBuilder {
        
        private String database;
        private String tableName;
        private String modifiers;
        private List<HiveTableField> fields = new ArrayList<>(); 
        private List<HiveTablePartition> partitions = new ArrayList<>();

        public HiveTableDatasourceBuilderImpl(String name) {
            super(name);
        }

        @Override
        public HiveTableDatasourceBuilder database(String name) {
            this.database = name;
            return this;
        }

        @Override
        public HiveTableDatasourceBuilder tableName(String name) {
            this.tableName = name;
            return this;
        }

        @Override
        public HiveTableDatasourceBuilder modifiers(String mods) {
            this.modifiers = mods;
            return this;
        }

        @Override
        public HiveTableDatasourceBuilder field(String name, String type) {
            this.fields.add(new HiveTableField(name, type));
            return this;
        }

        @Override
        public HiveTableDatasourceBuilder partition(String name, String formula, String value, String... more) {
            this.partitions.add(new HiveTablePartition(name, formula, value, more));
            return this;
        }

        @Override
        public HiveTableDatasource build() {
            HiveTableDatasource src = new HiveTableDatasource();
            src.setName(this.name);
            src.setDescription(this.description);
            src.setOwnder(this.owner);
            src.setEncrypted(this.encrypted);
            src.setCompressed(this.compressed);
            src.setDatabase(this.database);
            src.setTableName(this.tableName);
            src.setModifiers(this.modifiers);
            src.getFields().addAll(this.fields);
            src.getPartitions().addAll(this.partitions);

            return src;
        }

        @Override
        public HiveTableDatasource post() {
            HiveTableDatasource ds = build();
            return postDatasource(ds);
        }
    }
    
    private class DirectoryDatasourceBuilderImpl 
            extends DatasourceBuilderImpl<DirectoryDatasourceBuilder, DirectoryDatasource> 
            implements DirectoryDatasourceBuilder {
    
        private String path;
        private List<String> regexList = new ArrayList<>();
        private List<String> globList = new ArrayList<>();
        
        public DirectoryDatasourceBuilderImpl(String name) {
            super(name);
        }
    
        @Override
        public DirectoryDatasourceBuilder path(String path) {
            this.path = path;
            return this;
        }
    
        @Override
        public DirectoryDatasourceBuilder regexPattern(String pattern) {
            this.regexList.add(pattern);
            return this;
        }
    
        @Override
        public DirectoryDatasourceBuilder globPattern(String pattern) {
            this.globList.add(pattern);
            return this;
        }
        
        @Override
        public DirectoryDatasource build() {
            DirectoryDatasource src = new DirectoryDatasource();
            src.setName(this.name);
            src.setDescription(this.description);
            src.setOwnder(this.owner);
            src.setEncrypted(this.encrypted);
            src.setCompressed(this.compressed);
            src.setPath(this.path);
            
            for (String p : this.regexList) {
                src.addRegexPattern(p);
            }
            
            for (String p : this.globList) {
                src.addGlobPattern(p);
            }
            
            return src;
        }
    
        @Override
        public DirectoryDatasource post() {
            DirectoryDatasource dds = build();
            return postDatasource(dds);
        }
        
    }

    private static class TargetDatasourceCriteria implements DatasourceCriteria, Function<WebTarget, WebTarget> {
        
        public static final String NAME = "name";
        public static final String OWNER = "owner";
        public static final String ON = "on";
        public static final String AFTER = "after";
        public static final String BEFORE = "before";
        public static final String TYPE = "type";

        private String name;
        private String owner;
        private DateTime createdOn;
        private DateTime createdAfter;
        private DateTime createdBefore;
        private Set<String> types = new HashSet<>();
        
        public WebTarget apply(WebTarget target) {
            WebTarget result = target;
            
            if (! Strings.isNullOrEmpty(this.name)) result = result.queryParam(NAME, this.name);
            if (! Strings.isNullOrEmpty(this.owner)) result = result.queryParam(OWNER, this.owner);
            if (this.createdOn != null) result = result.queryParam(ON, this.createdOn);
            if (this.createdAfter != null) result = result.queryParam(AFTER, this.createdAfter);
            if (this.createdBefore != null) result = result.queryParam(BEFORE, this.createdBefore);
            if (! this.types.isEmpty()) result = result.queryParam(TYPE, types.toArray(new Object[types.size()]));
            
            return result;
        }
        
        /* (non-Javadoc)
         * @see com.thinkbiganalytics.metadata.rest.client.DatasourceCriteria#name(java.lang.String)
         */
        @Override
        public DatasourceCriteria name(String name) {
            this.name = name;
            return this;
        }

        /* (non-Javadoc)
         * @see com.thinkbiganalytics.metadata.rest.client.DatasourceCriteria#createdOn(org.joda.time.DateTime)
         */
        @Override
        public DatasourceCriteria createdOn(DateTime time) {
            this.createdOn = time;
            return this;
        }

        /* (non-Javadoc)
         * @see com.thinkbiganalytics.metadata.rest.client.DatasourceCriteria#createdAfter(org.joda.time.DateTime)
         */
        @Override
        public DatasourceCriteria createdAfter(DateTime time) {
            this.createdAfter = time;
            return this;
        }

        /* (non-Javadoc)
         * @see com.thinkbiganalytics.metadata.rest.client.DatasourceCriteria#createdBefore(org.joda.time.DateTime)
         */
        @Override
        public DatasourceCriteria createdBefore(DateTime time) {
            this.createdBefore = time;
            return this;
        }

        /* (non-Javadoc)
         * @see com.thinkbiganalytics.metadata.rest.client.DatasourceCriteria#owner(java.lang.String)
         */
        @Override
        public DatasourceCriteria owner(String owner) {
            this.owner = owner;
            return this;
        }

        /* (non-Javadoc)
         * @see com.thinkbiganalytics.metadata.rest.client.DatasourceCriteria#type(java.lang.Class, java.lang.Class[])
         */
        @Override
        @SuppressWarnings("unchecked")
        public DatasourceCriteria type(Class<? extends Datasource> type, Class<? extends Datasource>... others) {
            this.types.add(type.getSimpleName());
            for (Class<? extends Datasource> t : others) {
                this.types.add(t.getSimpleName());
            }
            return this;
        }
    }

    private static class TargetFeedCriteria implements FeedCriteria, Function<WebTarget, WebTarget> {
        
        public static final String NAME = "name";
        public static final String SRC_ID = "srcid";
        public static final String DEST_ID = "destid";

        private String name;
        private String sourceId;
        private String destinationId;

        public WebTarget apply(WebTarget target) {
            WebTarget result = target;
            
            if (! Strings.isNullOrEmpty(this.name)) result = result.queryParam(NAME, this.name);
            if (! Strings.isNullOrEmpty(this.sourceId)) result = result.queryParam(SRC_ID, this.name);
            if (! Strings.isNullOrEmpty(this.destinationId)) result = result.queryParam(DEST_ID, this.name);
            
            return result;
        }

        /* (non-Javadoc)
         * @see com.thinkbiganalytics.metadata.rest.client.FeedCriteria#sourceDatasource(java.lang.String)
         */
        @Override
        public FeedCriteria sourceDatasource(String dsId) {
            this.sourceId = dsId;
            return this;
        }

        /* (non-Javadoc)
         * @see com.thinkbiganalytics.metadata.rest.client.FeedCriteria#destinationDatasource(java.lang.String)
         */
        @Override
        public FeedCriteria destinationDatasource(String dsId) {
            this.destinationId = dsId;
            return this;
        }

        /* (non-Javadoc)
         * @see com.thinkbiganalytics.metadata.rest.client.FeedCriteria#name(java.lang.String)
         */
        @Override
        public FeedCriteria name(String name) {
            this.name = name;
            return this;
        }
    }

}
