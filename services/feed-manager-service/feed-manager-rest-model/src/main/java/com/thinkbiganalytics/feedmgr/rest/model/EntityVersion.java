/**
 * 
 */
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 *
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EntityVersion {

    private String id;
    private String name;
    private Date createdDate;
    private String createdBy;
    private String comment;
    private String entityId;
    private Object entity;
    
    public EntityVersion() {
        super();
    }
    
    public EntityVersion(String id, String name, Date createdDate, String createdBy, String comment, String entityId) {
        this(id, name, createdDate, createdBy, comment, entityId, null);
    }
    
    public EntityVersion(String id, String name, Date createdDate, String createdBy, String comment, String entityId, Object entity) {
        super();
        this.id = id;
        this.name = name;
        this.createdDate = createdDate;
        this.createdBy = createdBy;
        this.comment = comment;
        this.entity = entity;
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

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getEntityId() {
        return entityId;
    }
    
    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public Object getEntity() {
        return entity;
    }

    public void setEntity(Object entity) {
        this.entity = entity;
    }

    public boolean isDraft(){
        return this.name.equalsIgnoreCase("draft");
    }
    
}
