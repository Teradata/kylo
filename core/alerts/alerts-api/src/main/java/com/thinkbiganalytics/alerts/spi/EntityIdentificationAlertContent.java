package com.thinkbiganalytics.alerts.spi;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
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

    String contentType;

    public EntityIdentificationAlertContent(){

    }
    public EntityIdentificationAlertContent(String entityId, SecurityRole.ENTITY_TYPE entityType, C content) {
        this.entityId = entityId;
        this.entityType = entityType;
        this.content = content;
        this.contentType = content.getClass().getName();
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
