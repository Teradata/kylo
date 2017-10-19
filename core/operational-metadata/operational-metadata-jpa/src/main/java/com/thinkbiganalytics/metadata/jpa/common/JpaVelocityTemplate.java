package com.thinkbiganalytics.metadata.jpa.common;
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
import com.thinkbiganalytics.jpa.AbstractAuditedEntityAsMillis;
import com.thinkbiganalytics.jpa.BaseJpaId;
import com.thinkbiganalytics.common.velocity.model.VelocityTemplate;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "VELOCITY_TEMPLATE")
public class JpaVelocityTemplate extends AbstractAuditedEntityAsMillis implements VelocityTemplate {

    @EmbeddedId
    private JpaVelocityTemplateId id;

    @Column(name = "TYPE")
    private String type;

    @Column(name = "NAME")
    private String name;


    @Column(name = "SYSTEM_NAME")
    private String systemName;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "TEMPLATE")
    private String template;

    @Column(name = "ENABLED", length = 1)
    @org.hibernate.annotations.Type(type = "yes_no")
    private boolean enabled;

    @Column(name = "IS_DEFAULT", length = 1)
    @org.hibernate.annotations.Type(type = "yes_no")
    private boolean isDefault;

    public JpaVelocityTemplate() {

    }



    public JpaVelocityTemplate(String type, String name, String systemName, String title, String template, boolean enabled) {
        this.type = type;
        this.name = name;
        this.systemName = systemName;
        this.title = title;
        this.template = template;
        this.enabled =enabled;
    }

    @Override
    public ID getId() {
        return id;
    }

    public void setId(JpaVelocityTemplateId id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    @Override
    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    @Embeddable
    public static class JpaVelocityTemplateId extends BaseJpaId implements VelocityTemplate.ID {


        private static final long serialVersionUID = -5255357654415019054L;

        @Column(name = "id")
        private UUID uuid;

        public JpaVelocityTemplateId() {
        }

        public JpaVelocityTemplateId(Serializable ser) {
            super(ser);
        }

        public static JpaVelocityTemplateId create() {
            return new JpaVelocityTemplateId(UUID.randomUUID());
        }

        @Override
        public UUID getUuid() {
            return this.uuid;
        }

        @Override
        public void setUuid(UUID uuid) {
            this.uuid = uuid;
        }
    }

}
