package com.thinkbiganalytics.jira.domain;

/*-
 * #%L
 * thinkbig-jira-rest-client
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

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.net.URI;
import java.net.URISyntaxException;

/**
 */
public class BasicIssue {

    private URI self;
    private String key;
    private Long id;

    public BasicIssue(GetIssue getIssue) {
        try {
            this.self = new URI(getIssue.getSelf());
            this.key = getIssue.getKey();
            this.id = new Long(getIssue.getId());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public BasicIssue(URI self, String key, Long id) {
        this.self = self;
        this.key = key;
        this.id = id;
    }

    public BasicIssue() {

    }


    public URI getSelf() {
        return self;
    }

    /**
     * @return issue key
     */
    public String getKey() {
        return key;
    }

    public Long getId() {
        return id;
    }


    public String toString() {
        return getToStringHelper().toString();
    }

    protected MoreObjects.ToStringHelper getToStringHelper() {
        return MoreObjects.toStringHelper(this).
            add("self", self).
            add("key", key).
            add("id", id);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BasicIssue) {
            BasicIssue that = (BasicIssue) obj;
            return Objects.equal(this.self, that.self)
                   && Objects.equal(this.key, that.key)
                   && Objects.equal(this.id, that.id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(self, key, id);
    }

}
