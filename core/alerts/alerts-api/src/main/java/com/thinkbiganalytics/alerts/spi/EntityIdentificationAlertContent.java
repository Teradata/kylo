package com.thinkbiganalytics.alerts.spi;


/*-
 * #%L
 * thinkbig-alerts-api
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


import com.thinkbiganalytics.security.role.SecurityRole;

import java.io.Serializable;

/**
 * Created by sr186054 on 7/24/17.
 */
public class EntityIdentificationAlertContent<C extends Serializable> implements Serializable{

    private String entityId;
    /**
     * Should be of type SecurityRole.ENTITIES
     */
    private SecurityRole.ENTITY_TYPE entityType;

    C content;

    public EntityIdentificationAlertContent(){

    }

    public EntityIdentificationAlertContent(String entityId, SecurityRole.ENTITY_TYPE entityType) {
        this.entityId = entityId;
        this.entityType = entityType;
    }
    public EntityIdentificationAlertContent(String entityId, SecurityRole.ENTITY_TYPE entityType, C content) {
        this.entityId = entityId;
        this.entityType = entityType;
        this.content = content;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public SecurityRole.ENTITY_TYPE getEntityType() {
        return entityType;
    }

    public void setEntityType(SecurityRole.ENTITY_TYPE entityType) {
        this.entityType = entityType;
    }

    public C getContent() {
        return content;
    }

    public void setContent(C content) {
        this.content = content;
    }


}
