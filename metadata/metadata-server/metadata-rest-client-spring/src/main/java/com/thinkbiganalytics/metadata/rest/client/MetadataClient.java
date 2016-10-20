/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.client;

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.joda.time.DateTime;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.thinkbiganalytics.metadata.rest.model.data.Datasource;
import com.thinkbiganalytics.metadata.rest.model.data.DatasourceCriteria;
import com.thinkbiganalytics.metadata.rest.model.data.DirectoryDatasource;
import com.thinkbiganalytics.metadata.rest.model.data.HiveTableColumn;
import com.thinkbiganalytics.metadata.rest.model.data.HiveTableDatasource;
import com.thinkbiganalytics.metadata.rest.model.data.HiveTablePartition;
import com.thinkbiganalytics.metadata.rest.model.extension.ExtensibleTypeDescriptor;
import com.thinkbiganalytics.metadata.rest.model.feed.Feed;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedCategory;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedCriteria;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedDependencyGraph;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedPrecondition;
import com.thinkbiganalytics.metadata.rest.model.feed.InitializationStatus;
import com.thinkbiganalytics.metadata.rest.model.op.DataOperation;
import com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAssessment;
import com.thinkbiganalytics.metadata.sla.api.Metric;

/**
 *
 * @author Sean Felten
 */
public class MetadataClient {
    
