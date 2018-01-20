/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.feed.security;

/*-
 * #%L
 * kylo-operational-metadata-jpa
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

import com.google.common.collect.Lists;
import com.thinkbiganalytics.cluster.ClusterMessage;
import com.thinkbiganalytics.cluster.ClusterServiceMessageReceiver;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.event.MetadataEventService;
import com.thinkbiganalytics.metadata.api.feed.Feed.ID;
import com.thinkbiganalytics.metadata.api.feed.security.FeedOpsAccessControlProvider;
import com.thinkbiganalytics.metadata.api.feed.security.FeedOpsAclEntry;
import com.thinkbiganalytics.metadata.jpa.cache.AbstractCacheBackedProvider;
import com.thinkbiganalytics.metadata.jpa.cache.CacheBackedProviderClusterMessage;
import com.thinkbiganalytics.security.GroupPrincipal;
import com.thinkbiganalytics.security.UsernamePrincipal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.security.Principal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 *
 */
public class JpaFeedOpsAccessControlProvider extends AbstractCacheBackedProvider<JpaFeedOpsAclEntry, JpaFeedOpsAclEntry.EntryId> implements FeedOpsAccessControlProvider {

    private static final Logger log = LoggerFactory.getLogger(JpaFeedOpsAccessControlProvider.class);
    @Inject
    private MetadataEventService eventService;

    private FeedOpsAccessControlRepository repository;

    @Inject
    private FeedAclCache feedAclCache;


    @Inject
    private MetadataAccess metadataAccess;

    @Autowired
    public JpaFeedOpsAccessControlProvider(FeedOpsAccessControlRepository feedOpsAccessControlRepository){
        super(feedOpsAccessControlRepository);
        this.repository = feedOpsAccessControlRepository;
    }

    @PostConstruct
    private void init(){
        subscribeListener(feedAclCache);
        clusterService.subscribe(this,getClusterMessageKey());
        //initially populate
        metadataAccess.read(() ->populateCache(), MetadataAccess.SERVICE );
    }

    @Override
    public JpaFeedOpsAclEntry.EntryId getId(JpaFeedOpsAclEntry value) {
        return  value.getId();
    }

    @Override
    public String getClusterMessageKey() {
        return "FEED_ACL_CACHE_UPDATED";
    }

    public String getProviderName() {
        return this.getClass().getName();
    }

