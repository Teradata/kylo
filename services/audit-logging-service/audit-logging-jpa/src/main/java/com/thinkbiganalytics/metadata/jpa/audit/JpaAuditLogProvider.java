/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.audit;

import java.io.Serializable;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.inject.Inject;

import org.springframework.data.domain.PageRequest;

import com.thinkbiganalytics.metadata.api.audit.AuditLogEntry;
import com.thinkbiganalytics.metadata.api.audit.AuditLogEntry.ID;
import com.thinkbiganalytics.metadata.api.audit.AuditLogProvider;

/**
 * Retrieves and creates audit log entries.
 * @author Sean Felten
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
        PageRequest pager = new PageRequest(0, limit);
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
    public AuditLogEntry createEntry(Principal user, String type, String description, UUID entityId) {
        JpaAuditLogEntry entry = new JpaAuditLogEntry(user, type, description, entityId);
        return repository.save(entry);
    }

}
