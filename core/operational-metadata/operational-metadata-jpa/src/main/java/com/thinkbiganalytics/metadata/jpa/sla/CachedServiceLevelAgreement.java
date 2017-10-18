package com.thinkbiganalytics.metadata.jpa.sla;

/*-
 * #%L
 * thinkbig-operational-metadata-jpa
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
import java.util.Set;

/**
 * Cache object ServiceLevelAgreementDescription object
 * as they are changed infrequently.
 */
public class CachedServiceLevelAgreement implements Serializable {


    private String name;
    private String id;
    private Set<SimpleFeed> feeds;

    public CachedServiceLevelAgreement() {

    }

    public CachedServiceLevelAgreement(String name, String id, Set<SimpleFeed> feeds) {
        this.name = name;
        this.id = id;
        this.feeds = feeds;
    }

    public CachedServiceLevelAgreement(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<SimpleFeed> getFeeds() {
        return feeds;
    }

    public void setFeeds(Set<SimpleFeed> feeds) {
        this.feeds = feeds;
    }

    public static class SimpleFeed implements Serializable {

        private String id;
        private String name;

        public SimpleFeed() {

        }

        public SimpleFeed(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
