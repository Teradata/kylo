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

import org.joda.time.DateTime;

import java.io.Serializable;
import java.security.Principal;

/**
 * An audit log entry describing a metadata change or operation attempt.
 */
public interface AuditLogEntry {

    /**
     * Return the unique id associated with the audit entry
     *
     * @return the unique id associated with the audit entry
     */
    ID getId();

    /**
     * Return the time of the audit event
     *
     * @return the time of the audit event
     */
    DateTime getCreatedTime();

    /**
     * Return the user associated with the audit event
     *
     * @return the user associated with the audit event
     */
    Principal getUser();

    /**
     * Return a discriminator defining the type of audit log entry
     *
     * @return a string defining the type of audit log entry
     */
    String getType();

    /**
     * Return a description about the audit event
     *
     * @return a description about the audit event
     */
    String getDescription();

    /**
     * Return a entity id this audit event is realted to
     *
     * @return a entity id this audit event is realted to
     */
    String getEntityId();

    interface ID extends Serializable {

    }
}
