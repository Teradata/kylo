package com.thinkbiganalytics.metadata.core.feed;

/*-
 * #%L
 * thinkbig-metadata-core
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

import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.extension.UserFieldDescriptor;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedConnection;
import com.thinkbiganalytics.metadata.api.feed.FeedDestination;
import com.thinkbiganalytics.metadata.api.feed.FeedPrecondition;
import com.thinkbiganalytics.metadata.api.feed.FeedSource;
import com.thinkbiganalytics.metadata.api.feed.InitializationStatus;
import com.thinkbiganalytics.metadata.api.security.HadoopSecurityGroup;
import com.thinkbiganalytics.metadata.api.security.RoleAssignments;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment;
import com.thinkbiganalytics.security.action.AllowedActions;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A POJO implementation of {@link Feed}.
 *
 * @param <C> the type of parent category
 */
public class BaseFeed<C extends Category> implements Feed<C> {

    private ID Id;
    private String name;
    private String displayName;
    private String description;
    private State state;
    private boolean initialized;
    private DateTime createdTime;
    private Set<Feed<?>> dependentFeeds = new HashSet<>();
    private Set<FeedSource> sources = new HashSet<>();
    private Set<FeedDestination> destinations = new HashSet<>();
    private FeedPreconditionImpl precondition;
    private Map<String, Object> properties;
    private List<ServiceLevelAgreement> feedServiceLevelAgreements;
    private List<? extends HadoopSecurityGroup> hadoopSecurityGroups;
    private Map<String, String> waterMarkValues = new HashMap<>();

    /**
     * User-defined properties
     */
    private Map<String, String> userProperties;

    public BaseFeed(String name, String description) {
        this.Id = new FeedId();
        this.name = name;
        this.displayName = name;
        this.description = description;
        this.createdTime = DateTime.now();
    }

