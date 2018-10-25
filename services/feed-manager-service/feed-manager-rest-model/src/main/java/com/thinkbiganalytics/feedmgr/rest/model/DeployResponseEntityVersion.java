package com.thinkbiganalytics.feedmgr.rest.model;
/*-
 * #%L
 * kylo-feed-manager-rest-model
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
import java.util.Date;

public class DeployResponseEntityVersion extends EntityVersion {

    private EntityVersion entityVersion;
    private NifiFeed feed;

    public DeployResponseEntityVersion(EntityVersion entityVersion, NifiFeed feed) {
        this.entityVersion = entityVersion;
        this.feed = feed;
    }

    public DeployResponseEntityVersion(String id, String name, Date createdDate, String createdBy, String comment, String entityId) {
        this.entityVersion = new EntityVersion(id, name, createdDate, createdBy, comment, entityId);
    }

    public DeployResponseEntityVersion(String id, String name, Date createdDate, String createdBy, String comment, String entityId, Object entity) {
        this.entityVersion = new EntityVersion(id, name, createdDate, createdBy, comment, entityId, entity);
    }

    @Override
    public String getId() {
        return entityVersion.getId();
    }

    @Override
    public void setId(String id) {
        entityVersion.setId(id);
    }

    @Override
    public String getName() {
        return entityVersion.getName();
    }

    @Override
    public void setName(String name) {
        entityVersion.setName(name);
    }

    @Override
    public Date getCreatedDate() {
        return entityVersion.getCreatedDate();
    }

    @Override
    public void setCreatedDate(Date createdDate) {
        entityVersion.setCreatedDate(createdDate);
    }

    @Override
    public String getCreatedBy() {
        return entityVersion.getCreatedBy();
    }

    @Override
    public void setCreatedBy(String createdBy) {
        entityVersion.setCreatedBy(createdBy);
    }

    @Override
    public String getComment() {
        return entityVersion.getComment();
    }

    @Override
    public void setComment(String comment) {
        entityVersion.setComment(comment);
    }

    @Override
    public String getEntityId() {
        return entityVersion.getEntityId();
    }

    @Override
    public void setEntityId(String entityId) {
        entityVersion.setEntityId(entityId);
    }

    @Override
    public Object getEntity() {
        return entityVersion.getEntity();
    }

    @Override
    public void setEntity(Object entity) {
        entityVersion.setEntity(entity);
    }

    @Override
    public boolean isDraft() {
        return entityVersion.isDraft();
    }

    public NifiFeed getFeed() {
        return feed;
    }

    public void setFeed(NifiFeed feed) {
        this.feed = feed;
    }

    public boolean isSuccess(){
        return this.feed != null && this.feed.isSuccess();
    }
}
