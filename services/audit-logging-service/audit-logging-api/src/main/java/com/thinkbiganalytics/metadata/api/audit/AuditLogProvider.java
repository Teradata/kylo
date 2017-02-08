/**
 *
 */
package com.thinkbiganalytics.metadata.api.audit;

/*-
 * #%L
 * thinkbig-audit-logging-api
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

import java.io.Serializable;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

/**
 * Provider managing {@link AuditLogEntry} items
 */
public interface AuditLogProvider {

    AuditLogEntry.ID resolveId(Serializable id);

    /**
     * Return a list of the audit log entries
     *
     * @return a list of the audit log entries
     */
    List<AuditLogEntry> list();

    /**
     * Return a list of the latest audit entries limited by an amount
     *
     * @param limit the number of entries to return
     * @return a list of the latest log entries limited by an amount
     */
    List<AuditLogEntry> list(int limit);

    /**
     * Return an audit entry by its id
     *
     * @param id an audit log id
     * @return an audit log entry matching the id
     */
    Optional<AuditLogEntry> findById(AuditLogEntry.ID id);

    /**
     * Return the log entries associated with a given user
     *
     * @param user a user
     * @return the log entries associated with a given user
     */
    List<AuditLogEntry> findByUser(Principal user);

    /**
     * Create a new audit log entry
     *
     * @param user        a user attached to this audit entry
     * @param type        the type of entry
     * @param description a description about what happened
     * @return an audit log entry
     */
    AuditLogEntry createEntry(Principal user, String type, String description);

    /**
     * Create a new audit log entry
     *
     * @param user        a user attached to this audit entry
     * @param type        the type of entry
     * @param description a description about what happened
     * @param entityId    an entity id associated with this audit entry
     * @return an audit log entry
     */
    AuditLogEntry createEntry(Principal user, String type, String description, String entityId);
}
