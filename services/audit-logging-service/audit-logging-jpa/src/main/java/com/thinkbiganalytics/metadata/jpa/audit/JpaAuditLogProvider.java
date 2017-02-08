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
import com.thinkbiganalytics.metadata.api.audit.AuditLogEntry.ID;
import com.thinkbiganalytics.metadata.api.audit.AuditLogProvider;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import java.io.Serializable;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.inject.Inject;

/**
 * Retrieves and creates audit log entries.
 */
public class JpaAuditLogProvider implements AuditLogProvider {

    public static final int DEFAULT_LIMIT = 100;

    private AuditLogRepository repository;

    @Inject
    public JpaAuditLogProvider(AuditLogRepository repository) {
        super();
        this.repository = repository;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.audit.AuditLogProvider#resolveId(java.io.Serializable)
     */
    @Override
    public ID resolveId(Serializable id) {
        if (id instanceof JpaAuditLogEntry.AuditLogId) {
            return (JpaAuditLogEntry.AuditLogId) id;
        } else {
            return new JpaAuditLogEntry.AuditLogId(id);
        }
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.audit.AuditLogProvider#list()
     */
    @Override
    public List<AuditLogEntry> list() {
        return list(DEFAULT_LIMIT);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.audit.AuditLogProvider#list(int)
     */
    @Override
    public List<AuditLogEntry> list(int limit) {
        PageRequest pager = new PageRequest(0, limit, new Sort(Direction.DESC, "createdTime"));
        return StreamSupport.stream(this.repository.findAll(pager).spliterator(), false).collect(Collectors.toList());
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.audit.AuditLogProvider#findById(com.thinkbiganalytics.metadata.api.audit.AuditLogEntry.ID)
     */
    @Override
    public Optional<AuditLogEntry> findById(ID id) {
        return Optional.ofNullable(repository.findOne(id));
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.audit.AuditLogProvider#findByUser(java.security.Principal)
     */
    @Override
    public List<AuditLogEntry> findByUser(Principal user) {
        return this.repository.findByUsername(user);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.audit.AuditLogProvider#createEntry(java.security.Principal, java.lang.String, java.lang.String)
     */
    @Override
    public AuditLogEntry createEntry(Principal user, String type, String description) {
        return createEntry(user, type, description, null);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.audit.AuditLogProvider#createEntry(java.security.Principal, java.lang.String, java.lang.String, java.util.UUID)
     */
    @Override
    public AuditLogEntry createEntry(Principal user, String type, String description, String entityId) {
        JpaAuditLogEntry entry = new JpaAuditLogEntry(user, type, description, entityId);
        return repository.save(entry);
    }

}