    public static final List<MediaType> ACCEPT_TYPES = Collections.unmodifiableList(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN));
    
    public static final ParameterizedTypeReference<List<ExtensibleTypeDescriptor>> TYPE_LIST = new ParameterizedTypeReference<List<ExtensibleTypeDescriptor>>() { };
    public static final ParameterizedTypeReference<List<Feed>> FEED_LIST = new ParameterizedTypeReference<List<Feed>>() { };
    public static final ParameterizedTypeReference<Map<DateTime, Map<String, String>>> FEED_RESULT_DELTAS = new ParameterizedTypeReference<Map<DateTime, Map<String, String>>>() { };
    public static final ParameterizedTypeReference<List<Datasource>> DATASOURCE_LIST = new ParameterizedTypeReference<List<Datasource>>() { };
    public static final ParameterizedTypeReference<List<Metric>> METRIC_LIST = new ParameterizedTypeReference<List<Metric>>() { };
    
    private static final Function<UriComponentsBuilder, UriComponentsBuilder> ALL_DATASOURCES = new TargetDatasourceCriteria();
    private static final Function<UriComponentsBuilder, UriComponentsBuilder> ALL_FEEDS = new TargetFeedCriteria();
    
    private final URI base;
    private final RestTemplate template;
    
    
    public static CredentialsProvider createCredentialProvider(String username, String password) {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
        return credsProvider;
    }
    
    public static Path path(String first, String... more) {
        return Paths.get(first, more);
    }

    
    public MetadataClient(URI base) {
        this(base, null);
    }
    
    public MetadataClient(URI base, String username, String password) {
        this(base, createCredentialProvider(username, password));
    }
    
    public MetadataClient(URI base, CredentialsProvider credsProvider) {
        super();
        this.base = base;
        
        if (credsProvider != null) {
            HttpClient httpClient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
            ClientHttpRequestFactory reqFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
            this.template = new RestTemplate(reqFactory);
        } else {
            this.template = new RestTemplate();
        }
        
        ObjectMapper mapper = createObjectMapper();
        this.template.getMessageConverters().add(new MappingJackson2HttpMessageConverter(mapper));
    }
    
    public List<ExtensibleTypeDescriptor> getExtensibleTypes() {
        return get(path("extension", "type"), null, TYPE_LIST);
    }

    public ExtensibleTypeDescriptor getExtensibleType(String nameOrId) {
        return get(path("extension", "type", nameOrId), ExtensibleTypeDescriptor.class);
    }

    public FeedBuilder buildFeed(String categoryName, String name) {
        return new FeedBuilderImpl(categoryName, name);
    }
    
    
    public Optional<String> getHighWaterMarkValue(String feedId, String waterMarkName) {
        return optinal(() -> get(path("feed", feedId, "watermark", waterMarkName), String.class));
    }

    public void updateHighWaterMarkValue(String feedId, String waterMarkName, String value) {
        put(path("feed", feedId, "watermark", waterMarkName), value, MediaType.TEXT_PLAIN);
    }
    
    public InitializationStatus getCurrentInitStatus(String feedId) {
        return nullable(() -> get(Paths.get("feed", feedId, "initstatus"), InitializationStatus.class));
    }
    
    public void updateCurrentInitStatus(String feedId, InitializationStatus status) {
        put(Paths.get("feed", feedId, "initstatus"), status, MediaType.APPLICATION_JSON);
    }
    
    public Feed addSource(String feedId, String datasourceId) {
        Form form = new Form();
        form.add("datasourceId", datasourceId);
        
        return post(path("feed", feedId, "source"), form, Feed.class);
    }
    
    public Feed addDestination(String feedId, String datasourceId) {
        Form form = new Form();
        form.add("datasourceId", datasourceId);
        
        return post(path("feed", feedId, "destination"), form, Feed.class);
    }
    
    public ServiceLevelAgreement createSla(ServiceLevelAgreement sla) {
        return post(path("sla"), sla, MediaType.APPLICATION_JSON, ServiceLevelAgreement.class);
    }

    public Feed setPrecondition(String feedId, Metric... metrics) {
        return setPrecondition(feedId, Arrays.asList(metrics));
    }
    
    public Feed setPrecondition(String feedId, List<Metric> metrics) {
        FeedPrecondition precond = new FeedPrecondition("Feed " + feedId + " Precondition", "", metrics);
        return setPrecondition(feedId, precond);
    }
    
    public Feed setPrecondition(String feedId, FeedPrecondition precond) {
        return post(path("feed", feedId, "precondition"), precond, MediaType.APPLICATION_JSON, Feed.class);
    }

    public FeedCriteria feedCriteria() {
        return new TargetFeedCriteria();
    }
    
    public List<Feed> getFeeds() {
        return getFeeds((FeedCriteria) ALL_FEEDS);
    }
    
    public List<Feed> getFeeds(FeedCriteria criteria) {
        try {
            return get(path("feed"), (TargetFeedCriteria) criteria, FEED_LIST);
        } catch (ClassCastException e) {
            throw new IllegalThreadStateException("Unknown criteria type: " + criteria.getClass());
        }
    }
    
    public Feed getFeed(String id) {
        return get(path("feed", id), Feed.class);
    }

    public Feed getFeed(String categoryName, String feedName) {
        return get(path("feed"), Feed.class);
    }
    
    public FeedDependencyGraph getFeedDependency(String id) {
        return get(path("feed", id, "depfeeds"), FeedDependencyGraph.class);
    }
    
    public Map<DateTime, Map<String, String>> getFeedDependencyDeltas(String feedId) {
        return get(path("feed", feedId, "depfeeds", "delta"), FEED_RESULT_DELTAS);
    }

    public Feed updateFeed(Feed feed) {
        // Using POST here in since it behaves more like a PATCH than a PUT, and PATCH is not supported in jersey.
        return post(path("feed", feed.getId()), feed, MediaType.APPLICATION_JSON, Feed.class);
    }

    /**
     * Gets the properties of the specified feed.
     *
     * @param id the feed id
     * @return the metadata properties
     */
    public Properties getFeedProperties(@Nonnull final String id) {
        return get(path("feed", id, "props"), Properties.class);
    }
    
    public Properties mergeFeedProperties(String id, Properties props) {
        return post(path("feed", id, "props"), props, MediaType.APPLICATION_JSON, Properties.class);
    }
    
    public Properties replaceFeedProperties(String id, Properties props) {
        return put(path("feed", id), props, MediaType.APPLICATION_JSON, Properties.class);
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
        return get(path("datasource"), ALL_DATASOURCES, DATASOURCE_LIST);
    }
    
    public List<Datasource> getDatasources(DatasourceCriteria criteria) {
        try {
            return get(path("datasource"), (TargetDatasourceCriteria) criteria, DATASOURCE_LIST);
        } catch (ClassCastException e) {
            throw new IllegalThreadStateException("Unknown criteria type: " + criteria.getClass());
        }
    }

    public DataOperation beginOperation(String feedDestinationId, String status) {
        Form form = new Form();
        form.add("feedDestinationId", feedDestinationId);
        form.add("status", status);
        
        return post(path("dataop"), form, DataOperation.class);
    }
    
    public DataOperation updateDataOperation(DataOperation op) {
        return put(path("dataop", op.getId()), op, MediaType.APPLICATION_JSON, DataOperation.class);
    }
    
    public DataOperation getDataOperation(String id) {
        return get(path("dataop", id), DataOperation.class);
    }

    public ServiceLevelAssessment assessPrecondition(String id) {
        return get(path("feed", id, "precondition", "assessment"), ServiceLevelAssessment.class);
    }
    
    public String getPreconditionResult(String id) {
        return get(path("feed", id, "precondition", "assessment", "result"), String.class);
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        mapper.setSerializationInclusion(Include.NON_NULL);
        return mapper;
    }
    
    private <R> Optional<R> optinal(Supplier<R> supplier) {
        try {
            return Optional.ofNullable(supplier.get());
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return Optional.empty();
            } else {
                throw e;
            }
        }
    }
    
    private <R> R nullable(Supplier<R> supplier) {
        try {
            return supplier.get();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return null;
            } else {
                throw e;
            }
        }
    }

    private FeedPrecondition createTrigger(List<Metric> metrics) {
        if (! metrics.isEmpty()) {
            FeedPrecondition trigger = new FeedPrecondition();
            trigger.addMetrics("", metrics);
            return trigger;
        } else {
            return null;
        }
    }


    private Feed postFeed(Feed feed) {
        return post(path("feed"), feed, MediaType.APPLICATION_JSON, Feed.class);
    }
    
    private HiveTableDatasource postDatasource(HiveTableDatasource ds) {
        return post(path("datasource", "hivetable"), ds, MediaType.APPLICATION_JSON, HiveTableDatasource.class);
    }
    
    private DirectoryDatasource postDatasource(DirectoryDatasource ds) {
        return post(path("datasource", "directory"), ds, MediaType.APPLICATION_JSON, DirectoryDatasource.class);
    }
    
    private UriComponentsBuilder base(Path path) {
        return UriComponentsBuilder.fromUri(this.base).path("/").path(path.toString());
    }
    
    private <R> R get(Path path, Class<R> resultType) {
        return get(path, null, resultType);
    }
    
    private <R> R get(Path path, Function<UriComponentsBuilder, UriComponentsBuilder> filterFunct, Class<R> resultType) {
        return this.template.getForObject(
                (filterFunct != null ? filterFunct.apply(base(path)) : base(path)).build().toUri(),
                resultType);
    }
    
    private <R> R get(Path path, ParameterizedTypeReference<R> responseEntity) {
        return get(path, null, responseEntity);
    }
    
    private <R> R get(Path path, Function<UriComponentsBuilder, UriComponentsBuilder> filterFunct, ParameterizedTypeReference<R> responseEntity) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(ACCEPT_TYPES);
        
        ResponseEntity<R> resp = this.template.exchange(
                (filterFunct != null ? filterFunct.apply(base(path)) : base(path)).build().toUri(),
                HttpMethod.GET,
                new HttpEntity<Object>(headers),
                responseEntity);
        
        return handle(resp);
    }
    
    private <R> R post(Path path, Form form, Class<R> resultType) {
        return this.template.postForObject(base(path).build().toUri(), form, resultType);
    }
    
    private <R> R post(Path path, Object body, MediaType mediaType, Class<R> resultType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setAccept(ACCEPT_TYPES);
        
        return this.template.postForObject(base(path).build().toUri(), 
                                           new HttpEntity<>(body, headers), 
                                           resultType);
    }
    
    private void put(Path path, Object body, MediaType mediaType) {
        put(path, body, mediaType, null);
    }
    
    private <R> R put(Path path, Object body, MediaType mediaType, Class<R> resultType) {
        URI uri = base(path).build().toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setAccept(ACCEPT_TYPES);
        
        this.template.put(uri, new HttpEntity<>(body, headers));
        // Silly that put() doesn't return an object.
        if (resultType != null) {
            return get(path, resultType);
        } else {
            return null;
        }
    }
    

    private <R> R handle(ResponseEntity<R> resp) {
        if (resp.getStatusCode().is2xxSuccessful()) {
            return resp.getBody();
        } else {
            throw new WebResponseException(ResponseEntity.status(resp.getStatusCode()).headers(resp.getHeaders()).build());
        }
    }

    
    private static class Form extends LinkedMultiValueMap<String, String> {
    }


    private class FeedBuilderImpl implements FeedBuilder {
        private String displayName;
        private String systemName;
        private String systemCategoryName;
        private String description;
        private String owner;
        private List<Metric> preconditionMetrics = new ArrayList<>();
        private Properties properties = new Properties();

        public FeedBuilderImpl(String category, String name) {
            this.systemCategoryName = category;
            this.systemName = name;
        }
    
        @Override
        public FeedBuilder displayName(String name) {
            this.displayName = name;
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
        public FeedBuilder property(String key, String value) {
            this.properties.setProperty(key, value);
            return this;
        }
    
        @Override
        public Feed build() {
            Feed feed = new Feed();
            FeedCategory feedCategory = new FeedCategory();
            feedCategory.setSystemName(this.systemCategoryName);
            feed.setCategory(feedCategory);
            feed.setSystemName(this.systemName);
            feed.setDisplayName(this.displayName != null ? this.displayName : this.systemName);
            feed.setDescription(this.description);
            feed.setOwner(this.owner);
            feed.setPrecondition(createTrigger(this.preconditionMetrics));
            feed.setProperties(properties);
            
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
        private List<HiveTableColumn> fields = new ArrayList<>(); 
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
            this.fields.add(new HiveTableColumn(name, type));
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
            src.getColumns().addAll(this.fields);
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

    private static class TargetDatasourceCriteria implements DatasourceCriteria, Function<UriComponentsBuilder, UriComponentsBuilder> {
        
        private String name;
        private String owner;
        private DateTime createdOn;
        private DateTime createdAfter;
        private DateTime createdBefore;
        private Set<String> types = new HashSet<>();
        
        public UriComponentsBuilder apply(UriComponentsBuilder target) {
            UriComponentsBuilder result = target;
            
            if (! Strings.isNullOrEmpty(this.name)) result = result.queryParam(NAME, this.name);
            if (! Strings.isNullOrEmpty(this.owner)) result = result.queryParam(OWNER, this.owner);
            if (this.createdOn != null) result = result.queryParam(ON, this.createdOn.toString());
            if (this.createdAfter != null) result = result.queryParam(AFTER, this.createdAfter.toString());
            if (this.createdBefore != null) result = result.queryParam(BEFORE, this.createdBefore.toString());
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

    private static class TargetFeedCriteria implements FeedCriteria, Function<UriComponentsBuilder, UriComponentsBuilder> {
        
        private String category;
        private String name;
        private String sourceId;
        private String destinationId;

        public UriComponentsBuilder apply(UriComponentsBuilder target) {
            UriComponentsBuilder result = target;
            
            if (! Strings.isNullOrEmpty(this.name)) result = result.queryParam(CATEGORY, this.category);
            if (! Strings.isNullOrEmpty(this.name)) result = result.queryParam(NAME, this.name);
            if (! Strings.isNullOrEmpty(this.sourceId)) result = result.queryParam(SRC_ID, this.sourceId);
            if (! Strings.isNullOrEmpty(this.destinationId)) result = result.queryParam(DEST_ID, this.destinationId);
            
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
        
        /* (non-Javadoc)
         * @see com.thinkbiganalytics.metadata.rest.model.feed.FeedCriteria#category(java.lang.String)
         */
        @Override
        public FeedCriteria category(String category) {
            this.category = category;
            return this;
        }
    }


}
