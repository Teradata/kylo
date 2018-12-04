/**
 *
 */
package com.thinkbiganalytics.metadata.core.dataset;

/*-
 * #%L
 * thinkbig-metadata-core
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

import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.feed.FeedDestination;
import com.thinkbiganalytics.metadata.api.feed.FeedSource;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 *
 */
public class BaseDatasource implements Datasource {

    private ID id;
    private String name;
    private String description;
    private DateTime creationTime;
    private Set<FeedSource> feedSources = new HashSet<>();
    private Set<FeedDestination> feedDestinations = new HashSet<>();

    public BaseDatasource(String name, String descr) {
        this.id = new DatasourceId();
        this.creationTime = new DateTime();
        this.name = name;
        this.description = descr;
    }

    public ID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public DateTime getCreatedTime() {
        return creationTime;
    }

    @Override
    public Set<FeedSource> getFeedSources() {
        return this.feedSources;
    }

    @Override
    public Set<FeedDestination> getFeedDestinations() {
        return this.feedDestinations;
    }


    protected static class DatasourceId implements ID {

        private UUID uuid = UUID.randomUUID();

        public DatasourceId() {
        }

        public DatasourceId(Serializable ser) {
            if (ser instanceof String) {
                this.uuid = UUID.fromString((String) ser);
            } else if (ser instanceof UUID) {
                this.uuid = (UUID) ser;
            } else {
                throw new IllegalArgumentException("Unknown ID value: " + ser);
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof DatasourceId) {
                DatasourceId that = (DatasourceId) obj;
                return Objects.equals(this.uuid, that.uuid);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(getClass(), this.uuid);
        }

        @Override
        public String toString() {
            return this.uuid.toString();
        }
    }


    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.SystemEntity#getSystemName()
     */
    @Override
    public String getSystemName() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.SystemEntity#setSystemName(java.lang.String)
     */
    @Override
    public void setSystemName(String name) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.SystemEntity#getTitle()
     */
    @Override
    public String getTitle() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.SystemEntity#setTitle(java.lang.String)
     */
    @Override
    public void setTitle(String title) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.SystemEntity#setDescription(java.lang.String)
     */
    @Override
    public void setDescription(String description) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.Auditable#getModifiedTime()
     */
    @Override
    public DateTime getModifiedTime() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.Auditable#getCreatedBy()
     */
    @Override
    public String getCreatedBy() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.Auditable#getModifiedBy()
     */
    @Override
    public String getModifiedBy() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.Taggable#hasTag(java.lang.String)
     */
    @Override
    public boolean hasTag(String tag) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.Taggable#getTags()
     */
    @Override
    public Set<String> getTags() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.Taggable#addTag(java.lang.String)
     */
    @Override
    public Set<String> addTag(String tag) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.Taggable#removeTag(java.lang.String)
     */
    @Override
    public Set<String> removeTag(String tag) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.Iconable#getIcon()
     */
    @Override
    public String getIcon() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.Iconable#setIcon(java.lang.String)
     */
    @Override
    public void setIcon(String icon) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.Iconable#getIconColor()
     */
    @Override
    public String getIconColor() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.Iconable#setIconColor(java.lang.String)
     */
    @Override
    public void setIconColor(String iconColor) {
        // TODO Auto-generated method stub
        
    }
}
