/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.audit;

import java.security.Principal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.thinkbiganalytics.metadata.api.audit.AuditLogEntry;

/**
 *
 * @author Sean Felten
 */
public interface AuditLogRepository extends JpaRepository<JpaAuditLogEntry, AuditLogEntry.ID> {


    @Query("select entry from JpaAuditLogEntry as entry where entry.user = :user")
    List<AuditLogEntry> findByUsername(@Param("user") Principal username);

}