    @Override
    public String getVersionName() {
        return "";
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Feed<C>> getDependentFeeds() {
        return new ArrayList(this.dependentFeeds);
    }

    @Override
    public boolean addDependentFeed(Feed feed) {
        return this.dependentFeeds.add(feed);
    }

    @Override
    public boolean removeDependentFeed(Feed feed) {
        return this.dependentFeeds.remove(feed);
    }


    @Override
    public List<Feed<C>> getUsedByFeeds() {
        return null;
    }

    @Override
    public boolean addUsedByFeed(Feed<?> feed) {
        return false;
    }

    @Override
    public boolean removeUsedByFeed(Feed<?> feed) {
        return false;
    }

    @Override
    public Map<String, Object> getProperties() {
        return this.properties;
    }

    @Override
    public void setProperties(Map<String, Object> props) {
        this.properties = props;
    }

    @Override
    public Map<String, Object> mergeProperties(Map<String, Object> props) {
        for (Entry<String, Object> entry : props.entrySet()) {
            this.properties.put(entry.getKey(), entry.getValue());
        }
        return this.properties;
    }

    @Override
    public DateTime getCreatedTime() {
        return this.createdTime;
    }

    @Override
    public void setProperty(String key, Object value) {
        this.properties.put(key, value);
    }

    @Override
    public void removeProperty(String key) {
        this.properties.remove(key);
    }

    public ID getId() {
        return Id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String getQualifiedName() {
        return getCategory().getName() + "." + getName();
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    @Override
    public void setDisplayName(String name) {
        this.displayName = name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String descr) {
        this.description = descr;
    }

    @Override
    public State getState() {
        return this.state;
    }

    public void setState(State state) {
        this.state = state;
    }

    @Override
    public C getCategory() {
        return null;
    }

    @Override
    public DateTime getModifiedTime() {
        return null;
    }

    public List<FeedSource> getSources() {
        return new ArrayList<>(this.sources);
    }

    public List<FeedDestination> getDestinations() {
        return new ArrayList<>(destinations);
    }

    @Override
    public FeedDestination getDestination(Datasource.ID id) {
        for (FeedDestination dest : this.destinations) {
            if (dest.getDatasource().getId().equals(id)) {
                return dest;
            }
        }

        return null;
    }

    @Override
    public FeedPrecondition getPrecondition() {
        return this.precondition;
    }

    public FeedSource addSource(Datasource ds) {
        return addSource(ds, null);
    }

    public FeedSource addSource(Datasource ds, ServiceLevelAgreement agreement) {
        Source src = new Source(ds, agreement);
        this.sources.add(src);
        return src;
    }

    @Override
    public FeedSource getSource(Datasource.ID id) {
        for (FeedSource src : this.sources) {
            if (src.getFeed().getId().equals(id)) {
                return src;
            }
        }

        return null;
    }
//
//    @Override
//    public FeedSource getSource(FeedSource.ID id) {
//        return this.sources.get(id);
//    }

    public FeedDestination addDestination(Datasource ds) {
        FeedDestination dest = new Destination(ds);
        this.destinations.add(dest);
        return dest;
    }
//
//    @Override
//    public FeedDestination getDestination(FeedDestination.ID id) {
//        return this.destinations.get(id);
//    }

    public FeedPrecondition setPrecondition(ServiceLevelAgreement sla) {
        this.precondition = new FeedPreconditionImpl(this, sla);
        return this.precondition;
    }

    @Override
    public List<? extends HadoopSecurityGroup> getSecurityGroups() {
        return this.hadoopSecurityGroups;
    }

    @Override
    public void setSecurityGroups(List<? extends HadoopSecurityGroup> securityGroups) {
        hadoopSecurityGroups = securityGroups;
    }

    @Override
    public List<? extends ServiceLevelAgreement> getServiceLevelAgreements() {
        return feedServiceLevelAgreements;
    }

    @Nonnull
    @Override
    public Map<String, String> getUserProperties() {
        return userProperties;
    }

    @Override
    public void setUserProperties(@Nonnull Map<String, String> userProperties, @Nonnull Set<UserFieldDescriptor> userFields) {
        this.userProperties = userProperties;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.Feed#getWaterMarkValue(java.lang.String)
     */
    @Override
    public Optional<String> getWaterMarkValue(String waterMarkName) {
        return Optional.ofNullable(this.waterMarkValues.get(waterMarkName));
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.Feed#getWaterMarkNames()
     */
    @Override
    public Set<String> getWaterMarkNames() {
        return new HashSet<>(this.waterMarkValues.keySet());
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.Feed#setWaterMarkValue(java.lang.String, java.lang.String)
     */
    @Override
    public void setWaterMarkValue(String waterMarkName, String value) {
        this.waterMarkValues.put(waterMarkName, waterMarkName);
    }

    @Override
    public void setTags(@Nullable final Set<String> tags) {
        // ignored
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.Feed#getCurrentInitStatus()
     */
    @Override
    public InitializationStatus getCurrentInitStatus() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.Feed#updateInitStatus(com.thinkbiganalytics.metadata.api.feed.InitializationStatus)
     */
    @Override
    public void updateInitStatus(InitializationStatus status) {
        // TODO Auto-generated method stub

    }
//
//    protected static class SourceId extends BaseId implements FeedSource.ID {
//        public SourceId() {
//            super();
//        }
//
//        public SourceId(Serializable ser) {
//            super(ser);
//        }
//    }
//
//    protected static class DestinationId extends BaseId implements FeedDestination.ID {
//        public DestinationId() {
//            super();
//        }
//
//        public DestinationId(Serializable ser) {
//            super(ser);
//        }
//    }
//    @Override
//    public String getVersionName() {
//        return null;
//    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.Feed#getInitHistory()
     */
    @Override
    public List<InitializationStatus> getInitHistory() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AllowedActions getAllowedActions() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.security.AccessControlled#getRoleAssignments()
     */
    @Override
    public RoleAssignments getRoleAssignments() {
        // TODO Auto-generated method stub
        return null;
    }

    private static class BaseId {

        private final UUID uuid;

        public BaseId() {
            this.uuid = UUID.randomUUID();
        }

        public BaseId(Serializable ser) {
            if (ser instanceof String) {
                this.uuid = UUID.fromString((String) ser);
            } else if (ser instanceof UUID) {
                this.uuid = (UUID) ser;
            } else {
                throw new IllegalArgumentException("Unknown ID value: " + ser);
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (getClass().isAssignableFrom(obj.getClass())) {
                BaseId that = (BaseId) obj;
                return Objects.equals(this.uuid, that.uuid);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(getClass(), this.uuid);
        }

        @Override
        public String toString() {
            return this.uuid.toString();
        }
    }

    protected static class FeedId extends BaseId implements Feed.ID {

        public FeedId() {
            super();
        }

        public FeedId(Serializable ser) {
            super(ser);
        }
    }

    protected static class FeedPreconditionImpl implements FeedPrecondition {

        private ServiceLevelAgreement sla;
        private BaseFeed feed;
        private ServiceLevelAssessment lastAssessment;

        public FeedPreconditionImpl(BaseFeed feed, ServiceLevelAgreement sla) {
            this.sla = sla;
            this.feed = feed;
        }

        @Override
        public Feed<?> getFeed() {
            return this.feed;
        }

        @Override
        public ServiceLevelAgreement getAgreement() {
            return sla;
        }

        @Override
        public ServiceLevelAssessment getLastAssessment() {
            return lastAssessment;
        }

        @Override
        public void setLastAssessment(ServiceLevelAssessment assmnt) {
            this.lastAssessment = assmnt;
        }
    }

    private abstract class Data implements FeedConnection {

        private Datasource dataset;

        public Data(Datasource ds) {
            this.dataset = ds;
        }

        @Override
        public Feed getFeed() {
            return BaseFeed.this;
        }

        @Override
        public Datasource getDatasource() {
            return this.dataset;
        }
    }

    private class Source extends Data implements FeedSource {

        private static final long serialVersionUID = -2407190619538717445L;

        //        private SourceId id;
        private ServiceLevelAgreement agreement;

        public Source(Datasource ds, ServiceLevelAgreement agreement) {
            super(ds);
//            this.id = new SourceId();
            this.agreement = agreement;
        }
//
//        @Override
//        public ID getId() {
//            return this.id;
//        }

        @Override
        public ServiceLevelAgreement getAgreement() {
            return this.agreement;
        }
    }

    private class Destination extends Data implements FeedDestination {

        private static final long serialVersionUID = -6990911423133789381L;

//        private DestinationId id;

        public Destination(Datasource ds) {
            super(ds);
//            this.id = new DestinationId();
        }
//
//        @Override
//        public ID getId() {
//            return this.id;
//        }
    }
}