    /* (non-Javadoc)
         * @see com.thinkbiganalytics.metadata.api.feed.security.FeedOpsAccessControlProvider#grantAccess(com.thinkbiganalytics.metadata.api.feed.Feed.ID, java.security.Principal, java.security.Principal[])
         */
    @Override
    public void grantAccess(ID feedId, Principal principal, Principal... more) {
        Set<JpaFeedOpsAclEntry> entries = createEntries(feedId, Stream.concat(Stream.of(principal), Arrays.stream(more)));
        this.saveList(entries);
     //   feedAclCache.add(entries);
      //  notifyChange(feedId, FeedAclChange.FeedAclChangeType.GRANTED,entries);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.security.FeedOpsAccessControlProvider#grantAccessOnly(com.thinkbiganalytics.metadata.api.feed.Feed.ID, java.security.Principal, java.security.Principal[])
     */
    @Override
    public void grantAccessOnly(ID feedId, Principal principal, Principal... more) {
        revokeAllAccess(feedId);
        grantAccess(feedId, principal, more);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.security.FeedOpsAccessControlProvider#grantAccess(com.thinkbiganalytics.metadata.api.feed.Feed.ID, java.util.Set)
     */
    @Override
    public void grantAccess(ID feedId, Set<Principal> principals) {
        Set<JpaFeedOpsAclEntry> entries = createEntries(feedId, principals.stream());
        this.saveList(entries);
      //  feedAclCache.add(entries);
       // notifyChange(feedId, FeedAclChange.FeedAclChangeType.GRANTED,entries);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.security.FeedOpsAccessControlProvider#grantAccessOnly(com.thinkbiganalytics.metadata.api.feed.Feed.ID, java.util.Set)
     */
    @Override
    public void grantAccessOnly(ID feedId, Set<Principal> principals) {
        revokeAllAccess(feedId);
        grantAccess(feedId, principals);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.security.FeedOpsAccessControlProvider#revokeAccess(com.thinkbiganalytics.metadata.api.feed.Feed.ID, java.security.Principal, java.security.Principal[])
     */
    @Override
    public void revokeAccess(ID feedId, Principal principal, Principal... more) {
        Set<JpaFeedOpsAclEntry> entries = createEntries(feedId, Stream.concat(Stream.of(principal), Arrays.stream(more)));
        this.delete(entries);
        //feedAclCache.remove(feedId.toString());
       // notifyChange(feedId, FeedAclChange.FeedAclChangeType.REVOKED,entries);
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.security.FeedOpsAccessControlProvider#revokeAccess(com.thinkbiganalytics.metadata.api.feed.Feed.ID, java.util.Set)
     */
    @Override
    public void revokeAccess(ID feedId, Set<Principal> principals) {
        Set<JpaFeedOpsAclEntry> entries = createEntries(feedId, principals.stream());
        this.delete(entries);
      //  this.repository.delete(entries);
       // this.feedAclCache.remove(feedId.toString());
       // notifyChange(feedId, FeedAclChange.FeedAclChangeType.REVOKED,entries);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.security.FeedOpsAccessControlProvider#revokeAllAccess(java.security.Principal, java.security.Principal[])
     */
    @Override
    public void revokeAllAccess(Principal principal, Principal... more) {

        Set<String> principalNames = Stream.concat(Stream.of(principal), Arrays.stream(more))
                        .map(Principal::getName)
                        .collect(Collectors.toSet());
        Set<JpaFeedOpsAclEntry> entries = this.repository.findForPrincipals(principalNames);
        this.delete(entries);

        //Set<UUID>feedIds = this.repository.findFeedIdsForPrincipals(principalNames);
        //this.repository.deleteForPrincipals(principalNames);
        //this.notifyChange(feedIds, FeedAclChange.FeedAclChangeType.REVOKED);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.security.FeedOpsAccessControlProvider#revokeAllAccess(java.util.Set)
     */
    @Override
    public void revokeAllAccess(Set<Principal> principals) {
        Set<String> principalNames = principals.stream()
                        .map(Principal::getName)
                        .collect(Collectors.toSet());
        Set<JpaFeedOpsAclEntry> entries = this.repository.findForPrincipals(principalNames);
        this.delete(entries);
       // Set<UUID>feedIds = this.repository.findFeedIdsForPrincipals(principalNames);
       // this.repository.deleteForPrincipals(principalNames);
       // this.notifyChange(feedIds, FeedAclChange.FeedAclChangeType.REVOKED);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.security.FeedOpsAccessControlProvider#revokeAllAccess(com.thinkbiganalytics.metadata.api.feed.Feed.ID)
     */
    @Override
    public void revokeAllAccess(ID feedId) {
        List<JpaFeedOpsAclEntry> entries = this.repository.findForFeed(UUID.fromString(feedId.toString()));
        this.delete(entries);
     //   this.repository.deleteForFeed(UUID.fromString(feedId.toString()));
      //  this.feedAclCache.remove(feedId.toString());
      //  notifyChange(feedId, FeedAclChange.FeedAclChangeType.REVOKED,null);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.security.FeedOpsAccessControlProvider#getPrincipals(com.thinkbiganalytics.metadata.api.feed.Feed.ID)
     */
    @Override
    public Set<Principal> getPrincipals(ID feedId) {
        List<JpaFeedOpsAclEntry> entries = findForFeed(feedId.toString());
        return entries.stream().map(e -> asPrincipal(e)).collect(Collectors.toSet());
    }
    
    protected Principal asPrincipal(JpaFeedOpsAclEntry entry) {
        return entry.getPrincipalType() == FeedOpsAclEntry.PrincipalType.GROUP
                        ? new GroupPrincipal(entry.getPrincipalName())
                        : new UsernamePrincipal(entry.getPrincipalName());
    }

    protected Set<JpaFeedOpsAclEntry> createEntries(ID feedId, Stream<Principal> stream) {
        return stream.map(p -> new JpaFeedOpsAclEntry(feedId, p)).collect(Collectors.toSet());
    }

    public List<JpaFeedOpsAclEntry> findAll(){
        return repository.findAll();
    }

    public List<JpaFeedOpsAclEntry> findForFeed(String feedId) {
         return this.repository.findForFeed(UUID.fromString(feedId));
    }





}
