/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.feed.security;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import com.thinkbiganalytics.metadata.api.feed.Feed.ID;
import com.thinkbiganalytics.metadata.api.feed.security.FeedOpsAccessControlProvider;
import com.thinkbiganalytics.metadata.jpa.feed.security.JpaFeedOpsAclEntry.PrincipalType;
import com.thinkbiganalytics.security.GroupPrincipal;
import com.thinkbiganalytics.security.UsernamePrincipal;

/**
 *
 */
public class JpaFeedOpsAccessControlProvider implements FeedOpsAccessControlProvider {
    
    @Inject
    private FeedOpsAccessControlRepository repository;

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.security.FeedOpsAccessControlProvider#grantAccess(com.thinkbiganalytics.metadata.api.feed.Feed.ID, java.security.Principal, java.security.Principal[])
     */
    @Override
    public void grantAccess(ID feedId, Principal principal, Principal... more) {
        Set<JpaFeedOpsAclEntry> entries = createEntries(feedId, Stream.concat(Stream.of(principal), Arrays.stream(more)));
        this.repository.save(entries);
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
        this.repository.save(entries);
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
        this.repository.delete(entries);
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.security.FeedOpsAccessControlProvider#revokeAccess(com.thinkbiganalytics.metadata.api.feed.Feed.ID, java.util.Set)
     */
    @Override
    public void revokeAccess(ID feedId, Set<Principal> principals) {
        Set<JpaFeedOpsAclEntry> entries = createEntries(feedId, principals.stream());
        this.repository.delete(entries);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.security.FeedOpsAccessControlProvider#revokeAllAccess(java.security.Principal, java.security.Principal[])
     */
    @Override
    public void revokeAllAccess(Principal principal, Principal... more) {
        Set<String> principalNames = Stream.concat(Stream.of(principal), Arrays.stream(more))
                        .map(Principal::getName)
                        .collect(Collectors.toSet());
        this.repository.deleteForPrincipals(principalNames);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.security.FeedOpsAccessControlProvider#revokeAllAccess(java.util.Set)
     */
    @Override
    public void revokeAllAccess(Set<Principal> principals) {
        Set<String> principalNames = principals.stream()
                        .map(Principal::getName)
                        .collect(Collectors.toSet());
        this.repository.deleteForPrincipals(principalNames);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.security.FeedOpsAccessControlProvider#revokeAllAccess(com.thinkbiganalytics.metadata.api.feed.Feed.ID)
     */
    @Override
    public void revokeAllAccess(ID feedId) {
        this.repository.deleteForFeed(UUID.fromString(feedId.toString()));
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.feed.security.FeedOpsAccessControlProvider#getPrincipals(com.thinkbiganalytics.metadata.api.feed.Feed.ID)
     */
    @Override
    public Set<Principal> getPrincipals(ID feedId) {
        List<JpaFeedOpsAclEntry> entries = this.repository.findForFeed(UUID.fromString(feedId.toString()));
        return entries.stream().map(e -> asPrincipal(e)).collect(Collectors.toSet());
    }
    
    protected Principal asPrincipal(JpaFeedOpsAclEntry entry) {
        return entry.getPrincipalType() == PrincipalType.GROUP 
                        ? new GroupPrincipal(entry.getPrincipalName())
                        : new UsernamePrincipal(entry.getPrincipalName());
    }

    protected Set<JpaFeedOpsAclEntry> createEntries(ID feedId, Stream<Principal> stream) {
        return stream.map(p -> new JpaFeedOpsAclEntry(feedId, p)).collect(Collectors.toSet());
    }

}
