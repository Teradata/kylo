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

import java.io.Serializable;
import java.util.Objects;

/**
 * Base type describing the metadata change as part a metadata event.  Subclasses should provide the details of the change.
 */
public class MetadataChange implements Serializable {

    private static final long serialVersionUID = 1L;
    private final ChangeType change;
    private final String description;

    public MetadataChange(ChangeType change) {
        this(change, "");
    }

    public MetadataChange(ChangeType change, String descr) {
        super();
        this.change = change;
        this.description = descr;
    }

    public ChangeType getChange() {
        return change;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.change, this.description);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MetadataChange) {
            MetadataChange that = (MetadataChange) obj;
            return Objects.equals(this.change, this.change) &&
                   Objects.equals(this.description, that.description);
        } else {
            return false;
        }
    }

    public enum ChangeType {CREATE, UPDATE, DELETE}

}
