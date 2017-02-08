/**
 *
 */
package com.thinkbiganalytics.metadata.jpa.audit;

/*-
 * #%L
 * thinkbig-audit-logging-jpa
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

import com.thinkbiganalytics.metadata.api.audit.AuditLogEntry;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.security.Principal;
import java.util.List;

/**
 * Spring data repository for {@link JpaAuditLogEntry}
 */
public interface AuditLogRepository extends JpaRepository<JpaAuditLogEntry, AuditLogEntry.ID> {


    @Query("select entry from JpaAuditLogEntry as entry where entry.user = :user order by entry.createdTime desc")
    List<AuditLogEntry> findByUsername(@Param("user") Principal username);

}
