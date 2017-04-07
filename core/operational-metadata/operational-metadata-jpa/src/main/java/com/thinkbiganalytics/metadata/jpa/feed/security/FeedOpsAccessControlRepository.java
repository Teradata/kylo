/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.feed.security;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 */
public interface FeedOpsAccessControlRepository extends JpaRepository<JpaFeedOpsAclEntry, JpaFeedOpsAclEntry.EntryId> {
    
    @Query("select entry from JpaFeedOpsAclEntry as entry where entry.feedId = :id")
    List<JpaFeedOpsAclEntry> findForFeed(@Param("id") UUID feedId); 
    
    @Modifying
    @Query("delete from JpaFeedOpsAclEntry as entry where entry.principalName in (:names)")
    int deleteForPrincipals(@Param("names") Set<String> principalNames); 
    
    @Modifying
    @Query("delete from JpaFeedOpsAclEntry as entry where entry.feedId = :id")
    int deleteForFeed(@Param("id") UUID feedId); 

}
