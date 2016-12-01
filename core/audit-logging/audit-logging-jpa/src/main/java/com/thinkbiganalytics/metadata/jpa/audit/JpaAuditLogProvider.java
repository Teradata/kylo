/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.audit;

import java.io.Serializable;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.thinkbiganalytics.metadata.api.audit.AuditLogEntry;
import com.thinkbiganalytics.metadata.api.audit.AuditLogEntry.ID;
import com.thinkbiganalytics.metadata.api.audit.AuditLogProvider;

/**
 * Retrieves and creates audit log entries.
 * @author Sean Felten
 */
public class JpaAuditLogProvider implements AuditLogProvider {
    
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
     * @see com.thinkbiganalytics.metadata.api.audit.AuditLogProvider#findById(com.thinkbiganalytics.metadata.api.audit.AuditLogEntry.ID)
     */
    @Override
    public AuditLogEntry findById(ID id) {
        return repository.findOne(id);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.audit.AuditLogProvider#findByUser(java.security.Principal)
     */
    @Override
    public List<AuditLogEntry> findByUser(Principal user) {
        return this.repository.findByUsername(user.getName());
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
