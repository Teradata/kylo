/**
 *
 */
package com.thinkbiganalytics.metadata.api.event;

/*-
 * #%L
 * thinkbig-metadata-api
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
import java.util.Objects;

/**
 *
 */
public abstract class AbstractMetadataEvent<C extends Serializable> implements MetadataEvent<C> {

    private static final long serialVersionUID = 1L;

    private final DateTime timestamp;
    private final C data;
    private final Principal userPrincipal;

    public AbstractMetadataEvent(C data) {
        this(data, DateTime.now(), null);
    }

    public AbstractMetadataEvent(C data, Principal user) {
        this(data, DateTime.now(), user);
    }

    public AbstractMetadataEvent(C data, DateTime time, Principal user) {
        this.timestamp = time;
        this.userPrincipal = user;
        this.data = data;
    }

    @Override
    public DateTime getTimestamp() {
        return this.timestamp;
    }

    @Override
    public C getData() {
        return this.data;
    }

    public Principal getUserPrincipal() {
        return userPrincipal;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.timestamp, this.userPrincipal, this.data);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AbstractMetadataEvent) {
            AbstractMetadataEvent<?> that = (AbstractMetadataEvent<?>) obj;
            return Objects.equals(this.userPrincipal, this.userPrincipal) &&
                   Objects.equals(this.timestamp, that.timestamp) &&
                   Objects.equals(this.data, that.data);
        } else {
            return false;
        }
    }

}
