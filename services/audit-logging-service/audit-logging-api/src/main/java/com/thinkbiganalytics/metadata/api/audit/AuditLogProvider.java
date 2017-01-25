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
import java.util.UUID;

/**
 *
 * @author Sean Felten
 */
public interface AuditLogProvider {

    AuditLogEntry.ID resolveId(Serializable id);
    
    List<AuditLogEntry> list();
    
    List<AuditLogEntry> list(int limit);
    
    Optional<AuditLogEntry> findById(AuditLogEntry.ID id);

    List<AuditLogEntry> findByUser(Principal user);
    
    AuditLogEntry createEntry(Principal user, String type, String description);
    
    AuditLogEntry createEntry(Principal user, String type, String description, String entityId);
}
