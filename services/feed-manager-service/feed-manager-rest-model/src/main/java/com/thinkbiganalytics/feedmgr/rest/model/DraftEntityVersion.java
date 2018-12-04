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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.util.Date;

/**
 *
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DraftEntityVersion extends EntityVersion{

  private EntityVersion deployedVersion;

    public DraftEntityVersion() {
    }

    public DraftEntityVersion(EntityVersion draftVersion, EntityVersion deployedVersion){
        this(draftVersion.getId(),draftVersion.getName(),draftVersion.getCreatedDate(),draftVersion.getCreatedBy(),draftVersion.getComment(),draftVersion.getEntityId(),draftVersion.getEntity());
        this.deployedVersion = deployedVersion;
    }

    public DraftEntityVersion(String id, String name, Date createdDate, String createdBy, String comment, String entityId) {
        super(id, name, createdDate, createdBy, comment, entityId);
    }

    public DraftEntityVersion(String id, String name, Date createdDate, String createdBy, String comment, String entityId, Object entity) {
        super(id, name, createdDate, createdBy, comment, entityId, entity);
    }

    public EntityVersion getDeployedVersion() {
        return deployedVersion;
    }

    public void setDeployedVersion(EntityVersion deployedVersion) {
        this.deployedVersion = deployedVersion;
    }
}
